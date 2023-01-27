package de.torui.coflsky.listeners;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class AuctionViewListener {
    private Robot robot;

    public AuctionViewListener(){
        try{
            this.robot = new Robot();
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiChest){
            int slotNumber = 0;
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest containerChest = (ContainerChest) gui.inventorySlots;

            String containerName = containerChest.getLowerChestInventory().getDisplayName().getUnformattedText();

            if(containerName.trim().startsWith("Auction View")) {
                // Only move the mouse if the gui is auction view
                for (Slot slot : containerChest.inventorySlots) {
                    if (slotNumber == 13) {
                        if (this.robot == null) {
                            return;
                        }
                        this.robot.mouseMove(slot.xDisplayPosition, slot.yDisplayPosition);
                        break;
                    }
                    slotNumber++;
                }
            }

        }
    }


}
