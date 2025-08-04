package de.torui.coflsky.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central store for server-provided option values.
 * Key = option/field name as present in CoflConfig, value = parsed Object.
 * Values are overwritten whenever the server sends a new settings blob.
 */
public final class SettingsCache {
    private static final ConcurrentHashMap<String, Object> VALUES = new ConcurrentHashMap<>();

    private SettingsCache() {}

    public static Map<String, Object> snapshot() {
        return new ConcurrentHashMap<>(VALUES);
    }

    public static void put(String key, Object value) {
        if (key == null) return;
        VALUES.put(key, value);
    }

    public static Object get(String key) {
        return VALUES.get(key);
    }

    public static int getInt(String key, int def) {
        Object v = VALUES.get(key);
        return v instanceof Number ? ((Number) v).intValue() : def;
    }

    public static boolean getBool(String key, boolean def) {
        Object v = VALUES.get(key);
        return v instanceof Boolean ? (Boolean) v : def;
    }

    public static float getFloat(String key, float def) {
        Object v = VALUES.get(key);
        return v instanceof Number ? ((Number) v).floatValue() : def;
    }

    public static double getDouble(String key, double def) {
        Object v = VALUES.get(key);
        return v instanceof Number ? ((Number) v).doubleValue() : def;
    }

    public static long getLong(String key, long def) {
        Object v = VALUES.get(key);
        return v instanceof Number ? ((Number) v).longValue() : def;
    }

    public static String getString(String key, String def) {
        Object v = VALUES.get(key);
        return v != null ? String.valueOf(v) : def;
    }
}
