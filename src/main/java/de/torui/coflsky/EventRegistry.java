package de.torui.coflsky;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class EventRegistry{

	@SubscribeEvent
    public void PlayerLoggedIn(PlayerLoggedInEvent plie) {
    	
		System.out.println("COFLSKY initialized");
		
		if(plie.player.getEntityWorld().isRemote) {
			//is a server
			ServerData sd = Minecraft.getMinecraft().getCurrentServerData();
			if(sd != null) {
				System.out.println("ServerIP:= " + sd.serverIP);
			} else {
				System.out.println("Could not get serverdata");
			}
			
			
		} else {
			System.out.println("World is not remote");
		}
    	
    }
    
    @SubscribeEvent
    public void PlayerLoggedOut(PlayerLoggedOutEvent ploe) {
    	System.out.println("COFLSKY disabled");
    }
		
}
