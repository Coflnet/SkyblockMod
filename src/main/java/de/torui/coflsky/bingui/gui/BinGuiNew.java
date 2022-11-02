package de.torui.coflsky.bingui.gui;

import de.torui.coflsky.bingui.helper.ColorPallet;
import de.torui.coflsky.bingui.helper.RenderUtils;
import de.torui.coflsky.bingui.helper.inputhandler.InputHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

public class BinGuiNew extends GuiScreen {
    private IChatComponent message;
    private String[] lore;
    private String auctionId;
    private ItemStack itemStack;

    private String buyText = "Buy";
    private int buyState = 0;

    public BinGuiNew(IChatComponent message, String[] lore, String auctionId, ItemStack itemStack) {
        this.message = message;
        this.lore = lore;
        this.auctionId = auctionId;
        if (message == null || lore == null) return;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        InputHandler inputHandler = new InputHandler();
        int screenWidth = this.width;
        int screenHeight = this.height;

        //first i draw the main background
        RenderUtils.drawRoundedRect(screenWidth / 2 - 250, 10, 500, 300, 10, ColorPallet.PRIMARY.getColor());

        //next i draw the title
        RenderUtils.drawRoundedRect(screenWidth / 2 - 250 + 5, 10 + 5, 490, 14, 5, ColorPallet.SECONDARY.getColor());
        RenderUtils.drawString(message.getFormattedText().replaceAll("FLIP:", "").replaceAll(" sellers ah", ""), screenWidth / 2 - 250 + 7, 10 + 8, ColorPallet.WHITE.getColor());

        //now i draw the backround of the icon
        RenderUtils.drawRoundedRect(screenWidth / 2 - 250 + 5, 10 + 5 + 14 + 5, 20, 20, 5, ColorPallet.TERTIARY.getColor());

        //now i draw the icon
        if (itemStack == null) {
            //draw a question mark in the icon
            RenderUtils.drawString("?", screenWidth / 2 - 250 + 5 + 5, 10 + 5 + 14 + 5 + 2, ColorPallet.WHITE.getColor(), 40);
        } else {
            RenderUtils.drawItemStack(itemStack, screenWidth / 2 - 250 + 5 + 2, 10 + 5 + 14 + 5 + 2, 16, 16);
        }


        //draw the backorund for the lore
        RenderUtils.drawRoundedRect(screenWidth / 2 - 250 + 5 + 20 + 5, 10 + 5 + 14 + 5, 490 - 25, 200, 5, ColorPallet.SECONDARY.getColor());


        //now i draw the lore if its longer than 10 lines i draw a scrollbar
        if (lore.length > 10) {
            RenderUtils.drawRoundedRect(screenWidth / 2 - 250 + 5 + 20 + 5 + 490 - 25 + 5, 10 + 5 + 14 + 5, 10, 200, 5, ColorPallet.TERTIARY.getColor());
        }

        //draw the lore
        for (int i = 0; i < lore.length; i++) {
            RenderUtils.drawString(lore[i], screenWidth / 2 - 250 + 5 + 20 + 5 + 5, 10 + 5 + 14 + 5 + 5 + 10 * i, ColorPallet.WHITE.getColor());
        }

        //scrollbar
        if (lore.length > 10) {
            RenderUtils.drawRoundedRect(screenWidth / 2 - 250 + 5 + 20 + 5 + 490 - 25 + 5 + 1, 10 + 5 + 14 + 5 + 1, 8, 198, 5, ColorPallet.SECONDARY.getColor());
        }


        //now i draw the buttons buy and sell under the lore
        //buy button
        RenderUtils.drawRoundedRect(screenWidth / 2 - 250 + 5, 10 + 5 + 14 + 5 + 200 + 5, 490 / 2 - 25, 60, 5, ColorPallet.ERROR.getColor());
        RenderUtils.drawString("Cancel", screenWidth / 2 - 250 + 5 + 5, 10 + 5 + 14 + 5 + 200 + 5 + 5, ColorPallet.WHITE.getColor(), 40);
        if (mouseX > screenWidth / 2 - 250 + 5 && mouseX < screenWidth / 2 - 250 + 5 + 490 / 2 - 25 && mouseY > 10 + 5 + 14 + 5 + 200 + 5 && mouseY < 10 + 5 + 14 + 5 + 200 + 5 + 60) {
            RenderUtils.drawRoundedRect(screenWidth / 2 - 250 + 5, 10 + 5 + 14 + 5 + 200 + 5, 490 / 2 - 25, 60, 5, RenderUtils.setAlpha(ColorPallet.ERROR.getColor(), 100));
            if (inputHandler.isClicked()) {
                buyState = 0;
                buyText = "Buy";
                MinecraftForge.EVENT_BUS.unregister(this);
                mc.displayGuiScreen(null);
            }
        }


        //cancel button
        RenderUtils.drawRoundedRect(screenWidth / 2 - 250 + 5 + 490 / 2 - 20, 10 + 5 + 14 + 5 + 200 + 5, 490 / 2 + 20, 60, 5, ColorPallet.SUCCESS.getColor());
        RenderUtils.drawString(buyText, screenWidth / 2 - 250 + 5 + 490 / 2 + 5 - 20, 10 + 5 + 14 + 5 + 200 + 5 + 5, ColorPallet.WHITE.getColor(), 40);
        if (mouseX > screenWidth / 2 - 250 + 5 + 490 / 2 - 20 && mouseX < screenWidth / 2 - 250 + 5 + 490 && mouseY > 10 + 5 + 14 + 5 + 200 + 5 && mouseY < 10 + 5 + 14 + 5 + 200 + 5 + 60) {
            RenderUtils.drawRoundedRect(screenWidth / 2 - 250 + 5 + 490 / 2 - 20, 10 + 5 + 14 + 5 + 200 + 5, 490 / 2 + 20, 60, 5, RenderUtils.setAlpha(ColorPallet.SUCCESS.getColor(), 140));
            if (inputHandler.isClicked()) {
                if (buyState == 0) {
                    buyText = "Click again to confirm";
                    buyState = 1;
                } else if (buyState == 1) {
                    buyText = "Buying";
                    buyState = 2;
                    buy();
                }
            }
        }


        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void buy() {
        mc.thePlayer.closeScreen();
        mc.thePlayer.sendChatMessage("/viewauction " + auctionId);
        MinecraftForge.EVENT_BUS.register(this);
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

        if (inventory.getDisplayName().getFormattedText().contains("BIN Auction") && buyState == 2) {
            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 31, 0, 0, mc.thePlayer);
            buyState = 3;
        } else if (inventory.getDisplayName().getFormattedText().toLowerCase(Locale.ROOT).contains("confirm") && buyState == 3) {
            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 11, 0, 0, mc.thePlayer);
            buyState = 0;
            buyText = "Buy";
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    @SubscribeEvent
    public void onChatEvent(ClientChatReceivedEvent event) {
        if (buyState == 3 || buyState == 2) {
            String message = event.message.getFormattedText().toLowerCase(Locale.ROOT);
            if (
                    message.contains("you have bought") ||
                            message.contains("you don't have enough coins") ||
                            message.contains("this auction wasn't found") ||
                            message.contains("there was an error with the auction house") ||
                            message.contains("you didn't participate in this auction")
            ) {
                //close the gui
                buyState = 0;
                buyText = "Buy";
                mc.thePlayer.closeScreen();
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void onGuiClosed() {
        if (buyState == 1) {
            buyState = 0;
            buyText = "Buy";
            MinecraftForge.EVENT_BUS.unregister(this);
        }
        super.onGuiClosed();
    }


}
