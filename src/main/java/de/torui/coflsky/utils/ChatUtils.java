package de.torui.coflsky.utils;

public class ChatUtils {

    public static String cleanColour(String in) {
        return in.replaceAll("(?i)\\u00A7.", "");
    }


}
