package de.torui.coflsky.commands.models;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import scala.collection.parallel.ParIterableLike.Min;

import java.awt.*;
import java.util.Random;

public class LoadingGui extends GuiScreen {
    float alpha = (float) 0.3;
    boolean lastClick = false;
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Mouse.isButtonDown(0)) {
                alpha = (float) 0.7;
            } else if (Mouse.isButtonDown(1)) {
                Minecraft.getMinecraft().thePlayer.closeScreen();
            } else if (!Mouse.isButtonDown(0)) {
                lastClick = false;
                alpha = 0.3f;
            }


            drawRect(mouseX - 3, mouseY + 7, mouseX + 7, mouseY - 3, Color.PINK.getRGB(), alpha);
            if (Mouse.isButtonDown(0) && !lastClick) {
                lastClick = true;
                Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(new C0EPacketClickWindow(5, 31, 1, 2, new ItemStack(Items.gold_nugget), (short) new Random().nextInt(Short.MAX_VALUE)));
            }

        this.drawString(Minecraft.getMinecraft().fontRendererObj, "BIN Auction View Loading", Minecraft.getMinecraft().currentScreen.width/2 - (fontRendererObj.getStringWidth("BIN Auction View Loading")/2), Minecraft.getMinecraft().currentScreen.height/2- (fontRendererObj.getStringWidth("BIN Auction View Loading")/2), Color.GREEN.getRGB());
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
