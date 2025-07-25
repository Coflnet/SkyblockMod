package de.torui.coflsky;

import java.io.IOException;
import java.util.*;

import CoflCore.commands.Command;
import CoflCore.commands.CommandType;
import CoflCore.commands.JsonStringCommand;
import CoflCore.commands.RawCommand;
import CoflCore.commands.models.FlipData;
import CoflCore.configuration.GUIType;
import de.torui.coflsky.gui.bingui.BinGuiManager;
import de.torui.coflsky.gui.tfm.ButtonRemapper;
import CoflCore.misc.SessionManager;
import CoflCore.misc.SessionManager.CoflSession;
import CoflCore.network.WSClient;
import de.torui.coflsky.minecraft_integration.PlayerDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;

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
    public List getCommandAliases() {
        ArrayList<String> al = new ArrayList<String>();
        al.add("Cofl");
        al.add("coflnet");
        al.add("cl");
        return al;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Sends sub-arguments to the SkyCofl command server\n"
                + "§b/cofl §7will request help text with more info\n";
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            List<String> options = Arrays.asList(
                    "start", "stop", "report", "online", "delay", "blacklist", "bl", "whitelist", "wl",
                    "mute", "blocked", "chat", "c", "nickname", "nick", "profit", "worstflips", "bestflips",
                    "leaderboard", "lb", "loserboard", "buyspeedboard", "trades", "flips", "set", "s",
                    "purchase", "buy", "transactions", "balance", "help", "h", "logout", "backup", "restore",
                    "captcha", "importtfm", "replayactive", "reminder", "filters", "emoji", "addremindertime",
                    "lore", "fact", "flip", "preapi", "transfercoins", "ping", "setgui", "bazaar", "bz",
                    "switchregion", "craftbreakdown", "cheapattrib", "ca", "attributeupgrade", "au", "ownconfigs",
                    "configs", "config", "licenses", "license", "verify", "unverify", "attributeflip", "forge",
                    "crafts", "craft", "upgradeplan", "updatecurrentconfig", "settimezone", "cheapmuseum", "cm",
                    "replayflips", "lowball", "ahtax", "sethotkey");
            return CommandBase.getListOfStringsMatchingLastWord(args, options);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            List<String> options = Arrays.asList("lbin", "finders", "onlyBin", "whitelistAftermain", "DisableFlips",
                    "DebugMode", "blockHighCompetition", "minProfit", "minProfitPercent", "minVolume", "maxCost",
                    "modjustProfit", "modsoundOnFlip", "modshortNumbers", "modshortNames", "modblockTenSecMsg",
                    "modformat", "modblockedFormat", "modchat", "modcountdown", "modhideNoBestFlip", "modtimerX",
                    "modtimerY", "modtimerSeconds", "modtimerScale", "modtimerPrefix", "modtimerPrecision",
                    "modblockedMsg", "modmaxPercentOfPurse", "modnoBedDelay", "modstreamerMode", "modautoStartFlipper",
                    "modnormalSoldFlips", "modtempBlacklistSpam", "moddataOnlyMode", "modahListHours", "modquickSell",
                    "modmaxItemsInInventory", "moddisableSpamProtection", "showcost", "showestProfit", "showlbin",
                    "showslbin", "showmedPrice", "showseller", "showvolume", "showextraFields", "showprofitPercent",
                    "showprofit", "showsellerOpenBtn", "showlore", "showhideSold", "showhideManipulated",
                    "privacyExtendDescriptions", "privacyAutoStart", "loreHighlightFilterMatch",
                    "loreMinProfitForHighlight", "loreDisableHighlighting");
            return CommandBase.getListOfStringsMatchingLastWord(args, options);
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        new Thread(() -> {
            System.out.println(Arrays.toString(args));

            if (args.length >= 1) {
                switch (args[0].toLowerCase()) {
                    case "copytoclipboard":
                        String textForClipboard = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                        try {
                            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(
                                    textForClipboard);
                            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                            Minecraft.getMinecraft().thePlayer
                                    .addChatMessage(new ChatComponentText("Text copied to clipboard!"));
                        } catch (Exception e) {
                            Minecraft.getMinecraft().thePlayer
                                    .addChatMessage(new ChatComponentText("Failed to copy text to clipboard!"));
                        }
                        CoflCore.CoflCore.Wrapper.SendMessage(new JsonStringCommand(CommandType.Clicked,
                                WSClient.gson.toJson("copy:" + textForClipboard)));
                        break;
                    case "setgui":
                        if (args.length != 2) {
                            sender.addChatMessage(new ChatComponentText("[§1C§6oflnet§f]§7: §7Available GUIs:"));
                            sender.addChatMessage(new ChatComponentText("[§1C§6oflnet§f]§7: §7Cofl"));
                            sender.addChatMessage(new ChatComponentText("[§1C§6oflnet§f]§7: §7TFM"));
                            sender.addChatMessage(new ChatComponentText("[§1C§6oflnet§f]§7: §7Off"));
                            return;
                        }

                        if (args[1].equalsIgnoreCase("cofl")) {
                            CoflCore.CoflCore.config.purchaseOverlay = GUIType.COFL;
                            sender.addChatMessage(
                                    new ChatComponentText("[§1C§6oflnet§f]§7: §7Set §bPurchase Overlay §7to: §fCofl"));
                            MinecraftForge.EVENT_BUS.unregister(ButtonRemapper.getInstance());
                        }
                        if (args[1].equalsIgnoreCase("tfm")) {
                            CoflCore.CoflCore.config.purchaseOverlay = GUIType.TFM;
                            sender.addChatMessage(
                                    new ChatComponentText("[§1C§6oflnet§f]§7: §7Set §bPurchase Overlay §7to: §fTFM"));
                            MinecraftForge.EVENT_BUS.register(ButtonRemapper.getInstance());
                        }
                        if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false")) {
                            CoflCore.CoflCore.config.purchaseOverlay = null;
                            sender.addChatMessage(
                                    new ChatComponentText("[§1C§6oflnet§f]§7: §7Set §bPurchase Overlay §7to: §fOff"));
                            MinecraftForge.EVENT_BUS.unregister(ButtonRemapper.getInstance());
                        }
                        break;
                    default:
                        CoflCore.CoflSkyCommand.processCommand(args, PlayerDataProvider.getUsername());
                }
            } else {
                SendCommandToServer("help", "general", sender);
            }
        }).start();
    }



    public void SendCommandToServer(String[] args, ICommandSender sender) {
        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        SendCommandToServer(args[0], command, sender);
    }

    public void SendCommandToServer(String command, String arguments, ICommandSender sender) {
        RawCommand rc = new RawCommand(command, WSClient.gson.toJson(arguments));
        if (CoflCore.CoflCore.Wrapper.isRunning) {
            CoflCore.CoflCore.Wrapper.SendMessage(rc);
        } else {
            SendAfterStart(sender, rc);
        }
    }

    private static synchronized void SendAfterStart(ICommandSender sender, RawCommand rc) {
        sender.addChatMessage(new ChatComponentText("CoflSky wasn't active.")
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
        // CoflSky.Wrapper.stop();
        CoflCore.CoflCore.Wrapper.startConnection(PlayerDataProvider.getUsername());
        CoflCore.CoflCore.Wrapper.SendMessage(rc);
    }
}
