package de.torui.coflsky.core;

import com.google.gson.annotations.SerializedName;

public class SoundCommand {
	@SerializedName("name")
	public String Name;
	
	@SerializedName("pitch")
	public float Pitch;

	
	
	public SoundCommand() {
		super();
	}



	public SoundCommand(String name, float pitch) {
		super();
		Name = name;
		Pitch = pitch;
	}
	
	
}
