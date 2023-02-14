package de.torui.coflsky.configuration;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalConfig {
    public boolean autoStart;
    public boolean extendedTooltips;
    public LocalConfig(boolean autoStart,boolean extendedTooltips) {
        this.autoStart = autoStart;
        this.extendedTooltips = extendedTooltips;
    }
    public static void saveConfig(File file, LocalConfig Config) {
        Gson gson = new Gson();
        try {
            file.createNewFile();
            Files.write(Paths.get(file.getAbsolutePath()),
                    gson.toJson(Config).getBytes(StandardCharsets.UTF_8));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static LocalConfig createDefaultConfig() {
        return new LocalConfig(true,true);
    }
}
