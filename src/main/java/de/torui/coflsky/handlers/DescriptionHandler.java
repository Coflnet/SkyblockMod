package de.torui.coflsky.handlers;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.Config;
import de.torui.coflsky.network.QueryServerCommands;
import de.torui.coflsky.network.WSClient;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class DescriptionHandler {

    private static class InventoryWrapper {
        public String chestName;
        public String fullInventoryNbt;
    }

    private static class DescModification {
        public String type;
        public String value;
        public int line;
    }

    public static final HashMap<ItemStack, DescModification[]> tooltipItemMap = new HashMap<>();
    public static final HashMap<String, DescModification[]> tooltipItemIdMap = new HashMap<>();

    public static final DescModification[] EMPTY_ARRAY = new DescModification[0];
    public static final NBTTagCompound EMPTY_COMPOUND = new NBTTagCompound();

    private boolean IsOpen = true;
    private boolean shouldUpdate = false;

    public void Close() {
        IsOpen = false;
    }

    public static String ExtractStackableIdFromItemStack(ItemStack stack) {
        if (stack != null) {
            try {
                String uuid = stack.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes")
                        .getString("id") + ":" + stack.stackSize;
                if (uuid.length() == 0) {
                    throw new Exception();
                }
                return uuid;
            } catch (Exception ignored) {
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
            } catch (Exception ignored) {
            }
        }
        return ExtractStackableIdFromItemStack(stack);
    }

    private DescModification[] getTooltipData(ItemStack itemStack) {
        if (tooltipItemMap.containsKey(itemStack)) {
            return tooltipItemMap.getOrDefault(itemStack, EMPTY_ARRAY);
        }
        String id = ExtractIdFromItemStack(itemStack);
        if (tooltipItemIdMap.containsKey(id)) {
            return tooltipItemIdMap.getOrDefault(id, EMPTY_ARRAY);
        }
        shouldUpdate = true;

        return EMPTY_ARRAY;
    }

    public void loadDescriptionAndListenForChanges(GuiOpenEvent event) {

        GuiContainer gc = (GuiContainer) event.gui;

        loadDescriptionForInventory(event, gc, false);
        int iteration = 0;
        while (IsOpen) {
            iteration++;
            try {
                Thread.sleep(300 + iteration);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (shouldUpdate || iteration % 10 == 0 && hasAnyStackChanged(gc)) {
                shouldUpdate = false;
                loadDescriptionForInventory(event, gc, true);
            }
            iteration = Math.min(30, iteration); // cap at 9 second update interval
        }
    }

    private static boolean hasAnyStackChanged(GuiContainer gc) {
        for (Slot obj : gc.inventorySlots.inventorySlots) {
            ItemStack stack = obj.getStack();
            if (stack != null && !tooltipItemMap.containsKey(stack)) {
                return true;
            }
        }
        return false;
    }

    private static void loadDescriptionForInventory(GuiOpenEvent event, GuiContainer gc, boolean skipLoadCheck) {
        InventoryWrapper wrapper = new InventoryWrapper();
        if (event.gui instanceof GuiChest) {
            if (!skipLoadCheck)
                waitForChestContentLoad(gc);

            ContainerChest chest = (ContainerChest) ((GuiChest) event.gui).inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            if (inv.hasCustomName()) {
                wrapper.chestName = inv.getName();
            }
        }

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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            compound.setTag("i", tl);
            CompressedStreamTools.writeCompressed(compound, baos);

            wrapper.fullInventoryNbt = Base64.getEncoder().encodeToString(baos.toByteArray());

            List<ItemStack> stacks = new ArrayList<>();
            for (Slot obj : gc.inventorySlots.inventorySlots) {
                stacks.add(obj.getStack());
            }

            String data = WSClient.gson.toJson(wrapper);
            String info = QueryServerCommands.PostRequest(Config.BASE_URL + "/api/mod/description/modifications", data);

            DescModification[][] arr = WSClient.gson.fromJson(info, DescModification[][].class);
            int i = 0;
            for (ItemStack stack : stacks) {
                tooltipItemMap.put(stack, arr[i]);
                String id = ExtractIdFromItemStack(stack);
                if (id.length() > 0)
                    tooltipItemIdMap.put(id, arr[i]);
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void waitForChestContentLoad(GuiContainer gc) {
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
        DescModification[] data = getTooltipData(event.itemStack);

        if (data == null || data.length == 0)
            return;

        for (DescModification datum : data) {
            if (event.toolTip.size() <= datum.line) {
                CoflSky.logger.debug("Skipped line modification " + datum.line + " for " + event.itemStack.getDisplayName());
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

    /**
     * Called when the inventory is closed
     */
    public static void emptyTooltipData() {
        tooltipItemMap.clear();
        tooltipItemIdMap.clear();
    }
}
