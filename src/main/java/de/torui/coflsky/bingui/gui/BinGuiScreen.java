package de.torui.coflsky.bingui.gui;

import de.torui.coflsky.bingui.helper.ColorPallet;
import de.torui.coflsky.bingui.helper.RenderUtils;
import de.torui.coflsky.bingui.helper.inputhandler.InputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BinGuiScreen extends GuiScreen {

    private static Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void initGui() {
        super.initGui();
        System.out.println("init");
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        renderMainGui(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void renderMainGui(int mouseX, int mouseY) {
        //handles input
        InputHandler inputHandler = new InputHandler();

        //the item I use for testing
        ItemStack itemStack = new ItemStack(Items.item_frame);
        itemStack.setStackDisplayName("TestItem");

        //just to clear things up for myself
        float cornerRighBottomX = this.width;
        float cornerRighBottomY = this.height;

        //drawColorPallet();

        //now I draw a cancel button at the right bottom corner
        RenderUtils.drawRect(cornerRighBottomX - 40, cornerRighBottomY - 40, 40, 40, ColorPallet.ERROR.getColor().getRGB());

        //draw an x in the rectangle using lines
        RenderUtils.drawLine(cornerRighBottomX - 35, cornerRighBottomY - 35, cornerRighBottomX - 5, cornerRighBottomY - 5, 4, ColorPallet.WHITE.getColor());
        RenderUtils.drawLine(cornerRighBottomX - 35, cornerRighBottomY - 5, cornerRighBottomX - 5, cornerRighBottomY - 35, 4, ColorPallet.WHITE.getColor());

        //if mouse button is hovered over the rectangle, draw a darker rectangle
        if (isMouseOverClose(mouseX, mouseY)) {
            //error color but with less alpha
            Color errorAlpha = new Color(ColorPallet.ERROR.getColor().getRed(), ColorPallet.ERROR.getColor().getGreen(), ColorPallet.ERROR.getColor().getBlue(), 140);
            RenderUtils.drawRect(cornerRighBottomX - 40, cornerRighBottomY - 40, 40, 40, errorAlpha.getRGB());

            //check if mouse button is pressed
            if (inputHandler.isMouseDown(0)) {

                //close the gui
                mc.displayGuiScreen(null);
            }
        }

        //Draw tooltip in the top right corner
        String[] tooltip = {
                "Very long tooltip",
                "Very long tooltip",
                "Very long tooltipVery long tooltip",
                "Very long tooltip",
                "Very long tooltip",
                "Very long tooltip",
                "Very long tooltip",
                "Very long tooltip",
                "Very long tooltip",
                "§2Very long tooltip"
        };
        drawToolTip(this.width, 0, tooltip);

        //draw a title at the top of the screen
        RenderUtils.drawCenteredString("Item Name", this.width / 2, 5, ColorPallet.WHITE.getColor());

        //Draw the item icon directly under the title
        RenderUtils.drawItemStack(itemStack, this.width / 2 - 8, 15, 1);

        IChatComponent testText = new ChatComponentText("This §2could §lbe§r the flip message! Every 50 chars, the message will be split into more lines.");

        //draw the flip message in the top left corner
        drawFlipMessage(testText, 0, 0);

        //draw a warning that item cannot be clicked
        RenderUtils.drawCenteredString("§cItems cannot be clicked! If you click twice you buy the item!", this.width / 2, this.height - mc.fontRendererObj.FONT_HEIGHT - 22, ColorPallet.WHITE.getColor());

        //now I draw a big transparent green button in the middle of the screen that if clicked twice, buys the item
        Color successAlpha = new Color(ColorPallet.SUCCESS.getColor().getRed(), ColorPallet.SUCCESS.getColor().getGreen(), ColorPallet.SUCCESS.getColor().getBlue(), 140);
        RenderUtils.drawRect(this.width / 2 - 100, this.height / 2 - 20, 200, 40, successAlpha.getRGB());

        //draw a text in the button
        RenderUtils.drawCenteredString("Buy Item", this.width / 2, this.height / 2 - 10, ColorPallet.SUCCESS.getColor());

        //if mouse button is hovered over the rectangle, draw a darker rectangle
        if (isMouseOverBuy(mouseX, mouseY)) {

            //draws the rectangle again, but now it is darker because of the alpha
            RenderUtils.drawRect(this.width / 2 - 100, this.height / 2 - 20, 200, 40, successAlpha.getRGB());

            //check if you double clicked
            if (inputHandler.isClicked()) {
                //buy the item
                if (inputHandler.isClicked()) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§aYou bought the item!"));
                }
            }
        }
    }

    //Draw tooltip
    public void drawToolTip(float x, float y, String[] text) {
        //get the width of the longest line
        int width = 0;
        int height = 0;
        for (String s : text) {
            if (mc.fontRendererObj.getStringWidth(s) > width) {
                width = mc.fontRendererObj.getStringWidth(s);
            }
            height += 10;
        }

        //draw the background
        RenderUtils.drawRect(x - width, y, width, height, ColorPallet.SECONDARY.getColor().getRGB());

        //draw the text
        for (int i = 0; i < text.length; i++) {
            RenderUtils.drawString(text[i], (int) x - width, (int) (y + (i * 10)), ColorPallet.WHITE.getColor());
        }
    }

    public void drawFlipMessage(IChatComponent message, int x, int y) {
        float height = mc.fontRendererObj.FONT_HEIGHT;
        //every 50 chars, the message will be split into more lines
        List<String> lines = new ArrayList<>();
        String formattedMessage = message.getFormattedText();
        String unFormattedMessage = message.getUnformattedText();

        //split the message into lines
        for (int i = 0; i < formattedMessage.length(); i += 50) {
            lines.add(formattedMessage.substring(i, Math.min(formattedMessage.length(), i + 50)));
        }

        //loop through the lines
        for (int i = 0; i < lines.size(); i++) {
            //draw the background
            if (i == 0) {
                RenderUtils.drawRect(x, y, mc.fontRendererObj.getStringWidth(lines.get(i)), height * lines.size(), ColorPallet.SECONDARY.getColor().getRGB());
            } else {
                RenderUtils.drawRect(x, y + height, mc.fontRendererObj.getStringWidth(lines.get(i)), height * lines.size(), ColorPallet.SECONDARY.getColor().getRGB());
            }
            //draw the text
            RenderUtils.drawString(lines.get(i), x, (int) (y + (i * height)), ColorPallet.WHITE.getColor());
        }
    }

    public void drawColorPallet() {
        //for testing, I draw the color pallet at the top of the screen using rectangles
        RenderUtils.drawRect(0, 0, 20, 20, ColorPallet.PRIMARY.getColor().getRGB());
        RenderUtils.drawRect(20, 0, 20, 20, ColorPallet.SECONDARY.getColor().getRGB());
        RenderUtils.drawRect(40, 0, 20, 20, ColorPallet.TERTIARY.getColor().getRGB());
        RenderUtils.drawRect(60, 0, 20, 20, ColorPallet.WHITE.getColor().getRGB());
        RenderUtils.drawRect(80, 0, 20, 20, ColorPallet.ERROR.getColor().getRGB());
        RenderUtils.drawRect(100, 0, 20, 20, ColorPallet.SUCCESS.getColor().getRGB());
        RenderUtils.drawRect(120, 0, 20, 20, ColorPallet.WARNING.getColor().getRGB());
        RenderUtils.drawRect(140, 0, 20, 20, ColorPallet.INFO.getColor().getRGB());
    }

    public boolean isMouseOverClose(int mouseX, int mouseY) {
        float cornerRighBottomX = this.width;
        float cornerRighBottomY = this.height;
        return mouseX >= cornerRighBottomX - 40 && mouseX <= cornerRighBottomX && mouseY >= cornerRighBottomY - 40 && mouseY <= cornerRighBottomY;
    }

    public boolean isMouseOverBuy(int mouseX, int mouseY) {
        return mouseX >= this.width / 2 - 100 && mouseX <= this.width / 2 + 100 && mouseY >= this.height / 2 - 20 && mouseY <= this.height / 2 + 20;
    }

}
