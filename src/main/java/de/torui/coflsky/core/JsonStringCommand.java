package de.torui.coflsky.core;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonStringCommand extends Command<String> {
	
	public JsonStringCommand(String type, String data) {
		this.setType(CommandType.valueOf(type));
		this.setData(data);
	}
	
	public JsonStringCommand() {
		super();

	}

	public JsonStringCommand(CommandType type, String data) {
		super(type, data);
	}

	public <T> Command<T>  GetAs(TypeToken<T> type){
		T t = new GsonBuilder().create().fromJson(this.getData(),type.getType());
		Command<?> cmd = new Command<Object>(this.getType(), t);
		
		return (Command<T>) cmd;
	}
}
