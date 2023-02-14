package de.torui.coflsky;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.commands.JsonStringCommand;
import de.torui.coflsky.commands.RawCommand;
import de.torui.coflsky.commands.models.*;
import de.torui.coflsky.configuration.ConfigurationManager;
import de.torui.coflsky.handlers.EventRegistry;
import de.torui.coflsky.minecraft_integration.CountdownTimer;
import de.torui.coflsky.proxy.ProxyManager;
import de.torui.coflsky.utils.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;

public class WSCommandHandler {

	public static String lastOnClickEvent;
	public static final FlipHandler flipHandler = new FlipHandler();
	private static final ModListData modListData = new ModListData();
	private static final Gson gson = new Gson();
	private static final ProxyManager proxyManager = new ProxyManager();

	public static void HandleCommand(JsonStringCommand cmd, Entity sender) {
		// Entity sender = Minecraft.getMinecraft().thePlayer;
		CoflSky.logger.debug("Handling Command=" + cmd.toString());

		switch (cmd.getType()) {
		case WRITE_TO_CHAT:
			WriteToChat(cmd.GetAs(new TypeToken<ChatMessageData>() {}));
			break;
		case EXECUTE:
			Execute(cmd.GetAs(new TypeToken<String>() {}), sender);
			break;
		case PLAY_SOUND:
			PlaySound(cmd.GetAs(new TypeToken<SoundData>() {}), sender);
			break;
		case CHAT_MESSAGE:
			ChatMessage(cmd.GetAs(new TypeToken<ChatMessageData[]>() {}));
			break;
		case FLIP:
			Flip(cmd.GetAs(new TypeToken<FlipData>() {}));
			break;
		case PRIVACY_SETTINGS:
			new ConfigurationManager().UpdateConfiguration(cmd.getData());
		case COUNTDOWN:
			StartTimer(cmd.GetAs(new TypeToken<TimerData>() {}));
			break;
		case GET_MODS:
			getMods();
			break;
		case PROXY_REQUEST:
			handleProxyRequest(cmd.GetAs(new TypeToken<ProxyRequest[]>() {}).getData());
			break;
		default:
			break;
		}

	}

	private static void handleProxyRequest(ProxyRequest[] request){
		for(ProxyRequest req : request){
			proxyManager.handleRequestAsync(req);
		}
	}


	public static void cacheMods(){
		File modFolder = new File(Minecraft.getMinecraft().mcDataDir, "mods");
		for(File mods : modFolder.listFiles()){
			modListData.addFileName(mods.getName());
			try {
				modListData.addFileHashes(FileUtils.getMD5Checksum(mods));
			} catch (Exception exception){
				// Highly less likely to happen unless something goes wrong
				exception.printStackTrace();
			}
		}

		for(ModContainer mod : Loader.instance().getModList()){
			modListData.addModName(mod.getName());
			modListData.addModName(mod.getModId());
		}
	}

	private static void getMods(){
		// the Cofl server has asked for an mod list now let's respond with all the info
		CoflSky.Wrapper.sendMessage(new RawCommand("foundMods",gson.toJson(modListData)));
	}


	private static void Flip(Command<FlipData> cmd) {
		//handle chat message
		ChatMessageData[] messages = cmd.getData().messages;
		Command<ChatMessageData[]> showCmd = new Command<>(CommandType.CHAT_MESSAGE, messages);
		ChatMessage(showCmd);
		flipHandler.fds.insert(new de.torui.coflsky.FlipHandler.Flip(cmd.getData().id, cmd.getData().worth));
		
		// trigger the keyevent to execute the event handler
		CoflSky.Events.onKeyEvent(null);
	}

	private static void PlaySound(Command<SoundData> cmd, Entity sender) {
		
		SoundData sc = cmd.getData();
		
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();

		// random.explode
		PositionedSoundRecord psr = PositionedSoundRecord
				.create(new ResourceLocation(sc.name), sc.pitch);
		
		handler.playSound(psr);
	}

	private static void Execute(Command<String> cmd, Entity sender) {
		CoflSky.logger.debug("Execute: " + cmd.getData() + " sender:" + sender);
		//String dummy = WSClient.gson.fromJson(cmd.getData(), String.class);
		Execute(cmd.getData(),sender);	
	}

	/**
	 * Starts a countdown
	 */
	private static void StartTimer(Command<TimerData> cmd) {
		CountdownTimer.startCountdown(cmd.getData());
	}

	public static void Execute(String cmd, Entity sender)
	{
		if(cmd.startsWith("/viewauction")){
			String[] args = cmd.split(" ");

			EventRegistry.LastViewAuctionUUID = args[args.length-1];
			EventRegistry.LastViewAuctionInvocation = System.currentTimeMillis();
		}
		
		if(cmd.startsWith("/cofl") || cmd.startsWith("http")) {
			ClientCommandHandler.instance.executeCommand(sender, cmd);
		} else {
			Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
		}
	}

	
	private static IChatComponent CommandToChatComponent(ChatMessageData wcmd) {
		if(wcmd.onClick != null)
			lastOnClickEvent = "/cofl callback " + wcmd.onClick;
		if (wcmd.text != null) {
			IChatComponent comp = new ChatComponentText(wcmd.text);

			ChatStyle style;
			if (wcmd.onClick != null) {
				if (wcmd.onClick.startsWith("http")) {
					style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, wcmd.onClick));
				} else {
					style = new ChatStyle()
							.setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/cofl callback " + wcmd.onClick));
				}
				comp.setChatStyle(style);
			}

			if (wcmd.hover != null && !wcmd.hover.isEmpty()) {
				if (comp.getChatStyle() == null)
					comp.setChatStyle(new ChatStyle());
				comp.getChatStyle().setChatHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(wcmd.hover)));
			}
			return comp;
		}
		return null;
	}
	
	private static void ChatMessage(Command<ChatMessageData[]> cmd) {		
		ChatMessageData[] list = cmd.getData() ;//WSClient.gson.fromJson(cmd.getData(), WriteToChatCommand[].class);

		IChatComponent master = new ChatComponentText("");

		for (ChatMessageData wcmd : list) {
			IChatComponent comp = CommandToChatComponent(wcmd);
			if (comp != null)
				master.appendSibling(comp);
		}
		Minecraft.getMinecraft().thePlayer.addChatMessage(master);
	}

	

	private static void WriteToChat(Command<ChatMessageData> cmd) {
		ChatMessageData wcmd = cmd.getData();
		
		IChatComponent comp = CommandToChatComponent(wcmd);
		if (comp != null)
		{
			Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
		}
			
	}

}
