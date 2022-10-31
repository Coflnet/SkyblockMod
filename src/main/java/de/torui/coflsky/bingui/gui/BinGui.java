package de.torui.coflsky.bingui.gui;

import de.torui.coflsky.bingui.helper.ColorPallet;
import de.torui.coflsky.bingui.helper.RenderUtils;
import de.torui.coflsky.bingui.helper.inputhandler.InputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BinGui {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean shouldRenderOverlay = false;
    private static boolean shouldRenderBuyOverlay = false;
    private static final InputHandler inputHandler = new InputHandler();

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
        IInventory inventory = ((ContainerChest) chest.inventorySlots).getLowerChestInventory();

        //then a little null check
        if (inventory == null) return;

        if (inventory.getDisplayName().getFormattedText().contains("BIN Auction") && shouldRenderOverlay) {
            //now i render my overlay
            renderMainGui(event.mouseX, event.mouseY, gui.width, gui.height);
        } else if (inventory.getDisplayName().getFormattedText().toLowerCase(Locale.ROOT).contains("confirm") && shouldRenderBuyOverlay) {
            //buy overlay
            renderBuyOverlay(event.mouseX, event.mouseY, gui.width, gui.height);
        }

    }

    @SubscribeEvent
    public void onKeyEvent(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        //check if it should render the gui
        if (shouldRenderOverlay || shouldRenderBuyOverlay) {
            //check if esc was pressed
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                //close the gui
                close();
            }
        }
    }

    @SubscribeEvent
    public void onChatEvent(ClientChatReceivedEvent event) {
        //check if it should render the gui
        if (shouldRenderOverlay || shouldRenderBuyOverlay) {
            String message = event.message.getFormattedText().toLowerCase(Locale.ROOT);
            if (
                    message.contains("you have bought") ||
                            message.contains("you don't have enough coins") ||
                            message.contains("this auction wasn't found") ||
                            message.contains("there was an error with the auction house") ||
                            message.contains("you didn't participate in this auction")
            ) {
                //close the gui
                close();
                mc.thePlayer.closeScreen();
            }
        }
    }

    public void renderBuyOverlay(int mouseX, int mouseY, int width, int height) {
        Color successAlpha = new Color(ColorPallet.SUCCESS.getColor().getRed(), ColorPallet.SUCCESS.getColor().getGreen(), ColorPallet.SUCCESS.getColor().getBlue(), 140);
        RenderUtils.drawRect(width / 2 - 100, height / 2 - 100, 200, 100, successAlpha.getRGB());

        //if mouse button is hovered over the rectangle, draw a darker rectangle
        if (isMouseOverBuy(mouseX, mouseY, width, height)) {

            //draws the rectangle again, but now it is darker because of the alpha
            RenderUtils.drawRect(width / 2 - 100, height / 2 - 100, 200, 100, successAlpha.getRGB());

            //check if you clicked
            if (inputHandler.isClicked()) {

                //buy the item
                if (inputHandler.isClicked()) {

                    //click at slot 31
                    mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 11, 0, 0, mc.thePlayer);
                    close();
                    mc.thePlayer.closeScreen();
                }
            }
        }
    }


    public void renderMainGui(int mouseX, int mouseY, int width, int height) {
        //now I draw a cancel button under the buy button
        Color cancelAlpha = new Color(ColorPallet.ERROR.getColor().getRed(), ColorPallet.ERROR.getColor().getGreen(), ColorPallet.ERROR.getColor().getBlue(), 140);
        RenderUtils.drawRect(width / 2 - 100, height / 2, 200, 100, cancelAlpha.getRGB());

        //draw an x in the rectangle using lines
        RenderUtils.drawLine(width / 2 - 100, height / 2, width / 2 + 100, height / 2 + 100, 2, ColorPallet.WHITE.getColor());
        RenderUtils.drawLine(width / 2 - 100, height / 2 + 100, width / 2 + 100, height / 2, 2, ColorPallet.WHITE.getColor());

        //if mouse button is hovered over the rectangle, draw a darker rectangle
        if (isMouseOverClose(mouseX, mouseY, width, height)) {
            RenderUtils.drawRect(width / 2 - 100, height / 2, 200, 100, cancelAlpha.getRGB());

            //check if mouse button is pressed
            if (inputHandler.isClicked()) {
                close();
                mc.thePlayer.closeScreen();
            }
        }

        //draw the tooltip
        drawToolTip(width / 2 - 100 + 200, height / 2 - 100, lore);


        //remove everything after ✥ and remove the first 3 character also remove last 3 characters
        String flipMessage = message.getFormattedText().split("✥")[0].substring(3);//.substring(0, message.getFormattedText().split("✥")[0].length() - 3);

        //draw the flip message centered above the buy button with a background
        int flipMessageWidth = mc.fontRendererObj.getStringWidth(flipMessage);
        int flipMessageHeight = mc.fontRendererObj.FONT_HEIGHT;
        RenderUtils.drawRect(
                width / 2 - flipMessageWidth / 2 - 1,
                height / 2 - 100 - flipMessageHeight - 1,
                flipMessageWidth + 1,
                flipMessageHeight + 1,
                ColorPallet.SECONDARY.getColor().getRGB()
        );
        RenderUtils.drawString(
                flipMessage,
                width / 2 - flipMessageWidth / 2 + 1,
                height / 2 - 100 - flipMessageHeight,
                ColorPallet.WHITE.getColor()
        );


        //now I draw a big transparent green button in the middle of the screen that if clicked twice, buys the item
        Color successAlpha = new Color(ColorPallet.SUCCESS.getColor().getRed(), ColorPallet.SUCCESS.getColor().getGreen(), ColorPallet.SUCCESS.getColor().getBlue(), 140);
        RenderUtils.drawRect(width / 2 - 100, height / 2 - 100, 200, 100, successAlpha.getRGB());

        //if mouse button is hovered over the rectangle, draw a darker rectangle
        if (isMouseOverBuy(mouseX, mouseY, width, height)) {

            //draws the rectangle again, but now it is darker because of the alpha
            RenderUtils.drawRect(width / 2 - 100, height / 2 - 100, 200, 100, successAlpha.getRGB());

            //check if you double clicked
            if (inputHandler.isClicked()) {
                //buy the item
                if (inputHandler.isClicked()) {
                    //click at slot 31 by sending a packet
                    mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 31, 0, 0, mc.thePlayer);
                    shouldRenderOverlay = false;
                    shouldRenderBuyOverlay = true;
                }
            }
        }
    }

    public void close() {
        shouldRenderOverlay = false;
        shouldRenderBuyOverlay = false;
        MinecraftForge.EVENT_BUS.unregister(this);
        BinGuiManager.currentGui = null;
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
        RenderUtils.drawRect(x, y, width + 1, height, ColorPallet.SECONDARY.getColor().getRGB());

        //draw the text
        for (int i = 0; i < text.length; i++) {
            RenderUtils.drawString(text[i], (int) x, (int) (y + 1 + (i * 10)), ColorPallet.WHITE.getColor());
        }
    }

    public void drawFlipMessage(IChatComponent message, int x, int y) {
        float height = mc.fontRendererObj.FONT_HEIGHT;

        //every 50 chars, the message will be split into more lines
        List<String> lines = new ArrayList<>();
        String formattedMessage = message.getFormattedText();
        String unFormattedMessage = message.getUnformattedText();

        //split the message into lines
        for (int i = 0; i < unFormattedMessage.length(); i += 100) {
            lines.add(unFormattedMessage.substring(i, Math.min(unFormattedMessage.length(), i + 100)));
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
        return mouseX >= width / 2 - 100 && mouseX <= width / 2 + 100 && mouseY >= height / 2 && mouseY <= height / 2 + 100;
    }

    public boolean isMouseOverBuy(int mouseX, int mouseY, int width, int height) {
        return mouseX >= width / 2 - 100 && mouseX <= width / 2 + 100 && mouseY >= height / 2 - 100 && mouseY <= height / 2;
    }
}
