package de.torui.coflsky.bingui.gui;

import net.minecraft.util.IChatComponent;

public class BinGuiManager {

    public static void openNewFlipGui(IChatComponent message, String[] lore, String auctionId, String extraData) {
        new BetaGui(message, lore, auctionId); //faster gui but doesn't look as good
        //new BinGuiCurrent(message, lore, auctionId, extraData); //slower gui but looks better
        //new BinGuiNew(message, lore, auctionId, extraData); //slower gui but looks better v2
    }
}
