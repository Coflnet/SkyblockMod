package de.torui.coflsky.bingui.gui;

import de.torui.coflsky.bingui.helper.ColorPallet;
import de.torui.coflsky.bingui.helper.RenderUtils;
import de.torui.coflsky.bingui.helper.inputhandler.InputHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Base64;
import java.util.Locale;

public class BinGuiNew extends GuiScreen {
    private IChatComponent message;
    private String[] lore;
    private String auctionId;
    private ItemStack itemStack;

    private String buyText = "Buy";
    private int buyState = 0;

    public BinGuiNew(IChatComponent message, String[] lore, String auctionId, String extraData) {
        this.message = message;
        this.lore = lore;
        this.auctionId = auctionId;
        System.out.println(extraData);
        if (extraData.length() >= 32) {
            //now its a skull
            itemStack = getSkull("Name", "00000000-0000-0000-0000-000000000000", extraData);
        } else {
            itemStack = new ItemStack(getItemByText(extraData));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        InputHandler inputHandler = new InputHandler();
        int screenWidth = this.width;
        int screenHeight = this.height;

        String parsedMessage = message.getFormattedText().split("✥")[0].substring(3);

        int width = mc.fontRendererObj.getStringWidth(parsedMessage);
        int height = 300;

        if (lore.length > 25) {
            height = 300 + (lore.length - 25) * 10;
        }

        //first i draw the main background
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2, 10, width, height, 10, ColorPallet.PRIMARY.getColor());

        //next i draw the title
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5, 10 + 5, (width - 10), 14, 5, ColorPallet.SECONDARY.getColor());
        RenderUtils.drawString(parsedMessage.replaceAll("FLIP:", "").replaceAll(" sellers ah", ""), screenWidth / 2 - width / 2 + 7, 10 + 8, ColorPallet.WHITE.getColor());

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

        //draw the lore
        for (int i = 0; i < lore.length; i++) {
            RenderUtils.drawString(lore[i], screenWidth / 2 - width / 2 + 5 + 20 + 5 + 5, 10 + 5 + 14 + 5 + 5 + 10 * i, ColorPallet.WHITE.getColor());
        }

        //now i draw the buttons buy and sell under the lore
        //cancel button
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 - 25, 60, 5, ColorPallet.ERROR.getColor());
        RenderUtils.drawString("Cancel", screenWidth / 2 - width / 2 + 5 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
        if (mouseX > screenWidth / 2 - width / 2 + 5 && mouseX < screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 25 && mouseY > 10 + 5 + 14 + 5 + (height - 100) + 5 && mouseY < 10 + 5 + 14 + 5 + (height - 100) + 5 + 60) {
            RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 - 25, 60, 5, RenderUtils.setAlpha(ColorPallet.ERROR.getColor(), 100));
            if (inputHandler.isClicked()) {
                buyState = 0;
                buyText = "Buy";
                MinecraftForge.EVENT_BUS.unregister(this);
                mc.displayGuiScreen(null);
            }
        }


        //buy button
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 + 20, 60, 5, ColorPallet.SUCCESS.getColor());
        RenderUtils.drawString(buyText, screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 + 5 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
        if (mouseX > screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 20 && mouseX < screenWidth / 2 - width / 2 + 5 + (width - 10) && mouseY > 10 + 5 + 14 + 5 + (height - 100) + 5 && mouseY < 10 + 5 + 14 + 5 + (height - 100) + 5 + 60) {
            RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 + 20, 60, 5, RenderUtils.setAlpha(ColorPallet.SUCCESS.getColor(), 140));
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

    //get skull itemstack from base64 texture
    public static ItemStack getSkull(String displayName, String uuid, String value) {
        String url = "http://textures.minecraft.net/texture/" + value;
        ItemStack render = new ItemStack(Items.skull, 1, 3);

        NBTTagCompound skullOwner = new NBTTagCompound();
        skullOwner.setString("Id", uuid);
        skullOwner.setString("Name", uuid);

        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        NBTTagCompound textures_0 = new NBTTagCompound();
        textures_0.setString("Value", new String(encodedData));

        NBTTagList textures = new NBTTagList();
        textures.appendTag(textures_0);

        NBTTagCompound display = new NBTTagCompound();
        display.setString("Name", displayName);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("display", display);

        NBTTagCompound properties = new NBTTagCompound();
        properties.setTag("textures", textures);
        skullOwner.setTag("Properties", properties);
        tag.setTag("SkullOwner", skullOwner);
        render.setTagCompound(tag);
        return render;
    }


    public static Item getItemByText(String id) {
        try {
            ResourceLocation resourcelocation = new ResourceLocation(id);
            if (!Item.itemRegistry.containsKey(resourcelocation)) {
                throw new NumberInvalidException("block.notFound", resourcelocation);
            }
            Item item = Item.itemRegistry.getObject(resourcelocation);
            if (item == null) {
                throw new NumberInvalidException("block.notFound", resourcelocation);
            }
            return item;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }

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
