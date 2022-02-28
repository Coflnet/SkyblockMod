package de.torui.coflsky.configuration;

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
	}

	public String chatRegex = "";

	public String commandPrefixes = "";
	
	public boolean collectChat;
	public boolean collectInventories;
	public boolean collectTab;
	public boolean collectScoreboard;
	public boolean allowProxyUsage;
	public boolean collectInvClick;
	public boolean collectChatClicks;
	public boolean collectLobbyChanges;
	public boolean collectCoordinates;
	public boolean collectEntities;
	public boolean extendDescriptions;

}
