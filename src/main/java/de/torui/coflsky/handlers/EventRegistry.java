package de.torui.coflsky.handlers;

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
import de.torui.coflsky.CoflSky;
import de.torui.coflsky.WSCommandHandler;
import CoflCore.commands.Command;
import CoflCore.commands.CommandType;
import CoflCore.commands.JsonStringCommand;
import CoflCore.commands.models.AuctionData;
import CoflCore.commands.models.FlipData;
import CoflCore.commands.models.HotkeyRegister;
import CoflCore.network.WSClient;
import de.torui.coflsky.gui.bingui.helper.RenderUtils;
import de.torui.coflsky.mixins.AccessorGuiEditSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.event.HoverEvent;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
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
import static de.torui.coflsky.handlers.EventHandler.*;

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
            System.out.println("CoflSky stopped");
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
        if (CoflSky.keyBindings[0].isPressed()) {
            if (WSCommandHandler.lastOnClickEvent != null) {
                FlipData f = CoflCore.CoflCore.flipHandler.fds.GetLastFlip();
                if (f != null) {
                    WSCommandHandler.Execute("/cofl openauctiongui " + f.Id + " false",
                            Minecraft.getMinecraft().thePlayer);
                }
            }
        }
        if (CoflSky.keyBindings[1].isKeyDown()) {
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
                    if (CoflSky.keyBindings[1].isPressed())
                        WSCommandHandler.Execute("/cofl dialog nobestflip", Minecraft.getMinecraft().thePlayer);
                }
            }
        }
        if (CoflSky.keyBindings[2].isPressed()) {
            String toAppend = getContextToAppend();

            WSCommandHandler.Execute("/cofl hotkey upload_item" + toAppend,
                    Minecraft.getMinecraft().thePlayer);
        }
        if (CoflSky.keyBindings.length <= 3)
            return;
        for (int i = 3; i < CoflSky.keyBindings.length; i++) {
            if (CoflSky.keyBindings[i].isPressed()) {
                String keyName = CoflSky.keyBindings[i].getKeyDescription();
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

        // Get currently selected item
        ItemStack heldItem = mc.thePlayer.getHeldItem();

        if (heldItem != null && heldItem.hasTagCompound()) {
            // Get the item's NBT data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NBTTagCompound nbt = heldItem.serializeNBT();
            try {
                CompressedStreamTools.writeCompressed(nbt, baos);
                String item = Base64.getEncoder().encodeToString(baos.toByteArray());
                toAppend = "|" + item;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No NBT data found for the selected item.");
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
        for (int i = 0; i < CoflSky.keyBindings.length; i++) {
            keyMap.put(CoflSky.keyBindings[i].getKeyDescription(), i);
        }
        // resize the keybindings array
        CoflSky.keyBindings = java.util.Arrays.copyOf(CoflSky.keyBindings, defaultHotkeyCount + keys.length);
        for (int i = 0; i < keys.length; i++) {
            int key = Keyboard.getKeyIndex(keys[i].DefaultKey.toUpperCase());
            if(keyMap.containsKey(keys[i].Name))
            {
                continue;
            }
            CoflSky.keyBindings[i + defaultHotkeyCount] = new KeyBinding(keys[i].Name, key, "SkyCofl (unchangeable)");
            System.out.println("Registered Key: " + keys[i].Name + " with key " + key);
            ClientRegistry.registerKeyBinding(CoflSky.keyBindings[i + defaultHotkeyCount]);
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
        de.torui.coflsky.minecraft_integration.CountdownTimer.onRenderTick(event);
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

            // --- Define your info text lines ---
            ArrayList<String> lines = new ArrayList<>();
            int maxWidth = 0;
            for(DescriptionHandler.DescModification mod : toDisplay) {
                if (mod != null && mod.value != null) {
                    if(mod.type.equals("APPEND")) {
                        lines.add(mod.value);
                        int width = fontRenderer.getStringWidth(mod.value);
                        if (width > maxWidth) {
                            maxWidth = width;
                        }
                    }
                    else if(mod.type.equals("SUGGEST")) {
                        lines.add("§7Will suggest: §r" + mod.value.split(": ")[1]);
                    }
                }
            }

            // Start position for the text on the left.
            // (inventoryGuiLeft - padding - maxTextWidth) will place the right edge of the text
            // 'padding' pixels to the left of the inventory's left edge.
            int textX = inventoryGuiLeft - 10 - maxWidth; // 10 pixels padding to the left
            int textY = inventoryGuiTop + 10; // 10 pixels down from the top of the inventory

            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.enableBlend(); // Enable blending for transparency
            net.minecraft.client.renderer.GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // Standard alpha blend function
            for(int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                // Draw each line with a vertical offset
                fontRenderer.drawString(line, textX, textY + (fontRenderer.FONT_HEIGHT + 2) * i, 0xFFFFFFFF, true); // White
            }
            net.minecraft.client.renderer.GlStateManager.disableBlend(); // Disable blending
            net.minecraft.client.renderer.GlStateManager.popMatrix();
        }
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
