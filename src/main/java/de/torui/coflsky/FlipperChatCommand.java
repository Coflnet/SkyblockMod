package de.torui.coflsky;

import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;

public class FlipperChatCommand extends CoflSkyCommand {

    public static boolean useChatOnlyMode = false;


    @Override
    public String getCommandName() {
        return "fc";
    }

    @Override
    public List getCommandAliases() {
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
        new Thread(() -> {

            if (args.length == 1 && args[0].equals("toggle")) {
                FlipperChatCommand.useChatOnlyMode = !FlipperChatCommand.useChatOnlyMode;
                sender.addChatMessage(new ChatComponentText("[§1C§6oflnet§f]§7: §7Set §bChat only mode §7to: §f" + (FlipperChatCommand.useChatOnlyMode ? "true" : "false")));
            } else {
                String[] newArgs = new String[args.length + 1];
                System.arraycopy(args, 0, newArgs, 1, args.length);
                newArgs[0] = "chat";
                SendCommandToServer(newArgs, sender);
            }
        }).start();
    }
}
