package de.torui.coflsky.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.input.Mouse;


import java.awt.*;
import java.util.List;
import java.util.Locale;

public class OpenGuiRender extends GuiScreen {

    float alpha = (float) 0.3;
    boolean lastClick = false;
    boolean renderTooltips = false;


    @SubscribeEvent
    public void onGuiOpen (GuiScreenEvent.DrawScreenEvent.Post event) {

        if (!(event.gui instanceof GuiChest)) {
            return;
        }

        final ContainerChest containerChest = (ContainerChest) ((GuiChest) event.gui).inventorySlots;

        final String s = containerChest.getLowerChestInventory().getName();

        if (s.toLowerCase(Locale.ROOT).contains("bin auction view")) {

            if (Mouse.isButtonDown(0)) {
                alpha = (float) 0.7;
            } else if (Mouse.isButtonDown(1)) {
                Minecraft.getMinecraft().thePlayer.closeScreen();
            } else if (!Mouse.isButtonDown(0)) {
                lastClick = false;
                alpha = 0.3f;
            }
            if (((GuiChest) event.gui).inventorySlots.getSlot(13).getHasStack()) {
                renderToolTip(((GuiChest) event.gui).inventorySlots.getSlot(13).getStack(), event.mouseX, event.mouseY);
            }

            drawRect(event.mouseX - 3, event.mouseY + 7, event.mouseX + 7, event.mouseY - 3, Color.YELLOW.getRGB(), alpha);
            if (Mouse.isButtonDown(0) && !lastClick) {
                lastClick = true;
                Minecraft.getMinecraft().playerController.windowClick(containerChest.windowId, 31, 0, 3, (EntityPlayer) Minecraft.getMinecraft().thePlayer);
            }

        }
        if (s.contains("Confirm Purchase")) {
            if (Mouse.isButtonDown(0)) {
                alpha = (float) 0.7;
            } else if (Mouse.isButtonDown(1)) {
                Minecraft.getMinecraft().thePlayer.closeScreen();
            } else if (!Mouse.isButtonDown(0)) {
                lastClick = false;
                alpha = 0.3f;
            }

            drawRect(event.mouseX - 3, event.mouseY + 7, event.mouseX + 7, event.mouseY - 3, Color.GREEN.getRGB(), alpha);
            if (Mouse.isButtonDown(0) && !lastClick) {
                lastClick = true;
                Minecraft.getMinecraft().playerController.windowClick(containerChest.windowId, 11, 0, 3, (EntityPlayer) Minecraft.getMinecraft().thePlayer);
            }

        }
    }


    protected void renderToolTip (ItemStack stack, int x, int y) {
        List<String> list = stack.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);

        for (int i = 0; i < list.size(); ++i) {
            if (i == 0) {
                list.set(i, stack.getRarity().rarityColor + (String) list.get(i));
            } else {
                list.set(i, EnumChatFormatting.GRAY + (String) list.get(i));
            }
        }

        FontRenderer font = stack.getItem().getFontRenderer(stack);
        GuiUtils.drawHoveringText(list, x, y, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, 500, font == null ? Minecraft.getMinecraft().fontRendererObj : font);
    }


    public static void drawRect (int left, int top, int right, int bottom, int color, float alpha) {
        int i;
        if (left < right) {
            i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            i = top;
            top = bottom;
            bottom = i;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float j = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(g, h, j, alpha);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos((double) left, (double) bottom, 0.0).endVertex();
        worldRenderer.pos((double) right, (double) bottom, 0.0).endVertex();
        worldRenderer.pos((double) right, (double) top, 0.0).endVertex();
        worldRenderer.pos((double) left, (double) top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}

