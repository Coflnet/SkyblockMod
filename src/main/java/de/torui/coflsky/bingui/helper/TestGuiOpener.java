package de.torui.coflsky.bingui.helper;

import de.torui.coflsky.bingui.gui.BinGuiManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TestGuiOpener {
    @SubscribeEvent
    public void openChestEvent(net.minecraftforge.event.entity.player.PlayerInteractEvent event) {
        //ignore this, this will be removed when the gui is finished
        if (event.action.equals(PlayerInteractEvent.Action.RIGHT_CLICK_AIR)) {
            /*
            IChatComponent message = new ChatComponentText("FLIP: §6Bat Person Helmet §2500,000 -> 750,000 (+242,500 §448%§2) §7 Med: §b750,000§7 Vol: §b20.7§r§7 sellers ah aüsoijfaoßsijfá0isjf´0aisjfáoijsfá0isjf´0aijsfá9ijf");
            String[] lore = {
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test loreThis is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test loreThis is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test loreThis is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test loreThis is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test loreThis is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore",
                    "This is a Test lore"

            };
            //Minecraft.getMinecraft().displayGuiScreen(new BinGuiNew(message, lore,"0", "minecraft:apple", null));
            BinGuiManager.openNewFlipGui(message, lore, "0", "leather_leggings");
             */
        }
    }
}
