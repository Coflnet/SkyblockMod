package de.torui.coflsky.gui.tfm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class ToolTipHelper {
    Minecraft mc;
    ItemStack stack;
    List<String> toolTipLines;
    FontRenderer font;

    public ToolTipHelper(ItemStack stack) {
        mc = Minecraft.getMinecraft();
        this.stack = stack;
        toolTipLines = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
        for (int i = 0; i < toolTipLines.size(); ++i) {
            if (i == 0) {
                toolTipLines.set(i, stack.getRarity().rarityColor + toolTipLines.get(i));
            } else {
                toolTipLines.set(i, EnumChatFormatting.GRAY + toolTipLines.get(i));
            }
        }

        font = stack.getItem().getFontRenderer(stack);
        if (font == null) {
            font = mc.fontRendererObj;
        }
    }

    public int determineWidth() {
        int width = 0;

        for (String textLine : toolTipLines) {
            int textWidth = font.getStringWidth(textLine);
            if (textWidth > width) {
                width = textWidth;
            }
        }

        return width;
    }

    public int determineHeight() {
        int height = 8;
        if (toolTipLines.size() > 1) {
            height += (toolTipLines.size() - 1) * 10;
        }
        return height;
    }

}
