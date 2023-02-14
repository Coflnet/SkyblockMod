package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

public class ModListData {

    @SerializedName("fileNames")
    private final List<String> fileNames = new LinkedList<>();

    @SerializedName("modNames")
    private final List<String> modNames = new LinkedList<>();

    @SerializedName("fileHashes")
    private final List<String> fileHashes = new LinkedList<>();


    public void addFileName(String name){
        this.fileNames.add(name);
    }

    public void addModName(String modName){
        this.modNames.add(modName);
    }

    public void addFileHashes(String hash){
        this.fileHashes.add(hash);
    }


}
