package de.torui.coflsky.core;

import com.google.gson.annotations.SerializedName;

public enum CommandType {
	@SerializedName("writeToChat")
	WriteToChat,
	
	@SerializedName("execute")
	Execute,
	
	@SerializedName("tokenLogin")
	TokenLogin,
	
	@SerializedName("clicked")
	Clicked,
	@SerializedName("playSound")
	PlaySound,
	@SerializedName("chatMessage")
	ChatMessage;
}
