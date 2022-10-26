package de.torui.coflsky.bingui.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

public class GuiScreenInject {
    @SubscribeEvent
    public void onDrawGuiScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        //first i get myself the gui
        GuiScreen gui = event.gui;

        //then i check if it is a chest gui
        if (!(gui instanceof GuiChest)) return;
        GuiChest chest = (GuiChest) gui;

        //then i get the private field named lowerChestInventory
        IInventory inventory = null;
        try {
            Field field = GuiChest.class.getDeclaredField("lowerChestInventory");
            field.setAccessible(true);
            inventory = (IInventory) field.get(chest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //then a little null check
        if (inventory == null) return;

        if (inventory.getDisplayName().getFormattedText().contains("BIN Auction")) {
            //then i draw my own gui
            BinGuiOverlay overlay = new BinGuiOverlay(gui.width, gui.height);
            overlay.renderMainGui(event.mouseX, event.mouseY);
        }

    }
}
