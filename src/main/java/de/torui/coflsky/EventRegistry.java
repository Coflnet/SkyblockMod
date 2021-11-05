package de.torui.coflsky;

import java.util.UUID;

import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.commands.JsonStringCommand;
import de.torui.coflsky.network.WSClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventRegistry{

	
	@SubscribeEvent
	public void onConnectedToServerEvent(ClientConnectedToServerEvent event) {
	/*	if(!event.isLocal ) {
			//String serverIP =  Minecraft.getMinecraft().getCurrentServerData().serverIP;
			
		/*	if(false && serverIP.equals("hypixel.net")) {
				
			}*
		//UUID.randomUUID().toString();
		
		//String id = FMLClientHandler.instance().getClient().thePlayer.getUniqueID().toString();
		
		String id = UUID.randomUUID().toString();//Minecraft.getMinecraft().thePlayer.getUniqueID().toString();
		System.out.println("PlayerUUID:" + id);
		CoflSky.PlayerUUID = id;
		
		System.out.println("Connected to server");		
		CoflSky.Wrapper.start();
		System.out.println("CoflSky started");
		}*/
	}
	
	@SubscribeEvent
	public void onDisconnectedFromServerEvent(ClientDisconnectionFromServerEvent event) {	
		System.out.println("Disconnected from server");
		CoflSky.Wrapper.stop();
		System.out.println("CoflSky stopped");
	}
	
	
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
	public void onEvent(KeyInputEvent event) {

		if(CoflSky.keyBindings[0].isPressed()) {
			if(WSCommandHandler.lastOnClickEvent != null) {
				
				String command = WSCommandHandler.lastOnClickEvent;
				WSCommandHandler.lastOnClickEvent = null;
				WSCommandHandler.HandleCommand(new JsonStringCommand(CommandType.Execute, WSClient.gson.toJson(command)),
						Minecraft.getMinecraft().thePlayer);
			}
			
		
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void DrawOntoGUI(RenderGameOverlayEvent rgoe) {
		
		if(rgoe.type == ElementType.CROSSHAIRS) {
			Minecraft mc = Minecraft.getMinecraft();
			mc.ingameGUI.drawString(Minecraft.getMinecraft().fontRendererObj, "Hello World", 0, 0, Integer.MAX_VALUE);
		}
		
		//.currentScreen.
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void OnGuiOpen(GuiScreenEvent.KeyboardInputEvent goe) {
		
		
		if (goe.gui instanceof GuiChest) { // verify that it's really a chest
		    ContainerChest chest = (ContainerChest)Minecraft.getMinecraft().thePlayer.openContainer; // it's now safe to cast
		    IInventory inv = chest.getLowerChestInventory();
		    if (!inv.hasCustomName()) { // verify that the chest actually has a custom name
		        String chestName = inv.getName();
		        System.out.println("Opened chest with custo name " + chestName);
		    }
		    else {
		    	System.out.println("Opened regular chest");
		    }
		}	
			
			
		}
		
		/*try {
			ItemStack stack = ((GuiChest) Minecraft.getMinecraft().currentScreen).getSlotUnderMouse().getStack();

			System.out.println("Hovering over item: Pre " + stack.getDisplayName());
			stack.setStackDisplayName("Coflll");
			
			System.out.println("Hovering over item: " + stack.getDisplayName());
			} catch(ClassCastException e) {
				e.printStackTrace();
			}catch(NullPointerException e) {
				
			}*/
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
