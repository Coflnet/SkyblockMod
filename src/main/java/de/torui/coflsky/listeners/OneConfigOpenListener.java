package de.torui.coflsky.listeners;

import de.torui.coflsky.util.ServerSettingsLoader;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Listens for any GUI opened; if it is a OneConfig GUI, trigger remote settings load.
 */
public class OneConfigOpenListener {

    private static boolean guiSessionActive = false; // tracks a single OC GUI open chain

    private static boolean isOneConfigGui(GuiScreen gui) {
        if (gui == null) return false;
        String name = gui.getClass().getName();
        return name.startsWith("cc.polyfrost.oneconfig.gui") || name.contains("oneconfig");
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen gui = event.gui;
        // Reset session flag when OC GUI closed (gui == null) or a non-OC gui is opened
        if (gui == null || !isOneConfigGui(gui)) {
            guiSessionActive = false;
        }
        if (isOneConfigGui(gui)) {
            if (guiSessionActive) {
                return; // already handled in this session
            }
            guiSessionActive = true;
            // Avoid duplicate fetches if one is already in progress or just applied
            if (ServerSettingsLoader.isRunning() || ServerSettingsLoader.recentlyLoaded()) {
                return;
            }
            // delay until GUI shown to ensure command handler available
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(ServerSettingsLoader::requestSettings);
        }
    }
}
