package de.torui.coflsky.gui.tfm;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.WSCommandHandler;
import de.torui.coflsky.gui.GUIType;
import de.torui.coflsky.handlers.EventHandler;
import de.torui.coflsky.handlers.EventRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.client.gui.Gui.drawRect;

public class ButtonRemapper {
    private static ButtonRemapper instance;
    private final static int BUY_BUTTON_SLOT = 31;
    private final static int ITEM_SLOT = 13;
    private final static int CONFIRM_BUTTON_SLOT = 11;
    private final static int CANCEL_CONFIRM_SLOT = 15;
    private final static int BACK_BUTTON_SLOT = 49;
    private final static double SCALE_VALUE = 2.0d;
    private final static Pattern BED_TIME_PATTERN = Pattern.compile("Can buy in: (.*)");

    private final Method drawItemMethod;
    private final Method renderToolTipMethod;

    private ButtonRemapper() {
        // drawItemStack obfuscated
        String[] methodNames = new String[]{"drawItemStack", "func_146982_a"};
        drawItemMethod = ReflectionHelper.findMethod(GuiContainer.class, null, methodNames, ItemStack.class, int.class, int.class, String.class);
        drawItemMethod.setAccessible(true);
        // obfuscated renderToolTip method
        methodNames = new String[]{"renderToolTip", "func_146285_a"};
        renderToolTipMethod = ReflectionHelper.findMethod(GuiScreen.class, null, methodNames, ItemStack.class, int.class, int.class);
    }

    public static ButtonRemapper getInstance() {
        if (instance == null) {
            instance = new ButtonRemapper();
        }
        return instance;
    }

    public ItemStack getItem(int slotNum, GuiChest currentScreen) {
        ContainerChest container = (ContainerChest) currentScreen.inventorySlots;
        return container.getSlot(slotNum).getStack();
    }

    public boolean waitingForBed(GuiChest currentScreen) {
        ItemStack bedStack = getItem(BUY_BUTTON_SLOT, currentScreen);
        if (bedStack == null || !bedStack.getItem().equals(Item.getByNameOrId("minecraft:bed"))) {
            return false;
        }

        ItemStack itemStack = getItem(ITEM_SLOT, currentScreen);
        if (itemStack == null) {
            return false;
        }
        List<String> itemTooltip = itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
        for (String data : itemTooltip) {
            Matcher matcher = BED_TIME_PATTERN.matcher(EnumChatFormatting.getTextWithoutFormattingCodes(data));
            if (!matcher.find()) {
                continue;
            }
            String timeData = matcher.group(1);
            if (!timeData.equals("Soon!")) {
                return true;
            }
        }
        return false;
    }

    private int[] getBuyBoxDimensions() {
        int centerX = getGuiCenterX();
        int centerY = getGuiCenterY();
        double multiplier = 0.1;
        return new int[]{(int) (centerX - (centerX * multiplier)), (int) (centerY + (centerY * multiplier)),
                (int) (centerX + (centerX * multiplier)), (int) (centerY - (centerY * multiplier))};
    }

    private int[] getCancelBoxDimensions() {
        int centerX = getGuiCenterX();
        int centerY = (int) (getGuiCenterY() * 1.25);
        double multiplier = 0.05;
        return new int[]{(int) (centerX - (centerX * multiplier)), (int) (centerY + (centerY * multiplier)),
                (int) (centerX + (centerX * multiplier)), (int) (centerY - (centerY * multiplier))};
    }

    @SuppressWarnings("SameParameterValue")
    private void drawBoxWithShadow(int leftX, int topY, int rightX, int bottomY, int colour, int shadowSize, int shadowColour) {
        drawRect(leftX - shadowSize, topY + shadowSize, rightX + shadowSize, bottomY - shadowSize, shadowColour);
        drawRect(leftX, topY, rightX, bottomY, colour);
    }

    private void drawTfmBox(boolean isConfirm) {
        String titleText;
        if (isConfirm) {
            titleText = "Cofl - Confirm Purchase";
        } else {
            titleText = "Cofl - Auction View";
        }
        int outerBox = 0xff4f4f4f; // gray colour
        // draw the outer tfm box
        drawBoxWithShadow((int) (getGuiCenterX() * 0.5), (int) (getGuiCenterY() * 1.6), (int) (getGuiCenterX() * 1.5), (int) (getGuiCenterY() * 0.4), outerBox, 1, 0xff000000);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(titleText, (int) (getGuiCenterX() * 0.55), (int) (getGuiCenterY() * 0.45), 0xFFFFFFFF);
    }

    private void renderTooltip(GuiChest chest, ItemStack item) {
        ToolTipHelper toolTipData = new ToolTipHelper(item);
        int toolTipY = getGuiCenterY() - (toolTipData.determineHeight() / 2);
        int toolTipX = (int) (getGuiCenterX() * 1.25) - (toolTipData.determineWidth() / 2);
        if (toolTipX < getGuiCenterX()) {
            toolTipX = getGuiCenterX() + 10;
        }
        try {
            renderToolTipMethod.invoke(chest, item, toolTipX, toolTipY);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void renderItem(GuiChest chest, ItemStack item) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(SCALE_VALUE, SCALE_VALUE, 0);
        int itemX = (int) ((getGuiCenterX() - 16) / SCALE_VALUE);
        int itemY = (int) ((getGuiCenterY() - 16) / SCALE_VALUE);
        try {
            drawItemMethod.invoke(chest, item, itemX, itemY, null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        GlStateManager.popMatrix();
    }

    private void drawBuyBox(boolean shouldBeRed) {
        int colour = shouldBeRed ? 255 << 16 : 255 << 8;
        colour += 128 << 24;
        int[] dim = getBuyBoxDimensions();
        drawBoxWithShadow(dim[0], dim[1], dim[2], dim[3], colour, 1, 0xff000000);
    }

    private void drawCancelBox() {
        int colour = 0x7FFF0000;
        int[] dim = getCancelBoxDimensions();
        drawBoxWithShadow(dim[0], dim[1], dim[2], dim[3], colour, 1, 0xff000000);
    }

    public void drawProfitInfo() {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        String text = WSCommandHandler.flipHandler.lastClickedFlipMessage;

        if (text == null) {
            return;
        }

        StringBuilder current = new StringBuilder();
        int lineNo = 0;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            current.append(character);
            if (current.length() > 50 && character == ' ') {
                font.drawStringWithShadow(current.toString(), (int) (getGuiCenterX() * 0.55), (int) (getGuiCenterY() * 0.5 + (15 * lineNo)), 0xFF8F8F8F);
                current = new StringBuilder();
                lineNo++;
            }
        }
        font.drawStringWithShadow(current.toString(), (int) (getGuiCenterX() * 0.55), (int) (getGuiCenterY() * 0.5 + (15 * lineNo)), 0xFF8F8F8F);
    }

    private void drawBox(GuiChest chest, boolean shouldBeRed, boolean isConfirm) {
        GlStateManager.pushMatrix();
        // in front of the items displayed on screen
        GlStateManager.translate(0, 0, 512.0D);
        ItemStack item = getItem(ITEM_SLOT, chest);

        drawTfmBox(isConfirm);
        drawBuyBox(shouldBeRed);
        drawCancelBox();

        if (item != null) {
            drawProfitInfo();
            renderItem(chest, item);
            renderTooltip(chest, item);
        }

        GlStateManager.popMatrix();
    }

    private int getGuiCenterX() {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        return resolution.getScaledWidth() / 2;
    }

    private int getGuiCenterY() {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        return resolution.getScaledHeight() / 2;
    }

    private boolean isInBox(int x, int y) {
        int[] dim = getBuyBoxDimensions();
        return x > dim[0] && x < dim[2]
                && y > dim[3] && y < dim[1];
    }

    private boolean isInCancelBox(int x, int y) {
        int[] dim = getCancelBoxDimensions();
        return x > dim[0] && x < dim[2]
                && y > dim[3] && y < dim[1];
    }

    public void drawBuyButton(GuiChest currentScreen) {
        if (!shouldDrawGui(currentScreen)) {
            return;
        }
        drawBox(currentScreen, waitingForBed(currentScreen), false);
    }

    public boolean shouldDrawGui(GuiChest currentScreen) {
        ItemStack stack = getItem(BUY_BUTTON_SLOT, currentScreen);
        return stack != null && (stack.getItem().equals(Item.getByNameOrId("minecraft:bed")) || stack.getItem().equals(Item.getByNameOrId("minecraft:gold_nugget")));
    }

    public void drawConfirmButton(GuiChest currentScreen) {
        drawBox(currentScreen, false, true);
    }

    private boolean shouldSkip(GuiScreen screen) {
        return !(screen instanceof GuiChest) || CoflSky.config.purchaseOverlay != GUIType.TFM || !EventHandler.isInSkyblock;
    }

    @SubscribeEvent
    public void onPostRenderEvent(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (shouldSkip(event.gui)) {
            return;
        }

        GuiChest auctionView = (GuiChest) event.gui;
        ContainerChest container = (ContainerChest) auctionView.inventorySlots;
        String containerName = container.getLowerChestInventory().getDisplayName().getUnformattedText();
        if (containerName.trim().equals("BIN Auction View")) {
            drawBuyButton(auctionView);
        } else if (containerName.trim().equals("Confirm Purchase")) {
            drawConfirmButton(auctionView);
        }
    }

    private void clickSlot(int windowId, int slot) {
        Minecraft.getMinecraft().playerController.windowClick(windowId,
                slot, 2,3, Minecraft.getMinecraft().thePlayer);
    }

    private void handleBuyClick(GuiChest currentScreen, GuiScreenEvent.MouseInputEvent.Pre event) {
        if (waitingForBed(currentScreen) || !shouldDrawGui(currentScreen)) {
            return;
        }
        event.setCanceled(true);
        clickSlot(currentScreen.inventorySlots.windowId, BUY_BUTTON_SLOT);
    }

    private void handleConfirmClick(GuiChest currentScreen, int windowId) {
        clickSlot(windowId, CONFIRM_BUTTON_SLOT);
    }

    private void handlePositiveClick(GuiScreen screen, GuiScreenEvent.MouseInputEvent.Pre event) {
        if (shouldSkip(screen)) {
            return;
        }

        GuiChest auctionView = (GuiChest) screen;
        ContainerChest container = (ContainerChest) auctionView.inventorySlots;
        String containerName = container.getLowerChestInventory().getDisplayName().getUnformattedText();
        if (containerName.trim().equals("BIN Auction View")) {
            handleBuyClick(auctionView, event);
        } else if (containerName.trim().equals("Confirm Purchase")) {
            handleConfirmClick(auctionView, container.windowId);
            event.setCanceled(true);
        }
    }

    private void doCancelClick(ContainerChest chest, int slotId) {
        clickSlot(chest.windowId, slotId);
    }

    private void handleCancelClick(GuiScreen screen, GuiScreenEvent.MouseInputEvent.Pre event) {
        if (shouldSkip(screen)) {
            return;
        }

        GuiChest auctionView = (GuiChest) screen;
        ContainerChest container = (ContainerChest) auctionView.inventorySlots;
        String containerName = container.getLowerChestInventory().getDisplayName().getUnformattedText();
        if (containerName.trim().equals("BIN Auction View")) {
            doCancelClick(container, BACK_BUTTON_SLOT);
        } else if (containerName.trim().equals("Confirm Purchase")) {
            doCancelClick(container, CANCEL_CONFIRM_SLOT);
        } else {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onMouseClicked(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!Mouse.getEventButtonState()) {
            return;
        }
        GuiScreen screen = event.gui;
        if (screen == null) {
            return;
        }
        int x = Mouse.getEventX() * screen.width / Minecraft.getMinecraft().displayWidth;
        int y = screen.height - Mouse.getEventY() * screen.height / Minecraft.getMinecraft().displayHeight - 1;
        if (isInBox(x, y)) {
            handlePositiveClick(screen, event);
        } else if (isInCancelBox(x, y)) {
            handleCancelClick(screen, event);
        }
    }

}
