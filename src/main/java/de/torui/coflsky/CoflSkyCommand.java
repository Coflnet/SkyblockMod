package de.torui.coflsky;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.commands.JsonStringCommand;
import de.torui.coflsky.commands.RawCommand;
import de.torui.coflsky.minecraft_integration.CoflSessionManager;
import de.torui.coflsky.minecraft_integration.CoflSessionManager.CoflSession;
import de.torui.coflsky.network.QueryServerCommands;
import de.torui.coflsky.network.WSClient;
import de.torui.coflsky.minecraft_integration.PlayerDataProvider;
import de.torui.coflsky.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;

public class CoflSkyCommand extends CommandBase {

	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "cofl";
	}
	@Override
	public List<String> getCommandAliases()
	{
		ArrayList<String> al = new ArrayList<>();
		al.add("Cofl");
		al.add("coflnet");
		al.add("cl");
		return al;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return HelpText;
	}
	
	public static final String HelpText = "Available local sub-commands:\n"
			+ "§bstart: §7starts a new connection\n"
			+ "§bstop: §7stops the connection\n"
			+ "§bconnect: §7Connects to a different server\n"
			+ "§breset: §7resets all local session information and stops the connection\n"
			+ "§bstatus: §7Emits status information\nServer-Only Commands:";
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		new Thread(()->{
			CoflSky.logger.debug(Arrays.toString(args));
			
			if(args.length >= 1) {
				switch(args[0]) {
				case "start":
					//todo: start
					//possible workaround for https://github.com/Coflnet/SkyblockMod/issues/48
					CoflSky.Wrapper.stop();
					sender.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText("Starting connection!")));
					CoflSky.Wrapper.startConnection();
					break;
				case "stop":
					CoflSky.Wrapper.stop();
					sender.addChatMessage(ChatUtils.getCoflnetLogo(false)
									.appendSibling(new ChatComponentText("You stopped the connection to "))
									.appendSibling(ChatUtils.getCoflnetLogo(false))
									.appendSibling(new ChatComponentText(".\n    To reconnect enter "))
									.appendSibling(new ChatComponentText("/cofl start").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE).setBold(true)))
									.appendSibling(new ChatComponentText(" or click this message"))
									.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/cofl start")))
							);
					break;
				//case "debug":
				//	WSCommandHandler.HandleCommand(new Command(CommandType.Execute, "/me hewwo"), sender.getCommandSenderEntity());
				//	WSCommandHandler.HandleCommand(new Command(CommandType.WriteToChat, " {\"type\":\"writeToChat\",\"data\":\"{\\\"text\\\":\\\"\\\\nFLIP: º9Goblin Eg\r\n"
				//			+ "g º87,000 -> 13,999 ºg[BUY]\\\",\\\"onClick\\\":\\\"/viewauction f7d7295ca72f43e9876bf6da7424000c\\\",\\\"hover\\\":\\\"\\\"}\"}"), sender.getCommandSenderEntity());
				//WSCommandHandler.HandleCommand(new Command(CommandType.PlaySound, "{\"name\":\"random.orb\",\"pitch\":0.5}"), sender.getCommandSenderEntity());
				//	break;
				case "callback":
					getCallbackCommand(args);
					break;
				case "dev":
					if(Config.BASE_URL.contains("localhost")) {
						CoflSky.Wrapper.startConnection();
						Config.BASE_URL = "https://sky.coflnet.com";
					} else {
						CoflSky.Wrapper.initializeNewSocket("ws://localhost:"+(args.length>2?args[2]:"8009")+"/modsocket");
						Config.BASE_URL = "http://localhost:"+(args.length>1?args[1]:"5005");
					}
					sender.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText("Toggled dev mode, now using " + Config.BASE_URL)));
					break;
				case "status":
					sender.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText(getStatusMessage())));
					break;
				case "reset":
					handleReset();
					break;
				case "connect":
					
					if(args.length == 2) {
						String destination = args[1];
						
						if(!destination.contains("://")) {
							destination = new String(Base64.getDecoder().decode(destination));
						}
						sender.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText("Stopping connection!")));
						CoflSky.Wrapper.stop();
						sender.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText("Opening connection to " + destination)));
						if(CoflSky.Wrapper.initializeNewSocket(destination)) {
							sender.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText("Success!").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN))));
						} else {
							sender.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText("Could not open connection, please check the logs").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
						}
					} else {
						sender.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText("Please specify a server to connect to").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
					}
					break;
				default:
					sendCommandToServer(args, sender);
				}
			} 
			
			else {
				listHelp(sender);
			}
		}).start();		
	}
	
	private void handleReset() {
		CoflSky.Wrapper.sendMessage(new Command<>(CommandType.RESET, ""));
		CoflSky.Wrapper.stop();
		Minecraft.getMinecraft().thePlayer.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText("Stopping Connection to ").appendSibling(ChatUtils.getCoflnetLogo(false))));
		CoflSessionManager.DeleteAllCoflSessions();
		Minecraft.getMinecraft().thePlayer.addChatMessage(ChatUtils.getCoflnetLogo(true)
				.appendSibling(new ChatComponentText("Deleting "))
				.appendSibling(ChatUtils.getCoflnetLogo(false))
				.appendSibling(new ChatComponentText(" sessions..."))
		);
		if(CoflSky.Wrapper.startConnection())
			Minecraft.getMinecraft().thePlayer.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText("Started the Connection to ").appendSibling(ChatUtils.getCoflnetLogo(false))));
	}

	public String getStatusMessage() {
		
		String vendor = System.getProperty("java.vm.vendor");
		String name = System.getProperty("java.vm.name");
		String version = System.getProperty("java.version");
		String detailedVersion = System.getProperty("java.vm.version");
		
		String status = vendor + " " + name + " " + version + " " + detailedVersion + "|Connection = " + (CoflSky.Wrapper!=null?CoflSky.Wrapper.GetStatus():"UNINITIALIZED_WRAPPER");
		try {
		status += "  uri=" + CoflSky.Wrapper.socket.uri.toString();		
		} catch(NullPointerException ignored) {}
		
		
		try {
			CoflSession session = CoflSessionManager.GetCoflSession(PlayerDataProvider.getUsername());
			String sessionString = CoflSessionManager.gson.toJson(session);
			status += "  session=" + sessionString;
		} catch (IOException ignored) {}
		
		return status;
	}
	
	public void sendCommandToServer(String[] args, ICommandSender sender) {
		String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		
		//JsonStringCommand sc = new JsonStringCommand(args[0], WSClient.gson.toJson(command));
		RawCommand rc = new RawCommand(args[0], WSClient.gson.toJson(command));
		if(CoflSky.Wrapper.isRunning) {
			CoflSky.Wrapper.sendMessage(rc);
		} else {
			sendAfterStart(sender, rc);
		}
	}

	private static synchronized void sendAfterStart(ICommandSender sender, RawCommand rc) {
		sender.addChatMessage(ChatUtils.getCoflnetLogo(true).appendSibling(new ChatComponentText("CoflSky wasn't active.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
		//CoflSky.Wrapper.stop();
		CoflSky.Wrapper.startConnection();
		CoflSky.Wrapper.sendMessage(rc);
	}

	public void listHelp(ICommandSender sender) {
		sender.addChatMessage(new ChatComponentText(HelpText));
		sender.addChatMessage(new ChatComponentText(QueryServerCommands.QueryCommands()));
	}
	
	public void getCallbackCommand(String[] args) {
		
		String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		CoflSky.logger.debug("CallbackData: " + command);
		//new Thread(()->{
		CoflSky.logger.debug("Callback: " + command);
			WSCommandHandler.HandleCommand(new JsonStringCommand(CommandType.EXECUTE, WSClient.gson.toJson(command)), Minecraft.getMinecraft().thePlayer);
			CoflSky.Wrapper.sendMessage(new JsonStringCommand(CommandType.CLICKED, WSClient.gson.toJson(command)));

		CoflSky.logger.debug("Sent Command!");
		//}).start();
		
	}

}
