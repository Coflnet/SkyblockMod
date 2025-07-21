package de.torui.coflsky;

import java.io.IOException;
import java.util.*;

import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.commands.JsonStringCommand;
import de.torui.coflsky.commands.RawCommand;
import de.torui.coflsky.commands.models.FlipData;
import de.torui.coflsky.gui.GUIType;
import de.torui.coflsky.gui.bingui.BinGuiManager;
import de.torui.coflsky.gui.tfm.ButtonRemapper;
import de.torui.coflsky.minecraft_integration.CoflSessionManager;
import de.torui.coflsky.minecraft_integration.CoflSessionManager.CoflSession;
import de.torui.coflsky.network.WSClient;
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
                    "DebugMode", "blockHighCompetition", "minProfit", "MinProfitPercent", "minVolume", "maxCost",
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
                    case "start":
                        // todo: start
                        // possible workaround for https://github.com/Coflnet/SkyblockMod/issues/48
                        CoflSky.Wrapper.stop();
                        sender.addChatMessage(new ChatComponentText("starting SkyCofl connection..."));
                        CoflSky.Wrapper.startConnection();
                        break;
                    case "stop":
                        CoflSky.Wrapper.stop();
                        sender.addChatMessage(new ChatComponentText("you stopped the connection to ")
                                .appendSibling(new ChatComponentText("C")
                                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_BLUE)))
                                .appendSibling(new ChatComponentText("oflnet")
                                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)))
                                .appendSibling(new ChatComponentText(".\n    To reconnect enter "))
                                .appendSibling(new ChatComponentText("\"")
                                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)))
                                .appendSibling(new ChatComponentText("/cofl start"))
                                .appendSibling(new ChatComponentText("\"")
                                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)))
                                .appendSibling(new ChatComponentText(" or click this message"))
                                .setChatStyle(new ChatStyle()
                                        .setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/cofl start"))));
                        break;
                    case "callback":
                        CallbackCommand(args);
                        break;
                    case "dev":
                        if (Config.BaseUrl.contains("localhost")) {
                            CoflSky.Wrapper.startConnection();
                            Config.BaseUrl = "https://sky.coflnet.com";
                        } else {
                            CoflSky.Wrapper.initializeNewSocket("ws://localhost:8009/modsocket");
                            Config.BaseUrl = "http://localhost:5005";
                        }
                        sender.addChatMessage(new ChatComponentText("toggled dev mode, now using " + Config.BaseUrl));
                        break;
                    case "status":
                        sender.addChatMessage(new ChatComponentText(StatusMessage()));
                        break;
                    case "reset":
                        HandleReset();
                        break;
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
                        CoflSky.Wrapper.SendMessage(new JsonStringCommand(CommandType.Clicked,
                                WSClient.gson.toJson("copy:" + textForClipboard)));
                        break;
                    case "connect":
                        if (args.length == 2) {
                            String destination = args[1];

                            if (!destination.contains("://")) {
                                destination = new String(Base64.getDecoder().decode(destination));
                            }
                            sender.addChatMessage(new ChatComponentText("Stopping connection!"));
                            CoflSky.Wrapper.stop();
                            sender.addChatMessage(new ChatComponentText("Opening connection to " + destination));
                            if (CoflSky.Wrapper.initializeNewSocket(destination)) {
                                sender.addChatMessage(new ChatComponentText(
                                        "SkyCofl server is reachable, waiting for connection to be established"));
                            } else {
                                sender.addChatMessage(new ChatComponentText(
                                        "Could not open connection, please check the logs and report them on your Discord!"));
                            }
                        } else {
                            sender.addChatMessage(new ChatComponentText("§cPleace specify a server to connect to"));
                        }
                        break;
                    case "openauctiongui":
                        FlipData flip = WSCommandHandler.flipHandler.fds.getFlipById(args[1]);
                        boolean shouldInvalidate = args.length >= 3 && args[2].equals("true");

                        // Is not a stored flip -> just open the auction
                        if (flip == null) {
                            WSCommandHandler.flipHandler.lastClickedFlipMessage = "";
                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/viewauction " + args[1]);
                            return;
                        }

                        String oneLineMessage = String.join(" ", flip.getMessageAsString()).replaceAll("\n", "")
                                .split(",§7 sellers ah")[0];

                        if (shouldInvalidate) {
                            WSCommandHandler.flipHandler.fds.InvalidateFlip(flip);
                        }

                        WSCommandHandler.flipHandler.lastClickedFlipMessage = oneLineMessage;

                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            BinGuiManager.openNewFlipGui(oneLineMessage, flip.Render);
                        });

                        Minecraft.getMinecraft().thePlayer.sendChatMessage("/viewauction " + flip.Id);
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
                            CoflSky.config.purchaseOverlay = GUIType.COFL;
                            sender.addChatMessage(
                                    new ChatComponentText("[§1C§6oflnet§f]§7: §7Set §bPurchase Overlay §7to: §fCofl"));
                            MinecraftForge.EVENT_BUS.unregister(ButtonRemapper.getInstance());
                        }
                        if (args[1].equalsIgnoreCase("tfm")) {
                            CoflSky.config.purchaseOverlay = GUIType.TFM;
                            sender.addChatMessage(
                                    new ChatComponentText("[§1C§6oflnet§f]§7: §7Set §bPurchase Overlay §7to: §fTFM"));
                            MinecraftForge.EVENT_BUS.register(ButtonRemapper.getInstance());
                        }
                        if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false")) {
                            CoflSky.config.purchaseOverlay = null;
                            sender.addChatMessage(
                                    new ChatComponentText("[§1C§6oflnet§f]§7: §7Set §bPurchase Overlay §7to: §fOff"));
                            MinecraftForge.EVENT_BUS.unregister(ButtonRemapper.getInstance());
                        }
                        break;
                    default:
                        SendCommandToServer(args, sender);
                }
            } else {
                SendCommandToServer("help", "general", sender);
            }
        }).start();
    }

    private void HandleReset() {
        CoflSky.Wrapper.SendMessage(new Command<String>(CommandType.Reset, ""));
        CoflSky.Wrapper.stop();
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Stopping Connection to SkyCofl"));
        CoflSessionManager.DeleteAllCoflSessions();
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Deleting SkyCofl sessions..."));
        if (CoflSky.Wrapper.startConnection())
            Minecraft.getMinecraft().thePlayer
                    .addChatMessage(new ChatComponentText("Started the Connection to SkyCofl"));
    }

    public String StatusMessage() {

        String vendor = System.getProperty("java.vm.vendor");
        String name = System.getProperty("java.vm.name");
        String version = System.getProperty("java.version");
        String detailedVersion = System.getProperty("java.vm.version");

        String status = vendor + " " + name + " " + version + " " + detailedVersion + "|Connection = "
                + (CoflSky.Wrapper != null ? CoflSky.Wrapper.GetStatus() : "UNINITIALIZED_WRAPPER");
        try {
            status += "  uri=" + CoflSky.Wrapper.socket.uri.toString();
        } catch (NullPointerException npe) {
        }

        try {
            CoflSession session = CoflSessionManager.GetCoflSession(PlayerDataProvider.getUsername());
            String sessionString = CoflSessionManager.gson.toJson(session);
            status += "  session=" + sessionString;
        } catch (IOException e) {
        }

        return status;
    }

    public void SendCommandToServer(String[] args, ICommandSender sender) {
        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        SendCommandToServer(args[0], command, sender);
    }

    public void SendCommandToServer(String command, String arguments, ICommandSender sender) {
        RawCommand rc = new RawCommand(command, WSClient.gson.toJson(arguments));
        if (CoflSky.Wrapper.isRunning) {
            CoflSky.Wrapper.SendMessage(rc);
        } else {
            SendAfterStart(sender, rc);
        }
    }

    private static synchronized void SendAfterStart(ICommandSender sender, RawCommand rc) {
        sender.addChatMessage(new ChatComponentText("CoflSky wasn't active.")
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
        // CoflSky.Wrapper.stop();
        CoflSky.Wrapper.startConnection();
        CoflSky.Wrapper.SendMessage(rc);
    }

    public void CallbackCommand(String[] args) {

        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        System.out.println("CallbackData: " + command);
        // new Thread(()->{
        System.out.println("Callback: " + command);
        WSCommandHandler.HandleCommand(new JsonStringCommand(CommandType.Execute, WSClient.gson.toJson(command)),
                Minecraft.getMinecraft().thePlayer);
        CoflSky.Wrapper.SendMessage(new JsonStringCommand(CommandType.Clicked, WSClient.gson.toJson(command)));

        System.out.println("Sent!");
        // }).start();
    }
}
