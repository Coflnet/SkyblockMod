package de.torui.coflsky;

import java.util.Arrays;
import java.util.List;

import de.torui.coflsky.core.Command;
import de.torui.coflsky.core.CommandType;
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
	
	public static final String HelpText = "Available sub-commands:\n"
			+ "start: starts a new connection\n"
			+ "stop: stops the connection";
	
	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		// TODO Auto-generated method stub
		return super.addTabCompletionOptions(sender, args, pos);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		System.out.println(Arrays.toString(args));
		
		if(args.length >= 1) {
			switch(args[0]) {
			case "start":
				//todo: start
				CoflSky.Wrapper.start();
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
						
						//)§1C§6oflnet§8§f.\n    To restart enter §8\"§f/cofl start§8\"")));
				//todo: stop
				break;
			case "debug":
				WSCommandHandler.HandleCommand(new Command(CommandType.Execute, "/me hewwo"), sender.getCommandSenderEntity());
			//	WSCommandHandler.HandleCommand(new Command(CommandType.WriteToChat, "{ \"text\": \"Clickable Texts are fun\", \"onClick\": \"me Hello World\"}"), sender.getCommandSenderEntity());
				break;	
			case "callback":
				CallbackCommand(args);
				break;
			default:
				QueryServerCommands.QueryCommands();
				sender.addChatMessage(new ChatComponentText("" + args[0] +"is not a valid subcommand!"));
				System.out.println(args[0] +"is not a valid subcommand!");
				return;
			}
		} 
		
		else {
			ListHelp(sender);
		}
		
	}
	
	public void CommandNotRecognized(String[] args, ICommandSender sender) {
		
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
			WSCommandHandler.HandleCommand(new Command(CommandType.Execute, command), Minecraft.getMinecraft().thePlayer);
			CoflSky.Wrapper.SendMessage(new Command(CommandType.Clicked, command));		
			
			System.out.println("Sent!");
		//}).start();
		
	}

}
