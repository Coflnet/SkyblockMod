package com.coflnet.sky.config;

import java.io.*;
import java.util.Properties;
import net.minecraft.client.Minecraft;

/**
 * Configuration handler for sell protection feature
 */
public class SellProtectionConfig {
    private static final String CONFIG_FILE = "config" + File.separator + "SkyCofl" + File.separator + "skycofl-sellprotection.properties";
    private static Properties properties = new Properties();
    private static File configFile;
    
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        try {
            configFile = new File(Minecraft.getMinecraft().mcDataDir, CONFIG_FILE);
            if (configFile.exists()) {
                FileInputStream fis = new FileInputStream(configFile);
                properties.load(fis);
                fis.close();
            }
        } catch (Exception e) {
            System.err.println("Failed to load SkyCofl sell protection config: " + e.getMessage());
        }
    }
    
    private static void saveConfig() {
        try {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(configFile);
            properties.store(fos, "SkyCofl Sell Protection Configuration");
            fos.close();
        } catch (Exception e) {
            System.err.println("Failed to save SkyCofl sell protection config: " + e.getMessage());
        }
    }
    
    /**
     * Gets the minimum threshold for sell protection
     * Items worth more than this amount will trigger sell protection
     */
    public static long getSellProtectionThreshold() {
        return Long.parseLong(properties.getProperty("sellProtectionThreshold", "1000000"));
    }
    
    /**
     * Sets the minimum threshold for sell protection
     */
    public static void setSellProtectionThreshold(long threshold) {
        properties.setProperty("sellProtectionThreshold", String.valueOf(threshold));
        saveConfig();
    }
    
    /**
     * Gets whether sell protection is enabled
     */
    public static boolean isEnabled() {
        return Boolean.parseBoolean(properties.getProperty("sellProtectionEnabled", "true"));
    }
    
    /**
     * Sets whether sell protection is enabled
     */
    public static void setEnabled(boolean enabled) {
        properties.setProperty("sellProtectionEnabled", String.valueOf(enabled));
        saveConfig();
    }
    
    /**
     * Parses a human-readable threshold value like "2k", "3m", "1.5b"
     * @param value The string value to parse
     * @return The parsed long value
     * @throws NumberFormatException if the value cannot be parsed
     */
    public static long parseThresholdValue(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Empty value");
        }
        
        value = value.trim().toLowerCase();
        
        // Handle direct numbers first
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            // Continue to parse with suffixes
        }
        
        // Extract number and suffix
        String numberPart = value.replaceAll("[kmb]$", "");
        String suffix = value.replaceAll("^[0-9.]+", "");
        
        double number;
        try {
            number = Double.parseDouble(numberPart);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid number format: " + value);
        }
        
        // Apply multiplier based on suffix
        switch (suffix) {
            case "k":
                return (long) (number * 1_000);
            case "m":
                return (long) (number * 1_000_000);
            case "b":
                return (long) (number * 1_000_000_000L);
            case "":
                return (long) number;
            default:
                throw new NumberFormatException("Unknown suffix: " + suffix);
        }
    }
    
    /**
     * Formats a threshold value for display
     */
    public static String formatThreshold(long threshold) {
        if (threshold >= 1_000_000_000) {
            return String.format("%.1fB", threshold / 1_000_000_000.0);
        } else if (threshold >= 1_000_000) {
            return String.format("%.1fM", threshold / 1_000_000.0);
        } else if (threshold >= 1_000) {
            return String.format("%.1fK", threshold / 1_000.0);
        } else {
            return String.valueOf(threshold);
        }
    }
}
