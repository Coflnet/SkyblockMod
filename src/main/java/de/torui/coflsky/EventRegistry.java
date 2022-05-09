package de.torui.coflsky;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventRegistry {

	public boolean isInSkyblock = false;
	public boolean isInTheCatacombs = false;
	private List<String> scoreBoardLines;
	private int purse = 0;
	private int bits = 0;

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
	public void onMouseEvent(MouseInputEvent event) {
		onEvent(null);
	}

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
			if((System.currentTimeMillis() - LastClick) >= 400) {
						
				Flip f = WSCommandHandler.flipHandler.fds.GetHighestFlip();
				
				if(f != null) {
					WSCommandHandler.Execute("/viewauction " + f.id, null);
					LastClick = System.currentTimeMillis();		
					String command =  WSClient.gson.toJson("/viewauction " + f.id);
					WSCommandHandler.flipHandler.fds.InvalidateFlip(f);
					
					WSCommandHandler.Execute("/cofl track besthotkey " + f.id, Minecraft.getMinecraft().thePlayer);
					CoflSky.Wrapper.SendMessage(new JsonStringCommand(CommandType.Clicked, command));		
				} else {
					WSCommandHandler.Execute("/cofl dialog nobestflip", Minecraft.getMinecraft().thePlayer);
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
				
				if((LastViewAuctionInvocation+60*1000) >=  System.currentTimeMillis()) {
					ad.setAuctionId(LastViewAuctionUUID);
				} else {
					ad.setAuctionId("");
				}
				
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
	
	public static long lastStartTime = Long.MIN_VALUE;
	
	public static long LastViewAuctionInvocation = Long.MIN_VALUE;
	public static String LastViewAuctionUUID =null;
		
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
							
							if(System.currentTimeMillis() > lastStartTime) {
								
								if (heldItem.isItemEqual(GOLD_NUGGET)) {
									AuctionData ad = new AuctionData();
									ad.setItemId(itemUUID);
														
									if((LastViewAuctionInvocation+60*1000) >=  System.currentTimeMillis()) {
										ad.setAuctionId(LastViewAuctionUUID);
									} else {
										ad.setAuctionId("");
									}
									
									Command<AuctionData> data = new Command<>(CommandType.PurchaseStart, ad);
									CoflSky.Wrapper.SendMessage(data);
									System.out.println("PurchaseStart");
									last = Pair.of("You claimed ", Pair.of(itemUUID, LocalDateTime.now()));
									lastStartTime = System.currentTimeMillis() + 200 /*ensure a small debounce*/;
								} 
							}
						
						}

					}
				}
			}

		}

	}

	@SubscribeEvent
    public void OnRenderTick(TickEvent.RenderTickEvent event) {
		de.torui.coflsky.CountdownTimer.onRenderTick(event);
	}

	int UpdateThisTick = 0;
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onTick(TickEvent.ClientTickEvent event) {
		UpdateThisTick++;
		if (UpdateThisTick >= 200) UpdateThisTick = 0;
		if (UpdateThisTick == 0) {
			String s;
			try {
				Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
				ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
				s = objective.getDisplayName();
			} catch (Exception e) {
				s = "";
			}
			if (s.contains("SKYBLOCK") && !isInSkyblock) {
				CoflSky.Wrapper.stop();
				CoflSky.Wrapper.startConnection();
				isInSkyblock = true;
			} else if (!s.contains("SKYBLOCK") && isInSkyblock) {
				CoflSky.Wrapper.stop();
				isInSkyblock = false;
			}
			if (isInSkyblock) {
				scoreBoardLines = getScoreboard();
				int size = scoreBoardLines.size() - 1;
				boolean hasFoundCatacombs = false;
				for (int i = 0;i < scoreBoardLines.size();i++){
					String line = EnumChatFormatting.getTextWithoutFormattingCodes(scoreBoardLines.get(size-i).toLowerCase());
					System.out.println("In line:"+line);
					if (line.contains("the catacombs")) {
						hasFoundCatacombs = true;
					} else if (line.contains("purse")) {
						int purse_ = 0;
						try {
							purse_ = Integer.parseInt(line.split(": ")[1].replace(",",""));
						} catch (NumberFormatException e){
							e.printStackTrace();
						}
						if (purse != purse_){
							purse = purse_;
							Command<Integer> data = new Command<>(CommandType.updatePurse, purse);
							CoflSky.Wrapper.SendMessage(data);
						}
					} else if (line.contains("bits")) {
						int bits_ = 0;
						try {
							bits_ = Integer.parseInt(line.split(": ")[1].replace(",",""));
						} catch (NumberFormatException e){
							e.printStackTrace();
						}
						if (bits != bits_){
							bits = bits_;
							Command<Integer> data = new Command<>(CommandType.updateBits, bits);
							CoflSky.Wrapper.SendMessage(data);
						}
					}

				}
				System.out.println("has found:"+hasFoundCatacombs);
				if(hasFoundCatacombs && !isInTheCatacombs) {
					Command<String> data = new Command<>(CommandType.set, "disableFlips true");
					CoflSky.Wrapper.SendMessage(data);
					isInTheCatacombs = true;
				}
				if (isInTheCatacombs && !hasFoundCatacombs){
					Command<String> data = new Command<>(CommandType.set, "disableFlips false");
					CoflSky.Wrapper.SendMessage(data);
					isInTheCatacombs = false;
				}
				System.out.println("In Catacombs:"+isInTheCatacombs);
			}
		}
	}


	public static List<String> getScoreboard() {
		ArrayList<String> scoreboardAsText = new ArrayList<>();
		if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().theWorld == null) {
			return scoreboardAsText;
		}
		Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
		ScoreObjective sideBarObjective = scoreboard.getObjectiveInDisplaySlot(1);
		if (sideBarObjective == null) {
			return scoreboardAsText;
		}
		String scoreboardTitle = sideBarObjective.getDisplayName();
		scoreboardTitle = EnumChatFormatting.getTextWithoutFormattingCodes(scoreboardTitle);
		scoreboardAsText.add(scoreboardTitle);
		Collection<Score> scoreboardLines = scoreboard.getSortedScores(sideBarObjective);
		for (Score line : scoreboardLines) {
			String playerName = line.getPlayerName();
			if (playerName == null || playerName.startsWith("#")) {
				continue;
			}
			ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(playerName);
			String lineText = EnumChatFormatting.getTextWithoutFormattingCodes(
					ScorePlayerTeam.formatPlayerName(scorePlayerTeam, line.getPlayerName()));
			scoreboardAsText.add(lineText.replace(line.getPlayerName(),""));
		}
		return scoreboardAsText;
	}
}
