package de.torui.coflsky.bingui.gui;

import de.torui.coflsky.bingui.helper.Delay;
import net.minecraft.client.Minecraft;

public class BinGuiOpener {
    public static void openBinGui() {
        new Delay(() -> Minecraft.getMinecraft().displayGuiScreen(new BinGuiScreen()), 2);
    }
}
