package de.torui.coflsky.gui.bingui;


import de.torui.coflsky.CoflSky;
import de.torui.coflsky.WSCommandHandler;
import de.torui.coflsky.gui.GUIType;
import de.torui.coflsky.gui.bingui.helper.ColorPallet;
import de.torui.coflsky.gui.bingui.helper.RenderUtils;
import de.torui.coflsky.handlers.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.torui.coflsky.gui.bingui.helper.RenderUtils.mc;

public class BinGuiCurrent {

    private static BinGuiCurrent instance;
    private String[] lore;
    private ItemStack itemStack;
    private String buyText = "Buy(You can click anywhere)";
    private BuyState buyState = BuyState.INIT;
    private int pixelsScrolled = 0;
    private boolean wasMouseDown;
    private boolean isRendered = false;
    private boolean hasInitialMouseSet = false;

    /*
    public BinGuiCurrent(IChatComponent message, String[] lore, String auctionId, String extraData) {
        this.message = message;
        this.lore = lore;
        this.auctionId = auctionId;
        MinecraftForge.EVENT_BUS.register(this);
        mc.thePlayer.sendChatMessage("/viewauction " + auctionId);
        //System.out.println(extraData);
        if (extraData.length() >= 32) {
            //now its a skull
            itemStack = getSkull("Name", "00000000-0000-0000-0000-000000000000", extraData);
        } else {
            itemStack = new ItemStack(getItemByText(extraData));
            //if it is an armor item, we color it black
            if (itemStack.getItem() == null) return;
            if (itemStack.getItem() instanceof ItemArmor && (itemStack.getItem() == Items.leather_helmet || itemStack.getItem() == Items.leather_chestplate || itemStack.getItem() == Items.leather_leggings || itemStack.getItem() == Items.leather_boots)) {
                ((ItemArmor) itemStack.getItem()).setColor(itemStack, 0);
            }
        }
        //play a pling sound
        mc.thePlayer.playSound("note.pling", 1, 1);
    }
    */

    private BinGuiCurrent() {

    }

    public static BinGuiCurrent getInstance() {
        if (instance == null) {
            instance = new BinGuiCurrent();
        }
        return instance;
    }

    private boolean shouldSkip(GuiScreen screen) {
        return !(screen instanceof GuiChest) || CoflSky.config.purchaseOverlay != GUIType.COFL || !EventHandler.isInSkyblock;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDrawGuiScreen(GuiScreenEvent.DrawScreenEvent.Pre event) {
        isRendered = false;
        GuiScreen gui = event.gui;
        String message = WSCommandHandler.flipHandler.lastClickedFlipMessage;

        if (message == null || message.isEmpty()) {
            return;
        }

        if (shouldSkip(gui)) {
            return;
        }

        GuiChest chest = (GuiChest) gui;

        IInventory inventory = ((ContainerChest) chest.inventorySlots).getLowerChestInventory();
        if (inventory == null) return;

        String guiName = inventory.getDisplayName().getUnformattedText().trim();

        if (guiName.equalsIgnoreCase("auction view")) {
            return;
        }

        if (guiName.equalsIgnoreCase("BIN Auction View") && buyState == BuyState.INIT) {
            ItemStack item = inventory.getStackInSlot(13);
            if (item == null) return;

            itemStack = item;
            lore = item.getTooltip(mc.thePlayer, false).toArray(new String[0]);
            drawScreen(message, event.mouseX, event.mouseY, event.renderPartialTicks, gui.width, gui.height);
            if(waitingForBed(chest)){
                buyState = BuyState.INIT;
            }
            event.setCanceled(true);
        } else if (guiName.equalsIgnoreCase("BIN Auction View") && buyState == BuyState.PURCHASE) {
            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 31, 2, 3, mc.thePlayer);
            buyState = BuyState.CONFIRM;
            event.setCanceled(true);
        } else if ((guiName.equalsIgnoreCase("BIN Auction View") || guiName.equalsIgnoreCase("Confirm Purchase")) && buyState == BuyState.CONFIRM) {
            drawScreen(message, event.mouseX, event.mouseY, event.renderPartialTicks, gui.width, gui.height);
            event.setCanceled(true);
        } else if (guiName.equalsIgnoreCase("Confirm Purchase") && buyState == BuyState.BUYING) {
            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 11, 2, 3, mc.thePlayer);
            resetGUI();
        }
    }

    public void resetGUI() {
        buyState = BuyState.INIT;
        buyText = "Buy (You can click anywhere)";
        itemStack = null;
        hasInitialMouseSet = false;
        isRendered = false;
    }

    @SubscribeEvent
    public void onChatEvent(ClientChatReceivedEvent event) {
        String message = event.message.getFormattedText().toLowerCase(Locale.ROOT);
        if (
                message.contains("you have bought") ||
                        message.contains("you don't have enough coins") ||
                        message.contains("this auction wasn't found") ||
                        message.contains("there was an error with the auction house") ||
                        message.contains("you didn't participate in this auction") ||
                        message.contains("you claimed") ||
                        message.contains("you purchased")
        ) {
            //close the gui
            resetGUI();
            mc.thePlayer.closeScreen();
        }
    }

    public void drawScreen(String message, int mouseX, int mouseY, float partialTicks, int screenWidth, int screenHeight) {
        isRendered = true;

        String parsedMessage = message.split("✥")[0].replaceAll(" sellers ah", "");

        int width = mc.fontRendererObj.getStringWidth(parsedMessage) > 500 ? mc.fontRendererObj.getStringWidth(parsedMessage) + 5 : 500;
        int height = 300;

        //RenderUtils.drawRoundedRect(screenWidth / 2, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 + 20, 60, 5, ColorPallet.SUCCESS.getColor());
        if (!hasInitialMouseSet) {
            Mouse.setCursorPosition(mc.displayWidth / 2, mc.displayHeight / 2);
            hasInitialMouseSet = true;
        }

        //if (lore.length > 25) {
        //    height = 300 + (lore.length - 25) * 10;
        //}

        //first i draw the main background
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2, 10, width, height, 10, ColorPallet.PRIMARY.getColor());

        //next i draw the title
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5, 10 + 5, (width - 10), 14, 5, ColorPallet.SECONDARY.getColor());
        RenderUtils.drawString(parsedMessage, screenWidth / 2 - width / 2 + 7, 10 + 8, ColorPallet.WHITE.getColor());

        //now i draw the backround of the icon
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5, 10 + 5 + 14 + 5, 20, 20, 5, ColorPallet.TERTIARY.getColor());

        //now i draw the icon
        if (itemStack == null) {
            //draw a question mark in the icon
            RenderUtils.drawString("?", screenWidth / 2 - width / 2 + 5 + 5, 10 + 5 + 14 + 5 + 2, ColorPallet.WHITE.getColor(), 40);
        } else {
            RenderUtils.drawItemStack(itemStack, screenWidth / 2 - width / 2 + 5 + 2, 10 + 5 + 14 + 5 + 2);
        }


        //draw the backorund for the lore
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5 + 20 + 5, 10 + 5 + 14 + 5, (width - 10) - 25, (height - 100), 5, ColorPallet.SECONDARY.getColor());
        if (mouseX > screenWidth / 2 - width / 2 + 5 + 20 + 5 && mouseX < screenWidth / 2 - width / 2 + 5 + 20 + 5 + (width - 10) - 25 && mouseY > 10 + 5 + 14 + 5 && mouseY < 10 + 5 + 14 + 5 + (height - 100)) {
            //mouse is in the lore background
            pixelsScrolled += Mouse.getDWheel() / 4;//4 is the scroll speed
        }

        //draw the lore, every line that is out of the lore background will not be drawn
        int y = 10 + 5 + 14 + 5 + 2;
        for (
                int i = 0;
                i < lore.length; i++) {
            if (y + pixelsScrolled > 10 + 5 + 14 + 5 && y + pixelsScrolled < 10 + 5 + 14 + 5 + (height - 100)) {
                RenderUtils.drawString(lore[i], screenWidth / 2 - width / 2 + 5 + 20 + 5 + 2, y + pixelsScrolled, ColorPallet.WHITE.getColor());
            }
            y += 10;
        }


        //now i draw the buttons buy and sell under the lore
        //cancel button
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 - 25, 60, 5, ColorPallet.ERROR.getColor());
        RenderUtils.drawString("Cancel", screenWidth / 2 - width / 2 + 5 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
        if (
                isMouseOverCancel(mouseX, mouseY, screenWidth, screenHeight, width, height)) {
            RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 - 25, 60, 5, RenderUtils.setAlpha(ColorPallet.WHITE.getColor(), 100));
            RenderUtils.drawString("Cancel", screenWidth / 2 - width / 2 + 5 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
            if (isClicked()) {
                //play a anvilsound
                mc.thePlayer.playSound("random.anvil_land", 1, 1);
                resetGUI();
                mc.thePlayer.closeScreen();
            }
        }


        //buy button
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 + 20, 60, 5, ColorPallet.SUCCESS.getColor());
        RenderUtils.drawString(buyText, screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 + 5 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
        if (!

                isMouseOverCancel(mouseX, mouseY, screenWidth, screenHeight, width, height)) {
            RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 + 20, 60, 5, RenderUtils.setAlpha(ColorPallet.WHITE.getColor(), 50));
            RenderUtils.drawString(buyText, screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 + 5 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
            if (isClicked()) {
                if (buyState == BuyState.INIT) {
                    //play a sound
                    mc.thePlayer.playSound("random.click", 1, 1);
                    buyText = "Click again to confirm";
                    buyState = BuyState.PURCHASE;
                } else if (buyState == BuyState.CONFIRM) {
                    mc.thePlayer.playSound("random.click", 1, 1);
                    buyText = "Buying";
                    buyState = BuyState.BUYING;
                }
            }
        }

    }

    public ItemStack getItem(int slotNum, GuiChest currentScreen) {
        ContainerChest container = (ContainerChest) currentScreen.inventorySlots;
        return container.getSlot(slotNum).getStack();
    }

    public boolean waitingForBed(GuiChest currentScreen) {
        ItemStack bedStack = getItem(31, currentScreen);
        if (bedStack == null || !bedStack.getItem().equals(Item.getByNameOrId("minecraft:bed"))) {
            return false;
        }

        ItemStack itemStack = getItem(13, currentScreen);
        if (itemStack == null) {
            return false;
        }
        List<String> itemTooltip = itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
        System.out.println("-------------------------------");
        for (String data : itemTooltip) {
            System.out.println(data);
            Matcher matcher = Pattern.compile("Can buy in: (.*)").matcher(EnumChatFormatting.getTextWithoutFormattingCodes(data));
            if (!matcher.find()) {
                continue;
            }
            String timeData = matcher.group(1);
            if (timeData.contains("Soon!")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMouseOverCancel(int mouseX, int mouseY, int screenWidth, int screenHeight, int width, int height) {
        return mouseX > screenWidth / 2 - width / 2 + 5 && mouseX < screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 25 && mouseY > 10 + 5 + 14 + 5 + (height - 100) + 5 && mouseY < 10 + 5 + 14 + 5 + (height - 100) + 5 + 60;
    }

    private static boolean isMouseOverAccept(int mouseX, int mouseY, int screenWidth, int screenHeight, int width, int height) {
        return mouseX > screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 20 && mouseX < (screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 20) + (width - 10) / 2 + 20 && mouseY > 10 + 5 + 14 + 5 + (height - 100) + 5 && mouseY < 10 + 5 + 14 + 5 + (height - 100) + 5 + 60;
    }

    public boolean isClicked() {
        return wasMouseDown && !Mouse.isButtonDown(0);
    }

    @SubscribeEvent
    public void onRenderEvent(TickEvent.RenderTickEvent event) {
        if (Minecraft.getMinecraft() == null) return;
        if (event.phase == TickEvent.Phase.END) {
            wasMouseDown = Mouse.isButtonDown(0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent event) {
        // gui got closed
        if (event.gui == null) {
            resetGUI();
        }
    }

    @SubscribeEvent
    public void onMouseClicked(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!Mouse.getEventButtonState()) {
            return;
        }
        if (isRendered) {
            event.setCanceled(true);
        }
    }
}
