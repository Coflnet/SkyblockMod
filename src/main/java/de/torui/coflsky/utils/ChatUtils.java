package de.torui.coflsky.utils;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ChatUtils {

    public static String cleanColour(String in) {
        return in.replaceAll("(?i)\\u00A7.", "");
    }
    public static IChatComponent getCoflnetLogo(boolean includeBrackets){
        IChatComponent cofl = new ChatComponentText("C").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_BLUE))
							.appendSibling(new ChatComponentText("oflnet").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)));
        if (includeBrackets){
            cofl = new ChatComponentText("[").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE))
                    .appendSibling(cofl)
                    .appendSibling(new ChatComponentText("] ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE)));
        }
        return cofl;
    }


}
