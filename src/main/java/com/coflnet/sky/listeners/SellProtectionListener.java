package com.coflnet.sky.listeners;

import com.coflnet.sky.config.SellProtectionConfig;
import com.coflnet.sky.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener that provides sell protection for valuable items
 */
public class SellProtectionListener {
    
    // Patterns for clean text (without formatting codes)
    private static final Pattern CLEAN_SELL_INSTANTLY_PATTERN = Pattern.compile("Total: ([0-9,\\.]+) coins");
    private static final Pattern CLEAN_SELL_INVENTORY_PATTERN = Pattern.compile("You earn: ([0-9,\\.]+) coins");
    private static final Pattern CLEAN_SELL_SACKS_PATTERN = Pattern.compile("You earn: ([0-9,\\.]+) coins");
    
    // Patterns with formatting codes as backup
    private static final Pattern SELL_INSTANTLY_PATTERN = Pattern.compile("Total: §6([0-9,\\.]+) coins");
    private static final Pattern SELL_INVENTORY_PATTERN = Pattern.compile("You earn: §6([0-9,\\.]+) coins");
    private static final Pattern SELL_SACKS_PATTERN = Pattern.compile("You earn: §6([0-9,\\.]+) coins");
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInventoryClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!SellProtectionConfig.isEnabled()) {
            return;
        }
        
        if (!Mouse.getEventButtonState()) {
            return; // Only handle button press events
        }
        
        GuiScreen gui = event.gui;
        if (!(gui instanceof GuiChest)) {
            return;
        }
        
        GuiChest chestGui = (GuiChest) gui;
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
        
        // Check if Ctrl is pressed to bypass protection
        boolean ctrlPressed = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        
        // Get the hovered slot
        int mouseX = Mouse.getX() * gui.width / Minecraft.getMinecraft().displayWidth;
        int mouseY = gui.height - Mouse.getY() * gui.height / Minecraft.getMinecraft().displayHeight - 1;
        
        net.minecraft.inventory.Slot hoveredSlot = getHoveredSlot(chestGui, mouseX, mouseY);
        if (hoveredSlot == null || !hoveredSlot.getHasStack()) {
            return;
        }
        
        ItemStack hoveredItem = hoveredSlot.getStack();
        String itemName = hoveredItem.getDisplayName();
        String cleanItemName = ChatUtils.cleanColour(itemName);
        
        // Check for protected items
        boolean shouldProtect = false;
        int mouseButton = Mouse.getEventButton(); // 0 = left, 1 = right
        
        if (cleanItemName.contains("Sell Instantly")) {
            // Block left click unless ctrl is pressed
            if (mouseButton == 0 && !ctrlPressed) {
                shouldProtect = true;
            }
        } else if (cleanItemName.contains("Sell Sacks Now") || cleanItemName.contains("Sell Inventory Now")) {
            // Block both left and right click unless ctrl is pressed
            if (!ctrlPressed) {
                shouldProtect = true;
            }
        }
        
        if (shouldProtect) {
            // Parse the sell amount from the item lore
            long sellAmount = parseSellAmount(hoveredItem);
            long threshold = SellProtectionConfig.getSellProtectionThreshold();
            
            if (sellAmount >= threshold) {
                // Cancel the click and show warning message
                event.setCanceled(true);
                
                String formattedAmount = SellProtectionConfig.formatThreshold(sellAmount);
                String warningMessage = String.format("§c⚠ §lSell Protection §c⚠ §7Blocked sale of §6%s coins§7! Hold §bCtrl§7 to override.", formattedAmount);
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(warningMessage));
                
                return;
            }
        }
    }
    
    /**
     * Gets the slot that the mouse is hovering over
     */
    private net.minecraft.inventory.Slot getHoveredSlot(GuiChest gui, int mouseX, int mouseY) {
        try {
            // Try to use reflection to get the hovered slot
            java.lang.reflect.Method getSlotAtPosition = GuiChest.class.getMethod("getSlotAtPosition", int.class, int.class);
            Object result = getSlotAtPosition.invoke(gui, mouseX, mouseY);
            if (result instanceof net.minecraft.inventory.Slot) {
                return (net.minecraft.inventory.Slot) result;
            }
        } catch (Exception ignored) {
            // Fallback: iterate through slots manually
        }
        
        // Fallback method
        for (Object obj : gui.inventorySlots.inventorySlots) {
            if (!(obj instanceof net.minecraft.inventory.Slot)) continue;
            net.minecraft.inventory.Slot slot = (net.minecraft.inventory.Slot) obj;
            int slotX = gui.guiLeft + slot.xDisplayPosition;
            int slotY = gui.guiTop + slot.yDisplayPosition;
            if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                return slot;
            }
        }
        return null;
    }
    
    /**
     * Parses the sell amount from an item's lore
     */
    private long parseSellAmount(ItemStack item) {
        List<String> lore = item.getTooltip(Minecraft.getMinecraft().thePlayer, false);
        
        for (String line : lore) {
            String cleanLine = ChatUtils.cleanColour(line);
            
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
        
        // Default value if amount can't be determined - use threshold to trigger protection
        return SellProtectionConfig.getSellProtectionThreshold();
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
            // If parsing fails, return the threshold value to trigger protection
            return SellProtectionConfig.getSellProtectionThreshold();
        }
    }
}
