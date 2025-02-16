package de.torui.coflsky;

import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.Arrays;

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
                sender.addChatMessage(new ChatComponentText("[§1C§6oflnet§f]§7: §7Set §bChat only mode §7to: §f"
                        + (FlipperChatCommand.useChatOnlyMode ? "true" : "false")));
            } else {
                String[] newArgs = new String[args.length + 1];
                System.arraycopy(args, 0, newArgs, 1, args.length);
                newArgs[0] = "chat";
                SendCommandToServer(newArgs, sender);
            }
        }).start();
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> options = Arrays.asList(
                ":tableflip:", ":sad:", ":smile:", ":grin:", ":heart:", ":skull:", ":airplane:", ":check:", "<3",
                ":star:", ":yes:", ":no:", ":java:", ":arrow", ":shrug:", "o/", ":123:", ":totem:", ":typing:",
                ":maths:", ":snail:", ":thinking:", ":gimme:", ":wizard:", ":pvp:", ":peace:", ":oof:", ":puffer:",
                ":yey:", ":cat:", ":dab:", ":dj:", ":snow:", ":^_^:", ":^-^:", ":sloth:", ":cute:", ":dog:",
                ":fyou:", ":angwyflip:", ":snipe:", ":preapi:", ":tm:", ":r:", ":c:", ":crown:", ":fire:",
                ":sword:", ":shield:", ":cross:", ":star1:", ":star2:", ":star3:", ":star4:", ":rich:", ":boop:",
                ":yay:", ":gg:");
        return CommandBase.getListOfStringsMatchingLastWord(args, options);
    }
}
