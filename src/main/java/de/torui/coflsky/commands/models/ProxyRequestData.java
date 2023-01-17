package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;

public class ProxyRequestData {

    @SerializedName("upload")
    private boolean uploadEnabled;

    @SerializedName("id")
    private String id;

    @SerializedName("url")
    private String url;

    public ProxyRequestData(){}

    public boolean isUploadEnabled(){
        return uploadEnabled;
    }
    public void setUploadEnabled(boolean uploadEnabled){
        this.uploadEnabled = uploadEnabled;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getUrl(){
        return url;
    }
    public void setUrl(String url){
        this.url = url;
    }

    public ProxyRequestData(boolean uploadEnabled, String id, String url){
        super();
        this.uploadEnabled = uploadEnabled;
        this.id = id;
        this.url = url;
    }
}
