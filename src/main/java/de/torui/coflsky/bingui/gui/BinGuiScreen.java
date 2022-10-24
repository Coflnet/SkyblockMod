package de.torui.coflsky.bingui.gui;

import de.torui.coflsky.bingui.helper.ColorPallet;
import de.torui.coflsky.bingui.helper.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.io.IOException;

public class BinGuiScreen extends GuiScreen {

    private boolean isRMouseDown = false;
    private boolean isLMouseDown = false;

    private static Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void initGui() {
        super.initGui();
        System.out.println("init");
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        exRender(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 2){
            isLMouseDown = true;
        }else if (mouseButton == 3){
            isRMouseDown = true;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        isLMouseDown = true;
        isRMouseDown = true;
    }

    public void exRender(int mouseX, int mouseY) {
        float cornerRighBottomX = this.width;
        float cornerRighBottomY = this.height;

        //for testing i draw the color pallet at the top of the screen using rectangles
        RenderUtils.drawRect(0, 0, 20, 20, ColorPallet.PRIMARY.getColor().getRGB());
        RenderUtils.drawRect(20, 0, 20, 20, ColorPallet.SECONDARY.getColor().getRGB());
        RenderUtils.drawRect(40, 0, 20, 20, ColorPallet.TERTIARY.getColor().getRGB());
        RenderUtils.drawRect(60, 0, 20, 20, ColorPallet.WHITE.getColor().getRGB());
        RenderUtils.drawRect(80, 0, 20, 20, ColorPallet.ERROR.getColor().getRGB());
        RenderUtils.drawRect(100, 0, 20, 20, ColorPallet.SUCCESS.getColor().getRGB());
        RenderUtils.drawRect(120, 0, 20, 20, ColorPallet.WARNING.getColor().getRGB());
        RenderUtils.drawRect(140, 0, 20, 20, ColorPallet.INFO.getColor().getRGB());

        //now i draw a cancel button at the right bottom corner
        RenderUtils.drawRect(cornerRighBottomX - 40, cornerRighBottomY - 40, 40, 40, ColorPallet.ERROR.getColor().getRGB());
        //draw a x in the rectangle using lines
        RenderUtils.drawLine(cornerRighBottomX - 39.5f, cornerRighBottomY - 39.5f, cornerRighBottomX, cornerRighBottomY, 4, ColorPallet.WHITE.getColor());
        RenderUtils.drawLine(cornerRighBottomX - 39.5f, cornerRighBottomY, cornerRighBottomX, cornerRighBottomY - 39.5f, 4, ColorPallet.WHITE.getColor());
        //if mouse button is hovered over the rectangle, draw a darker rectangle
        if (isMouseOverClose(mouseX, mouseY)) {
            //error color but with less alpha
            Color errorAlpha = new Color(ColorPallet.ERROR.getColor().getRed(),ColorPallet.ERROR.getColor().getGreen(),ColorPallet.ERROR.getColor().getBlue(),140);
            RenderUtils.drawRect(cornerRighBottomX - 40, cornerRighBottomY - 40, 40, 40, errorAlpha.getRGB());
            //check if mouse button is pressed
            if (isLMouseDown) {
                //close the gui
                mc.displayGuiScreen(null);
            }
        }

        //draw a title at the top of the screen
        RenderUtils.drawCenteredString("Item Name", this.width / 2, 10, ColorPallet.INFO.getColor());

        //Draw

    }

    public boolean isMouseOverClose(int mouseX, int mouseY) {
        float cornerRighBottomX = this.width;
        float cornerRighBottomY = this.height;
        return mouseX >= cornerRighBottomX - 40 && mouseX <= cornerRighBottomX && mouseY >= cornerRighBottomY - 40 && mouseY <= cornerRighBottomY;
    }
}
