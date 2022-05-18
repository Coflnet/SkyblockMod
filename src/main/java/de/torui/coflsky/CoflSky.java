package de.torui.coflsky;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;


import com.google.gson.Gson;
import de.torui.coflsky.configuration.LocalConfig;
import de.torui.coflsky.handlers.EventRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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

@Mod(modid = CoflSky.MODID, version = CoflSky.VERSION)
public class CoflSky
{
    public static final String MODID = "CoflSky";
    public static final String VERSION = "1.4-Alpha";
    
    public static WSClientWrapper Wrapper;
    public static KeyBinding[] keyBindings;

    public static EventRegistry Events;
    public static File configFile;
    private File coflDir;
    public static LocalConfig config;
    
    public static final String[] webSocketURIPrefix = new String [] {
        	"wss://sky.coflnet.com/modsocket",
        	"wss://sky-mod.coflnet.com/modsocket",
        	"ws://sky.coflnet.com/modsocket",
        	"ws://sky-mod.coflnet.com/modsocket",
    };
    
    public static String CommandUri = "https://sky-commands.coflnet.com/api/mod/commands";
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (configString == null) {
            config =  LocalConfig.createDefaultConfig();
        }
    }
    @EventHandler
    public void init(FMLInitializationEvent event) throws URISyntaxException
    {
		System.out.println(">>>Started");
        
        CoflSky.Wrapper = new WSClientWrapper(webSocketURIPrefix);
        
        keyBindings = new KeyBinding[] {
        		new KeyBinding("key.replay_last.onclick", Keyboard.KEY_NONE, "SkyCofl"),
        		new KeyBinding("key.start_highest_bid", Keyboard.KEY_NONE, "SkyCofl")
        };
        
        if(event.getSide() == Side.CLIENT) {
        	ClientCommandHandler.instance.registerCommand(new CoflSkyCommand());
        	ClientCommandHandler.instance.registerCommand(new ColfCommand());
        	ClientCommandHandler.instance.registerCommand(new FlipperChatCommand());
        	
        	for (int i = 0; i < keyBindings.length; ++i) 
        	{
        	    ClientRegistry.registerKeyBinding(keyBindings[i]);
        	}
        	
        	
        }   
        Events = new EventRegistry();
        MinecraftForge.EVENT_BUS.register(Events);
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> config.saveConfig(configFile , config)));
    }   

    
}
	
