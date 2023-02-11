package de.torui.coflsky.proxy;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.commands.models.ProxyRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyManager {
    private final String ProxyResponseUrl = "http://sky.coflnet.com/api/data/proxy";
    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor();


    public void handleRequestAsync(ProxyRequest request){
         CompletableFuture<String> req = this.doRequest(request.getUrl());
        if(request.isUploadEnabled()) {
            req.thenAcceptAsync(res -> this.uploadData(res,request.getId()));
        }
    }


    private String getString(HttpURLConnection con) {
        try {
            InputStream in = new BufferedInputStream(con.getInputStream());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = in.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            String resString = result.toString("UTF-8");
            return resString;
        } catch(IOException e){
            return null;
        }
    }

    public void uploadData(String data,String id){
        this.requestExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(ProxyManager.this.ProxyResponseUrl);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");

                    con.setRequestProperty("X-Request-Id", id);

                    con.setDoOutput(true);
                    con.setDoInput(true);

                    OutputStream os = con.getOutputStream();
                    os.write(data.getBytes("UTF-8"));
                    os.close();
                    String response = getString(con);
                    System.out.println("Response=" + response);
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        });
    }


    private CompletableFuture<String> doRequest(String targetUrl){
        CompletableFuture<String> future = new CompletableFuture<>();

        this.requestExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(targetUrl);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Accept", "application/json");
                    con.setRequestProperty("User-Agent", "CoflMod");

                    String key = CoflSky.getAPIKeyManager().getApiInfo().key;

                    if(targetUrl.startsWith("https://api.hypixel.net") && !key.isEmpty()){
                        con.setRequestProperty("API-Key", key);
                    }

                    con.setDoInput(true);
                    future.complete(getString(con));
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        });

        return future;
    }


}
