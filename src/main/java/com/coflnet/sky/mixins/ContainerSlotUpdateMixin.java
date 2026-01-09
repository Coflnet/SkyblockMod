package com.coflnet.sky.mixins;

import com.coflnet.sky.handlers.ForgeDescriptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tracks UUID changes when items update with new UUIDs but same title
 */
@Mixin(Container.class)
public class ContainerSlotUpdateMixin {

    @Inject(method = "putStackInSlot", at = @At("HEAD"))
    private void trackUuidChanges(int slotIndex, ItemStack stack, CallbackInfo ci) {
        // Track UUID changes before the slot is updated
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null || mc.thePlayer.openContainer == null) {
                return;
            }
            
            Container container = mc.thePlayer.openContainer;
            if (slotIndex < 0 || slotIndex >= container.inventorySlots.size()) {
                return;
            }
            
            Slot slot = (Slot) container.inventorySlots.get(slotIndex);
            ItemStack previousStack = slot.getStack();
            ItemStack newStack = stack;
            
            if (previousStack == null || newStack == null) {
                return;
            }
            
            String prevTitle = previousStack.getDisplayName() != null ? previousStack.getDisplayName() : "";
            String newTitle = newStack.getDisplayName() != null ? newStack.getDisplayName() : "";
            
            // If item title is the same but UUIDs differ, map new UUID to original
            if (!prevTitle.isEmpty() && prevTitle.equals(newTitle)) {
                String prevUuid = extractUuid(previousStack);
                String newUuid = extractUuid(newStack);
                
                if (prevUuid != null && newUuid != null && !prevUuid.equals(newUuid)) {
                    // Find the original UUID (follow chain if exists)
                    String originalUuid = ForgeDescriptionHandler.uuidToOriginalUuid.getOrDefault(prevUuid, prevUuid);
                    ForgeDescriptionHandler.uuidToOriginalUuid.put(newUuid, originalUuid);
                }
            }
        } catch (Exception e) {
            // Silently ignore errors in UUID tracking
        }
    }
    
    private String extractUuid(ItemStack stack) {
        try {
            NBTTagCompound compound = stack.serializeNBT();
            NBTTagCompound tag = compound.getCompoundTag("tag");
            if (tag != null) {
                NBTTagCompound extraAttributes = tag.getCompoundTag("ExtraAttributes");
                if (extraAttributes != null) {
                    String uuid = extraAttributes.getString("uuid");
                    if (uuid != null && !uuid.isEmpty()) {
                        return uuid;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore parse errors
        }
        return null;
    }
}

