package de.torui.coflsky.listeners;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatListener {

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public void onGuiChat(ClientChatReceivedEvent e) {
        String unformatted = ChatUtils.cleanColour(e.message.getUnformattedText());
        if(unformatted.startsWith("Your new API key is ")){
            // We have found the api key YAY!
            if (!CoflSky.getAPIKeyManager().getApiInfo().setKey(unformatted.substring("Your new API key is ".length()).substring(0, 36))){
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatUtils.getCoflnetLogo(true)
                        .appendSibling(new ChatComponentText("ERROR! ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED).setBold(true)))
                        .appendSibling(new ChatComponentText("Invalid API Key!").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)))
                );
            }
        }
    }


}
