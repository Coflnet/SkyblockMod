package de.torui.coflsky.mixins;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(GuiScreen.class)
public interface IMixinGuiScreen {
    @Accessor("buttonList")
    List<GuiButton> getButtonList();

    @Accessor("fontRendererObj")
    FontRenderer getFontRendererObj();

    @Invoker("renderToolTip")
    void renderToolTip(ItemStack stack, int x, int y);
}