package de.torui.coflsky;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;

import de.torui.coflsky.websocket.WSClient;
import de.torui.coflsky.websocket.WSClientWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = CoflSky.MODID, version = CoflSky.VERSION)
public class CoflSky
{
    public static final String MODID = "CoflSky";
    public static final String VERSION = "1.0";
    public static WSClientWrapper Wrapper;
    
    public static String PlayerUUID = "";
    
    @EventHandler
    public void init(FMLInitializationEvent event) throws URISyntaxException
    {
    	
    	//Minecraft.getSessionInfo().forEach((a,b) -> System.out.println("Key=" + a + " value=" + b));
    	
    	//System.out.println("Loggerfactory: " + LoggerFactory.getILoggerFactory());
    //	Logger log = LoggerFactory.getLogger(CoflSky.class);
   // 	log.debug("Testing");
    	
		// some example code
        System.out.println("Initializing");
        
        //new Thread(new WSClient(new URI("ws://localhost:8080"))).start();        
        System.out.println(">>>Started");
        
        String username = Minecraft.getSessionInfo().get("X-Minecraft-Username");
        System.out.println(">>> Username= " + username);
        /*try {
			QueryUUID("pingulinoo");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        
        String URI = "https://api.mojang.com/profiles/minecraft";
        
        
        CoflSky.Wrapper = new WSClientWrapper("ws://sky-commands.coflnet.com/modsocket?version=" + CoflSky.VERSION  + "&uuid=");
        
        if(event.getSide() == Side.CLIENT)
        	ClientCommandHandler.instance.registerCommand(new CoflSkyCommand());
        MinecraftForge.EVENT_BUS.register(new EventRegistry());	   
    }   
    
    public static String QueryUUID(String username) throws MalformedURLException {
    	URL url = new URL("https://api.mojang.com/profiles/minecraft");
    	HttpURLConnection con;
		try {
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoInput(true);
			con.setDoOutput(true);

			// ...

			OutputStream os = con.getOutputStream();
			byte[] bytes = ("[\"" + username + "\"]").getBytes("UTF-8");
			os.write(bytes);
			os.close();
			
			 InputStream in = new BufferedInputStream(con.getInputStream());
			 ByteArrayOutputStream result = new ByteArrayOutputStream();
			 byte[] buffer = new byte[1024];
			 for (int length; (length = in.read(buffer)) != -1; ) {
			     result.write(buffer, 0, length);
			 }
			 // StandardCharsets.UTF_8.name() > JDK 7
			 String resString =  result.toString("UTF-8");
			 
			 System.out.println("Result= " + resString);
			 
			 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    

   /* @EventHandler
    public void init(FMLServerStartingEvent event)
    {
    	
    	if(event.getSide() == Side.CLIENT)    	return;
    		//event.registerServerCommand(new CoflSkyCommand());
    }*/
    
}
	