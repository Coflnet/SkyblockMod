package de.torui.coflsky.bingui.gui;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class TestCommand extends CommandBase {

    @Override
    public int getRequiredPermissionLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "test open gui";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        System.out.println("test");
        BinGuiOpener.openBinGui();
    }
}
