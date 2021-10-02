package de.torui.coflsky;

import de.torui.coflsky.core.Command;
import de.torui.coflsky.core.WriteToChatCommand;
import de.torui.coflsky.websocket.WSClient;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.sound.SoundEvent;

public class WSCommandHandler {
	public static boolean HandleCommand(Command cmd, Entity sender) {
		//Entity sender = Minecraft.getMinecraft().thePlayer;
		System.out.println("Handling Command=" + cmd.toString());
		switch(cmd.getType()) {
		case WriteToChat:
			WriteToChat(cmd);
			break;
		case Execute:
			Execute(cmd, sender);
			break;
		default:
			break;
		}
		
		return true;
	}
	
	private static void PlaySound(Command cmd, Entity sender) {
      
	}

	private static void Execute(Command cmd, Entity sender) {
		
		//Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd.getData());
		System.out.println("Execute: " + cmd.getData() + " sender:" + sender);
		
		Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd.getData());
		
		//ICommandManager manager = MinecraftServer.getServer().getCommandManager();
		//System.out.println("CommandManager: " + manager);
		//manager.executeCommand(sender, cmd.getData());
	}

	private static void WriteToChat(Command cmd) {
		WriteToChatCommand wcmd = WSClient.gson.fromJson(cmd.getData(), WriteToChatCommand.class);
		//System.out.println("Executing wcmd Text=" + wcmd.Text + " OnClick="  + wcmd.OnClick);
		
		if(wcmd.Text != null ) {
			IChatComponent comp = new ChatComponentText(wcmd.Text);
			
			ChatStyle style;
			if(wcmd.OnClick != null) {
				if(wcmd.OnClick.startsWith("http")) {
					style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, wcmd.OnClick));
				} else {
					style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/cofl callback "  +wcmd.OnClick));
				}
				comp.setChatStyle(style);
			}
			
			Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
		}
		
	}
}
