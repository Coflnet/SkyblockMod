package de.torui.coflsky;

import de.torui.coflsky.core.Command;
import de.torui.coflsky.core.WriteToChatCommand;
import de.torui.coflsky.websocket.WSClient;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;

public class WSCommandHandler {
	public static boolean HandleCommand(Command cmd, Entity sender) {
		
		switch(cmd.getType()) {
		case WriteToChat:
			WriteToChat(cmd);
			break;
		case Execute:
			Execute(cmd, sender);
			break;
		}
		
		return true;
	}

	private static void Execute(Command cmd, Entity sender) {
		//Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd.getData());
		MinecraftServer.getServer().getCommandManager().executeCommand(sender, cmd.getData());
	}

	private static void WriteToChat(Command cmd) {
		WriteToChatCommand wcmd = WSClient.gson.fromJson(cmd.getData(), WriteToChatCommand.class);
		System.out.println("Executing wcmd Text=" + wcmd.Text + " OnClick="  + wcmd.OnClick);
		wcmd.Text = "Hello World";
		wcmd.OnClick = "/give @p apple 1"
;		/*String command = "tellraw @p [\"\", {\"text\":\"Hewwo World\", \"clickEvent\": {\"action\":\"run_command\", \"value\":\"/say hi\"}}]";
		//"/tellraw @p  [\"\",{\"text\":\"" + wcmd.Text + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + wcmd.OnClick + "\"}}]"
		int result = ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command);
		System.out.println("Sent to commandhandler with result" + result);*/
		
			
		IChatComponent comp = new ChatComponentText(wcmd.Text);
		ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, wcmd.OnClick));
		comp.setChatStyle(style);
		
		Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
	}
}
