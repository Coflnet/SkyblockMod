package de.torui.coflsky.commands;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.libs.universal.UChat;
import de.torui.coflsky.config.CoflConfig;
import de.torui.coflsky.util.ServerSettingsLoader;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

@Command(value = "coflgui", aliases = {"cg"}, description = "Open the Cofl settings GUI.")
public class CoflGuiCommand extends CommandBase {

    @Main
    private void handle() {
        // If a load is already underway, inform user and avoid duplicate trigger
        if (ServerSettingsLoader.isRunning()) {
            UChat.chat("&7[Cofl] &eSettings are already loading...");
            return;
        }
        // Fetch latest settings every time; open GUI after load.
        ServerSettingsLoader.requestSettingsAndThen(CoflConfig::openGuiStatic);
    }

    @Override
    public String getCommandName() {
        return "coflgui";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("cg");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/coflgui - Open the Cofl settings GUI";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        handle();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    // For 1.8.9, override to support tab completion (none needed)
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }
}
