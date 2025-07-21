package de.torui.coflsky.util;

import cc.polyfrost.oneconfig.libs.universal.UChat;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.torui.coflsky.config.CoflConfig;
import de.torui.coflsky.config.SettingsCache;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Utility that, on request, executes the server command "/cofl get json", tails
 * latest.log until the JSON blob is encountered, then applies those values to
 * {@link CoflConfig} via reflection.
 */
public class ServerSettingsLoader {
    private static final Path LOG_FILE = resolveLatestFile();
    private static final Pattern JSON_PATTERN = Pattern.compile("\\[CoflSettings] (\\{.*})");
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "CoflSettingsLoader");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean APPLIED = new AtomicBoolean(false); // true once settings successfully applied
    private static final AtomicBoolean LOADED_MSG_SHOWN = new AtomicBoolean(false);
    private static final Gson GSON = new Gson();
    private static volatile boolean APPENDER_REGISTERED = false;
    private static volatile String LAST_JSON_APPLIED = null;
    private static final AtomicBoolean GUI_REFRESHED = new AtomicBoolean(false);
    private static Runnable onLoadedCallback = null;
    private static final long COOLDOWN_MS = 5000; // 5-second cooldown between loads
    private static volatile long lastLoadTime = 0;

    private static Path resolveLatestFile() {
        Path dir = Paths.get(System.getenv("APPDATA"), ".minecraft", "logs");
        String[] candidates = {"latest.log", "latest", "latest.txt"};
        for (String name : candidates) {
            Path p = dir.resolve(name);
            if (Files.exists(p)) return p;
        }
        return dir.resolve("latest.log"); // fallback
    }

    /**
     * Fire the command and begin listening for settings. If already running it is a no-op.
     */
    public static void requestSettings() {
        requestSettings(false);
    }

    public static void requestSettingsSilent() {
        requestSettings(true);
    }

    private static void requestSettings(boolean silent) {
        // cooldown logic: if recently loaded and not running, skip
        if (!RUNNING.get() && (System.currentTimeMillis() - lastLoadTime) < COOLDOWN_MS) {
            if (!silent) {
                UChat.chat("&7[Cofl] &eSettings were just loaded. Please wait a moment...");
            }
            return;
        }
        if (RUNNING.getAndSet(true)) return;
        // register console listener once
        if (!APPENDER_REGISTERED) {
            registerConsoleListener();
            APPENDER_REGISTERED = true;
        }
        // Tell the user unless silent
        if (!silent) {
            UChat.chat("&7[Cofl] &eSettings loading...");
        }
        // reset state flags for this request
        APPLIED.set(false);
        LOADED_MSG_SHOWN.set(false);
        // schedule send next tick to ensure player is in proper state
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            boolean handled = net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/cofl get json") != 0;
            if (!handled) {
                mc.thePlayer.sendChatMessage("/cofl get json");
            }
        });
        // Start background tailer
        EXECUTOR.submit(ServerSettingsLoader::tailerLoop);
    }

    /**
     * Request settings from server and invoke the supplied callback once they have been
     * successfully applied on the client thread.
     *
     * @param onLoaded Callback executed exactly once after settings load or immediately if they are already fresh.
     */
    public static void requestSettingsAndThen(Runnable onLoaded) {
        // If a load is already running, just queue the callback normally
        if (isRunning()) {
            addPostLoadAction(onLoaded);
            return;
        }
        // If settings are already fresh (within cooldown) invoke callback immediately
        if (recentlyLoaded()) {
            Minecraft.getMinecraft().addScheduledTask(onLoaded);
            return;
        }
        // Otherwise trigger a fresh load and invoke later
        onLoadedCallback = onLoaded;
        requestSettings();
    }

    /**
     * Register an action to be executed after the current (or next) settings load completes.
     *
     * @param r callback to execute
     */
    public static void addPostLoadAction(Runnable r) {
        if (onLoadedCallback == null) {
            onLoadedCallback = r;
        } else {
            Runnable existing = onLoadedCallback;
            onLoadedCallback = () -> {
                existing.run();
                r.run();
            };
        }
    }

    private static void tailerLoop() {
        long start = System.currentTimeMillis();
        try (SeekableByteChannel ch = Files.newByteChannel(LOG_FILE, StandardOpenOption.READ)) {
            ch.position(ch.size()); // jump to EOF
            ByteBuffer buf = ByteBuffer.allocate(4096);
            StringBuilder line = new StringBuilder();
            while (RUNNING.get()) { // exit once settings applied
                // timeout after 30 seconds
                if (System.currentTimeMillis() - start > 30_000) {
                    if (!APPLIED.get()) {
                        UChat.chat("&c[Cofl] Settings load timed out.");
                    }
                    RUNNING.set(false);
                    return;
                }
                int read = ch.read(buf);
                if (read == 0) {
                    Thread.sleep(200);
                    continue;
                }
                buf.flip();
                while (buf.hasRemaining()) {
                    char c = (char) buf.get();
                    if (c == '\n' || c == '\r') {
                        if (processLine(line.toString())) {
                            // successfully applied settings, stop loop
                            return;
                        }
                        line.setLength(0);
                    } else {
                        line.append(c);
                    }
                }
                buf.clear();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            UChat.chat("&cFailed to load settings: " + e.getMessage());
            RUNNING.set(false);
        }
    }

    /**
     * Processes a single console line for settings JSON.
     * @param l line content
     * @return true if settings were found and applied
     */
    private static boolean processLine(String l) {
        // First try tag pattern
        Matcher m = JSON_PATTERN.matcher(l);
        if (m.find()) {
            applySettings(m.group(1));
            RUNNING.set(false);
            return true;
        }
        // Fallback: if line contains "data=" pattern
        int idx = l.indexOf("data=");
        if (idx != -1) {
            String json = l.substring(idx + 5).trim();
            applySettings(json);
            RUNNING.set(false);
            return true;
        }
        // Final fallback: raw JSON line starting with '{' or '['
        String t = l.trim();
        if (!t.isEmpty() && (t.startsWith("{") || t.startsWith("["))) {
            applySettings(t);
            RUNNING.set(false);
            return true;
        }
        return false;
    }

    public static void applySettings(String raw) {
        String json = raw.trim();
        if (json.equals(LAST_JSON_APPLIED)) {
            return; // duplicate, ignore
        }
        // Remember whether this invocation was triggered by a manual "/cofl get json" request
        final boolean fromRequestFlow = RUNNING.get();
        // Attempt to remove an extra trailing ']' common in Command toString
        if ((json.startsWith("[") && json.endsWith("]]") )) {
            json = json.substring(0, json.length() - 1);
        }
        JsonElement rootEl;
        try {
            rootEl = GSON.fromJson(json, JsonElement.class);
        } catch (Exception ex) {
            return; // parsing failed
        }
        if (rootEl == null) return;

        // mark as applied before mutating fields so timeout logic knows
        APPLIED.set(true);

        if (rootEl.isJsonArray()) {
            for (JsonElement elem : rootEl.getAsJsonArray()) {
                if (!elem.isJsonObject()) continue;
                JsonObject obj = elem.getAsJsonObject();
                if (!obj.has("value")) continue;
                String k = obj.has("key") ? obj.get("key").getAsString() :
                           (obj.has("name") ? obj.get("name").getAsString() : null);
                if (k == null) continue;
                setField(k, obj.get("value"));
            }
        } else if (rootEl.isJsonObject()) {
            JsonObject root = rootEl.getAsJsonObject();
            if (root.has("value") && (root.has("key") || root.has("name"))) {
                String k = root.has("key") ? root.get("key").getAsString() : root.get("name").getAsString();
                setField(k, root.get("value"));
            } else {
                for (Map.Entry<String, JsonElement> e : root.entrySet()) {
                    setField(e.getKey(), e.getValue());
                }
            }
        }
        // Force OneConfig to update UI and save
        Minecraft.getMinecraft().addScheduledTask(() -> {
            // Refresh GUI only if user currently has a OneConfig screen open
            if (Minecraft.getMinecraft().currentScreen != null &&
                    Minecraft.getMinecraft().currentScreen.getClass().getName().contains("oneconfig") &&
                    !GUI_REFRESHED.get()) {
                Minecraft.getMinecraft().displayGuiScreen(null);
                de.torui.coflsky.config.CoflConfig.openGuiStatic();
                GUI_REFRESHED.set(true);
            }
            // Only announce success if this was part of a manual request flow
            if (fromRequestFlow && LOADED_MSG_SHOWN.compareAndSet(false, true)) {
                UChat.chat("&aSettings loaded!");
            }
            // Invoke caller-supplied callback once after applying settings
            if (onLoadedCallback != null) {
                onLoadedCallback.run();
                onLoadedCallback = null;
            }
        });
        LAST_JSON_APPLIED = json;
        RUNNING.set(false);
        lastLoadTime = System.currentTimeMillis();
    }

    private static void setField(String key, JsonElement valueEl) {
        try {
            Field f;
            try {
                f = CoflConfig.class.getDeclaredField(key);
            } catch (NoSuchFieldException nf) {
                f = null;
                for (Field cand : CoflConfig.class.getDeclaredFields()) {
                    if (cand.getName().equalsIgnoreCase(key)) {
                        f = cand;
                        break;
                    }
                }
                if (f == null) {
                    // Store unknown key in cache for other consumers but don't error spam
                    try {
                        Object valObj = valueEl.isJsonNull() ? null : (valueEl.isJsonPrimitive() ?
                                valueEl.getAsString() : valueEl.toString());
                        SettingsCache.put(key, valObj);
                    } catch (Throwable ignored) {}
                    return;
                }
            }
            // store into cache instead of reflection (for now still set static for compat)
            f.setAccessible(true);
            Object parsedValue = parseElement(valueEl, f.getType());
            if (parsedValue == null) {
                // store raw JSON string for reference but skip setting field to avoid exception spam
                SettingsCache.put(key, valueEl.isJsonPrimitive() ? valueEl.getAsString() : valueEl.toString());
                return;
            }
            /*
             * Write value under BOTH keys:
             *  1. The raw key received from server (JSON) → lets CoflConfig.bool/int/... helpers that
             *     use the same spelling pick up the value.
             *  2. The exact static field name (f.getName()) → preserves compatibility with any existing
             *     client-side code that still references the uppercase/camel variant directly.
             */
            Object oldVal = SettingsCache.get(key);
            SettingsCache.put(key, parsedValue);
            if (!key.equals(f.getName())) {
                SettingsCache.put(f.getName(), parsedValue);
            }
            try { f.set(null, parsedValue); } catch (Throwable ignored) {}
            if (LAST_JSON_APPLIED != null && (oldVal == null || !oldVal.equals(parsedValue))) {
                String typeNote = (parsedValue != null && !f.getType().isAssignableFrom(parsedValue.getClass())) ? " &6(coerced)" : "";
                UChat.chat("&7[Settings] " + key + " &e" + String.valueOf(oldVal) + " &7→ &a" + String.valueOf(parsedValue) + typeNote);
            }
        } catch (Exception ex) {
            UChat.chat("&c[Settings] Failed to set " + key + ": " + ex.getMessage());
        }
    }

    private static Object parseElement(JsonElement el, Class<?> target) {
        if (target == int.class || target == Integer.class) {
            return el.isJsonPrimitive() && el.getAsJsonPrimitive().isString() ?
                    Integer.parseInt(el.getAsString()) : el.getAsInt();
        } else if (target == boolean.class || target == Boolean.class) {
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                return Boolean.parseBoolean(el.getAsString());
            }
            return el.getAsBoolean();
        } else if (target == float.class || target == Float.class) {
            return el.isJsonPrimitive() && el.getAsJsonPrimitive().isString() ?
                    Float.parseFloat(el.getAsString()) : el.getAsFloat();
        } else if (target == double.class || target == Double.class) {
            return el.isJsonPrimitive() && el.getAsJsonPrimitive().isString() ?
                    Double.parseDouble(el.getAsString()) : el.getAsDouble();
        } else if (target == byte.class || target == Byte.class) {
            return el.isJsonPrimitive() ? el.getAsByte() : null;
        } else if (target == short.class || target == Short.class) {
            return el.isJsonPrimitive() ? el.getAsShort() : null;
        } else if (target == long.class || target == Long.class) {
            return el.isJsonPrimitive() && el.getAsJsonPrimitive().isString() ?
                    Long.parseLong(el.getAsString()) : el.getAsLong();
        } else if (target.isEnum()) {
            try {
                String s = el.getAsString();
                if (s == null) return null;
                @SuppressWarnings({"rawtypes","unchecked"})
                Object ev = Enum.valueOf((Class<? extends Enum>) target, s.toUpperCase());
                return ev;
            } catch (Exception ignored) { return null; }
        } else if (target == String.class) {
            if (el.isJsonNull()) return null;
            if (el.isJsonPrimitive()) return el.getAsString();
            return el.toString();
        }
        // Fallback: attempt numeric coercion (e.g., server sent double but field is int)
        try {
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                Number n = el.getAsNumber();
                if (target == int.class || target == Integer.class) {
                    return n.intValue();
                } else if (target == long.class || target == Long.class) {
                    return n.longValue();
                } else if (target == float.class || target == Float.class) {
                    return n.floatValue();
                } else if (target == double.class || target == Double.class) {
                    return n.doubleValue();
                } else if (target == byte.class || target == Byte.class) {
                    return n.byteValue();
                } else if (target == short.class || target == Short.class) {
                    return n.shortValue();
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /** Attach a Log4j2 appender to intercept console lines for settings JSON. */
    private static void registerConsoleListener() {
        Logger root = (Logger) LogManager.getRootLogger();
        Appender app = new AbstractAppender("CoflSettingsConsole", null, null) {
            @Override
            public void append(LogEvent event) {
                String msg = event.getMessage().getFormattedMessage();
                // Try original tag based pattern first
                Matcher m = JSON_PATTERN.matcher(msg);
                if (m.find()) {
                    applySettings(m.group(1));
                    return; // avoid logging recursion
                } else if (msg.contains("data=")) {
                    int idx = msg.indexOf("data=") + 5;
                    String json = msg.substring(idx).trim();
                    applySettings(json);
                    return;
                } else if (msg.toLowerCase().contains("updated")) {
                    // Heuristic: server indicates a single setting changed; fetch full set again.
                    net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                        if (!isRunning()) {
                            requestSettingsSilent(); // silent auto-refresh
                        }
                    });
                }
            }
        };
        app.start();
        root.addAppender(app);
    }

    /** Returns true while a settings load request is currently in progress. */
    public static boolean isRunning() {
        return RUNNING.get();
    }

    /** Returns true when the last settings request has completed and been applied. */
    public static boolean isUpToDate() {
        return APPLIED.get();
    }

    /** Returns true if settings were loaded very recently (within cooldown window). */
    public static boolean recentlyLoaded() {
        return (System.currentTimeMillis() - lastLoadTime) < COOLDOWN_MS;
    }
}
