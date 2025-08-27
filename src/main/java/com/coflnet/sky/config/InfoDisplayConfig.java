package com.coflnet.sky.config;

import java.io.*;
import java.util.Properties;
import net.minecraft.client.Minecraft;

/**
 * Simple configuration handler for SkyCofl settings
 */
public class InfoDisplayConfig {
    private static final String CONFIG_FILE = "config" + File.separator + "SkyCofl" + File.separator + "skycofl-infodisplay.properties";
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
            System.err.println("Failed to load SkyCofl info display config: " + e.getMessage());
        }
    }
    
    private static void saveConfig() {
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(configFile);
            properties.store(fos, "SkyCofl Info Display Configuration");
            fos.close();
        } catch (Exception e) {
            System.err.println("Failed to save SkyCofl info display config: " + e.getMessage());
        }
    }
    
    public static int getTextOffsetX() {
        return Integer.parseInt(properties.getProperty("textOffsetX", "0"));
    }
    
    public static int getTextOffsetY() {
        return Integer.parseInt(properties.getProperty("textOffsetY", "0"));
    }
    
    public static void setTextOffset(int offsetX, int offsetY) {
        properties.setProperty("textOffsetX", String.valueOf(offsetX));
        properties.setProperty("textOffsetY", String.valueOf(offsetY));
        saveConfig();
    }
    
    public static void resetTextOffset() {
        properties.remove("textOffsetX");
        properties.remove("textOffsetY");
        saveConfig();
    }
}
