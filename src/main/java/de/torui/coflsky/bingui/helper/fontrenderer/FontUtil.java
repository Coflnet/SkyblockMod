package de.torui.coflsky.bingui.helper.fontrenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

/*
Thanks to https://github.com/Godwhitelight/FontRenderer
for this class
 */

public class FontUtil {

    public static Font getFontFromTTF(ResourceLocation fontLocation, float fontSize, int fontType) {
        Font output = null;
        try {
            output = Font.createFont(fontType, Minecraft.getMinecraft().getResourceManager().getResource(fontLocation).getInputStream());
            output = output.deriveFont(fontSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }
}
