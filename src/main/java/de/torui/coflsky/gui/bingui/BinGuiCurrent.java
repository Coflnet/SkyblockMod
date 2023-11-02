package de.torui.coflsky.gui.bingui;


import de.torui.coflsky.CoflSky;
import de.torui.coflsky.gui.GUIType;
import de.torui.coflsky.gui.bingui.helper.ColorPallet;
import de.torui.coflsky.gui.bingui.helper.RenderUtils;
import de.torui.coflsky.handlers.EventHandler;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BinGuiCurrent extends GuiChest {
    private String message;
    private String[] lore;
    private ItemStack itemStack;
    private String buyText = "Buy(You can click anywhere)";
    private BuyState buyState = BuyState.INIT;
    private int pixelsScrolled = 0;
    private boolean wasMouseDown;
    private boolean isRendered = false;
    private boolean isClosing = false;
    private boolean hasInitialMouseSet = false;

    // set if the auction was already bought
    private String buyer = null;

    private static final Pattern CAN_BUY_IN_MATCHER = Pattern.compile("Can buy in: (.*)");
    private static final Pattern BUYER_MATCHER = Pattern.compile("Buyer: (.*)");

    private GuiChest chestGui;

    public BinGuiCurrent(IInventory playerInventory, IInventory chestInventory, String message, String extraData) {
        super(playerInventory, chestInventory);
        this.message = message;
        this.lore = new String[]{"Loading..."};
        if (extraData.length() >= 32) {
            itemStack = getSkull("Name", "00000000-0000-0000-0000-000000000000", extraData);
        } else {
            itemStack = new ItemStack(getItemByText(extraData));
            //if it is an armor item, we color it black
            if (itemStack.getItem() == null) return;
            if (itemStack.getItem() instanceof ItemArmor && (itemStack.getItem() == Items.leather_helmet || itemStack.getItem() == Items.leather_chestplate || itemStack.getItem() == Items.leather_leggings || itemStack.getItem() == Items.leather_boots)) {
                ((ItemArmor) itemStack.getItem()).setColor(itemStack, 0);
            }
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    private boolean shouldSkip(GuiScreen screen) {
        return !(screen instanceof GuiChest) || CoflSky.config.purchaseOverlay != GUIType.COFL || !EventHandler.isInSkyblock;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent event) {

        if (event.gui == null) {
            return;
        }

        isRendered = false;
        GuiScreen gui = event.gui;

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
        if (guiName.equalsIgnoreCase("BIN Auction View") || guiName.equalsIgnoreCase("Confirm Purchase")) {
            this.chestGui = (GuiChest) event.gui;
            this.inventorySlots = ((GuiChest) event.gui).inventorySlots;
            event.gui = this;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDrawGuiScreen(GuiScreenEvent.DrawScreenEvent.Pre event) {
        isRendered = false;
        GuiScreen gui = event.gui;

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

        ItemStack item = inventory.getStackInSlot(13);
        if (item == null) return;

        String[] tooltip = item.getTooltip(mc.thePlayer, false).toArray(new String[0]);

        itemStack = item;

        if (guiName.equalsIgnoreCase("BIN Auction View")) {
            lore = tooltip;
            buyer = isAlreadyBought(tooltip);
        }
        if (guiName.equalsIgnoreCase("BIN Auction View") && buyState == BuyState.PURCHASE) {
            if (waitingForBed(chest)) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[§1C§6oflnet§f]§7: §cBed is not purchasable yet."));
                buyState = BuyState.INIT;
            } else {
                mc.playerController.windowClick(this.chestGui.inventorySlots.windowId, 31, 2, 3, mc.thePlayer);
                wasMouseDown = false;
                buyState = BuyState.CONFIRM;
            }
        } else if (guiName.equalsIgnoreCase("Confirm Purchase") && buyState == BuyState.BUYING) {
            mc.playerController.windowClick(this.chestGui.inventorySlots.windowId, 11, 2, 3, mc.thePlayer);
            resetGUI();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if(isClosing){
            return;
        }
        Mouse.setGrabbed(false);
        isRendered = true;

        int screenWidth = this.width;
        int screenHeight = this.height;

        int width = mc.fontRendererObj.getStringWidth(message) > 500 ? mc.fontRendererObj.getStringWidth(message) + 5 : 500;
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
        RenderUtils.drawString(message, screenWidth / 2 - width / 2 + 7, 10 + 8, ColorPallet.WHITE.getColor());

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

        //draw the lore, every line that is out of the lore background will not be drawn
        int y = 10 + 5 + 14 + 5 + 2;
        for (int i = 0; i < lore.length; i++) {
            if (y + pixelsScrolled > 10 + 5 + 14 + 5 && y + pixelsScrolled < 10 + 5 + 14 + 5 + (height - 100)) {
                RenderUtils.drawString(lore[i], screenWidth / 2 - width / 2 + 5 + 20 + 5 + 2, y + pixelsScrolled, ColorPallet.WHITE.getColor());
            }
            y += 10;
        }


        //now i draw the buttons buy and sell under the lore
        //cancel button
        RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 - 25, 60, 5, ColorPallet.ERROR.getColor());
        RenderUtils.drawString("Cancel", screenWidth / 2 - width / 2 + 5 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
        if (isMouseOverCancel(mouseX, mouseY, screenWidth, screenHeight, width, height)) {
            RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 - 25, 60, 5, RenderUtils.setAlpha(ColorPallet.WHITE.getColor(), 100));
            RenderUtils.drawString("Cancel", screenWidth / 2 - width / 2 + 5 + 5, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
            if (isClicked()) {
                //play a anvilsound
                mc.thePlayer.playSound("random.anvil_land", 1, 1);
                resetGUI();
            }
        }


        //buy button
        if (buyer == null) {
            RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 + 20, 60, 5, ColorPallet.SUCCESS.getColor());
            RenderUtils.drawString(buyText, screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 + 5 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
        } else {
            RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 + 20, 60, 5, ColorPallet.WARNING.getColor());
            RenderUtils.drawString(getAlreadyBoughtText(buyer), screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 + 5 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
        }
        if (!isMouseOverCancel(mouseX, mouseY, screenWidth, screenHeight, width, height)) {
            if (buyer == null) {
                RenderUtils.drawRoundedRect(screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5, (width - 10) / 2 + 20, 60, 5, RenderUtils.setAlpha(ColorPallet.WHITE.getColor(), 50));
                RenderUtils.drawString(buyText, screenWidth / 2 - width / 2 + 5 + (width - 10) / 2 + 5 - 20, 10 + 5 + 14 + 5 + (height - 100) + 5 + 5, ColorPallet.WHITE.getColor(), 40);
            }
            if (isClicked() && buyer == null) {
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

    public String getAlreadyBoughtText(String buyer) {
        return "§5§o§7Bought by §b" + buyer;
    }

    @SubscribeEvent
    public void onGuiMouseInput(GuiScreenEvent.MouseInputEvent event) {
        if (event.gui instanceof BinGuiCurrent) {
            int dwheel = Mouse.getDWheel();
            pixelsScrolled += dwheel / 4;
        }
    }

    public void resetGUI() {
        buyState = BuyState.INIT;
        buyText = "Buy (You can click anywhere)";
        itemStack = null;
        hasInitialMouseSet = false;
        isRendered = false;
        isClosing = true;
        Mouse.setGrabbed(true);
        mc.thePlayer.closeScreen();
        MinecraftForge.EVENT_BUS.unregister(this);
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
                        message.contains("you purchased") ||
                        message.contains("you cannot view this auction")
        ) {
            //close the gui
            resetGUI();
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
        for (String data : itemTooltip) {
            Matcher matcher = CAN_BUY_IN_MATCHER.matcher(EnumChatFormatting.getTextWithoutFormattingCodes(data));
            if (!matcher.find()) {
                continue;
            }
            String timeData = matcher.group(1);
            if (timeData.equals("Soon!")) {
                return true;
            }
        }
        return false;
    }

    public String isAlreadyBought(String[] tooltip) {
        for (String data : tooltip) {
            Matcher matcher = BUYER_MATCHER.matcher(EnumChatFormatting.getTextWithoutFormattingCodes(data));
            if (!matcher.find()) {
                continue;
            }
            return data.replaceAll("§5§o§7Buyer: ", "");
        }
        return null;
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
