package com.coflnet.sky.handlers;


import CoflCore.classes.Position;
import com.coflnet.sky.minecraft_integration.PlayerDataProvider;
import com.coflnet.sky.utils.ReflectionUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ForgeDescriptionHandler {

    private static class InventoryWrapper {
        public String chestName;
        public String fullInventoryNbt;
    }

    public static String allItemIds;

    // Maps new UUIDs to original UUID when items update with new UUIDs but same title
    // This allows finding descriptions loaded for the original UUID when hovering an item with updated UUID
    public static final java.util.Map<String, String> uuidToOriginalUuid = new java.util.HashMap<>();

    public static final NBTTagCompound EMPTY_COMPOUND = new NBTTagCompound();

    private boolean IsOpen = true;
    private boolean shouldUpdate = false;

    public void Close() {
        IsOpen = false;
    }

    public static String ExtractStackableIdFromItemStack(ItemStack stack) {
        if (stack != null) {
            try {
                NBTTagCompound serialized = stack.serializeNBT();
                String name = serialized.getCompoundTag("tag").getCompoundTag("display")
                        .getString("Name");
                if(name != null && (name.contains("BUY") || name.contains("SELL"))) {
                    NBTTagList loreNbtList = serialized.getCompoundTag("tag").getCompoundTag("display").getTagList("Lore", 8);

                    for (int i = 0; i < loreNbtList.tagCount(); i++) {
                        if (loreNbtList.getStringTagAt(i).contains("Price per unit")) {
                            return name + loreNbtList.getStringTagAt(i);
                        }
                    }
                }
                return name + ":" + stack.stackSize;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String ExtractIdFromItemStack(ItemStack stack) {
        if (stack != null) {
            try {
                String uuid = stack.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes")
                        .getString("uuid");
                if (uuid.length() == 0) {
                    throw new Exception();
                }
                return uuid;
            } catch (Exception e) {
            }
        }
        return ExtractStackableIdFromItemStack(stack);
    }

    private CoflCore.handlers.DescriptionHandler.DescModification[] getTooltipData(ItemStack itemStack) {
        String id = ExtractIdFromItemStack(itemStack);
        return CoflCore.handlers.DescriptionHandler.getTooltipData(id);
    }

    /**
     * Called when the inventory is opened
     * checks for changes every once in a while and updates the description if
     * there was a change found
     *
     * @param event
     */
    public void loadDescriptionAndListenForChanges(GuiOpenEvent event) {

        GuiContainer gc = (GuiContainer) event.gui;

        shouldUpdate = loadDescriptionForInventory(event, gc, false);
        int iteration = 1;
        while (IsOpen) {
            try {
                Thread.sleep(300 * iteration++);
                iteration++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(!IsOpen) {
                break;
            }
            if (shouldUpdate || hasAnyStackChanged(gc)) {
                shouldUpdate = loadDescriptionForInventory(event, gc, true);
                // reduce update time since its more likely that more changes occure after one
                iteration = 5;
            }
            if (iteration >= 30)
                iteration = 29; // cap at 9 second update interval
        }
    }

    private static boolean hasAnyStackChanged(GuiContainer gc) {
        return !allItemIds.equals(getCurrentInventoryIds(gc));
    }

    private static String getCurrentInventoryIds(GuiContainer gc) {
        StringBuilder builder = new StringBuilder();

        for (Slot obj : gc.inventorySlots.inventorySlots) {
            ItemStack stack = obj.getStack();
            String id = ExtractIdFromItemStack(stack);
            builder.append(id);
        }

        return builder.toString();
    }

    public static void uploadInventory() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.thePlayer.inventory == null) {
            return;
        }
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList tl = new NBTTagList();
        List<ItemStack> items = new ArrayList<>();
        List<String> itemIds = new ArrayList<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < mc.thePlayer.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            String id = ExtractIdFromItemStack(stack);
            itemIds.add(id);
            items.add(stack);
            if (stack != null) {
                tl.appendTag(stack.serializeNBT());
            } else {
                tl.appendTag(EMPTY_COMPOUND);
            }
        }
        try {
            compound.setTag("i", tl);
            CompressedStreamTools.writeCompressed(compound, baos);

            String fullNbt = Base64.getEncoder().encodeToString(baos.toByteArray());
            String username = PlayerDataProvider.getUsername();
            String[] itemArr = itemIds.toArray(new String[0]);

            // Diagnostics
            int nullOrEmpty = 0;
            for (String s : itemArr) if (s == null || s.isEmpty()) nullOrEmpty++;
            System.out.println("uploadInventory: sending " + itemArr.length + " items, null/empty ids=" + nullOrEmpty + ", username=" + username + ", nbtSize=" + (fullNbt == null ? 0 : fullNbt.length()));

            try {
                CoflCore.handlers.DescriptionHandler.loadDescriptionForInventory(itemArr, "Crafting", fullNbt, username);
            } catch (Throwable t) {
                System.err.println("Exception while calling DescriptionHandler.loadDescriptionForInventory from uploadInventory: username=" + username + " items=" + itemArr.length + " nullOrEmptyIds=" + nullOrEmpty + " nbtSize=" + (fullNbt == null ? 0 : fullNbt.length()));
                t.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean loadDescriptionForInventory(GuiOpenEvent event, GuiContainer gc, boolean skipLoadCheck) {
        InventoryWrapper wrapper = new InventoryWrapper();
        Position pos = null;
        if (event.gui != null && event.gui instanceof GuiChest) {
            if (!skipLoadCheck)
                waitForChestContentLoad(event, gc);

            ContainerChest chest = (ContainerChest) ((GuiChest) event.gui).inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            if (inv.hasCustomName()) {
                String chestName = inv.getName();
                wrapper.chestName = chestName;
                BlockPos chestPos = ChestUtils.getLookedAtChest();
                if (chestPos != null && chestName.endsWith("hest")) {
                    pos = new Position(chestPos.getX(), chestPos.getY(), chestPos.getZ());
                }
            }
        }

        allItemIds = getCurrentInventoryIds(gc);

        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList tl = new NBTTagList();

        for (Slot obj : gc.inventorySlots.inventorySlots) {
            ItemStack stack = obj.getStack();
            if (stack != null) {
                tl.appendTag(stack.serializeNBT());
            } else {
                tl.appendTag(EMPTY_COMPOUND);
            }
        }

        boolean shouldGetRefreshed = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            compound.setTag("i", tl);
            CompressedStreamTools.writeCompressed(compound, baos);

            wrapper.fullInventoryNbt = Base64.getEncoder().encodeToString(baos.toByteArray());

            List<ItemStack> stacks = new ArrayList<>();
            List<String> itemIds = new ArrayList<>();
            for (Slot obj : gc.inventorySlots.inventorySlots) {
                ItemStack stack = obj.getStack();
                stacks.add(stack);
                String id = ExtractIdFromItemStack(stack);
                itemIds.add(id);
            }

            System.out.println("Loading description for inventory: " + wrapper.chestName + " with " + itemIds.size() + " items");

            // Defensive: ensure we don't pass nulls into the external DescriptionHandler which may cause an NPE.
            String[] itemIdArr = itemIds.toArray(new String[0]);
            String chestName = wrapper.chestName; // may be null for player inventory
            String fullNbt = wrapper.fullInventoryNbt;
            String username = PlayerDataProvider.getUsername();

            if (itemIdArr == null) {
                System.err.println("Skipping loadDescriptionForInventory: itemIdArr is null");
            } else if (fullNbt == null) {
                System.err.println("Skipping loadDescriptionForInventory: fullInventoryNbt is null for chestName=" + chestName + " username=" + username + " items=" + itemIdArr.length);
            } else if (username == null || username.isEmpty()) {
                System.err.println("Skipping loadDescriptionForInventory: username is null/empty for chestName=" + chestName + " items=" + itemIdArr.length);
            } else {
                try {
                    CoflCore.handlers.DescriptionHandler.loadDescriptionForInventory(itemIdArr, chestName, fullNbt, username, pos);
                } catch (Throwable t) {
                    System.err.println("Exception while calling DescriptionHandler.loadDescriptionForInventory: chestName=" + chestName + " username=" + username + " items=" + itemIdArr.length + " pos=" + pos);
                    t.printStackTrace();
                }
            }

            /* TODO: migrate this
            for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);
                String id = ExtractIdFromItemStack(stack);
                if (id.length() > 0)
                    tooltipItemIdMap.put(id, arr[i]);

                if (stack == null)
                    continue;
                NBTTagList lore = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                for (int j = 0; j < lore.tagCount(); j++) {
                    String tag = lore.get(j).toString();
                    if (tag.contains("§7Refreshing...")) {
                        shouldGetRefreshed = true;
                    }
                }
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }

        return shouldGetRefreshed;
    }

    private static void waitForChestContentLoad(GuiOpenEvent event, GuiContainer gc) {
        for (int i = 1; i < 10; i++) {
            try {
                int idx = gc.inventorySlots.inventorySlots.size() - 37;
                if (idx >= 0 && idx < gc.inventorySlots.inventorySlots.size()) {
                    if (gc.inventorySlots.inventorySlots.get(idx).getStack() != null)
                        break;
                } else {
                    // Index not available yet; wait a bit
                }
                // incremental backoff to wait for all inventory packages to arrive
                // (each slot is sent individually)
                Thread.sleep(20 * i);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            } catch (Throwable t) {
                // Defensive: log unexpected errors but don't crash the thread
                System.err.println("Unexpected error while waiting for chest content: " + t);
                t.printStackTrace();
                break;
            }
        }
    }

    public void setTooltips(ItemTooltipEvent event) {
        String stackId = ExtractIdFromItemStack(event.itemStack);
        
        // Check if this UUID maps to an original UUID that has descriptions
        String lookupId = uuidToOriginalUuid.getOrDefault(stackId, stackId);
        
        CoflCore.handlers.DescriptionHandler.DescModification[] data = getTooltipData(event.itemStack);
        
        // Try to get descriptions using the original UUID if mapped
        if (data == null && !lookupId.equals(stackId)) {
            // Fallback: try to get descriptions for the original UUID
            ItemStack tempStack = event.itemStack.copy();
            data = CoflCore.handlers.DescriptionHandler.getTooltipData(lookupId);
        }

        if (data == null || data.length == 0){
            return;
        }

        for (CoflCore.handlers.DescriptionHandler.DescModification datum : data) {
            // Defensive checks: ensure datum.line is within bounds and non-negative
            if (datum == null) {
                continue;
            }
            int lineIdx = datum.line;
            if (lineIdx < 0 || lineIdx > event.toolTip.size()) {
                System.out.println("Skipped line modification (out of range) " + lineIdx + " for " + event.itemStack.getDisplayName() + " tooltipSize=" + event.toolTip.size() + " type=" + datum.type + " value=" + datum.value);
                continue;
            }
            switch (datum.type) {
                case "APPEND":
                    event.toolTip.add(datum.value);
                    break;
                case "REPLACE":
                    if (lineIdx < event.toolTip.size()) {
                        event.toolTip.set(lineIdx, datum.value);
                    } else {
                        System.out.println("REPLACE skipped, index >= tooltip size: " + lineIdx + " for " + event.itemStack.getDisplayName());
                    }
                    break;
                case "INSERT":
                    // Insert is allowed at index == size() (append) as well
                    if (lineIdx <= event.toolTip.size()) {
                        event.toolTip.add(lineIdx, datum.value);
                    } else {
                        System.out.println("INSERT skipped, index > tooltip size: " + lineIdx + " for " + event.itemStack.getDisplayName());
                    }
                    break;
                case "DELETE":
                    if (lineIdx >= 0 && lineIdx < event.toolTip.size()) {
                        event.toolTip.remove(lineIdx);
                    } else {
                        System.out.println("DELETE skipped, index out of range: " + lineIdx + " for " + event.itemStack.getDisplayName());
                    }
                    break;
            }
        }
    }

    public static MethodHandle xSizeField = ReflectionUtil.getField(GuiContainer.class, "xSize", "field_146999_f", "f");
    public static MethodHandle ySizeField = ReflectionUtil.getField(GuiContainer.class, "ySize", "field_147000_g", "g");

    public void highlightSlots(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!(event.gui instanceof GuiContainer)) {
            return;
        }
        GuiContainer containerGui = (GuiContainer) event.gui;
        for (Slot inventorySlot : containerGui.inventorySlots.inventorySlots) {
            if (!inventorySlot.getHasStack())
                continue;
            CoflCore.handlers.DescriptionHandler.DescModification[] tooltipData = getTooltipData(inventorySlot.getStack());
            if (tooltipData == null || tooltipData.length == 0) {
                continue;
            }
            for (CoflCore.handlers.DescriptionHandler.DescModification modification : tooltipData) {
                if ("HIGHLIGHT".equals(modification.type)) {
                    int color = (int) (Long.parseLong(modification.value, 16) & 0xFFFFFFFFL);
                    try {
                        int guiTop = (containerGui.height - (int) ySizeField.invokeExact(containerGui)) / 2;
                        int guiLeft = (containerGui.width - (int) xSizeField.invokeExact(containerGui)) / 2;
                        int slotX = inventorySlot.xDisplayPosition + guiLeft;
                        int slotY = inventorySlot.yDisplayPosition + guiTop;
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0, 0, 0.1);
                        Gui.drawRect(slotX, slotY, slotX + 16, slotY + 16,
                                modification.value.length() > 6 ? color : (color | 0xFF000000));
                        GlStateManager.popMatrix();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * Called when the inventory is closed
     */
    public static void emptyTooltipData() {
        //TODO: clear tooltip data
    }
}
