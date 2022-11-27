package de.torui.coflsky.bingui.gui;

import net.minecraft.util.IChatComponent;

public class BinGuiManager {
    public static BinGui currentGui = null;

    //this can be used to open the gui
    public static void openOldFlipGui(IChatComponent message, String[] lore, String auctionId) {
        if (currentGui != null) {
            currentGui.close();
        }
        currentGui = new BinGui(message, lore);
        currentGui.open(auctionId);
    }

    public static void openNewFlipGui(IChatComponent message, String[] lore, String auctionId, String extraData) {
        new BinGuiCurrent(message, lore, auctionId, extraData);
    }
}
