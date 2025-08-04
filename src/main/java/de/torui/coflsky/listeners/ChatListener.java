package de.torui.coflsky.listeners;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.utils.ChatUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatListener {

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public void onGuiChat(ClientChatReceivedEvent e) {
        String unformatted = ChatUtils.cleanColour(e.message.getUnformattedText());

        if(unformatted.startsWith("Your new API key is ")){
            // We have found the api key YAY!
            CoflSky.getAPIKeyManager().getApiInfo().key = unformatted.substring("Your new API key is ".length()).substring(0, 36);
        }
    }


}
