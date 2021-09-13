package de.torui.coflsky;

import java.util.List;

import de.torui.coflsky.core.Command;
import de.torui.coflsky.core.CommandType;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import scala.actors.threadpool.Arrays;

public class CoflSkyCommand extends CommandBase {

	
	@Override
	public int getRequiredPermissionLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCommandName() {
		return "coflsky";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/coflsky token <token> to register\n/coflsky start to connect\n/coflsky stop to stop";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		System.out.println(Arrays.toString(args));
		
		if(args.length == 1) {
			switch(args[0]) {
			case "start":
				//todo: start
				break;
			case "stop":
				//todo: stop
				break;
			case "debug":
				WSCommandHandler.HandleCommand(new Command(CommandType.Execute, "/say hewwo"), sender.getCommandSenderEntity());
				WSCommandHandler.HandleCommand(new Command(CommandType.WriteToChat, "{ \"text\": \"Clickable Texts are fun\", \"onClick\": \"/give @p minecraft:apple 1\"}"), sender.getCommandSenderEntity());
				break;	
			case "callback":
				break;
			default:
				sender.addChatMessage(new ChatComponentText("" + args[0] +"is not a valid subcommand!"));
				return;
			}
		}
		
		if(args.length == 2 && args[0].equals("token")) {
			//todo: send authorisation message
		}
		
	}

}
