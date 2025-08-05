package de.torui.coflsky.listeners;

import de.torui.coflsky.SkyCofl;
import de.torui.coflsky.utils.ChatUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatListener {

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public void onGuiChat(ClientChatReceivedEvent e) {
        String unformatted = ChatUtils.cleanColour(e.message.getUnformattedText());
    }


}
