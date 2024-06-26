package de.torui.coflsky.handlers;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.configuration.Configuration;
import de.torui.coflsky.minecraft_integration.PlayerDataProvider;
import de.torui.coflsky.minecraft_integration.PlayerDataProvider.PlayerPosition;
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
        if (isInSkyblock && CoflSky.Wrapper.isRunning && Configuration.getInstance().collectTab) {
            List<String> tabdata = getTabList();
            int size = tabdata.size() - 1;
            for (int i = 0; i < tabdata.size(); i++) {
                String line = tabdata.get(size - i).toLowerCase();
                ProcessTabMenu(line);
            }
        }
    }

    public static void UploadTabData() {
        if (!CoflSky.Wrapper.isRunning)
            return;
        Command<List<String>> data = new Command<>(CommandType.uploadTab, getTabList());
        CoflSky.Wrapper.SendMessage(data);
    }

    public static void UploadScoreboardData() {
        if (!CoflSky.Wrapper.isRunning)
            return;
        Command<List<String>> data = new Command<>(CommandType.uploadScoreboard, getScoreboard());
        CoflSky.Wrapper.SendMessage(data);
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
        if (!isInSkyblock || !CoflSky.Wrapper.isRunning)
            return;

        List<String> scoreBoardLines = getScoreboard();
        int size = scoreBoardLines.size() - 1;
        boolean foundPurse = false;
        for (int i = 0; i < scoreBoardLines.size(); i++) {
            String line = EnumChatFormatting.getTextWithoutFormattingCodes(scoreBoardLines.get(size - i).toLowerCase());
            if (Configuration.getInstance().collectScoreboard) {
                if(ProcessScoreboard(line))
                    foundPurse = true;
            }
            if (line.contains("⏣") && !line.equals(location)) {
                location = line;
                try {
                    Thread.sleep(20);
                    UploadLocation();
                    Thread.sleep(20);
                    UploadScoreboardData();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!foundPurse && purse != -1)
        {
            purse = -1; // no purse found, sync that to server
            Command<Long> data = new Command<>(CommandType.updatePurse, purse);
            CoflSky.Wrapper.SendMessage(data);
            UploadScoreboardData();
            UploadTabData();
        }
    }

    private static void UploadLocation() {
        if (!Configuration.getInstance().collectLocation)
            return;
        Command<PlayerPosition> data = new Command<>(CommandType.updateLocation,
                PlayerDataProvider.getPlayerPosition());
        CoflSky.Wrapper.SendMessage(data);
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
            scoreboardAsText.add(lineText.replace(line.getPlayerName(), ""));
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
                toDisplay = ScorePlayerTeam.formatPlayerName(playerInfo.getPlayerTeam(),
                        playerInfo.getGameProfile().getName());
            }
            tabListAsString.add(EnumChatFormatting.getTextWithoutFormattingCodes(toDisplay));
        }
        return tabListAsString;
    }

    private static void ProcessTabMenu(String line) {
        if (line.contains("server:")) {
            String server_ = line.split("server: ")[1];
            if (!server.equals(server_)) {
                server = server_;
                UploadTabData();
            }
        }
    }

    private static void checkIfInSkyblock(String s) {
        if ((s.contains("SKYBLOCK") || s.contains("E")) && !isInSkyblock) {
            if (config.autoStart) {
                CoflSky.Wrapper.stop();
                CoflSky.Wrapper.startConnection();
            }
            isInSkyblock = true;
        } else if (!s.contains("SKYBLOCK") && !s.contains("E") && isInSkyblock) {
            if (config.autoStart) {
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

    private static boolean ProcessScoreboard(String line) {
        if (line.contains("purse") || line.contains("piggy")) {
            long purse_ = 0;
            try {
                purse_ = parseLong(line.split(" ")[1].replace(",", "").split("\\.")[0]);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("unparsable purse: " + line);
            }
            if (purse != purse_) {
                purse = purse_;
                Command<Long> data = new Command<>(CommandType.updatePurse, purse);
                CoflSky.Wrapper.SendMessage(data);
                UploadLocation();
            }
            return true;
        } else if (line.contains("bits")) {
            long bits_ = 0;
            try {
                bits_ = parseLong(line.split(" ")[1].replace(",", ""));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (bits != bits_) {
                bits = bits_;
                Command<Long> data = new Command<>(CommandType.updateBits, bits);
                CoflSky.Wrapper.SendMessage(data);
            }
            return true;
        }
        return false;
    }
}
