package de.torui.coflsky.configuration;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.handlers.EventRegistry;

import java.util.regex.Pattern;

public class Configuration {
	public Configuration() {

	}

	private static Configuration instance;

	public static Configuration getInstance() {
		if (instance == null)
			instance = new Configuration();
		return instance;
	}

	public static void setInstance(Configuration config) {
		instance = config;
		CoflSky.config.autoStart = instance.autoStart;
		CoflSky.config.extendedtooltips = instance.extendDescriptions;
		EventRegistry.chatpattern = Pattern.compile(instance.chatRegex, Pattern.CASE_INSENSITIVE);;
	}

	public String chatRegex;
	public boolean collectChat;
	public boolean collectInventory;
	public boolean collectTab;
	public boolean collectScoreboard;
	public boolean allowProxy;
	public boolean collectInvClick;
	public boolean collectChatClicks;
	public boolean collectLobbyChanges;
	public boolean collectEntities;
	public boolean collectLocation;
	public boolean autoStart;
	@Description("Wherever or not to send item descriptions for extending to the server")
	public boolean extendDescriptions;

	@Description("Chat input starting with one of these prefixes is sent to the server")
	public String[] CommandPrefixes;

}
