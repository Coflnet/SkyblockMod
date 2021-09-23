package de.torui.coflsky;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.server.FMLServerHandler;

public class EventRegistry{

	@SubscribeEvent
	public void onConnectedToServerEvent(ClientConnectedToServerEvent event) {
		//String serverIP =  Minecraft.getMinecraft().getCurrentServerData().serverIP;
		//System.out.println("ServerIP: " + serverIP);
		if(!event.isLocal) {
			//String serverIP =  Minecraft.getMinecraft().getCurrentServerData().serverIP;
			
		/*	if(false && serverIP.equals("hypixel.net")) {
				
			}*/
		//UUID.randomUUID().toString();
			
		//String username = Minecraft.getSessionInfo().get("X-Minecraft-Username");
		
		//String id = FMLClientHandler.instance().getClient().thePlayer.getUniqueID().toString();
		
		String id = UUID.randomUUID().toString();//Minecraft.getMinecraft().thePlayer.getUniqueID().toString();
		System.out.println("PlayerUUID:" + id);
		CoflSky.PlayerUUID = id;
		
		System.out.println("Connected to server");		
		CoflSky.Wrapper.start();
		System.out.println("CoflSky started");
		}
	}
	
	@SubscribeEvent
	public void onDisconnectedFromServerEvent(ClientDisconnectionFromServerEvent event) {	
		System.out.println("Disconnected from server");
		CoflSky.Wrapper.stop();
		System.out.println("CoflSky stopped");
	}
	/*@SubscribeEvent
public void OnSomething(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		System.out.println("Client connect to server from network");
}
    
    @SubscribeEvent
    public void PlayerLoggedOut(PlayerLoggedOutEvent ploe) {
    	//CoflSky.Wrapper.stop();
    	System.out.println("COFLSKY disabled");   	
    }*/
		
}
