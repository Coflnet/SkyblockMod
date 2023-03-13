package de.torui.coflsky.gui.bingui;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.gui.GUIType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.InventoryBasic;
import org.lwjgl.input.Mouse;

public class BinGuiManager {

    public static void openNewFlipGui(String message, String extraData) {
        if( CoflSky.config.purchaseOverlay != GUIType.COFL){
            return;
        }

        GuiChest currentGui = new BinGuiCurrent(Minecraft.getMinecraft().thePlayer.inventory, new InventoryBasic("", false, 27), message, extraData);
        Mouse.setGrabbed(false);
        Minecraft.getMinecraft().displayGuiScreen(currentGui);
    }
}