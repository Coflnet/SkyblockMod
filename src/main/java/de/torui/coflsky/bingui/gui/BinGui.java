package de.torui.coflsky.bingui.gui;

import de.torui.coflsky.bingui.helper.ColorPallet;
import de.torui.coflsky.bingui.helper.RenderUtils;
import de.torui.coflsky.bingui.helper.inputhandler.InputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BinGui {
    private static Minecraft mc = Minecraft.getMinecraft();
    private static boolean shouldRenderOverlay = false;
    private static boolean shouldRenderBuyOverlay = false;
    private static InputHandler inputHandler = new InputHandler();

    private IChatComponent message;
    private String[] lore;

    public BinGui(IChatComponent message, String[] lore) {
        this.message = message;
        this.lore = lore;
        if (message == null || lore == null) return;
    }

    public void open(String auctionId) {
        //null check
        if (message == null || lore == null || mc.thePlayer == null || mc.theWorld == null) return;
        MinecraftForge.EVENT_BUS.register(this);
        mc.thePlayer.sendChatMessage("/viewauction " + auctionId);
        shouldRenderOverlay = true;
    }

    @SubscribeEvent
    public void onDrawGuiScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        //first i get myself the gui
        GuiScreen gui = event.gui;

        //then i check if it is a chest gui
        if (!(gui instanceof GuiChest)) return;
        GuiChest chest = (GuiChest) gui;

        //then i get the private field named lowerChestInventory
        IInventory inventory = null;
        try {
            Field field = chest.getClass().getDeclaredField("lowerChestInventory");
            field.setAccessible(true);
            inventory = (IInventory) field.get(chest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //then a little null check
        if (inventory == null) return;

        if (inventory.getDisplayName().getFormattedText().contains("BIN Auction") && shouldRenderOverlay) {
            //now i render my overlay
            renderMainGui(event.mouseX, event.mouseY, gui.width, gui.height);
        } else if (inventory.getDisplayName().getFormattedText().contains("confirm") && shouldRenderBuyOverlay) {
            //buy overlay
            renderBuyOverlay(event.mouseX, event.mouseY, gui.width, gui.height);
        }

    }

    @SubscribeEvent
    public void onKeyEvent(GuiScreenEvent.KeyboardInputEvent event){
        //check if it should render the gui
        if (shouldRenderOverlay || shouldRenderBuyOverlay) {
            //check if esc was pressed
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                //close the gui
                shouldRenderOverlay = false;
                shouldRenderBuyOverlay = false;
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    public void renderBuyOverlay(int mouseX, int mouseY, int width, int height) {
        Color successAlpha = new Color(ColorPallet.SUCCESS.getColor().getRed(), ColorPallet.SUCCESS.getColor().getGreen(), ColorPallet.SUCCESS.getColor().getBlue(), 140);
        RenderUtils.drawRect(width / 2 - 100, height / 2 - 20, 200, 40, successAlpha.getRGB());

        //if mouse button is hovered over the rectangle, draw a darker rectangle
        if (isMouseOverBuy(mouseX, mouseY, width, height)) {

            //draws the rectangle again, but now it is darker because of the alpha
            RenderUtils.drawRect(width / 2 - 100, height / 2 - 20, 200, 40, successAlpha.getRGB());

            //check if you double clicked
            if (inputHandler.isClicked()) {

                //buy the item
                if (inputHandler.isClicked()) {

                    //click at slot 31
                    mc.playerController.windowClick(0, 11, 0, 0, mc.thePlayer);
                    shouldRenderOverlay = false;
                    shouldRenderBuyOverlay = false;
                }
            }
        }
    }

    public void renderMainGui(int mouseX, int mouseY, int width, int height) {
        //the item I use for testing
        ItemStack itemStack = new ItemStack(Items.item_frame);
        itemStack.setStackDisplayName("Item Name");

        //just to clear things up for myself

        //now I draw a cancel button at the right bottom corner
        RenderUtils.drawRect((float) width - 40, (float) height - 40, 40, 40, ColorPallet.ERROR.getColor().getRGB());

        //draw an x in the rectangle using lines
        RenderUtils.drawLine((float) width - 35, (float) height - 35, (float) width - 5, (float) height - 5, 4, ColorPallet.WHITE.getColor());
        RenderUtils.drawLine((float) width - 35, (float) height - 5, (float) width - 5, (float) height - 35, 4, ColorPallet.WHITE.getColor());

        //if mouse button is hovered over the rectangle, draw a darker rectangle
        if (isMouseOverClose(mouseX, mouseY, width, height)) {
            //error color but with less alpha
            Color errorAlpha = new Color(ColorPallet.ERROR.getColor().getRed(), ColorPallet.ERROR.getColor().getGreen(), ColorPallet.ERROR.getColor().getBlue(), 140);
            RenderUtils.drawRect((float) width - 40, (float) height - 40, 40, 40, errorAlpha.getRGB());

            //check if mouse button is pressed
            if (inputHandler.isMouseDown(0)) {

                shouldRenderOverlay = false;
                shouldRenderBuyOverlay = false;
                //close the gui
                mc.displayGuiScreen(null);
            }
        }

        drawToolTip(width, 0, lore);

        //draw a title at the top of the screen
        RenderUtils.drawCenteredString("Item Name", width / 2, 5, ColorPallet.WHITE.getColor());

        //Draw the item icon directly under the title
        RenderUtils.drawItemStack(itemStack, width / 2 - 8, 15, 1);

        //draw the flip message in the top left corner
        drawFlipMessage(message, 0, 0);

        //now I draw a big transparent green button in the middle of the screen that if clicked twice, buys the item
        Color successAlpha = new Color(ColorPallet.SUCCESS.getColor().getRed(), ColorPallet.SUCCESS.getColor().getGreen(), ColorPallet.SUCCESS.getColor().getBlue(), 140);
        RenderUtils.drawRect(width / 2 - 100, height / 2 - 20, 200, 40, successAlpha.getRGB());

        //if mouse button is hovered over the rectangle, draw a darker rectangle
        if (isMouseOverBuy(mouseX, mouseY, width, height)) {

            //draws the rectangle again, but now it is darker because of the alpha
            RenderUtils.drawRect(width / 2 - 100, height / 2 - 20, 200, 40, successAlpha.getRGB());

            //check if you double clicked
            if (inputHandler.isClicked()) {

                //buy the item
                if (inputHandler.isClicked()) {

                    //click at slot 31
                    mc.playerController.windowClick(0, 31, 0, 0, mc.thePlayer);
                    shouldRenderOverlay = false;
                    shouldRenderBuyOverlay = true;
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

        //longest message
        String longestMessage = "";
        for (String s : lines) {
            if (mc.fontRendererObj.getStringWidth(s) > mc.fontRendererObj.getStringWidth(longestMessage)) {
                longestMessage = s;
            }
        }
        //draw the background
        RenderUtils.drawRect(x, y, mc.fontRendererObj.getStringWidth(longestMessage), height * lines.size(), ColorPallet.SECONDARY.getColor().getRGB());

        //loop through the lines
        for (int i = 0; i < lines.size(); i++) {
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

    public boolean isMouseOverClose(int mouseX, int mouseY, int width, int height) {
        return mouseX >= (float) width - 40 && mouseX <= (float) width && mouseY >= (float) height - 40 && mouseY <= (float) height;
    }

    public boolean isMouseOverBuy(int mouseX, int mouseY, int width, int height) {
        return mouseX >= width / 2 - 100 && mouseX <= width / 2 + 100 && mouseY >= height / 2 - 20 && mouseY <= height / 2 + 20;
    }
}
