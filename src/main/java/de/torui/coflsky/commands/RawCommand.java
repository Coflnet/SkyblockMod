package de.torui.coflsky.commands;

import com.google.gson.annotations.SerializedName;

public class RawCommand {
	@SerializedName("type")
	private String Type;
	
	@SerializedName("data")
	private String Data;
	
	public RawCommand(String type, String data) {
		this.Type = type;
		this.Data=data;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getData() {
		return Data;
	}

	public void setData(String data) {
		Data = data;
	}
	
}
