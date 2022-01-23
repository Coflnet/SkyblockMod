package de.torui.coflsky.commands;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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
	ChatMessage,

	@SerializedName("purchaseStart")
	PurchaseStart, 
	
	@SerializedName("purchaseConfirm")
	PurchaseConfirm,
	
	@SerializedName("reset")
	Reset,
	@SerializedName("flip")
	Flip,
;
	public static Map<CommandType,String> data;
	static {
		data = new HashMap<>();
		for(CommandType ct : CommandType.values()) {
			try {
				Field f = CommandType.class.getField(ct.name());
				
				if(f.isAnnotationPresent(SerializedName.class)) {
					SerializedName sn = f.getAnnotation(SerializedName.class);
					data.put(ct, sn.value());
				} else {
					throw new RuntimeException("Commandtype must have SerializeName Annotation!");
				}
				
			} catch (NoSuchFieldException | SecurityException e) {
				System.err.println("This should never occur!");
				e.printStackTrace();
			}
		}
	}
	
	public String ToJson() {
		return data.get(this);
	}	
}
