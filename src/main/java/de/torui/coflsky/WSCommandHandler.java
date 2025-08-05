package de.torui.coflsky;

import CoflCore.events.*;
import com.google.gson.Gson;

import CoflCore.commands.Command;
import CoflCore.commands.RawCommand;
import CoflCore.commands.models.*;
import de.torui.coflsky.gui.bingui.BinGuiManager;
import de.torui.coflsky.handlers.ForgeDescriptionHandler;
import de.torui.coflsky.handlers.EventRegistry;
import CoflCore.proxy.ProxyManager;
import de.torui.coflsky.utils.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.greenrobot.eventbus.Subscribe;
import CoflCore.CoflCore;

import java.io.File;

import java.util.Arrays;
import java.util.stream.Stream;

public class WSCommandHandler {

    public static transient String lastOnClickEvent;
    private static final ModListData modListData = new ModListData();
    private static final Gson gson = new Gson();
    private static final ProxyManager proxyManager = new ProxyManager();
    public static int[][] highlightCoordinates = new int[0][];

    @Subscribe
    public void onReceiveCommand(ReceiveCommand event){
        // called on every command
    }

    @Subscribe
    public void onFlipReceive(OnFlipReceive event){
        // handle chat message

        ChatMessageData[] messages = event.FlipData.Messages;
        SoundData sound = event.FlipData.Sound;
        if (sound != null && sound.Name != null) {
            PlaySound(sound.Name, sound.Pitch);
        }
        ChatMessage(messages);
        CoflCore.flipHandler.fds.Insert(event.FlipData);
        // trigger the onAfterHotkeyPressed function to open the flip if the correct
        // hotkey is currently still pressed
        EventRegistry.onAfterKeyPressed();
    }
    @Subscribe
    public void onChatMessage(OnChatMessageReceive event){
        ChatMessage(event.ChatMessages);
    }

    @Subscribe
    public void onModChatMessage(OnModChatMessage event){
        ChatMessage(new ChatMessageData[]{
                new ChatMessageData(event.message, null,null)
        });
    }

    @Subscribe
    public void onChatMessageDataReceive(OnWriteToChatReceive event){
        ChatMessage(new ChatMessageData[]{
                event.ChatMessage
        });
    }

    @Subscribe
    public void onPlaySoundReceive(OnPlaySoundReceive event){
        if(event.Sound == null || event.Sound.getSoundName() == null) return;

        String soundName = event.Sound.getSoundName();
        float pitch = event.Sound.getSoundPitch();
        PlaySound(soundName, pitch);
    }

    @Subscribe
    public void onCountdownReceive(OnCountdownReceive event){
        de.torui.coflsky.minecraft_integration.CountdownTimer.startCountdown(event.CountdownData);
    }

    @Subscribe
    public void onOpenAuctionGUI(OnOpenAuctionGUI event){
        if(event.flip == null)
        {
            // open only the gui without any flip data
            BinGuiManager.openNewFlipGui("", "simulated-item-0000-0000-0000-000000000000");
            Minecraft.getMinecraft().thePlayer.sendChatMessage(event.openAuctionCommand);
            return;
        }
        BinGuiManager.openNewFlipGui(event.flip.getMessageAsString().replaceAll("\n", "")
                .split(",§7 sellers ah")[0], event.flip.Render);
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/viewauction " + event.flip.Id);
    }

    @Subscribe
    public void onExecuteCommand(OnExecuteCommand event){
        Execute(event.Command, Minecraft.getMinecraft().thePlayer);
    }
    @Subscribe
    public void onCloseGUI(OnCloseGUI event){
        // close the current open gui if any
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) {
            mc.displayGuiScreen(null);
        }
    }

    @Subscribe
    public void onGetInventory(OnGetInventory event) {
        ForgeDescriptionHandler.uploadInventory();
    }
    @Subscribe
    public void onHighlightBlocks(OnHighlightBlocks event) {
        highlightCoordinates = new int[event.positions.size()][];
        for (int i = 0; i < event.positions.size(); i++) {
            highlightCoordinates[i] = new int[]{
                    event.positions.get(i).getX(),
                    event.positions.get(i).getY(),
                    event.positions.get(i).getZ()
            };
        }
    }

    private static void handleProxyRequest(ProxyRequest[] request) {
        for (ProxyRequest req : request) {
            proxyManager.handleRequestAsync(req);
        }
    }

    public static void cacheMods() {
        File modFolder = new File(Minecraft.getMinecraft().mcDataDir, "mods");
        for (File mods : modFolder.listFiles()) {
            if (mods.isDirectory())
                continue;
            modListData.addFilename(mods.getName());
            try {
                modListData.addFileHashes(FileUtils.getSha256Checksum(mods));
            } catch (Exception exception) {
                // Highly less likely to happen unless something goes wrong
                exception.printStackTrace();
            }
        }

        for (ModContainer mod : Loader.instance().getModList()) {
            modListData.addModname(mod.getName());
            modListData.addModname(mod.getModId());
        }
    }

    private static void getMods() {
        // the Cofl server has asked for an mod list now let's respond with all the info
        CoflCore.Wrapper.SendMessage(new RawCommand("foundMods", gson.toJson(modListData)));
    }

    private static void PlaySound(String soundName, float pitch) {
        SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();

        // random.explode
        PositionedSoundRecord psr = PositionedSoundRecord
                .create(new ResourceLocation(soundName), pitch);

        handler.playSound(psr);
    }

    private static void Execute(Command<String> cmd, Entity sender) {
        System.out.println("Execute: " + cmd.getData() + " sender:" + sender);
        // String dummy = WSClient.gson.fromJson(cmd.getData(), String.class);
        Execute(cmd.getData(), sender);
    }


    public static void Execute(String cmd, Entity sender) {
        if (cmd.startsWith("/cofl") || cmd.startsWith("http")) {
            ClientCommandHandler.instance.executeCommand(sender, cmd);
        } else {
            Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
        }
    }

    private static IChatComponent CommandToChatComponent(ChatMessageData wcmd, String fullMessage) {
        if (wcmd.OnClick != null) {
            if (wcmd.Text != null && wcmd.OnClick.startsWith("/viewauction")) {
                lastOnClickEvent = "/cofl openauctiongui " + wcmd.OnClick.split(" ")[1] + " false";
            } else {
                lastOnClickEvent = "/cofl callback " + wcmd.OnClick;
            }
        }
        if (wcmd.Text != null) {
            IChatComponent comp = new ChatComponentText(wcmd.Text);

            ChatStyle style;
            if (wcmd.OnClick != null) {
                if (wcmd.OnClick.startsWith("http")) {
                    style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, wcmd.OnClick));
                } else if (wcmd.OnClick.startsWith("suggest:")) {
                    style = new ChatStyle()
                            .setChatClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, wcmd.OnClick.substring(8)));
                } else if (wcmd.OnClick.startsWith("copy:")) {
                    style = new ChatStyle().setChatClickEvent(
                            new ClickEvent(Action.RUN_COMMAND, "/cofl copyToClipboard " + wcmd.OnClick.substring(5)));
                } else {
                    style = new ChatStyle()
                            .setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, lastOnClickEvent));
                }
                comp.setChatStyle(style);
            }

            if (wcmd.Hover != null && !wcmd.Hover.isEmpty()) {
                if (comp.getChatStyle() == null)
                    comp.setChatStyle(new ChatStyle());
                comp.getChatStyle().setChatHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(wcmd.Hover)));
            }
            return comp;
        }
        return null;
    }

    public static void sendChatMessage(IChatComponent message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(message);
    }

    public static IChatComponent ChatMessage(ChatMessageData[] array) {
        IChatComponent master = new ChatComponentText("");
        String fullMessage = ChatMessageDataToString(array);

        for (ChatMessageData wcmd : array) {
            IChatComponent comp = CommandToChatComponent(wcmd, fullMessage);
            if (comp != null)
                master.appendSibling(comp);
        }
        Minecraft.getMinecraft().thePlayer.addChatMessage(master);
        return master;
    }

    private static void WriteToChat(Command<ChatMessageData> cmd) {
        ChatMessageData wcmd = cmd.getData();

        IChatComponent comp = CommandToChatComponent(wcmd, wcmd.Text);
        if (comp != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
        }

    }

    public static String ChatMessageDataToString(ChatMessageData[] messages) {
        Stream<String> stream = Arrays.stream(messages).map(message -> message.Text);
        String s = String.join(",", stream.toArray(String[]::new));
        stream.close();
        return s;
    }

}
