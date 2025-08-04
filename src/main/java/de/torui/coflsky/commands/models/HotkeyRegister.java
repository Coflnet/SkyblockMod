package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;
import de.torui.coflsky.WSCommandHandler;

public class HotkeyRegister {
	@SerializedName("name")
	public String Name;
	@SerializedName("defaultKey")
	public String DefaultKey;

	public HotkeyRegister() {
	}
}
