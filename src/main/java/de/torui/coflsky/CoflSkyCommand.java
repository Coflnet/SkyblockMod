package de.torui.coflsky;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.torui.coflsky.core.Command;
import de.torui.coflsky.core.CommandType;
import de.torui.coflsky.core.StringCommand;
import de.torui.coflsky.minecraft_integration.CoflSessionManager;
import de.torui.coflsky.minecraft_integration.CoflSessionManager.CoflSession;
import de.torui.coflsky.network.QueryServerCommands;
import de.torui.coflsky.network.WSClient;
import de.torui.coflsky.minecraft_integration.PlayerDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class CoflSkyCommand extends CommandBase {

	
	@Override
	public int getRequiredPermissionLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCommandName() {
		return "cofl";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return HelpText;
	}
	
	public static final String HelpText = "Available local sub-commands:\n"
			+ "start: starts a new connection\n"
			+ "stop: stops the connection\n"
			+ "reset: resets all local session information and stops the connection\n"
			+ "status: Emits status information\nServer-Only Commands:";
	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		new Thread(()->{
			System.out.println(Arrays.toString(args));
			
			if(args.length >= 1) {
				switch(args[0]) {
				case "start":
					//todo: start
					sender.addChatMessage(new ChatComponentText("starting connection..."));
					CoflSky.Wrapper.startConnection();
					break;
				case "stop":
					CoflSky.Wrapper.stop();
					sender.addChatMessage(new ChatComponentText("you stopped the connection to ")
							.appendSibling(new ChatComponentText("C").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_BLUE)))
							.appendSibling(new ChatComponentText("oflnet").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)))
							.appendSibling(new ChatComponentText(".\n    To reconnect enter "))
							.appendSibling(new ChatComponentText("\"").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)))
							.appendSibling(new ChatComponentText("/cofl start"))
							.appendSibling(new ChatComponentText("\"").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)))
							.appendSibling(new ChatComponentText(" or click this message"))
							.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/cofl start")))
							);
					break;
				case "debug":
				//	WSCommandHandler.HandleCommand(new Command(CommandType.Execute, "/me hewwo"), sender.getCommandSenderEntity());
				//	WSCommandHandler.HandleCommand(new Command(CommandType.WriteToChat, " {\"type\":\"writeToChat\",\"data\":\"{\\\"text\\\":\\\"\\\\nFLIP: º9Goblin Eg\r\n"
				//			+ "g º87,000 -> 13,999 ºg[BUY]\\\",\\\"onClick\\\":\\\"/viewauction f7d7295ca72f43e9876bf6da7424000c\\\",\\\"hover\\\":\\\"\\\"}\"}"), sender.getCommandSenderEntity());
				//WSCommandHandler.HandleCommand(new Command(CommandType.PlaySound, "{\"name\":\"random.orb\",\"pitch\":0.5}"), sender.getCommandSenderEntity());
					break;	
				case "callback":
					CallbackCommand(args);
					break;
				case "status":
					sender.addChatMessage(new ChatComponentText(StatusMessage()));
					break;
				case "reset":
					HandleReset();
					break;
				default:
					CommandNotRecognized(args, sender);
					return;
				}
			} 
			
			else {
				ListHelp(sender);
			}
		}).start();		
	}
	
	private void HandleReset() {
		CoflSky.Wrapper.stop();
		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Stopping Connection to CoflNet"));
		CoflSessionManager.DeleteAllCoflSessions();
		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Deleting CoflNet sessions..."));
		if(CoflSky.Wrapper.startConnection())
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Started the Connection to CoflNet"));
	}

	public String StatusMessage() {
		
		String vendor = System.getProperty("java.vm.vendor");
		String name = System.getProperty("java.vm.name");
		String version = System.getProperty("java.version");
		String detailedVersion = System.getProperty("java.vm.version");
		
		String status = vendor + " " + name + " " + version + " " + detailedVersion + "|Connection = " + (CoflSky.Wrapper!=null?CoflSky.Wrapper.GetStatus():"UNINITIALIZED_WRAPPER");
		try {
		status += "  uri=" + CoflSky.Wrapper.socket.uri.toString();		
		} catch(NullPointerException npe) {}
		
		
		try {
			CoflSession session = CoflSessionManager.GetCoflSession(PlayerDataProvider.getUsername());
			String sessionString = WSClient.gson.toJson(session);
			status += "  session=" + sessionString;
		} catch (IOException e) {
		}
		
		return status;
	}
	
	public void CommandNotRecognized(String[] args, ICommandSender sender) {
		String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		
		StringCommand sc = new StringCommand(args[0], WSClient.gson.toJson(command));
		
		if(CoflSky.Wrapper.isRunning) {
			CoflSky.Wrapper.SendMessage(sc);
		} else {
			sender.addChatMessage(new ChatComponentText("CoflSky not active. Server Commands are currently not available.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		}
		
		
	}
	
	public void ListHelp(ICommandSender sender) {
		sender.addChatMessage(new ChatComponentText(HelpText));
		sender.addChatMessage(new ChatComponentText(QueryServerCommands.QueryCommands()));
	}
	
	public void CallbackCommand(String[] args) {
		
		String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		System.out.println("CallbackData: " + command);
		//new Thread(()->{
			System.out.println("Callback: " + command);
			WSCommandHandler.HandleCommand(new Command(CommandType.Execute, WSClient.gson.toJson(command)), Minecraft.getMinecraft().thePlayer);
			CoflSky.Wrapper.SendMessage(new Command(CommandType.Clicked, WSClient.gson.toJson(command)));		
			
			System.out.println("Sent!");
		//}).start();
		
	}

}
