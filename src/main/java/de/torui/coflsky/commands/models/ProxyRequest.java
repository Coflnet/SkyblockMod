package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;

public class ProxyRequest {
    @SerializedName("uploadTo")
    private String uploadTo;

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

    public boolean isUploadEnabled() {
        return this.uploadTo != null;
    }
    
    public String getUploadTo() {
        return this.uploadTo;
    }
}
