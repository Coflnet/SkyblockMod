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

	public String ChatRegex;
	public boolean CollectChat;
	public boolean CollectInventory;
	public boolean CollectTab;
	public boolean CollectScoreboard;
	public boolean AllowProxy;
	public boolean CollectInvClick;
	public boolean CollectChatClicks;
	public boolean CollectLobbyChanges;
	public boolean CollectEntities;

	@Description("Wherever or not to send item descriptions for extending to the server")
	public boolean ExtendDescriptions;

	@Description("Chat input starting with one of these prefixes is sent to the server")
	public String[] CommandPrefixes;

}
