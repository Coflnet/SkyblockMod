package de.torui.coflsky.bingui.gui;

import de.torui.coflsky.bingui.helper.GuiUtilsClone;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;

import static de.torui.coflsky.bingui.helper.RenderUtils.mc;

//this is very work in progress and not finished so don't expect it to work
public class BetaGui {
    private IChatComponent message;
    private String[] lore;
    private String auctionId;
    private ItemStack itemStack;

    private static String title;
    private int buyState = 0;
    private static final InputHandler inputHandler = new InputHandler();

    public BetaGui(IChatComponent message, String[] lore, String auctionId) {
        this.message = message;
        this.lore = lore;
        this.auctionId = auctionId;
        MinecraftForge.EVENT_BUS.register(this);
        String parsedMessage = message.getFormattedText().split("✥")[0].substring(3).replaceAll(" sellers ah", "");
        title = parsedMessage;
        mc.thePlayer.sendChatMessage("/viewauction " + auctionId);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
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

        if (inventory.getDisplayName().getFormattedText().contains("BIN Auction") && buyState == 0) {
            if (itemStack == null) {
                //before i draw the gui, i check if there is a item in slot 13
                ItemStack item = inventory.getStackInSlot(13);
                if (item == null) return;
                itemStack = item;
                //set the lore to the lore of the item
                lore = item.getTooltip(mc.thePlayer, false).toArray(new String[0]);
            }
            RenderUtils.drawCenteredString(title, gui.width / 2, 10, new Color(255, 255, 255, 255));
            GuiUtilsClone.drawHoveringText(Arrays.asList(lore), 0, 0, gui.width, gui.height, 500, mc.fontRendererObj);
            if (inputHandler.isClicked()) {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 31, 0, 0, mc.thePlayer);
                buyState = 1;
            }
        } else if (inventory.getDisplayName().getFormattedText().toLowerCase(Locale.ROOT).contains("confirm") && buyState == 1) {
            if (itemStack == null) return;
            RenderUtils.drawCenteredString(title, gui.width / 2, 10, new Color(255, 255, 255, 255));
            GuiUtilsClone.drawHoveringText(Arrays.asList(lore), 0, 15, gui.width, gui.height, 500, mc.fontRendererObj);
            if (inputHandler.isClicked()) {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 11, 0, 0, mc.thePlayer);
                buyState = 0;
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }

    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        //first i get myself the gui
        GuiScreen gui = event.gui;

        //then i check if it is a chest gui
        if (!(gui instanceof GuiChest)) return;
        GuiChest chest = (GuiChest) gui;

        //then i get the private field named lowerChestInventory
        IInventory inventory = ((ContainerChest) chest.inventorySlots).getLowerChestInventory();

        //then a little null check
        if (inventory == null) return;

        if (inventory.getDisplayName().getFormattedText().contains("BIN Auction") && buyState == 0) {
            try {
                Robot robot = new Robot();
                System.out.println("moving mouse");
                robot.mouseMove(0, 64);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
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
            buyState = 2;
            mc.thePlayer.closeScreen();
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    @SubscribeEvent
    public void onTickEvent(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (buyState == 0 || buyState == 1) {
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                buyState = 2;
                mc.thePlayer.closeScreen();
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    @SubscribeEvent
    public void onKeyPressEven(net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent event) {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) || Keyboard.isKeyDown(Keyboard.KEY_E)) {
            buyState = 2;
            mc.thePlayer.closeScreen();
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }
}
