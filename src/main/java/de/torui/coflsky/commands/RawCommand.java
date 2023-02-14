package de.torui.coflsky.commands;

import com.google.gson.annotations.SerializedName;

public class RawCommand {
	@SerializedName("type")
	private String type;
	
	@SerializedName("data")
	private String data;
	
	public RawCommand(String type, String data) {
		this.type = type;
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
}
