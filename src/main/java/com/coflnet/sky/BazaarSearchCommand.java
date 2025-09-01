package com.coflnet.sky;

import com.coflnet.sky.utils.BazaarSearchUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/**
 * Command for automatically searching in the bazaar
 * Usage: /bazaarsearch <item_name>
 */
public class BazaarSearchCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "bazaarsearch";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bazaarsearch <item_name> - Automatically search for an item in the bazaar";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command can only be used by players!"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        if (args.length == 0) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: /bazaarsearch <item_name>"));
            return;
        }

        // Join all arguments to form the search term
        StringBuilder searchTerm = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                searchTerm.append(" ");
            }
            searchTerm.append(args[i]);
        }

        String itemName = searchTerm.toString();

        // Check if already searching
        if (BazaarSearchUtil.isSearching()) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "A search is already in progress. Please wait..."));
            return;
        }

        // Attempt to search in bazaar
        boolean success = BazaarSearchUtil.searchInBazaar(itemName);

        if (success) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Searching for '" + itemName + "' in bazaar..."));
        } else {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Could not initiate bazaar search. Make sure you're in a bazaar GUI."));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // No special permissions required
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return sender instanceof EntityPlayer;
    }
}
