package de.torui.coflsky;

import java.util.Arrays;
import java.util.List;

import de.torui.coflsky.core.Command;
import de.torui.coflsky.core.CommandType;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
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
		return "/cofl token <token> to register\n/coflsky start to connect\n/coflsky stop to stop";
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
				sender.addChatMessage(new ChatComponentText("" + args[0] +"is not a valid subcommand!"));
				System.out.println(args[0] +"is not a valid subcommand!");
				return;
			}
		}
		
		/*if(args.length == 2 && args[0].equals("token")) {
			//todo: send authorisation message
		}*/
		
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
