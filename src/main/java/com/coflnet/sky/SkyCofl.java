package com.coflnet.sky;


import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import CoflCore.CoflCore;
import com.google.gson.Gson;
import de.torui.CoflCore.CoflCore.configuration.LocalConfig;
import CoflCore.configuration.GUIType;
import com.coflnet.sky.handlers.EventRegistry;
import com.coflnet.sky.listeners.ChatListener;
import com.coflnet.sky.gui.tfm.ButtonRemapper;
import com.coflnet.sky.gui.tfm.ChatMessageSendHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;
import CoflCore.network.WSClientWrapper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = SkyCofl.MODID, version = SkyCofl.VERSION)
public class SkyCofl {
    // this is used as the forge id and has to be lowercase
    public static final String MODID = "skycofl";
    public static final String VERSION = "1.7.9";

    public static KeyBinding[] keyBindings;

    public static EventRegistry Events;
    public static File configFile;
    private File coflDir;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        String configString = null;
        Gson gson = new Gson();
        coflDir = new File(event.getModConfigurationDirectory(), "SkyCofl");
        coflDir.mkdirs();
        configFile = new File(coflDir, "config.json");
        CoflCore cofl = new CoflCore();
        cofl.init(coflDir.getAbsoluteFile().toPath());
        cofl.registerEventFile(new WSCommandHandler());

        MinecraftForge.EVENT_BUS.register(new ChatListener());
        MinecraftForge.EVENT_BUS.register(new com.coflnet.sky.listeners.SellProtectionListener());
        MinecraftForge.EVENT_BUS.register(new com.coflnet.sky.handlers.SellProtectionTooltipHandler());

        // Cache all the mods on load
        WSCommandHandler.cacheMods();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        keyBindings = new KeyBinding[]{
                new KeyBinding("key.replay_last.onclick", Keyboard.KEY_NONE, "SkyCofl"),
                new KeyBinding("key.start_highest_bid", Keyboard.KEY_NONE, "SkyCofl"),
                new KeyBinding("key.upload_selected", Keyboard.KEY_U, "SkyCofl")
        };

        if (event.getSide() == Side.CLIENT) {
            ClientCommandHandler.instance.registerCommand(new SkyCoflCommand());
            ClientCommandHandler.instance.registerCommand(new ColfCommand());
            ClientCommandHandler.instance.registerCommand(new FlipperChatCommand());
            ClientCommandHandler.instance.registerCommand(new BazaarSearchCommand());

            for (int i = 0; i < keyBindings.length; ++i) {
                ClientRegistry.registerKeyBinding(keyBindings[i]);
            }
        }
        Events = new EventRegistry();
        MinecraftForge.EVENT_BUS.register(Events);
        if (CoflCore.config.purchaseOverlay == GUIType.TFM) {
            MinecraftForge.EVENT_BUS.register(ButtonRemapper.getInstance());
        }
        MinecraftForge.EVENT_BUS.register(new ChatMessageSendHandler());
    }
}
	
