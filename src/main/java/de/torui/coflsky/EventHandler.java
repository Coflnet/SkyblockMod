package de.torui.coflsky;

import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.configuration.Configuration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EventHandler {

    public static boolean isInSkyblock = false;
    public static boolean isInTheCatacombs = false;
    private static int purse = 0;
    private static int bits = 0;
    private static String location = "";
    private static String server = "";

    public static void TabMenuData() {
        if (isInSkyblock ){
            List<String> tabdata = getTabList();
            int size = tabdata.size() - 1;
            for (int i = 0; i < tabdata.size(); i++) {
                String line = tabdata.get(size - i).toLowerCase();
                if (line.contains("server:")) {
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
        }
    }
    public static void ScoreboardData() {
        String s;
        try {
            Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            s = objective.getDisplayName();
        } catch (Exception e) {
            s = "";
        }
        if (s.contains("SKYBLOCK") && !isInSkyblock) {
            CoflSky.Wrapper.stop();
            CoflSky.Wrapper.startConnection();
            isInSkyblock = true;
        } else if (!s.contains("SKYBLOCK") && isInSkyblock) {
            CoflSky.Wrapper.stop();
            isInSkyblock = false;
        }
        if (isInSkyblock) {
            List<String> scoreBoardLines = getScoreboard();
            int size = scoreBoardLines.size() - 1;
            boolean hasFoundCatacombs = false;
            for (int i = 0; i < scoreBoardLines.size(); i++) {
                String line = EnumChatFormatting.getTextWithoutFormattingCodes(scoreBoardLines.get(size - i).toLowerCase());
                if (line.contains("the catacombs")) {
                    hasFoundCatacombs = true;
                }
                if (Configuration.getInstance().collectScoreboard) {
                    if (line.contains("purse")) {
                        int purse_ = 0;
                        try {
                            purse_ = Integer.parseInt(line.split(": ")[1].replace(",", ""));
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
                            bits_ = Integer.parseInt(line.split(": ")[1].replace(",", ""));
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
    public static List<String> getScoreboard() {
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

    public static List<String> getTabList() {
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
}
