package de.torui.coflsky.handlers;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.configuration.Configuration;
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
import static java.lang.Integer.parseInt;

public class EventHandler {

    public static boolean isInSkyblock = false;
    public static boolean isInTheCatacombs = false;
    private static int purse = 0;
    private static int bits = 0;
    private static String location = "";
    private static String server = "";

    public static void TabMenuData() {
        if (isInSkyblock && CoflSky.Wrapper.isRunning && Configuration.getInstance().collectTab){
            List<String> tabdata = getTabList();
            int size = tabdata.size() - 1;
            for (int i = 0; i < tabdata.size(); i++) {
                String line = tabdata.get(size - i).toLowerCase();
                ProcessTabMenu(line);
            }
        }
    }
    public static void ScoreboardData() {
        String s;
        try {
            Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            s = EnumChatFormatting.getTextWithoutFormattingCodes(objective.getDisplayName());
        } catch (Exception e) {
            s = "";
        }
        checkIfInSkyblock(s);
        if (isInSkyblock && CoflSky.Wrapper.isRunning) {
            List<String> scoreBoardLines = getScoreboard();
            int size = scoreBoardLines.size() - 1;
            boolean hasFoundCatacombs = false;
            for (int i = 0; i < scoreBoardLines.size(); i++) {
                String line = EnumChatFormatting.getTextWithoutFormattingCodes(scoreBoardLines.get(size - i).toLowerCase());
                if (line.contains("the catacombs")) {
                    hasFoundCatacombs = true;
                }
                if (Configuration.getInstance().collectScoreboard) {
                    ProcessScoreboard(line);
                }

            }
            if (hasFoundCatacombs && !isInTheCatacombs) {
                Command<String> data = new Command<>(CommandType.set, "disableFlips true");
                CoflSky.Wrapper.SendMessage(data);
                isInTheCatacombs = true;
            }
            if (isInTheCatacombs && !hasFoundCatacombs) {
                Command<String> data = new Command<>(CommandType.set, "disableFlips false");
                CoflSky.Wrapper.SendMessage(data);
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
                Command<String> data = new Command<>(CommandType.updateServer, server);
                CoflSky.Wrapper.SendMessage(data);
            }
        } else if (line.contains("area:")) {
            String location_ = line.split("area: ")[1];
            if (!location.equals(location_)) {
                location = location_;
                Command<String> data = new Command<>(CommandType.updateLocation, location);
                CoflSky.Wrapper.SendMessage(data);
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
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("connection to ")
                        .appendSibling(new ChatComponentText("C").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_BLUE)))
                        .appendSibling(new ChatComponentText("oflnet").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)))
                        .appendSibling(new ChatComponentText(" has been stopped since you left skyblock.\n    To reconnect enter "))
                        .appendSibling(new ChatComponentText("\"").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)))
                        .appendSibling(new ChatComponentText("/cofl start"))
                        .appendSibling(new ChatComponentText("\"").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)))
                        .appendSibling(new ChatComponentText(" or click this message"))
                        .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cofl start")))
                );
            }
            isInSkyblock = false;
        }
    }
    private static void ProcessScoreboard(String line){
        if (line.contains("purse") || line.contains("piggy")) {
            int purse_ = 0;
            try {
                purse_ = parseInt(line.split(": ")[1].replace(",", ""));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (purse != purse_) {
                purse = purse_;
                Command<Integer> data = new Command<>(CommandType.updatePurse, purse);
                CoflSky.Wrapper.SendMessage(data);
            }
        } else if (line.contains("bits")) {
            int bits_ = 0;
            try {
                bits_ = parseInt(line.split(": ")[1].replace(",", ""));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (bits != bits_) {
                bits = bits_;
                Command<Integer> data = new Command<>(CommandType.updateBits, bits);
                CoflSky.Wrapper.SendMessage(data);
            }
        }
    }
}
