package de.torui.coflsky.commands;

import com.google.gson.annotations.SerializedName;

public class Command<T> {
	@SerializedName("type")
	private CommandType type;
	@SerializedName("data")
	private T data;


	public Command(CommandType type, T data) {
		super();
		this.type = type;
		this.data = data;
	}
	public Command(){}
	public CommandType getType() {
		return type;
	}

	public void setType(CommandType type) {
		this.type = type;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Command [Type=" + type + ", data=" + data + "]";
	}	

}

