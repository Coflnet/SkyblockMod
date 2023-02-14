package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;

public class ChatMessageData {
	@SerializedName("text")
	public final String text;
	@SerializedName("onClick")
	public final String onClick;
	
	@SerializedName("hover")
	public final String hover;

	public ChatMessageData(String text, String onClick, String hover) {
		super();
		this.text = text;
		this.onClick = onClick;
		this.hover = hover;
	}	
	
}
