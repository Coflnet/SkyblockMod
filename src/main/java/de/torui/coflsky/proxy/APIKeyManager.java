package de.torui.coflsky.proxy;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;

public class APIKeyManager {
    private final Gson gson = new Gson();
    private APIInfo apiInfo = new APIInfo();

    public APIInfo getApiInfo(){
        return this.apiInfo;
    }


    public class APIInfo{
        @SerializedName("api-key")
        private String key;
        public boolean setKey(String key){
            try {
                UUID.fromString(key);
                apiInfo.key = key;
                return true;
            } catch (Exception exception){
                return false;
            }
        }
        public String getKey(){
            return key;
        }
    }

    public void loadIfExists() throws Exception {
        Path dataPath = Paths.get(Loader.instance().getConfigDir().getPath(), "CoflSky", "api-key.json");
        File file = dataPath.toFile();
        if(file.exists()) {
            BufferedReader reader = new BufferedReader( new InputStreamReader(Files.newInputStream(file.toPath())));
            String raw = reader.lines().collect(Collectors.joining("\n"));
            this.apiInfo = gson.fromJson(raw,APIInfo.class);
            reader.close();
        }
    }


    public void saveKey() throws Exception {
        Path dataPath = Paths.get(Loader.instance().getConfigDir().getPath(), "CoflSky", "api-key.json");
        File file = dataPath.toFile();
        if(file.exists()) {
            file.delete();
        }
        file.createNewFile();

        String data = gson.toJson(apiInfo);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath())));
        bw.append(data);
        bw.flush();
        bw.close();
    }


}
