package de.torui.coflsky.bingui.helper.inputhandler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public class MouseHelper {
    public static boolean wasMouseDown = false;

    public static boolean wasMouseDown() {
        return wasMouseDown;
    }

    @SubscribeEvent
    public void onRenderEvent(TickEvent.RenderTickEvent event) {
        if (Minecraft.getMinecraft() == null) return;
        if (event.phase == TickEvent.Phase.END) {
            wasMouseDown = Mouse.isButtonDown(0);
        }
    }


}
