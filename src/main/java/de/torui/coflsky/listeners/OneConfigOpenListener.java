package de.torui.coflsky.listeners;

import de.torui.coflsky.util.ServerSettingsLoader;
import de.torui.coflsky.util.SettingsUploader;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Listens for any GUI opened; if it is a OneConfig GUI, trigger remote settings load.
 */
public class OneConfigOpenListener {

    /** Tracks whether a OneConfig GUI is currently open so we can detect when it fully closes */
    private static boolean oneConfigOpen = false;

    private static boolean isOneConfigGui(GuiScreen gui) {
        if (gui == null) return false;
        String name = gui.getClass().getName();
        return name.startsWith("cc.polyfrost.oneconfig.gui") || name.contains("oneconfig");
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen gui = event.gui;
        // ──────────────────────── OPEN ──────────────────────────
        if (isOneConfigGui(gui)) {
            oneConfigOpen = true;
            // Avoid duplicate fetches if one is already in progress or just applied
            if (ServerSettingsLoader.isRunning() || ServerSettingsLoader.recentlyLoaded()) {
                return;
            }
            // delay until GUI shown to ensure command handler available
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(ServerSettingsLoader::requestSettings);
            return;
        }
        // ──────────────────────── CLOSE ─────────────────────────
        // We previously had OneConfig open, and now it is closed (gui == null) or another non-OneConfig GUI is opening (this might be fucked for other mods idk!)
        if (oneConfigOpen && (gui == null || !isOneConfigGui(gui))) {
            oneConfigOpen = false;
            // Upload settings asynchronously on the next client tick to ensure that OneConfig has flushed writes.
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(
                    SettingsUploader::uploadSettings);
        }
    }
}
