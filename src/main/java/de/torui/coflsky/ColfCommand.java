package de.torui.coflsky;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ColfCommand extends CommandBase{
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return "colf";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "you misspelled /cofl";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		throw new CommandException("you misspelled /cofl");
	}

}
