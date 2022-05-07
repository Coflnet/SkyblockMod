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
import java.util.ArrayList;

public class FlipperChatCommand extends CoflSkyCommand {

	
	@Override
	public String getCommandName() {
		return "fc";
	}

	@Override
	public List getCommandAliases()
	{
		ArrayList<String> al = new ArrayList<String>();
		al.add("coflchat");
		return al;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Shorthand for /cofl chat";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		new Thread(()->{
            String[] newArgs = new String[args.length +1];
			System.arraycopy(args, 0, newArgs, 1, args.length);
            newArgs[0] = "chat";
			SendCommandToServer(newArgs, sender);
		}).start();
	}
}
