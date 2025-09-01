package com.coflnet.sky.utils;

import com.coflnet.sky.mixins.AccessorGuiEditSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/**
 * Utility class for automating bazaar search functionality
 */
public class BazaarSearchUtil {
    
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static String pendingSearchTerm = null;
    private static boolean isSearching = false;
    
    /**
     * Automatically searches for an item in the bazaar
     * @param searchTerm The item name to search for
     * @return true if the search was initiated, false if not in a valid bazaar GUI
     */
    public static boolean searchInBazaar(String searchTerm) {
        if (mc.thePlayer == null || mc.currentScreen == null) {
            return false;
        }
        
        GuiScreen currentGui = mc.currentScreen;
        if (!(currentGui instanceof GuiChest)) {
            return false;
        }
        
        GuiChest chestGui = (GuiChest) currentGui;
        ContainerChest container = (ContainerChest) chestGui.inventorySlots;
        IInventory inventory = container.getLowerChestInventory();
        
        if (inventory == null) {
            return false;
        }
        
        String guiName = inventory.getDisplayName().getUnformattedText().trim();
        
        // Check if we're in a bazaar GUI (common names for bazaar interfaces)
        if (!isBazaarGui(guiName)) {
            return false;
        }
        
        // Look for the search button/item in the GUI
        ItemStack searchItem = findSearchItem(container);
        if (searchItem == null) {
            return false;
        }
        
        // Find the slot containing the search item
        int searchSlot = findSearchSlot(container);
        if (searchSlot == -1) {
            return false;
        }
        
        // Store the search term for when the sign opens
        pendingSearchTerm = searchTerm;
        isSearching = true;
        
        // Register the event handler to handle the sign GUI
        MinecraftForge.EVENT_BUS.register(new BazaarSearchEventHandler());
        
        // Click on the search item
        mc.playerController.windowClick(container.windowId, searchSlot, 0, 0, mc.thePlayer);
        
        return true;
    }
    
    /**
     * Checks if the current GUI is a bazaar interface
     */
    private static boolean isBazaarGui(String guiName) {
        String lowerName = guiName.toLowerCase();
        return lowerName.contains("bazaar") || 
               lowerName.contains("buy") && lowerName.contains("sell") ||
               lowerName.contains("instant buy") ||
               lowerName.contains("instant sell");
    }
    
    /**
     * Finds the search item in the chest inventory
     */
    private static ItemStack findSearchItem(ContainerChest container) {
        IInventory inventory = container.getLowerChestInventory();
        
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) {
                String displayName = stack.getDisplayName();
                if (displayName != null) {
                    String cleanName = EnumChatFormatting.getTextWithoutFormattingCodes(displayName).toLowerCase();
                    // Look for common search button names
                    if (cleanName.contains("search") || 
                        cleanName.contains("find") ||
                        cleanName.contains("look up")) {
                        return stack;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Finds the slot number containing the search item
     */
    private static int findSearchSlot(ContainerChest container) {
        IInventory inventory = container.getLowerChestInventory();
        
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) {
                String displayName = stack.getDisplayName();
                if (displayName != null) {
                    String cleanName = EnumChatFormatting.getTextWithoutFormattingCodes(displayName).toLowerCase();
                    if (cleanName.contains("search") || 
                        cleanName.contains("find") ||
                        cleanName.contains("look up")) {
                        return i;
                    }
                }
                
                // Also check lore for search functionality
                List<String> lore = stack.getTooltip(mc.thePlayer, false);
                for (String line : lore) {
                    String cleanLine = EnumChatFormatting.getTextWithoutFormattingCodes(line).toLowerCase();
                    if (cleanLine.contains("search") || 
                        cleanLine.contains("find") ||
                        cleanLine.contains("look up")) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    /**
     * Event handler class for handling the sign GUI that opens after clicking search
     */
    public static class BazaarSearchEventHandler {
        
        @SubscribeEvent
        public void onGuiOpen(GuiOpenEvent event) {
            if (!isSearching || pendingSearchTerm == null) {
                return;
            }
            
            if (event.gui instanceof GuiEditSign) {
                GuiEditSign signGui = (GuiEditSign) event.gui;
                
                try {
                    // Use the accessor to get the tile entity sign
                    TileEntitySign tileEntitySign = ((AccessorGuiEditSign) signGui).getTileSign();
                    
                    // Fill in the search term on the first line of the sign
                    tileEntitySign.signText[0] = new ChatComponentText(pendingSearchTerm);
                    
                    // Schedule the sign to be closed after a short delay to save the search
                    mc.addScheduledTask(() -> {
                        // Close the sign by sending the sign update and closing the GUI
                        if (mc.currentScreen instanceof GuiEditSign) {
                            // Trigger the sign update by setting the text again
                            tileEntitySign.signText[0] = new ChatComponentText(pendingSearchTerm);
                            
                            // Close the GUI to save the search
                            mc.thePlayer.closeScreen();
                        }
                        
                        // Clean up
                        cleanup();
                    });
                    
                } catch (Exception e) {
                    System.err.println("Failed to access tileSign field in GuiEditSign for bazaar search: " + e.getMessage());
                    e.printStackTrace();
                    cleanup();
                }
            }
        }
        
        private void cleanup() {
            pendingSearchTerm = null;
            isSearching = false;
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }
    
    /**
     * Checks if a search operation is currently in progress
     */
    public static boolean isSearching() {
        return isSearching;
    }
    
    /**
     * Gets the current pending search term
     */
    public static String getPendingSearchTerm() {
        return pendingSearchTerm;
    }
    
    /**
     * Cancels the current search operation
     */
    public static void cancelSearch() {
        pendingSearchTerm = null;
        isSearching = false;
    }
}
