package de.torui.coflsky.bingui.gui;

import net.minecraft.util.IChatComponent;

public class BinGuiManager {
    public static BinGui currentGui = null;

    //this can be used to open the gui
    public static void openFlipGui(IChatComponent message, String[] lore, String auctionId) {
        currentGui = new BinGui(message, lore);
        currentGui.open(auctionId);
    }
}
