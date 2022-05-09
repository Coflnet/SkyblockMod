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

	@Description("Wherever or not to send item descriptions for extending to the server")
	public boolean ExtendDescriptions;

	@Description("Chat input starting with one of these prefixes is sent to the server")
	public String[] CommandPrefixes;

}
