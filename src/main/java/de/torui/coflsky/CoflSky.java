package de.torui.coflsky;


import java.net.URISyntaxException;


import org.lwjgl.input.Keyboard;

import de.torui.coflsky.configuration.ConfigurationManager;
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
    
    public static final String[] webSocketURIPrefix = new String [] {
        	"wss://sky.coflnet.com/modsocket",
        	"wss://sky-mod.coflnet.com/modsocket",
        	"ws://sky.coflnet.com/modsocket",
        	"ws://sky-mod.coflnet.com/modsocket",
    };
    
    public static String CommandUri = "https://sky-commands.coflnet.com/api/mod/commands";
    
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
        	
        	for (int i = 0; i < keyBindings.length; ++i) 
        	{
        	    ClientRegistry.registerKeyBinding(keyBindings[i]);
        	}
        	
        	
        }   
        Events = new EventRegistry();
        MinecraftForge.EVENT_BUS.register(Events);	   
    }   

    
}
	
