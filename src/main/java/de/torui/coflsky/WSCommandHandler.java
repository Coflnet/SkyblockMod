package de.torui.coflsky;

import com.google.gson.reflect.TypeToken;

import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.commands.JsonStringCommand;
import de.torui.coflsky.commands.models.ChatMessageData;
import de.torui.coflsky.commands.models.FlipData;
import de.torui.coflsky.commands.models.SoundData;
import de.torui.coflsky.configuration.ConfigurationManager;
import de.torui.coflsky.commands.models.TimerData;
import de.torui.coflsky.handlers.EventRegistry;
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

public class WSCommandHandler {

	public static transient String lastOnClickEvent;
	public static FlipHandler flipHandler = new FlipHandler();

	public static boolean HandleCommand(JsonStringCommand cmd, Entity sender) {
		// Entity sender = Minecraft.getMinecraft().thePlayer;
		System.out.println("Handling Command=" + cmd.toString());

		switch (cmd.getType()) {
		case WriteToChat:
			WriteToChat(cmd.GetAs(new TypeToken<ChatMessageData>() {}));
			break;
		case Execute:
			Execute(cmd.GetAs(new TypeToken<String>() {}), sender);
			break;
		case PlaySound:
			PlaySound(cmd.GetAs(new TypeToken<SoundData>() {}), sender);
			break;
		case ChatMessage:
			ChatMessage(cmd.GetAs(new TypeToken<ChatMessageData[]>() {}));
			break;
		case Flip:
			Flip(cmd.GetAs(new TypeToken<FlipData>() {}));
			break;
		case PrivacySettings:
			new ConfigurationManager().UpdateConfiguration(cmd.getData());
		case Countdown:
			StartTimer(cmd.GetAs(new TypeToken<TimerData>() {}));
			break;
		default:
			break;
		}

		return true;
	}

	private static void Flip(Command<FlipData> cmd) {
		//handle chat message
		ChatMessageData[] messages = cmd.getData().Messages;
		Command<ChatMessageData[]> showCmd = new Command<ChatMessageData[]>(CommandType.ChatMessage, messages);
		ChatMessage(showCmd);
		flipHandler.fds.Insert(new de.torui.coflsky.FlipHandler.Flip(cmd.getData().Id, cmd.getData().Worth));
		
		// trigger the keyevent to execute the event handler
		CoflSky.Events.onKeyEvent(null);
	}

	private static void PlaySound(Command<SoundData> cmd, Entity sender) {
		
		SoundData sc = cmd.getData();
		
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();

		// random.explode
		PositionedSoundRecord psr = PositionedSoundRecord
				.create(new ResourceLocation(sc.Name), sc.Pitch);
		
		handler.playSound(psr);
	}

	private static void Execute(Command<String> cmd, Entity sender) {
		System.out.println("Execute: " + cmd.getData() + " sender:" + sender);
		//String dummy = WSClient.gson.fromJson(cmd.getData(), String.class);
		Execute(cmd.getData(),sender);	
	}

	/**
	 * Starts a countdown
	 */
	private static void StartTimer(Command<TimerData> cmd) {
		de.torui.coflsky.CountdownTimer.startCountdown(cmd.getData());
	}

	public static void Execute(String cmd, Entity sender)
	{
		if(cmd.startsWith("/viewauction")){
			String[] args = cmd.split(" ");
			
			String uuid = args[args.length-1];
			EventRegistry.LastViewAuctionUUID = uuid;
			EventRegistry.LastViewAuctionInvocation = System.currentTimeMillis();
		}
		
		if(cmd.startsWith("/cofl") || cmd.startsWith("http")) {
			ClientCommandHandler.instance.executeCommand(sender, cmd);
		} else {
			Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
		}
	}

	
	private static IChatComponent CommandToChatComponent(ChatMessageData wcmd) {
		if(wcmd.OnClick != null)
			lastOnClickEvent = "/cofl callback " + wcmd.OnClick;
		if (wcmd.Text != null) {
			IChatComponent comp = new ChatComponentText(wcmd.Text);

			ChatStyle style;
			if (wcmd.OnClick != null) {
				if (wcmd.OnClick.startsWith("http")) {
					style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, wcmd.OnClick));
				} else {
					style = new ChatStyle()
							.setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/cofl callback " + wcmd.OnClick));
				}
				comp.setChatStyle(style);
			}

			if (wcmd.Hover != null && !wcmd.Hover.isEmpty()) {
				if (comp.getChatStyle() == null)
					comp.setChatStyle(new ChatStyle());
				comp.getChatStyle().setChatHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(wcmd.Hover)));
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
