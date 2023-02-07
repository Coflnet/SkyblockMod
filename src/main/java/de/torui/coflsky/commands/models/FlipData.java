package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;
import de.torui.coflsky.WSCommandHandler;

public class FlipData {

	@SerializedName("messages")
	public ChatMessageData[] Messages;
	@SerializedName("id")
	public String Id;
	@SerializedName("worth")
	public int Worth;
	@SerializedName("sound")
	public SoundData Sound;
	@SerializedName("render")
	public String Render;

	public FlipData() {
	}

	public FlipData(ChatMessageData[] messages, String id, int worth, SoundData sound, String render) {
		super();
		Messages = messages;
		Id = id;
		Worth = worth;
		Sound = sound;
		Render = render;
	}

	public String getMessageAsString(){
		return WSCommandHandler.ChatMessageDataToString(this.Messages);
	}
}
