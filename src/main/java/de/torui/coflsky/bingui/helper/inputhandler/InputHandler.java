package de.torui.coflsky.bingui.helper.inputhandler;


import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

public class InputHandler {
    private Minecraft mc = Minecraft.getMinecraft();
    private double scaleX = 1d;
    private double scaleY = 1d;

    public boolean isAreaHovered(float x, float y, float width, float height, boolean ignoreBlock) {
        float mouseX = mouseX();
        float mouseY = mouseY();
        return mouseX > x && mouseY > y && mouseX < x + width && mouseY < y + height;
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

    public float mouseX() {
        return (float) (Mouse.getDX() / scaleX);
    }

    public float mouseY() {
        return (float) ((mc.displayHeight - Math.abs(Mouse.getDY())) / scaleY);
    }

}
