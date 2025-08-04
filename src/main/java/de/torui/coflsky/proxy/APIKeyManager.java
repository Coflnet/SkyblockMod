package de.torui.coflsky.proxy;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class APIKeyManager {
    private final Gson gson = new Gson();
    private APIInfo apiInfo = new APIInfo();

    public APIInfo getApiInfo(){
        return this.apiInfo;
    }


    public class APIInfo{
        @SerializedName("api-key")
        public String key;
    }

    public void loadIfExists() throws Exception {
        Path dataPath = Paths.get(Loader.instance().getConfigDir().getPath(), "CoflSky", "api-key.json");
        File file = dataPath.toFile();
        if(file.exists()) {
            BufferedReader reader = new BufferedReader( new InputStreamReader(new FileInputStream(file)));
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
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        bw.append(data);
        bw.flush();
        bw.close();
    }


}
