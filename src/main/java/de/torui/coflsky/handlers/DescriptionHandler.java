package de.torui.coflsky.handlers;

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
import java.util.Base64;
import java.util.HashMap;

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

    public static HashMap<ItemStack, DescModification[]> tooltipItemMap = new HashMap<>();
    public static HashMap<String, DescModification[]> tooltipItemUuidMap = new HashMap<>();
    public static HashMap<String, DescModification[]> tooltipItemIdMap = new HashMap<>();

    public static final DescModification[] EMPTY_ARRAY = new DescModification[0];
    public static final NBTTagCompound EMPTY_COMPOUND = new NBTTagCompound();

    public static String ExtractStackableIdFromItemStack(ItemStack stack) {
        if (stack != null) {
            try {
                String uuid = stack.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes")
                        .getString("id") + ":" + stack.stackSize;
                if (uuid.length() == 0) {
                    throw new Exception();
                }
                return uuid;
            } catch (Exception e) {
            }
        }
        return "";
    }
    public static String ExtractUuidFromItemStack(ItemStack stack) {
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
        return "";
    }
    private static DescModification[] getTooltipData(ItemStack itemStack) {
        if (tooltipItemMap.containsKey(itemStack)) {
            return tooltipItemMap.getOrDefault(itemStack, EMPTY_ARRAY);
        }
        if(!itemStack.isStackable()){
            String id = ExtractUuidFromItemStack(itemStack);
            if (tooltipItemUuidMap.containsKey(id)) {
                return tooltipItemUuidMap.getOrDefault(id, EMPTY_ARRAY);
            }
        } else {
            String itemId = ExtractStackableIdFromItemStack(itemStack);
            if(tooltipItemIdMap.containsKey(itemId)){
                return tooltipItemIdMap.getOrDefault(itemId, EMPTY_ARRAY);
            }
        }

        return EMPTY_ARRAY;
    }
    public static void getTooltipDataFromBackend(GuiOpenEvent event){
        try {
            // delay a bit to wait for all inventory packages to arrive (each slot is sent individually)
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        InventoryWrapper wrapper = new InventoryWrapper();

        GuiContainer gc = (GuiContainer) event.gui;

        if (event.gui instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) ((GuiChest) event.gui).inventorySlots;

            IInventory inv = chest.getLowerChestInventory();
            if (inv.hasCustomName()) {
                String chestName = inv.getName();
                wrapper.chestName = chestName;
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

            String data = WSClient.gson.toJson(wrapper);
            String info = QueryServerCommands.PostRequest("https://sky.coflnet.com/api/mod/description/modifications", data);

            DescModification[][] arr = WSClient.gson.fromJson(info, DescModification[][].class);
            int i = 0;
            for (Slot obj : gc.inventorySlots.inventorySlots) {
                ItemStack stack = obj.getStack();
                tooltipItemMap.put(stack, arr[i]);
                String uuid = ExtractUuidFromItemStack(stack);
                if(uuid.length()>0) tooltipItemUuidMap.put(uuid, arr[i]);

                String id = ExtractStackableIdFromItemStack(stack);
                if(id.length()>0) tooltipItemIdMap.put(id, arr[i]);
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void setTooltips(ItemTooltipEvent event) {
        DescModification[] data = getTooltipData(event.itemStack);

        if (data == null || data.length == 0)
            return;

        for (DescModification datum : data) {
            if (!(event.toolTip.size() >= datum.line)) return;
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

    public static void emptyTooltipData(){
        tooltipItemMap.clear();
        tooltipItemIdMap.clear();
        tooltipItemUuidMap.clear();
    }
}
