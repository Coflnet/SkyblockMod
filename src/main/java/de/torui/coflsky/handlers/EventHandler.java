package de.torui.coflsky.handlers;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.configuration.Configuration;
import de.torui.coflsky.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.event.ClickEvent;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import java.util.*;

import static de.torui.coflsky.CoflSky.config;
import static java.lang.Long.parseLong;

public class EventHandler {

    public static boolean isInSkyblock = false;
    public static boolean isInTheCatacombs = false;
    private static long purse = 0;
    private static long bits = 0;
    private static String location = "";
    private static String server = "";

    public static void TabMenuData() {
        if (isInSkyblock && CoflSky.Wrapper.isRunning && Configuration.getInstance().collectTab){
            getTabList().forEach(EventHandler::ProcessTabMenu);
        }
    }

    public static void UploadTabData() {
        if (!CoflSky.Wrapper.isRunning)
            return;
        Command<List<String>> data = new Command<>(CommandType.UPLOAD_TAB, getTabList());
        CoflSky.Wrapper.sendMessage(data);
    }

    public static void ScoreboardData() {
        String s;
        try {
            Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            s = EnumChatFormatting.getTextWithoutFormattingCodes(objective.getDisplayName());
        } catch (Exception e) {
            return;
        }
        checkIfInSkyblock(s);
        if (isInSkyblock && CoflSky.Wrapper.isRunning) {
            List<String> scoreBoardLines = getScoreboard();
            boolean hasFoundCatacombs = false;
            for (String line : scoreBoardLines) {
                line = EnumChatFormatting.getTextWithoutFormattingCodes(line.toLowerCase());
                if (line.contains("the catacombs")) {
                    hasFoundCatacombs = true;
                }
                if (Configuration.getInstance().collectScoreboard) {
                    ProcessScoreboard(line);
                }

            }
            if (hasFoundCatacombs && !isInTheCatacombs) {
                Command<String> data = new Command<>(CommandType.SET, "disableFlips true");
                CoflSky.Wrapper.sendMessage(data);
                isInTheCatacombs = true;
            }
            if (isInTheCatacombs && !hasFoundCatacombs) {
                Command<String> data = new Command<>(CommandType.SET, "disableFlips false");
                CoflSky.Wrapper.sendMessage(data);
                isInTheCatacombs = false;
            }
        }
    }
    private static List<String> getScoreboard() {
        ArrayList<String> scoreboardAsText = new ArrayList<>();
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().theWorld == null) {
            return scoreboardAsText;
        }
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective sideBarObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (sideBarObjective == null) {
            return scoreboardAsText;
        }
        String scoreboardTitle = sideBarObjective.getDisplayName();
        scoreboardTitle = EnumChatFormatting.getTextWithoutFormattingCodes(scoreboardTitle);
        scoreboardAsText.add(scoreboardTitle);
        Collection<Score> scoreboardLines = scoreboard.getSortedScores(sideBarObjective);
        for (Score line : scoreboardLines) {
            String playerName = line.getPlayerName();
            if (playerName == null || playerName.startsWith("#")) {
                continue;
            }
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(playerName);
            String lineText = EnumChatFormatting.getTextWithoutFormattingCodes(
                    ScorePlayerTeam.formatPlayerName(scorePlayerTeam, line.getPlayerName()));
            scoreboardAsText.add(lineText.replace(line.getPlayerName(),""));
        }
        return scoreboardAsText;
    }

    private static List<String> getTabList() {
        ArrayList<String> tabListAsString = new ArrayList<>();
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().getNetHandler() == null) {
            return tabListAsString;
        }
        Collection<NetworkPlayerInfo> playerInfoMap = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
        for (NetworkPlayerInfo playerInfo : playerInfoMap) {
            String toDisplay;
            if (playerInfo.getDisplayName() != null) {
                toDisplay = playerInfo.getDisplayName().getFormattedText();
            } else {
                toDisplay = ScorePlayerTeam.formatPlayerName(playerInfo.getPlayerTeam(), playerInfo.getGameProfile().getName());
            }
            tabListAsString.add(EnumChatFormatting.getTextWithoutFormattingCodes(toDisplay));
        }
        return tabListAsString;
    }
    private static void ProcessTabMenu(String line) {
        if (Configuration.getInstance().collectLobbyChanges && line.contains("server:")) {
            String server_ = line.split("server: ")[1];
            if (!server.equals(server_)) {
                server = server_;
                Command<String> data = new Command<>(CommandType.UPDATE_SERVER, server);
                CoflSky.Wrapper.sendMessage(data);
                UploadTabData();
            }
        } else if (line.contains("area:")) {
            String location_ = line.split("area: ")[1];
            if (!location.equals(location_)) {
                location = location_;
                Command<String> data = new Command<>(CommandType.UPDATE_LOCATION, location);
                CoflSky.Wrapper.sendMessage(data);
            }
        }
    }
    private static void checkIfInSkyblock(String s) {
        if (s.contains("SKYBLOCK") && !isInSkyblock) {
            if (config.autoStart){
                CoflSky.Wrapper.stop();
                CoflSky.Wrapper.startConnection();
            }
            isInSkyblock = true;
        } else if (!s.contains("SKYBLOCK") && isInSkyblock) {
            if (config.autoStart){
                CoflSky.Wrapper.stop();
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Your connection to ")
                        .appendSibling(ChatUtils.getCoflnetLogo(false))
                        .appendSibling(new ChatComponentText(" has been stopped since you left Skyblock.\n    To reconnect enter "))
                        .appendSibling(new ChatComponentText("/cofl start").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE).setBold(true)))
                        .appendSibling(new ChatComponentText(" or click this message"))
                        .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cofl start")))
                );
            }
            isInSkyblock = false;
        }
    }
    private static void ProcessScoreboard(String line){
        if (line.contains("purse") || line.contains("piggy")) {
            long purse_ = 0;
            try {
                purse_ = parseLong(line.split(" ")[1].replace(",", ""));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (purse != purse_) {
                purse = purse_;
                Command<Long> data = new Command<>(CommandType.UPDATE_PURSE, purse);
                CoflSky.Wrapper.sendMessage(data);
            }
        } else if (line.contains("bits")) {
            long bits_ = 0;
            try {
                bits_ = parseLong(line.split(" ")[1].replace(",", ""));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (bits != bits_) {
                bits = bits_;
                Command<Long> data = new Command<>(CommandType.UPDATE_BITS, bits);
                CoflSky.Wrapper.sendMessage(data);
            }
        }
    }
}
