package com.coflnet.sky.handlers;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Removed swing KeyBinding import

import CoflCore.handlers.DescriptionHandler;
import com.mojang.realmsclient.util.Pair;
import com.coflnet.sky.SkyCofl;
import com.coflnet.sky.WSCommandHandler;
import com.coflnet.sky.models.ClickableTextElement;
import com.coflnet.sky.config.InfoDisplayConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import CoflCore.commands.Command;
import CoflCore.commands.CommandType;
import CoflCore.commands.JsonStringCommand;
import CoflCore.commands.models.AuctionData;
import CoflCore.commands.models.FlipData;
import CoflCore.commands.models.HotkeyRegister;
import CoflCore.network.WSClient;
import com.coflnet.sky.gui.bingui.helper.RenderUtils;
import com.coflnet.sky.mixins.AccessorGuiEditSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import static CoflCore.CoflCore.config;
import static CoflCore.handlers.DescriptionHandler.*;
import static com.coflnet.sky.handlers.EventHandler.*;

public class EventRegistry {
    public static Pattern chatpattern = Pattern.compile("a^", Pattern.CASE_INSENSITIVE);
    public final ExecutorService chatThreadPool = Executors.newFixedThreadPool(2);
    public final ExecutorService tickThreadPool = Executors.newFixedThreadPool(2);

    @SubscribeEvent
    public void onDisconnectedFromServerEvent(ClientDisconnectionFromServerEvent event) {
        if (CoflCore.CoflCore.Wrapper.isRunning) {
            System.out.println("Disconnected from server");
            CoflCore.CoflCore.Wrapper.stop();
            EventHandler.isInSkyblock = false;
            System.out.println("SkyCofl stopped");
        }
    }

    public static long LastClick = System.currentTimeMillis();
    public static Boolean LastHotkeyState;
    public static Boolean LastEventButtonState;
    private ForgeDescriptionHandler forgeDescriptionHandler;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onMouseEvent(InputEvent.MouseInputEvent event) {
        if (LastEventButtonState != null && Mouse.getEventButtonState() == LastEventButtonState) {
            return;
        }
        LastEventButtonState = Mouse.getEventButtonState();
        onAfterKeyPressed();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyEvent(KeyInputEvent event) {
        if (LastHotkeyState != null && Keyboard.getEventKeyState() == LastHotkeyState) {
            return;
        }
        LastHotkeyState = Keyboard.getEventKeyState();
        onAfterKeyPressed();
    }

    public static void onAfterKeyPressed() {
        if (SkyCofl.keyBindings[0].isPressed()) {
            if (WSCommandHandler.lastOnClickEvent != null) {
                FlipData f = CoflCore.CoflCore.flipHandler.fds.GetLastFlip();
                if (f != null) {
                    WSCommandHandler.Execute("/cofl openauctiongui " + f.Id + " false",
                            Minecraft.getMinecraft().thePlayer);
                }
            }
        }
        if (SkyCofl.keyBindings[1].isKeyDown()) {
            if ((System.currentTimeMillis() - LastClick) >= 300) {

                FlipData f = CoflCore.CoflCore.flipHandler.fds.GetHighestFlip();

                if (f != null) {
                    WSCommandHandler.Execute("/cofl openauctiongui " + f.Id + " true",
                            Minecraft.getMinecraft().thePlayer);
                    EventRegistry.LastViewAuctionUUID = f.Id;
                    EventRegistry.LastViewAuctionInvocation = System.currentTimeMillis();
                    LastClick = System.currentTimeMillis();
                    String command = WSClient.gson.toJson("/viewauction " + f.Id);

                    CoflCore.CoflCore.Wrapper.SendMessage(new JsonStringCommand(CommandType.Clicked, command));
                    WSCommandHandler.Execute("/cofl track besthotkey " + f.Id, Minecraft.getMinecraft().thePlayer);
                } else {
                    // only display message once (if this is the key down event)
                    if (SkyCofl.keyBindings[1].isPressed())
                        WSCommandHandler.Execute("/cofl dialog nobestflip", Minecraft.getMinecraft().thePlayer);
                }
            }
        }
        triggerItemKeys(false);
    }

    public static void triggerItemKeys(boolean checkForKey) {
        if (SkyCofl.keyBindings[2].isPressed() || checkForKey && Keyboard.getEventKey() == SkyCofl.keyBindings[2].getKeyCode()) {
            String toAppend = getContextToAppend();

            WSCommandHandler.Execute("/cofl hotkey upload_item" + toAppend,
                    Minecraft.getMinecraft().thePlayer);
        }
        if (SkyCofl.keyBindings.length <= 3)
            return;
        System.out.println("Checking for additional hotkeys...");
        for (int i = 3; i < SkyCofl.keyBindings.length; i++) {
            if (SkyCofl.keyBindings[i].isPressed()  || checkForKey && Keyboard.getEventKey() == SkyCofl.keyBindings[i].getKeyCode()) {
                String keyName = SkyCofl.keyBindings[i].getKeyDescription();
                String toAppend = getContextToAppend();

                WSCommandHandler.Execute("/cofl hotkey " + keyName + toAppend,
                        Minecraft.getMinecraft().thePlayer);
            }
        }
    }

    private static String getContextToAppend() {
        String toAppend = "";
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return toAppend;
        }

        // Prefer hovered item in open inventory GUIs
        ItemStack relevantItem = null;
        if (mc.currentScreen instanceof GuiContainer) {
            GuiContainer gui = (GuiContainer) mc.currentScreen;
            int mx = Mouse.getX() * gui.width / mc.displayWidth;
            int my = gui.height - Mouse.getY() * gui.height / mc.displayHeight - 1;
            try {
                Slot hovered = null;

                // Try to call getSlotAtPosition if present via reflection
                try {
                    java.lang.reflect.Method m = GuiContainer.class.getMethod("getSlotAtPosition", int.class, int.class);
                    Object res = m.invoke(gui, mx, my);
                    if (res instanceof Slot) hovered = (Slot) res;
                } catch (NoSuchMethodException ignored) {
                    // method not present, will fallback to manual lookup
                }

                // Fallback: iterate slots and compare coordinates
                if (hovered == null) {
                    for (Object o : gui.inventorySlots.inventorySlots) {
                        if (!(o instanceof Slot)) continue;
                        Slot s = (Slot) o;
                        int slotX = gui.guiLeft + s.xDisplayPosition;
                        int slotY = gui.guiTop + s.yDisplayPosition;
                        if (mx >= slotX && mx < slotX + 16 && my >= slotY && my < slotY + 16) {
                            hovered = s;
                            break;
                        }
                    }
                }

                if (hovered != null && hovered.getHasStack()) {
                    relevantItem = hovered.getStack();
                }
            } catch (Throwable ignored) {
            }
        }

        // Fallback to held item
        if (relevantItem == null) {
            relevantItem = mc.thePlayer.getHeldItem();
        }

        if (relevantItem != null && relevantItem.hasTagCompound()) {
            // Get the item's NBT data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NBTTagCompound nbt = relevantItem.serializeNBT();
            try {
                CompressedStreamTools.writeCompressed(nbt, baos);
                String item = Base64.getEncoder().encodeToString(baos.toByteArray());
                toAppend = "|" + item;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No NBT data found for the selected or hovered item.");
        }
        return toAppend;
    }

    @SubscribeEvent
    public void onDimensionLoad(net.minecraftforge.event.world.WorldEvent.Load event) {
        // Callback for when a dimension (world) is loaded, in skyblock used when switching islands
        WSCommandHandler.highlightCoordinates = new int[0][];
    }

    @SideOnly(Side.CLIENT)
    // @SubscribeEvent
    public void DrawOntoGUI(RenderGameOverlayEvent rgoe) {

        if (rgoe.type == ElementType.CROSSHAIRS) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.ingameGUI.drawString(Minecraft.getMinecraft().fontRendererObj,
                    "Flips in Pipeline:" + CoflCore.CoflCore.flipHandler.fds.CurrentFlips(), 0, 0, Integer.MAX_VALUE);
        }
    }

    public static void AddHotKeys(HotkeyRegister[] keys) {
        int defaultHotkeyCount = 3;
        Map<String, Integer> keyMap = new HashMap<String, Integer>();
        for (int i = 0; i < SkyCofl.keyBindings.length; i++) {
            keyMap.put(SkyCofl.keyBindings[i].getKeyDescription(), i);
        }
        // resize the keybindings array
        SkyCofl.keyBindings = java.util.Arrays.copyOf(SkyCofl.keyBindings, defaultHotkeyCount + keys.length);
        for (int i = 0; i < keys.length; i++) {
            int key = Keyboard.getKeyIndex(keys[i].DefaultKey.toUpperCase());
            if(keyMap.containsKey(keys[i].Name))
            {
                continue;
            }
            SkyCofl.keyBindings[i + defaultHotkeyCount] = new KeyBinding(keys[i].Name, key, "SkyCofl (unchangeable)");
            System.out.println("Registered Key: " + keys[i].Name + " with key " + key);
            ClientRegistry.registerKeyBinding(SkyCofl.keyBindings[i + defaultHotkeyCount]);
        }
    }

    public static String ExtractUuidFromInventory(IInventory inventory) {

        ItemStack stack = inventory.getStackInSlot(13);
        if (stack != null) {
            try {
                String uuid = stack.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes")
                        .getString("uuid");
                if (uuid.length() == 0) {
                    throw new Exception();
                }
                return uuid;
            } catch (Exception e) {
                System.out.println(
                        "Clicked item " + stack.getDisplayName() + " has the following meta: " + stack.serializeNBT());
            }
        }
        return "";
    }

    public static ItemStack GOLD_NUGGET = new ItemStack(
            Item.itemRegistry.getObject(new ResourceLocation("minecraft:gold_nugget")));

    public static final Pair<String, Pair<String, LocalDateTime>> EMPTY = Pair.of(null, Pair.of("", LocalDateTime.MIN));
    public static Pair<String, Pair<String, LocalDateTime>> last = EMPTY;

    @SubscribeEvent
    public void HandleChatEvent(ClientChatReceivedEvent sce) {
        CoflCore.handlers.EventRegistry.onChatMessage(sce.message.getUnformattedText());
        String previousHover = null;
        for (IChatComponent component : sce.message.getSiblings()) {
            if(component.getChatStyle().getChatHoverEvent() != null
                    && component.getChatStyle().getChatHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
                String text = component.getChatStyle().getChatHoverEvent().getValue().getUnformattedText();
                if (text.equals(previousHover))
                    continue; // skip if the text is the same as the previous one, different colored text often has the same hover text
                previousHover = text;
                CoflCore.handlers.EventRegistry.onChatMessage(text);
            }
        }
    }

    public static long lastStartTime = Long.MIN_VALUE;

    public static long LastViewAuctionInvocation = Long.MIN_VALUE;
    public static String LastViewAuctionUUID = null;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void OnGuiClick(GuiScreenEvent.MouseInputEvent mie) {
        if (!CoflCore.CoflCore.Wrapper.isRunning)
            return;
        if (!(mie.gui instanceof GuiChest))
            return; // verify that it's really a chest
        if (!(((GuiChest) mie.gui).inventorySlots instanceof ContainerChest))
            return;
        ContainerChest chest = (ContainerChest) ((GuiChest) mie.gui).inventorySlots;
        IInventory inv = chest.getLowerChestInventory();
        if (inv.hasCustomName()) { // verify that the chest actually has a custom name
            String chestName = inv.getName();
            if (chestName.equalsIgnoreCase("BIN Auction View")) {

                ItemStack heldItem = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();

                if (heldItem != null) {
                    System.out.println("Clicked on: " + heldItem.getItem().getRegistryName());

                    String itemUUID = ExtractUuidFromInventory(inv);

                    if (System.currentTimeMillis() > lastStartTime) {

                        if (heldItem.isItemEqual(GOLD_NUGGET)) {
                            AuctionData ad = new AuctionData();
                            ad.setItemId(itemUUID);

                            if ((LastViewAuctionInvocation + 60 * 1000) >= System.currentTimeMillis()) {
                                ad.setAuctionId(LastViewAuctionUUID);
                            } else {
                                ad.setAuctionId("");
                            }

                            Command<AuctionData> data = new Command<>(CommandType.PurchaseStart, ad);
                            CoflCore.CoflCore.Wrapper.SendMessage(data);
                            System.out.println("PurchaseStart");
                            last = Pair.of("You claimed ", Pair.of(itemUUID, LocalDateTime.now()));
                            lastStartTime = System.currentTimeMillis() + 200 /* ensure a small debounce */;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void OnRenderTick(TickEvent.RenderTickEvent event) {
        com.coflnet.sky.minecraft_integration.CountdownTimer.onRenderTick(event);
    }

    @SubscribeEvent
    public void highlightChests(DrawBlockHighlightEvent e){
        if (WSCommandHandler.highlightCoordinates.length > 0) {
            RenderUtils.renderWaypointHighlightBoxes(WSCommandHandler.highlightCoordinates);
        }
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();

        // Check if the current GUI screen is the player's inventory or a chest GUI
        if (event.gui instanceof GuiContainer) {
            // Handle dragging during rendering for real-time updates
            if (isDragging) {
                int mouseX = Mouse.getX() * event.gui.width / Minecraft.getMinecraft().displayWidth;
                int mouseY = event.gui.height - Mouse.getY() * event.gui.height / Minecraft.getMinecraft().displayHeight - 1;
                
                // Update text offset based on mouse movement
                int deltaX = mouseX - dragStartX;
                int deltaY = mouseY - dragStartY;
                
                textOffsetX = deltaX;
                textOffsetY = deltaY;
            }
            
            DescriptionHandler.DescModification[] toDisplay = DescriptionHandler.getInfoDisplay();
            if(toDisplay.length == 0)
                return; // No info to display, exit early
            GuiContainer inventoryGui = (GuiContainer) event.gui;
            FontRenderer fontRenderer = mc.fontRendererObj;

            // --- Get the actual rendered top-left position of the inventory GUI ---
            // 'guiLeft' and 'guiTop' are protected but directly accessible from the instance
            // (or via a getter if one existed, but direct access is common for protected fields in mods).
            // This position accounts for potion effect shifts.
            int inventoryGuiLeft = inventoryGui.guiLeft;
            int inventoryGuiTop = inventoryGui.guiTop;

            ArrayList<String> lines = new ArrayList<>();
            ArrayList<List<ClickableTextElement>> clickableLines = new ArrayList<>();
            int maxWidth = 0;
            for(DescriptionHandler.DescModification mod : toDisplay) {
                if (mod != null && mod.value != null) {
                    if(mod.type.equals("APPEND")) {
                        // Check if the value is JSON for clickable text
                        List<ClickableTextElement> clickableElements = parseClickableText(mod.value);
                        if (clickableElements != null && !clickableElements.isEmpty()) {
                            // Calculate combined width of all clickable elements
                            int totalWidth = 0;
                            for (ClickableTextElement element : clickableElements) {
                                if (element.getText() != null) {
                                    totalWidth += fontRenderer.getStringWidth(element.getText());
                                }
                            }
                            lines.add(""); // Empty line placeholder for clickable content
                            clickableLines.add(clickableElements);
                            if (totalWidth > maxWidth) {
                                maxWidth = totalWidth;
                            }
                        } else {
                            // Regular text
                            lines.add(mod.value);
                            clickableLines.add(null);
                            int width = fontRenderer.getStringWidth(mod.value);
                            if (width > maxWidth) {
                                maxWidth = width;
                            }
                        }
                    }
                    else if(mod.type.equals("SUGGEST")) {
                        String displayText = "§7Will suggest: §r" + mod.value.split(": ")[1];
                        lines.add(displayText);
                        clickableLines.add(null);
                        int width = fontRenderer.getStringWidth(displayText);
                        if (width > maxWidth) {
                            maxWidth = width;
                        }
                    }
                }
            }

            // Start position for the text on the left.
            // (inventoryGuiLeft - padding - maxTextWidth) will place the right edge of the text
            // 'padding' pixels to the left of the inventory's left edge.
            int defaultTextX = inventoryGuiLeft - 5 - maxWidth;
            int defaultTextY = inventoryGuiTop + 5;
            if (inventoryGui instanceof GuiInventory) {
                defaultTextY += 30;
            }
            
            // Apply user-defined offset for repositioning
            int textX = defaultTextX + textOffsetX;
            int textY = defaultTextY + textOffsetY;

            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.enableBlend(); // Enable blending for transparency
            net.minecraft.client.renderer.GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // Standard alpha blend function
            
            int lineCount = lines.size();
            int textHeight = lineCount * (fontRenderer.FONT_HEIGHT + 2);
            
            // Check if mouse is over text area for drag indicator
            int mouseX = Mouse.getX() * event.gui.width / Minecraft.getMinecraft().displayWidth;
            int mouseY = event.gui.height - Mouse.getY() * event.gui.height / Minecraft.getMinecraft().displayHeight - 1;
            boolean isMouseOverText = isMouseOverTextArea(mouseX, mouseY, textX, textY, maxWidth, textHeight);
            
            for(int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                List<ClickableTextElement> clickableElements = clickableLines.get(i);
                int currentY = textY + (fontRenderer.FONT_HEIGHT + 2) * i;
                
                if (clickableElements != null && !clickableElements.isEmpty()) {
                    renderClickableTextLine(clickableElements, textX, currentY, fontRenderer, event);
                } else if (line != null && !line.isEmpty()) {
                    fontRenderer.drawString(line, textX, currentY, 0xFFFFFFFF, true); // White
                }
            }
            
            net.minecraft.client.renderer.GlStateManager.disableBlend(); // Disable blending
            net.minecraft.client.renderer.GlStateManager.popMatrix();
            
            // Render tooltip immediately after text rendering to ensure it's on top but doesn't affect click detection
            if (currentHoverText != null && !currentHoverText.isEmpty()) {
                String[] tooltipLineArray = currentHoverText.split("\\n");
                List<String> tooltipLines = Arrays.asList(tooltipLineArray);
                
                drawHoverTooltip(tooltipLines, hoverX, hoverY, event.gui.width, event.gui.height);
            }
        }
        
        currentHoverText = null;
    }

    /**
     * Parses a string that might contain JSON for clickable text elements
     * @param value The string value that might be JSON
     * @return List of ClickableTextElement if valid JSON, null otherwise
     */
    private List<ClickableTextElement> parseClickableText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = value.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return null;
        }
        
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ClickableTextElement>>(){}.getType();
            return gson.fromJson(value, listType);
        } catch (Exception e) {
            // If parsing fails, return null to fall back to regular text
            return null;
        }
    }

    /**
     * Renders a line of clickable text elements
     * @param elements List of clickable text elements to render
     * @param startX Starting X position
     * @param y Y position for the line
     * @param fontRenderer Font renderer to use
     * @param event GUI event for mouse interaction handling
     */
    private void renderClickableTextLine(List<ClickableTextElement> elements, int startX, int y, FontRenderer fontRenderer, GuiScreenEvent.DrawScreenEvent.Post event) {
        int currentX = startX;
        
        for (ClickableTextElement element : elements) {
            if (element.getText() == null || element.getText().isEmpty()) {
                continue;
            }
            
            String text = element.getText();
            int textWidth = fontRenderer.getStringWidth(text);
            
            // Check if mouse is hovering over this text element
            int mouseX = Mouse.getX() * event.gui.width / Minecraft.getMinecraft().displayWidth;
            int mouseY = event.gui.height - Mouse.getY() * event.gui.height / Minecraft.getMinecraft().displayHeight - 1;
            
            boolean isHovered = mouseX >= currentX && mouseX <= currentX + textWidth && 
                               mouseY >= y && mouseY <= y + fontRenderer.FONT_HEIGHT;
            
            // Draw the text with underline if hovered and clickable
            int color = 0xFFFFFFFF; // Default white
            if (element.getOnClick() != null && !element.getOnClick().isEmpty()) {
                color = isHovered ? 0xFFFFFF00 : 0xFF55FFFF; // Yellow when hovered, cyan when clickable
            }
            
            fontRenderer.drawString(text, currentX, y, color, true);
            
            // Draw underline if hovered and clickable
            if (isHovered && element.getOnClick() != null && !element.getOnClick().isEmpty()) {
                net.minecraft.client.gui.Gui.drawRect(currentX, y + fontRenderer.FONT_HEIGHT, 
                                                    currentX + textWidth, y + fontRenderer.FONT_HEIGHT + 1, 
                                                    0xFFFFFF00);
            }
            
            if (element.getOnClick() != null && !element.getOnClick().isEmpty()) {
                storeClickableArea(currentX, y, textWidth, fontRenderer.FONT_HEIGHT, element.getOnClick());
            }
            
            if (element.getHover() != null && !element.getHover().isEmpty()) {
                storeHoverArea(currentX, y, textWidth, fontRenderer.FONT_HEIGHT, element.getHover(), isHovered);
            }
            
            currentX += textWidth;
        }
    }

    // Storage for clickable areas
    private static class ClickableArea {
        public int x, y, width, height;
        public String command;
        
        public ClickableArea(int x, int y, int width, int height, String command) {
            this.x = x; this.y = y; this.width = width; this.height = height; this.command = command;
        }
        
        public boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
    
    private static final List<ClickableArea> clickableAreas = new ArrayList<>();
    private static String currentHoverText = null;
    private static int hoverX = 0, hoverY = 0;
    
    // Dragging state for repositioning text
    private static boolean isDragging = false;
    private static int dragStartX = 0, dragStartY = 0;
    private static int textOffsetX = InfoDisplayConfig.getTextOffsetX();
    private static int textOffsetY = InfoDisplayConfig.getTextOffsetY();
    
    private void storeClickableArea(int x, int y, int width, int height, String command) {
        clickableAreas.add(new ClickableArea(x, y, width, height, command));
    }
    
    private void storeHoverArea(int x, int y, int width, int height, String hoverText, boolean isHovered) {
        if (isHovered) {
            currentHoverText = hoverText;
            hoverX = x + width / 2;
            hoverY = y;
        }
    }
    
    /**
     * Checks if the mouse is over the text display area
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param textX Text area X position
     * @param textY Text area Y position
     * @param textWidth Text area width
     * @param textHeight Text area height
     * @return true if mouse is over the text area
     */
    private boolean isMouseOverTextArea(int mouseX, int mouseY, int textX, int textY, int textWidth, int textHeight) {
        return mouseX >= textX && mouseX <= textX + textWidth && 
               mouseY >= textY && mouseY <= textY + textHeight;
    }

    @SubscribeEvent
    public void onInfoDisplayMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!(event.gui instanceof GuiContainer)) {
            return;
        }
        
        int mouseX = Mouse.getX() * event.gui.width / Minecraft.getMinecraft().displayWidth;
        int mouseY = event.gui.height - Mouse.getY() * event.gui.height / Minecraft.getMinecraft().displayHeight - 1;
        
        // Handle left click for clickable text
        if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0) {
            for (ClickableArea area : clickableAreas) {
                if (area.contains(mouseX, mouseY)) {
                    handleClickableTextClick(area.command);
                    event.setCanceled(true);
                    return;
                }
            }
        }
        
        // Handle right click for dragging
        if (Mouse.getEventButton() == 1) { // Right mouse button
            if (Mouse.getEventButtonState()) {
                DescriptionHandler.DescModification[] toDisplay = DescriptionHandler.getInfoDisplay();
                if (toDisplay.length > 0) {
                    GuiContainer inventoryGui = (GuiContainer) event.gui;
                    FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                    
                    // Calculate text area dimensions (same logic as in onGuiDraw)
                    int maxWidth = 0;
                    int lineCount = 0;
                    for(DescriptionHandler.DescModification mod : toDisplay) {
                        if (mod != null && mod.value != null && (mod.type.equals("APPEND") || mod.type.equals("SUGGEST"))) {
                            lineCount++;
                            String displayText = mod.type.equals("SUGGEST") ? 
                                "§7Will suggest: §r" + mod.value.split(": ")[1] : mod.value;
                            
                            List<ClickableTextElement> clickableElements = parseClickableText(mod.value);
                            if (clickableElements != null && !clickableElements.isEmpty()) {
                                int totalWidth = 0;
                                for (ClickableTextElement element : clickableElements) {
                                    if (element.getText() != null) {
                                        totalWidth += fontRenderer.getStringWidth(element.getText());
                                    }
                                }
                                if (totalWidth > maxWidth) {
                                    maxWidth = totalWidth;
                                }
                            } else {
                                int width = fontRenderer.getStringWidth(displayText);
                                if (width > maxWidth) {
                                    maxWidth = width;
                                }
                            }
                        }
                    }
                    
                    int defaultTextX = inventoryGui.guiLeft - 5 - maxWidth;
                    int defaultTextY = inventoryGui.guiTop + 5;
                    if (inventoryGui instanceof GuiInventory) {
                        defaultTextY += 30;
                    }
                    
                    int textX = defaultTextX + textOffsetX;
                    int textY = defaultTextY + textOffsetY;
                    int textHeight = lineCount * (fontRenderer.FONT_HEIGHT + 2);
                    
                    if (isMouseOverTextArea(mouseX, mouseY, textX, textY, maxWidth, textHeight)) {
                        isDragging = true;
                        dragStartX = mouseX;
                        dragStartY = mouseY;
                        event.setCanceled(true);
                    }
                }
            } else { // Button released
                if (isDragging) {
                    isDragging = false;
                    // Save the new position to config
                    InfoDisplayConfig.setTextOffset(textOffsetX, textOffsetY);
                    event.setCanceled(true);
                }
            }
        }
    }
    
    /**
     * Resets the text position to default
     */
    public static void resetTextPosition() {
        textOffsetX = 0;
        textOffsetY = 0;
        isDragging = false;
        InfoDisplayConfig.resetTextOffset();
    }
    
    /**
     * Gets the current text offset for external use
     * @return array with [offsetX, offsetY]
     */
    public static int[] getTextOffset() {
        return new int[]{textOffsetX, textOffsetY};
    }
    
    /**
     * Sets the text offset for external use
     * @param offsetX X offset
     * @param offsetY Y offset
     */
    public static void setTextOffset(int offsetX, int offsetY) {
        textOffsetX = offsetX;
        textOffsetY = offsetY;
        InfoDisplayConfig.setTextOffset(offsetX, offsetY);
    }
    
    private void handleClickableTextClick(String command) {
            System.err.println("Clicked command: " + command);
        if (command == null || command.isEmpty()) {
            return;
        }
        
        try {
            // Execute the command using WSCommandHandler
            WSCommandHandler.Execute(command, Minecraft.getMinecraft().thePlayer);
        } catch (Exception e) {
            System.err.println("Failed to execute clickable text command: " + command);
            e.printStackTrace();
        }
    }
    
    private void drawHoverTooltip(List<String> lines, int x, int y, int screenWidth, int screenHeight) {
        if (lines.isEmpty()) {
            return;
        }
        
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        
        // Calculate tooltip dimensions
        int maxWidth = 0;
        for (String line : lines) {
            int width = fontRenderer.getStringWidth(line);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        
        int tooltipHeight = 8;
        if (lines.size() > 1) {
            tooltipHeight += (lines.size() - 1) * 10;
        }
        
        // Position tooltip to avoid going off screen
        int tooltipX = x + 12;
        int tooltipY = y - 12;
        
        if (tooltipX + maxWidth + 4 > screenWidth) {
            tooltipX = x - maxWidth - 16;
        }
        if (tooltipY + tooltipHeight + 6 > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - 6;
        }
        if (tooltipY < 4) {
            tooltipY = 4;
        }
        if (tooltipX < 4) {
            tooltipX = 4;
        }
        
        // Set up OpenGL state for tooltip rendering in front
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.disableLighting();
        net.minecraft.client.renderer.GlStateManager.disableDepth();
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        // Translate to ensure tooltip is rendered on top
        net.minecraft.client.renderer.GlStateManager.translate(0.0F, 0.0F, 300.0F);
        
        // Draw tooltip background
        final int backgroundColor = 0xF0100010;
        final int borderColorStart = 0x505000FF;
        final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        
        // Background
        net.minecraft.client.gui.Gui.drawRect(tooltipX - 3, tooltipY - 4, tooltipX + maxWidth + 3, tooltipY - 3, backgroundColor);
        net.minecraft.client.gui.Gui.drawRect(tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor);
        net.minecraft.client.gui.Gui.drawRect(tooltipX - 3, tooltipY - 3, tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor);
        net.minecraft.client.gui.Gui.drawRect(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor);
        net.minecraft.client.gui.Gui.drawRect(tooltipX + maxWidth + 3, tooltipY - 3, tooltipX + maxWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor);
        
        // Borders
        net.minecraft.client.gui.Gui.drawRect(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart);
        net.minecraft.client.gui.Gui.drawRect(tooltipX + maxWidth + 2, tooltipY - 3 + 1, tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorEnd);
        net.minecraft.client.gui.Gui.drawRect(tooltipX - 3, tooltipY - 3, tooltipX + maxWidth + 3, tooltipY - 3 + 1, borderColorStart);
        net.minecraft.client.gui.Gui.drawRect(tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd);
        
        // Draw text lines
        int currentTooltipY = tooltipY;
        for (int lineNumber = 0; lineNumber < lines.size(); ++lineNumber) {
            String line = lines.get(lineNumber);
            fontRenderer.drawStringWithShadow(line, tooltipX, currentTooltipY, -1);
            currentTooltipY += 10;
        }
        
        // Restore OpenGL state
        net.minecraft.client.renderer.GlStateManager.disableBlend();
        net.minecraft.client.renderer.GlStateManager.enableDepth();
        net.minecraft.client.renderer.GlStateManager.enableLighting();
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }

    long UpdateThisTick = 0;

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onTick(TickEvent.ClientTickEvent event) {
        UpdateThisTick++;
        if (UpdateThisTick % 200 == 0) {
            tickThreadPool.submit(() -> {
                try {
                    ScoreboardData();
                    TabMenuData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent event) {

        // if gui is null, a gui was closed
        // therefore clear the lastClickFlipMessage, so it doesn't show on other
        // auctions
        if (event.gui == null) {
            CoflCore.CoflCore.flipHandler.lastClickedFlipMessage = "";
        }

        if (event.gui instanceof GuiEditSign) {
            GuiEditSign signGui = (GuiEditSign) event.gui;
            DescriptionHandler.DescModification[] toDisplay = DescriptionHandler.getInfoDisplay();
            for (DescriptionHandler.DescModification mod : toDisplay) {
                if (mod == null || !mod.type.equals("SUGGEST")) {
                    continue;
                }
                String[] parts = mod.value.split(": ");
                if (parts.length < 2)
                    return;
                try {
                    TileEntitySign tileEntitySign = ((AccessorGuiEditSign) signGui).getTileSign();
                    if(tileEntitySign.signText[3].getUnformattedText().contains(parts[0]))
                        tileEntitySign.signText[0] = new ChatComponentText(parts[1]);
                } catch (RuntimeException e) {
                    System.err.println("Failed to access tileSign field in GuiEditSign: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return;
        }

        if (!config.extendedtooltips)
            return;
        if (forgeDescriptionHandler != null)
            forgeDescriptionHandler.Close();
        if (event.gui == null)
            emptyTooltipData();

        if (!(event.gui instanceof GuiContainer))
            return;
        new Thread(() -> {
            try {
                forgeDescriptionHandler = new ForgeDescriptionHandler();
                forgeDescriptionHandler.loadDescriptionAndListenForChanges(event);
            } catch (Exception e) {
                System.out.println("failed to update description " + e);
            }
        }).start();
    }

    @SubscribeEvent
    public void onBackgroundRenderDone(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (forgeDescriptionHandler != null)
            forgeDescriptionHandler.highlightSlots(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemTooltipEvent(ItemTooltipEvent event) {
        if (!config.extendedtooltips)
            return;
        if (forgeDescriptionHandler == null)
            return;
        forgeDescriptionHandler.setTooltips(event);
    }
}
