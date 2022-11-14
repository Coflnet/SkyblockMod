package de.torui.coflsky.bingui.helper.inputhandler;


import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

/**
 * Created by ForBai
 */
public class InputHandler {

    public boolean isAreaHovered(float x, float y, float width, float height, boolean ignoreBlock) {
        return mouseX() > x && mouseY() > y && mouseX() < x + width && mouseY() < y + height;
    }

    public boolean isAreaHovered(float x, float y, float width, float height) {
        return isAreaHovered(x, y, width, height, false);
    }

    public boolean isAreaClicked(float x, float y, float width, float height, boolean ignoreBlock) {
        return isAreaHovered(x, y, width, height, ignoreBlock) && isClicked();
    }

    public boolean isClicked() {
        return MouseHelper.wasMouseDown() && !Mouse.isButtonDown(0);
    }

    public boolean isMouseDown(int button) {
        return Mouse.isButtonDown(button);
    }

    public boolean isMouseDown() {
        return isMouseDown(0);
    }

    //get scroll wheel
    public int getScrollWheel() {
        return Mouse.getDWheel();
    }

    public float mouseX() {
        double scaleX = 1d;
        return (float) (Mouse.getDX() / scaleX);
    }

    public float mouseY() {
        double scaleY = 1d;
        return (float) ((Minecraft.getMinecraft().displayHeight - Math.abs(Mouse.getDY())) / scaleY);
    }
}
