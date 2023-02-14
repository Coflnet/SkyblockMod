package de.torui.coflsky;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class ColfCommand extends CommandBase{
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "colf";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "you misspelled /cofl";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		sender.addChatMessage(new ChatComponentText("No such command! ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
				.appendSibling(new ChatComponentText("Perhaps try ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
				.appendSibling(new ChatComponentText(("/cofl "+String.join(" ", args)).trim()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE).setBold(true).setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ("/cofl "+String.join(" ", args)).trim())).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Run command!"))))))
				.appendSibling(new ChatComponentText("?").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)))
		);
	}

}
