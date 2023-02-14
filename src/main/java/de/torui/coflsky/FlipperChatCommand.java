package de.torui.coflsky;

import java.util.List;

import net.minecraft.command.ICommandSender;

import java.util.ArrayList;

public class FlipperChatCommand extends CoflSkyCommand {

	
	@Override
	public String getCommandName() {
		return "fc";
	}

	@Override
	public List<String> getCommandAliases()
	{
		ArrayList<String> al = new ArrayList<>();
		al.add("coflchat");
		return al;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Shorthand for /cofl chat";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		new Thread(()->{
            String[] newArgs = new String[args.length + 1];
			System.arraycopy(args, 0, newArgs, 1, args.length);
            newArgs[0] = "chat";
			sendCommandToServer(newArgs, sender);
		}).start();
	}
}
