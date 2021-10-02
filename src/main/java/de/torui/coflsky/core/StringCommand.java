package de.torui.coflsky.core;

import com.google.gson.annotations.SerializedName;

public class StringCommand {
	@SerializedName("type")
	private String Type;
	@SerializedName("data")
	private String data;
	
	public StringCommand() {}
	
	public StringCommand(String type, String data) {
		super();
		this.Type = type;
		this.data = data;
	}
	
	public String getType() {
		return Type;
	}

	public void setType(String type) {
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
