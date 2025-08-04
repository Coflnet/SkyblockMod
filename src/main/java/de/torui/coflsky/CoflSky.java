package de.torui.coflsky;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;
import de.torui.coflsky.configuration.LocalConfig;
import de.torui.coflsky.config.CoflConfig;
import de.torui.coflsky.gui.GUIType;
import de.torui.coflsky.handlers.EventRegistry;
import de.torui.coflsky.listeners.ChatListener;
import de.torui.coflsky.listeners.OneConfigOpenListener;
import de.torui.coflsky.proxy.APIKeyManager;
import de.torui.coflsky.gui.tfm.ButtonRemapper;
import de.torui.coflsky.gui.tfm.ChatMessageSendHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;
import de.torui.coflsky.network.WSClientWrapper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = CoflSky.MODID, version = CoflSky.VERSION)
public class CoflSky {
    public static final String MODID = "CoflSky";
    public static final String VERSION = "1.6.0";

    public static WSClientWrapper Wrapper;
    public static KeyBinding[] keyBindings;

    public static EventRegistry Events;
    public static File configFile;
    private File coflDir;
    public static LocalConfig config;

    public static final String[] webSocketURIPrefix = new String[]{
        "wss://sky.coflnet.com/modsocket",
        // fallback for old java versions not supporting new tls certificates
        "ws://sky-mod.coflnet.com/modsocket",
    };

    public static String CommandUri = Config.BaseUrl + "/api/mod/commands";
    private final static APIKeyManager apiKeyManager = new APIKeyManager();


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

        try {
            this.apiKeyManager.loadIfExists();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        MinecraftForge.EVENT_BUS.register(new ChatListener());
        // Initialise OneConfig configuration (GUI will open via keybind automatically)
        CoflConfig oneConfig = new CoflConfig();
        // Cache all the mods on load
        WSCommandHandler.cacheMods();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        CoflSky.Wrapper = new WSClientWrapper(webSocketURIPrefix);

        keyBindings = new KeyBinding[]{
                new KeyBinding("key.replay_last.onclick", Keyboard.KEY_NONE, "SkyCofl"),
                new KeyBinding("key.start_highest_bid", Keyboard.KEY_NONE, "SkyCofl"),
                new KeyBinding("key.upload_selected", Keyboard.KEY_U, "SkyCofl")
        };

        if (event.getSide() == Side.CLIENT) {
            ClientCommandHandler.instance.registerCommand(new CoflSkyCommand());
            ClientCommandHandler.instance.registerCommand(new ColfCommand());
            ClientCommandHandler.instance.registerCommand(new FlipperChatCommand());
            // register OneConfig command
            ClientCommandHandler.instance.registerCommand(new de.torui.coflsky.commands.CoflGuiCommand());

            for (int i = 0; i < keyBindings.length; ++i) {
                ClientRegistry.registerKeyBinding(keyBindings[i]);
            }
        }
        Events = new EventRegistry();
        MinecraftForge.EVENT_BUS.register(Events);
        if (config.purchaseOverlay == GUIType.TFM) {
            MinecraftForge.EVENT_BUS.register(ButtonRemapper.getInstance());
        }
        MinecraftForge.EVENT_BUS.register(new ChatMessageSendHandler());
        // GUI open listener for OneConfig to fetch server settings
        MinecraftForge.EVENT_BUS.register(new OneConfigOpenListener());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            config.saveConfig(configFile, config);
            try {
                apiKeyManager.saveKey();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }));
    }

    public static APIKeyManager getAPIKeyManager() {
        return apiKeyManager;
    }

}
