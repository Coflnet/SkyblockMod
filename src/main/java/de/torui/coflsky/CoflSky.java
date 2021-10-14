package de.torui.coflsky;


import java.net.URISyntaxException;


import org.lwjgl.input.Keyboard;

import de.torui.coflsky.websocket.WSClientWrapper;
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
    public static final String VERSION = "1.2-Alpha";
    
    public static WSClientWrapper Wrapper;
    public static KeyBinding[] keyBindings;

    
    public static final String[] webSocketURIPrefix = new String [] {
        	"wss://sky-commands.coflnet.com/modsocket",
            "wss://sky-mod.coflnet.com/modsocket",
        	"ws://sky-commands.coflnet.com/modsocket",
        	"ws://sky-mod.coflnet.com/modsocket",
    };
    
    public static String CommandUri = "https://sky-commands.coflnet.com/api/mod/commands";
    
    @EventHandler
    public void init(FMLInitializationEvent event) throws URISyntaxException
    {
    	
		System.out.println(">>>Started");
        
        CoflSky.Wrapper = new WSClientWrapper(webSocketURIPrefix);
        
        keyBindings = new KeyBinding[] {
        		new KeyBinding("key.replay_last.onclick", Keyboard.KEY_R, "SkyCofl"),
        };
        
        if(event.getSide() == Side.CLIENT) {
        	ClientCommandHandler.instance.registerCommand(new CoflSkyCommand());
        	ClientCommandHandler.instance.registerCommand(new ColfCommand());
        	
        	for (int i = 0; i < keyBindings.length; ++i) 
        	{
        	    ClientRegistry.registerKeyBinding(keyBindings[i]);
        	}
        	
        	
        }        	
        MinecraftForge.EVENT_BUS.register(new EventRegistry());	   
    }   

    
}
	