package com.coflnet.sky.mixins;

import com.coflnet.sky.handlers.EventRegistry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Needed to get hotkeys in inventory guis
 */
@Mixin(GuiContainer.class)
public class GuiScreenKeyForwardMixin {

    @Inject(method = "keyTyped", at = @At("HEAD"))
    private void onHandleKeyboardInput(CallbackInfo ci) {
        try {
            // If there's a key event and it's a key down event, directly call our handler
            if (Keyboard.getEventKey() != 0 && Keyboard.getEventKeyState()) {
                EventRegistry.triggerItemKeys(true);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
