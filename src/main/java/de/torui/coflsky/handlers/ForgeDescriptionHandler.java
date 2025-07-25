package de.torui.coflsky.handlers;


import CoflCore.classes.Position;
import de.torui.coflsky.minecraft_integration.PlayerDataProvider;
import de.torui.coflsky.utils.ReflectionUtil;
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
        try
        {
        compound.setTag("i", tl);
        CompressedStreamTools.writeCompressed(compound, baos);

        CoflCore.handlers.DescriptionHandler.loadDescriptionForInventory(itemIds.toArray(new String[0]), "Crafting", Base64.getEncoder().encodeToString(baos.toByteArray()), PlayerDataProvider.getUsername());

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
            CoflCore.handlers.DescriptionHandler.loadDescriptionForInventory(itemIds.toArray(new String[0]), wrapper.chestName, wrapper.fullInventoryNbt, PlayerDataProvider.getUsername(), pos);

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
            if (gc.inventorySlots.inventorySlots.get(gc.inventorySlots.inventorySlots.size() - 37).getStack() != null)
                break;
            try {
                // incremental backoff to wait for all inventory packages to arrive
                // (each slot is sent individually)
                Thread.sleep(20 * i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTooltips(ItemTooltipEvent event) {
        CoflCore.handlers.DescriptionHandler.DescModification[] data = getTooltipData(event.itemStack);

        if (data == null || data.length == 0){
            return;
        }

        for (CoflCore.handlers.DescriptionHandler.DescModification datum : data) {
            if (event.toolTip.size() <= datum.line) {
                System.out.println(
                        "Skipped line modification " + datum.line + " for " + event.itemStack.getDisplayName());
                continue;
            }
            switch (datum.type) {
                case "APPEND":
                    event.toolTip.add(datum.value);
                    break;
                case "REPLACE":
                    event.toolTip.set(datum.line, datum.value);
                    break;
                case "INSERT":
                    event.toolTip.add(datum.line, datum.value);
                    break;
                case "DELETE":
                    event.toolTip.remove(datum.line);
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
