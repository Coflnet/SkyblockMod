package de.torui.coflsky;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;


import com.google.gson.Gson;
import de.torui.coflsky.configuration.LocalConfig;
import de.torui.coflsky.handlers.EventRegistry;
import de.torui.coflsky.listeners.ChatListener;
import de.torui.coflsky.proxy.APIKeyManager;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import de.torui.coflsky.network.WSClientWrapper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = CoflSky.MODID, version = CoflSky.VERSION, clientSideOnly = true)
public class CoflSky
{
    public static final String MODID = "CoflSky";
    public static final String VERSION = "1.4.4-Alpha";
    
    public static WSClientWrapper Wrapper;
    public static KeyBinding[] keyBindings;

    public static EventRegistry Events;
    public static File configFile;
    public static LocalConfig config;
    
    public static final String[] webSocketURIPrefix = new String [] {
        	"wss://sky.coflnet.com/modsocket",
        	"wss://sky-mod.coflnet.com/modsocket",
        	"ws://sky.coflnet.com/modsocket",
        	"ws://sky-mod.coflnet.com/modsocket",
    };
    
    public static final String CommandUri = Config.BASE_URL + "/api/mod/commands";
    private final static APIKeyManager apiKeyManager = new APIKeyManager();
    public static final Logger logger = LogManager.getLogger("skycofl");

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        String configString;
        Gson gson = new Gson();
        File coflDir = new File(event.getModConfigurationDirectory(), "CoflSky");
        coflDir.mkdirs();
        configFile = new File(coflDir, "config.json");
        try {
            if (configFile.isFile()) {
                configString = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
                config = gson.fromJson(configString, LocalConfig.class);
            }
        } catch (Exception e) {
            logger.error("Error loading config! "+e);
        }
        if (config == null) {
            config =  LocalConfig.createDefaultConfig();
        }

        try {
             CoflSky.apiKeyManager.loadIfExists();
        }catch (Exception exception){
            exception.printStackTrace();
        }

        MinecraftForge.EVENT_BUS.register(new ChatListener());

        // Cache all the mods on load
        WSCommandHandler.cacheMods();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        CoflSky.logger.info("Initialized CoflMod");
        
        CoflSky.Wrapper = new WSClientWrapper(webSocketURIPrefix);
        
        keyBindings = new KeyBinding[] {
        		new KeyBinding("key.replay_last.onclick", Keyboard.KEY_NONE, "SkyCofl"),
        		new KeyBinding("key.start_highest_bid", Keyboard.KEY_NONE, "SkyCofl")
        };

        	ClientCommandHandler.instance.registerCommand(new CoflSkyCommand());
        	ClientCommandHandler.instance.registerCommand(new ColfCommand());
        	ClientCommandHandler.instance.registerCommand(new FlipperChatCommand());

            for (KeyBinding keyBinding : keyBindings) {
                ClientRegistry.registerKeyBinding(keyBinding);
            }

        Events = new EventRegistry();
        MinecraftForge.EVENT_BUS.register(Events);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LocalConfig.saveConfig(configFile , config);
            try {
                apiKeyManager.saveKey();
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }));
    }   


    public static APIKeyManager getAPIKeyManager(){
        return apiKeyManager;
    }

}
	
