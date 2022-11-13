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
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
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
            //if it is an armor item, we color it black
            if (itemStack.getItem() == null) return;
            if (itemStack.getItem() instanceof ItemArmor && (itemStack.getItem() == Items.leather_helmet || itemStack.getItem() == Items.leather_chestplate || itemStack.getItem() == Items.leather_leggings || itemStack.getItem() == Items.leather_boots)) {
                ((ItemArmor) itemStack.getItem()).setColor(itemStack, 0);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        InputHandler inputHandler = new InputHandler();
        int screenWidth = this.width;
        int screenHeight = this.height;

        String parsedMessage = message.getFormattedText().split("✥")[0].substring(3).replaceAll(" sellers ah", "");

        int width = 600;
        int height = 300;

        if (lore.length > 25 && mc.fontRendererObj.FONT_HEIGHT*lore.length-40 < screenHeight ) {
            height = 300 + (lore.length - 25) * 10;
        }

        //first i draw the main background
        RenderUtils.drawRoundedRect(10, 10, width, height, 10, ColorPallet.PRIMARY.getColor());


        //next i draw the title
        RenderUtils.drawRoundedRect(10 + 30 + 5, 10 + 5, (width - 40), 25, 5, ColorPallet.SECONDARY.getColor());
        //RenderUtils.drawString(parsedMessage, 10 + 35 +5, 10 + 8, ColorPallet.WHITE.getColor());
        List<String> wrappedLine = mc.fontRendererObj.listFormattedStringToWidth(parsedMessage, width - 45);
        for (int i = 0; i < wrappedLine.size(); i++) {
            RenderUtils.drawString(wrappedLine.get(i), 10 + 35 + 5, 10 + 8 + (i * 10), ColorPallet.WHITE.getColor());
        }

        //now i draw the backround of the icon, the icon is left of the title
        RenderUtils.drawRoundedRect(10 + 5, 10 + 5, 25, 25, 5, ColorPallet.TERTIARY.getColor());


        //now i draw the icon
        if (itemStack == null) {
            //draw a question mark in the icon
            RenderUtils.drawString("?", 10 + 5 + 12, 10 + 5 + 12, ColorPallet.WHITE.getColor());
        } else {
            //draw the item in the icon
            RenderUtils.drawItemStack(itemStack, 10 + 5 + 5 - 8, 10 + 5 + 5 - 8, 1.35f, 1.35f);
        }

        //if the longest line of the lore is longer than the width of the gui, i use GuiUtils.drawHoveringText to draw the lore
        int longestLine = 0;
        for (String s : lore) {
            if (mc.fontRendererObj.getStringWidth(s) > longestLine) {
                longestLine = mc.fontRendererObj.getStringWidth(s);
            }
        }

        boolean loreTooLong = longestLine > width - 20||mc.fontRendererObj.FONT_HEIGHT*lore.length-40 > screenHeight;
        if (!loreTooLong) {
            //draw the backorund for the lore
            RenderUtils.drawRoundedRect(10 + 5, 10 + 5 + 25 + 5, (width / 3) * 2 - 10, height - 10 - 25 - 5, 5, ColorPallet.SECONDARY.getColor());

            //draw the lore
            for (int i = 0; i < lore.length; i++) {
                RenderUtils.drawString(lore[i], 10 + 5 + 5, 10 + 5 + 25 + 5 + 5 + (i * 10), ColorPallet.WHITE.getColor());

            }
        }

        //now i draw the buttons buy and cancel right of the lore
        //cancel button
        RenderUtils.drawRoundedRect(10 + 5 + (width / 3) * 2, 10 + 5 + 25 + 5, (width / 3) - 10, 125, 5, ColorPallet.ERROR.getColor());
        RenderUtils.drawString("Cancel", 10 + 5 + (width / 3) * 2 + 5, 10 + 5 + 25 + 5 + 5, ColorPallet.WHITE.getColor());
        if (mouseX >= 10 + 5 + (width / 3) * 2 && mouseX <= 10 + 5 + (width / 3) * 2 + (width / 3) - 10 && mouseY >= 10 + 5 + 25 + 5 && mouseY <= 10 + 5 + 25 + 5 + 125) {
            RenderUtils.drawRoundedRect(10 + 5 + (width / 3) * 2, 10 + 5 + 25 + 5, (width / 3) - 10, 125, 5, RenderUtils.setAlpha(ColorPallet.WHITE.getColor(), 64));
            RenderUtils.drawString("Cancel", 10 + 5 + (width / 3) * 2 + 5, 10 + 5 + 25 + 5 + 5, ColorPallet.WHITE.getColor());
            if (inputHandler.isClicked()) {
                buyState = 0;
                buyText = "Buy";
                MinecraftForge.EVENT_BUS.unregister(this);
                mc.displayGuiScreen(null);
            }
        }


        //buy button
        RenderUtils.drawRoundedRect(10 + 5 + (width / 3) * 2, 10 + 5 + 25 + 5 + 25 + 5 + 100, (width / 3) - 10, height - 10 - 25 - 5 -130, 5, ColorPallet.SUCCESS.getColor());
        RenderUtils.drawString(buyText, 10 + 5 + (width / 3) * 2 + 5, 10 + 5 + 25 + 5 + 25 + 5 + 100 + 5, ColorPallet.WHITE.getColor());
        if (mouseX >= 10 + 5 + (width / 3) * 2 && mouseX <= 10 + 5 + (width / 3) * 2 + (width / 3) - 10 && mouseY >= 10 + 5 + 25 + 5 + 25 + 5 + 100 && mouseY <= 10 + 5 + 25 + 5 + 25 + 5 + 100 + 220) {
            RenderUtils.drawRoundedRect(10 + 5 + (width / 3) * 2, 10 + 5 + 25 + 5 + 25 + 5 + 100, (width / 3) - 10, height - 10 - 25 - 5 -130, 5, RenderUtils.setAlpha(ColorPallet.WHITE.getColor(), 64));
            RenderUtils.drawString(buyText, 10 + 5 + (width / 3) * 2 + 5, 10 + 5 + 25 + 5 + 25 + 5 + 100 + 5, ColorPallet.WHITE.getColor());
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

        if (loreTooLong) {
            GuiUtils.drawHoveringText(Arrays.asList(lore), mouseX, mouseY, screenWidth, screenHeight, -1, mc.fontRendererObj);
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
        String url = "https://textures.minecraft.net/texture/" + value;
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
    public void onGuiClosed() {
        if (buyState == 1) {
            buyState = 0;
            buyText = "Buy";
            MinecraftForge.EVENT_BUS.unregister(this);
        }
        super.onGuiClosed();
    }


}
