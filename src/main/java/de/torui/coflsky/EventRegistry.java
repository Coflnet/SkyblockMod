package de.torui.coflsky;

import java.time.LocalDateTime;
import com.mojang.realmsclient.util.Pair;

import de.torui.coflsky.FlipHandler.Flip;
import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.CommandType;
import de.torui.coflsky.commands.JsonStringCommand;
import de.torui.coflsky.commands.models.AuctionData;
import de.torui.coflsky.network.WSClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventRegistry {

	@SubscribeEvent
	public void onDisconnectedFromServerEvent(ClientDisconnectionFromServerEvent event) {
		if(CoflSky.Wrapper.isRunning) {
			System.out.println("Disconnected from server");
			CoflSky.Wrapper.stop();
			System.out.println("CoflSky stopped");
		}
	}
	
	public long LastClick = System.currentTimeMillis();

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
	public void onEvent(KeyInputEvent event) {

		if (CoflSky.keyBindings[0].isPressed()) {
			if (WSCommandHandler.lastOnClickEvent != null) {

				String command = WSCommandHandler.lastOnClickEvent;
				WSCommandHandler.lastOnClickEvent = null;
				WSCommandHandler.HandleCommand(
						new JsonStringCommand(CommandType.Execute, WSClient.gson.toJson(command)),
						Minecraft.getMinecraft().thePlayer);
			}

		}
		if(CoflSky.keyBindings[1].isKeyDown()) {
			if((System.currentTimeMillis() - LastClick) >= 500) {
						
				Flip f = WSCommandHandler.flipHandler.fds.GetHighestFlip();
				
				if(f != null) {
					LastClick = System.currentTimeMillis();		
					String command =  WSClient.gson.toJson("/viewauction " + f.id);
					WSCommandHandler.Execute("/viewauction " + f.id, null);
					WSCommandHandler.flipHandler.fds.InvalidateFlip(f);
					
					CoflSky.Wrapper.SendMessage(new JsonStringCommand(CommandType.Clicked, command));		
				}			
				
			}
		}

	}

	@SideOnly(Side.CLIENT)
	//@SubscribeEvent
	public void DrawOntoGUI(RenderGameOverlayEvent rgoe) {

		if (rgoe.type == ElementType.CROSSHAIRS) {
			Minecraft mc = Minecraft.getMinecraft();
			mc.ingameGUI.drawString(Minecraft.getMinecraft().fontRendererObj, "Flips in Pipeline:" + WSCommandHandler.flipHandler.fds.CurrentFlips(), 0, 0, Integer.MAX_VALUE);
		}
	}

	public static String ExtractUuidFromInventory(IInventory inventory) {

		ItemStack stack = inventory.getStackInSlot(13);
		if (stack != null) {
			try {
				String uuid = stack.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes")
						.getString("uuid");
				if (uuid.length() == 0) {
					throw new Exception();
				}
				System.out.println("Item has the UUID: " + uuid);
				return uuid;
			} catch (Exception e) {
				System.out.println("Clicked item " + stack.getDisplayName() + " has the following meta: "
						+ stack.serializeNBT().toString());
			}
		}
		return "";
	}

	public static ItemStack GOLD_NUGGET = new ItemStack(
			Item.itemRegistry.getObject(new ResourceLocation("minecraft:gold_nugget")));

	public static final Pair<String, Pair<String, LocalDateTime>> EMPTY = Pair.of(null, Pair.of("",LocalDateTime.MIN));
	public static Pair<String, Pair<String, LocalDateTime>> last = EMPTY;
	
	@SubscribeEvent
	public void HandleChatEvent(ClientChatReceivedEvent sce) {
		if(CoflSky.Wrapper.isRunning && last.first() != null) {
			if(sce.message.getUnformattedText().startsWith("You claimed ")) {
				
				AuctionData ad = new AuctionData();
				ad.setItemId(last.second().first());
				ad.setAuctionId("");
				Command<AuctionData> data = new Command<>(CommandType.PurchaseConfirm, ad);
				CoflSky.Wrapper.SendMessage(data);
				System.out.println("PurchaseConfirm");
				last = EMPTY;
			}
			else if(last.second().second().plusSeconds(10).isBefore(LocalDateTime.now())) {
				last = EMPTY;
			}
		}
		
	}
	
	public static long lastStartTime = Long.MAX_VALUE;
		
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void OnGuiClick(GuiScreenEvent.MouseInputEvent mie) {
		if (CoflSky.Wrapper.isRunning) {
			if (mie.gui instanceof GuiChest) { // verify that it's really a chest

				ContainerChest chest = (ContainerChest) ((GuiChest) mie.gui).inventorySlots;

				IInventory inv = chest.getLowerChestInventory();
				if (inv.hasCustomName()) { // verify that the chest actually has a custom name
					String chestName = inv.getName();

					if (chestName.equalsIgnoreCase("BIN Auction View") || chestName.equalsIgnoreCase("Ekwav")) {
					
						ItemStack heldItem = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
						
						if (heldItem != null) {
							System.out.println("Clicked on: " + heldItem.getItem().getRegistryName());
							
							String itemUUID = ExtractUuidFromInventory(inv);
							
							if((System.currentTimeMillis()+200) < lastStartTime) {
								
								if (heldItem.isItemEqual(GOLD_NUGGET)) {
									AuctionData ad = new AuctionData();
									ad.setItemId(itemUUID);
									ad.setAuctionId("");
									Command<AuctionData> data = new Command<>(CommandType.PurchaseStart, ad);
									CoflSky.Wrapper.SendMessage(data);
									System.out.println("PurchaseStart");
									last = Pair.of("You claimed ", Pair.of(itemUUID, LocalDateTime.now()));
									lastStartTime = System.currentTimeMillis();
								} 
							}
						
						}

					}
				}
			}

		}

	}
}
