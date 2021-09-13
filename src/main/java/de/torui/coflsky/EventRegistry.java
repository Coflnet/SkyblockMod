package de.torui.coflsky;

import java.net.URI;
import java.net.URISyntaxException;

import de.torui.coflsky.websocket.WSClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventRegistry{

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
    public void PlayerLoggedIn(PlayerLoggedInEvent plie) {
    	
		System.out.println("COFLSKY initialized");
		
		if(plie.player.getEntityWorld().isRemote) {
			//is a server
			/*ServerData sd = Minecraft.getMinecraft().getCurrentServerData();
			if(sd != null) {
				System.out.println("ServerIP:= " + sd.serverIP);
			} else {
				System.out.println("Could not get serverdata");
			}
			*/
			if(CoflSky.WS == null) {
				try {
					String uuid = Minecraft.getMinecraft().thePlayer.getPersistentID().toString();
					//String uuid = Minecraft.getMinecraft().thePlayer.getUUID(null)
					CoflSky.WS = new WSClient(new URI("wss://sky-commands.coflnet.com/modsocket?uuid=" + uuid));
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Thread t = new Thread(CoflSky.WS);
				
			}
			
		} else {
			System.out.println("World is not remote");
		}
    	
    }
    
	@SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void PlayerLoggedOut(PlayerLoggedOutEvent ploe) {
    	System.out.println("COFLSKY disabled");
    	
    	if(CoflSky.WS != null) {
    		try {
				CoflSky.WS.closeBlocking();
				CoflSky.WS = null;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    }
		
}
