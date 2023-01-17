package de.torui.coflsky.network;

import de.torui.coflsky.Config;
import de.torui.coflsky.commands.models.ProxyRequestData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Proxy {

    public void handleSafe(ProxyRequestData requestData){
        try {
            handle(requestData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handle(ProxyRequestData request) throws IOException {


        if(request.getUrl().startsWith("https://api.hypixel.net")){

        }

        URL url = new URL(request.getUrl());
        HttpURLConnection con;
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("User-Agent", "CoflMod");
        con.setDoInput(true);

        if(request.isUploadEnabled()){
            String resString = getString(con);
            Map<String, List<String>> headerFields = con.getHeaderFields();
            sendResponse(request.getId(), resString, headerFields);
        }
    }

    private String getString(HttpURLConnection con) {

        try{
            InputStream in = new BufferedInputStream(con.getInputStream());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = in.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            String resString =  result.toString("UTF-8");
            return resString;}
            catch(IOException e){
                return null;
            }
    }

    private void sendResponse(String id, String result, Map<String, List<String>> headerfields) throws IOException {
        URL url = new URL(Config.ProxyResponseUrl);
        HttpURLConnection con;
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        con.setRequestProperty("X-Request-Id", id);

        headerfields.forEach((key, value) -> {
            con.setRequestProperty(key!=null? key :  "", String.join(" ", value));
        });

        con.setDoOutput(true);
        con.setDoInput(true);

        if(result != null) {
            OutputStream os = con.getOutputStream();
            byte[] bytes = result.getBytes("UTF-8");
            os.write(bytes);
            os.close();
        }
        String response = getString(con);
        System.out.println("response=" +response);
    }
}
