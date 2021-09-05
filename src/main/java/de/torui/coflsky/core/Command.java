package de.torui.coflsky.core;

import com.google.gson.annotations.SerializedName;

public class Command {
	@SerializedName("type")
	private CommandType Type;
	@SerializedName("data")
	private String data;
	
	public Command() {}
	
	public Command(CommandType type, String data) {
		super();
		this.Type = type;
		this.data = data;
	}
	
	public CommandType getType() {
		return Type;
	}

	public void setType(CommandType type) {
		Type = type;
	}

	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Command [Type=" + Type + ", data=" + data + "]";
	}
	
	
}
