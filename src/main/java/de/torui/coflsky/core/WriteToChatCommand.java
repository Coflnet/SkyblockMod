package de.torui.coflsky.core;

import com.google.gson.annotations.SerializedName;

public class WriteToChatCommand {
	@SerializedName("text")
	public String Text;
	@SerializedName("onClick")
	public String OnClick;
	
	@SerializedName("hover")
	public String Hover;
	
	public WriteToChatCommand() {

	}

	public WriteToChatCommand(String text, String onClick, String hover) {
		super();
		Text = text;
		OnClick = onClick;
		Hover = hover;
	}	
	
}
