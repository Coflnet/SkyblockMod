package de.torui.coflsky;

import de.torui.coflsky.core.Command;
import de.torui.coflsky.core.SoundCommand;
import de.torui.coflsky.core.WriteToChatCommand;
import de.torui.coflsky.websocket.WSClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.event.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServerCommandManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;

public class WSCommandHandler {

	public static String lastOnClickEvent;

	public static boolean HandleCommand(Command cmd, Entity sender) {
		// Entity sender = Minecraft.getMinecraft().thePlayer;
		System.out.println("Handling Command=" + cmd.toString());
		switch (cmd.getType()) {
		case WriteToChat:
			WriteToChat(cmd);
			break;
		case Execute:
			Execute(cmd, sender);
			break;
		case PlaySound:
			PlaySound(cmd, sender);
			break;
		case ChatMessage:
			ChatMessage(cmd);
			break;
		default:
			break;
		}

		return true;
	}

	private static void PlaySound(Command cmd, Entity sender) {

		SoundCommand sc = WSClient.gson.fromJson(cmd.getData(), SoundCommand.class);
		
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();

		// random.explode
		PositionedSoundRecord psr = PositionedSoundRecord
				.create(new ResourceLocation(sc.Name), sc.Pitch);
		
		handler.playSound(psr);
	}

	private static void Execute(Command cmd, Entity sender) {
		System.out.println("Execute: " + cmd.getData() + " sender:" + sender);
		String dummy = WSClient.gson.fromJson(cmd.getData(), String.class);
		
		if(dummy.startsWith("/cofl")) {
			ClientCommandHandler.instance.executeCommand(sender, dummy);
		} else {
			Minecraft.getMinecraft().thePlayer.sendChatMessage(dummy);
		}
		
	}

	
	private static IChatComponent CommandToChatComponent(WriteToChatCommand wcmd) {
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
	
	private static void ChatMessage(Command cmd) {
		WriteToChatCommand[] list = WSClient.gson.fromJson(cmd.getData(), WriteToChatCommand[].class);

		IChatComponent master = new ChatComponentText("");

		for (WriteToChatCommand wcmd : list) {
			IChatComponent comp = CommandToChatComponent(wcmd);
			if (comp != null)
				master.appendSibling(comp);
		}
		Minecraft.getMinecraft().thePlayer.addChatMessage(master);
	}

	

	private static void WriteToChat(Command cmd) {
		WriteToChatCommand wcmd = WSClient.gson.fromJson(cmd.getData(), WriteToChatCommand.class);
		
		IChatComponent comp = CommandToChatComponent(wcmd);
		if (comp != null)
		{
			if(wcmd.OnClick != null)
				lastOnClickEvent = "/cofl callback " + wcmd.OnClick;
			Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
		}
			
	}

}
