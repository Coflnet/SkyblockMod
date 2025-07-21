package de.torui.coflsky.util;

import cc.polyfrost.oneconfig.libs.universal.UChat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.torui.coflsky.config.CoflConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serialises the current CoflConfig static field values into a simple
 * {@code Map<String,String>} and uploads them to the backend using the
 * soon-to-be-available <code>/uploadSettings</code> command.
 * <p>
 * The command is invoked on the next client tick to guarantee that OneConfig
 * has finished writing any pending changes to the static fields after the GUI
 * is closed.
 */
public final class SettingsUploader {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private SettingsUploader() {}

    /**
     * Collect all public static option fields from {@link CoflConfig}, serialise
     * them to JSON and send the <code>/uploadSettings</code> command.
     * <p>
     * Any reflection issues are swallowed; a chat message is shown on severe
     * failure so the user can report stacktraces.
     */
    public static void uploadSettings() {
        try {
            Map<String, String> values = new LinkedHashMap<>();
            for (Field field : CoflConfig.class.getDeclaredFields()) {
                int mod = field.getModifiers();
                if (!Modifier.isStatic(mod)) continue;
                if (field.getName().startsWith("_")) continue; // skip helper headers/spacers
                // Only primitive wrapper, String and boolean/int/float etc.
                Class<?> type = field.getType();
                if (type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class) {
                    field.setAccessible(true);
                    Object v = field.get(null);
                    values.put(field.getName(), String.valueOf(v));
                }
            }
            String json = GSON.toJson(values);
            // Send the command on next tick to ensure it actually works
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                if (mc.thePlayer == null) return;
                String cmd = "/cofl uploadSettings " + json;
                boolean handled = ClientCommandHandler.instance.executeCommand(mc.thePlayer, cmd) != 0;
                if (!handled) {
                    mc.thePlayer.sendChatMessage(cmd);
                }
                UChat.chat("&7[Cofl] &aSettings uploaded (" + values.size() + ")");
            });
        } catch (Exception ex) {
            UChat.chat("&c[Cofl] Failed to upload settings: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
