package de.torui.coflsky.core;

import com.google.gson.annotations.SerializedName;

public class WriteToChatCommand {
	@SerializedName("text")
	public String Text;
	@SerializedName("onClick")
	public String OnClick;
	public WriteToChatCommand(String text, String onClickEvent) {
		super();
		Text = text;
		OnClick = onClickEvent;
	}
	
	
	
}
