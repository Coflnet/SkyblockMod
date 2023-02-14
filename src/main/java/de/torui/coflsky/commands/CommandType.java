package de.torui.coflsky.commands;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public enum CommandType {
	@SerializedName("writeToChat")
	WRITE_TO_CHAT,
	@SerializedName("set")
	SET,

	@SerializedName("execute")
	EXECUTE,

	@SerializedName("tokenLogin")
	TOKEN_LOGIN,

	@SerializedName("clicked")
	CLICKED,
	
	@SerializedName("playSound")
	PLAY_SOUND,
	
	@SerializedName("chatMessage")
	CHAT_MESSAGE,

	@SerializedName("purchaseStart")
	PURCHASE_START,
	
	@SerializedName("purchaseConfirm")
	PURCHASE_CONFIRM,
	
	@SerializedName("reset")
	RESET,
	@SerializedName("flip")
	FLIP,
	@SerializedName("privacySettings")
	PRIVACY_SETTINGS,
	@SerializedName("countdown")
	COUNTDOWN,
	@SerializedName("updatePurse")
	UPDATE_PURSE,
	@SerializedName("updateBits")
	UPDATE_BITS,
	@SerializedName("updateServer")
	UPDATE_SERVER,
	@SerializedName("updateLocation")
	UPDATE_LOCATION,
	@SerializedName("chatBatch")
	CHAT_BATCH,
	@SerializedName("uploadTab")
	UPLOAD_TAB,
	@SerializedName("getMods")
	GET_MODS,
	@SerializedName("proxy")
	PROXY_REQUEST;


	public static final Map<CommandType,String> data = new HashMap<>();
	static {
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
	
	public String toJson() {
		return data.get(this);
	}	
}
