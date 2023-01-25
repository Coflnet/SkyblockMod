package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;

public class ProxyRequest {
    @SerializedName("upload")
    private boolean uploadEnabled;

    @SerializedName("id")
    private String id;

    @SerializedName("url")
    private String url;


    public String getId(){
        return id;
    }

    public String getUrl(){
        return url;
    }

    public boolean isUploadEnabled(){
        return this.uploadEnabled;
    }
}
