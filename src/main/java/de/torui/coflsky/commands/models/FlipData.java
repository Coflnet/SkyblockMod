package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;

public class FlipData {
	
	@SerializedName("messages")
	public final ChatMessageData[] messages;
	
	@SerializedName("id")
	public final String id;
	
	@SerializedName("worth")
	public final int worth;


	public FlipData(ChatMessageData[] messages, String id, int worth) {
		super();
		this.messages = messages;
		this.id = id;
		this.worth = worth;
	}
}
