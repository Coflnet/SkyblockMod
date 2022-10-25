package de.torui.coflsky;


import com.google.gson.Gson;
import de.torui.coflsky.bingui.gui.TestCommand;
import de.torui.coflsky.bingui.helper.inputhandler.MouseHelper;
import de.torui.coflsky.configuration.LocalConfig;
import de.torui.coflsky.handlers.EventRegistry;
import de.torui.coflsky.network.WSClientWrapper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Mod(modid = CoflSky.MODID, version = CoflSky.VERSION)
public class CoflSky {
    public static final String MODID = "CoflSky";
    public static final String VERSION = "1.4.3-Alpha";

    public static WSClientWrapper Wrapper;
    public static KeyBinding[] keyBindings;

    public static EventRegistry Events;
    public static File configFile;
    private File coflDir;
    public static LocalConfig config;

    public static final String[] webSocketURIPrefix = new String[]{
            "wss://sky.coflnet.com/modsocket",
            "wss://sky-mod.coflnet.com/modsocket",
            "ws://sky.coflnet.com/modsocket",
            "ws://sky-mod.coflnet.com/modsocket",
    };

    public static String CommandUri = Config.BaseUrl + "/api/mod/commands";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        String configString = null;
        Gson gson = new Gson();
        coflDir = new File(event.getModConfigurationDirectory(), "CoflSky");
        coflDir.mkdirs();
        configFile = new File(coflDir, "config.json");
        try {
            if (configFile.isFile()) {
                configString = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
                config = gson.fromJson(configString, LocalConfig.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (config == null) {
            config = LocalConfig.createDefaultConfig();
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) throws URISyntaxException {
        System.out.println(">>>Started");

        CoflSky.Wrapper = new WSClientWrapper(webSocketURIPrefix);

        keyBindings = new KeyBinding[]{
                new KeyBinding("key.replay_last.onclick", Keyboard.KEY_NONE, "SkyCofl"),
                new KeyBinding("key.start_highest_bid", Keyboard.KEY_NONE, "SkyCofl")
        };

        if (event.getSide() == Side.CLIENT) {
            ClientCommandHandler.instance.registerCommand(new CoflSkyCommand());
            ClientCommandHandler.instance.registerCommand(new ColfCommand());
            ClientCommandHandler.instance.registerCommand(new FlipperChatCommand());
            ClientCommandHandler.instance.registerCommand(new TestCommand());
            for (int i = 0; i < keyBindings.length; ++i) {
                ClientRegistry.registerKeyBinding(keyBindings[i]);
            }


        }
        Events = new EventRegistry();
        MinecraftForge.EVENT_BUS.register(Events);
        MinecraftForge.EVENT_BUS.register(new MouseHelper());
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> config.saveConfig(configFile, config)));
    }


}
	
