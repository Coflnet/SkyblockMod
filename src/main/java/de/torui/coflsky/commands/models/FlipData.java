package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;

public class FlipData {
	
	@SerializedName("messages")
	public ChatMessageData[] Messages;
	
	@SerializedName("id")
	public String Id;
	
	@SerializedName("worth")
	public int Worth;
	@SerializedName("sound")
	public String Sound;
	
	public FlipData() {}

	public FlipData(ChatMessageData[] messages, String id, int worth, String sound) {
		super();
		Messages = messages;
		Id = id;
		Worth = worth;
		Sound = sound;
	}
}
