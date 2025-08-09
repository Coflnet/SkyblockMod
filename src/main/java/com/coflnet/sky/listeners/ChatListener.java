package com.coflnet.sky.listeners;

import com.coflnet.sky.SkyCofl;
import com.coflnet.sky.utils.ChatUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatListener {

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public void onGuiChat(ClientChatReceivedEvent e) {
        String unformatted = ChatUtils.cleanColour(e.message.getUnformattedText());
    }


}
