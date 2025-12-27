package com.coflnet.sky.handlers;

import com.coflnet.sky.config.SellProtectionConfig;
import com.coflnet.sky.utils.ChatUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles adding sell protection information to item tooltips
 */
public class SellProtectionTooltipHandler {
    
    // Patterns for clean text (without formatting codes)
    private static final Pattern CLEAN_SELL_INSTANTLY_PATTERN = Pattern.compile("Total: ([0-9,\\.]+) coins");
    private static final Pattern CLEAN_SELL_INVENTORY_PATTERN = Pattern.compile("You earn: ([0-9,\\.]+) coins");
    private static final Pattern CLEAN_SELL_SACKS_PATTERN = Pattern.compile("You earn: ([0-9,\\.]+) coins");
    private static final Pattern CLEAN_NO_ITEMS_PATTERN = Pattern.compile("You don't have anything to sell");
    
    // Patterns with formatting codes as backup
    private static final Pattern SELL_INSTANTLY_PATTERN = Pattern.compile("Total: §6([0-9,\\.]+) coins");
    private static final Pattern SELL_INVENTORY_PATTERN = Pattern.compile("You earn: §6([0-9,\\.]+) coins");
    private static final Pattern SELL_SACKS_PATTERN = Pattern.compile("You earn: §6([0-9,\\.]+) coins");
    private static final Pattern NO_ITEMS_PATTERN = Pattern.compile("You don't have anything to sell");
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!SellProtectionConfig.isEnabled()) {
            return;
        }
        
        // Check if we're in a chest GUI
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen == null || !(mc.currentScreen instanceof GuiChest)) {
            return;
        }
        
        GuiChest chestGui = (GuiChest) mc.currentScreen;
        ContainerChest container = (ContainerChest) chestGui.inventorySlots;
        IInventory inventory = container.getLowerChestInventory();
        
        if (!inventory.hasCustomName()) {
            return;
        }
        
        String chestName = inventory.getDisplayName().getUnformattedText();
        
        // Check if this is a chest with ➜ in the name
        if (!chestName.contains("➜")) {
            return;
        }
        
        String itemName = event.itemStack.getDisplayName();
        String cleanItemName = ChatUtils.cleanColour(itemName);
        
        // Check if this is a sell item
        boolean isSellItem = cleanItemName.contains("Sell Instantly") || 
                           cleanItemName.contains("Sell Sacks Now") || 
                           cleanItemName.contains("Sell Inventory Now");
        
        if (isSellItem) {
            // Parse the sell amount
            long sellAmount = parseSellAmountFromTooltip(event.toolTip);
            long threshold = SellProtectionConfig.getSellProtectionThreshold();
            
            // Only add tooltip if threshold is met (item is protected)
            if (sellAmount >= threshold) {
                String formattedThreshold = SellProtectionConfig.formatThreshold(threshold);
                
                event.toolTip.add("");
                event.toolTip.add("§c⚠ §lSell Protection §c⚠");
                event.toolTip.add("§7All clicks blocked if > §6" + formattedThreshold + " coins");
                event.toolTip.add("§bHold Ctrl§7 to override.");
                event.toolTip.add("§8/cofl set sellProtectionThreshold <amount>");
            }
        }
    }
    
    /**
     * Parses the sell amount from tooltip lines
     */
    private long parseSellAmountFromTooltip(List<String> tooltip) {
        for (String line : tooltip) {
            String cleanLine = ChatUtils.cleanColour(line);
            
            // Check if there's nothing to sell
            Matcher noItemsMatcher = CLEAN_NO_ITEMS_PATTERN.matcher(cleanLine);
            if (noItemsMatcher.find()) {
                return 0; // Return 0 for empty inventory
            }
            noItemsMatcher = NO_ITEMS_PATTERN.matcher(line);
            if (noItemsMatcher.find()) {
                return 0; // Return 0 for empty inventory
            }
            
            // Try different patterns with clean text
            Matcher matcher = CLEAN_SELL_INSTANTLY_PATTERN.matcher(cleanLine);
            if (matcher.find()) {
                return parseCoins(matcher.group(1));
            }
            
            matcher = CLEAN_SELL_INVENTORY_PATTERN.matcher(cleanLine);
            if (matcher.find()) {
                return parseCoins(matcher.group(1));
            }
            
            matcher = CLEAN_SELL_SACKS_PATTERN.matcher(cleanLine);
            if (matcher.find()) {
                return parseCoins(matcher.group(1));
            }
            
            // Also try with formatted text as backup
            matcher = SELL_INSTANTLY_PATTERN.matcher(line);
            if (matcher.find()) {
                return parseCoins(matcher.group(1));
            }
            
            matcher = SELL_INVENTORY_PATTERN.matcher(line);
            if (matcher.find()) {
                return parseCoins(matcher.group(1));
            }
            
            matcher = SELL_SACKS_PATTERN.matcher(line);
            if (matcher.find()) {
                return parseCoins(matcher.group(1));
            }
        }
        
        // Default value if amount can't be determined
        return 0; // Return 0 so it won't show tooltip unless we know the value
    }
    
    /**
     * Parses a coin string like "1,873,813" or "13.1" to long value
     */
    private long parseCoins(String coinStr) {
        try {
            // Remove commas and convert to number
            String cleaned = coinStr.replace(",", "");
            
            // Handle decimal values (like "13.1")
            if (cleaned.contains(".")) {
                double value = Double.parseDouble(cleaned);
                return (long) value;
            } else {
                return Long.parseLong(cleaned);
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return 0 so tooltip won't show
            return 0;
        }
    }
}
