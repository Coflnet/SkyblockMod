package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;

public class SoundData {
	@SerializedName("name")
	public final String name;
	
	@SerializedName("pitch")
	public final float pitch;


	public SoundData(String name, float pitch) {
		super();
		this.name = name;
		this.pitch = pitch;
	}
	
	
}
