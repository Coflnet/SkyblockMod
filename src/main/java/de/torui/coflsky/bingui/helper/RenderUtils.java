package de.torui.coflsky.bingui.helper;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderUtils {
    public static Minecraft mc = Minecraft.getMinecraft();

    //draw a rectangle
    public static void drawRect(float x, float y, int width, int height, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y + height);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x + width, y);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }


    //draws an outlined rectangle with a given color and size and a given line width
    public static void drawRectOutline(int x, int y, int width, int height, float lineWidth, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setColor(color);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    //draws a circle with a given radius and thickness
    public static void drawCircle(int x, int y, int radius, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x, y);
        for (int i = 0; i <= 360; i++) {
            GL11.glVertex2d(x + Math.sin(i * Math.PI / 180) * radius, y + Math.cos(i * Math.PI / 180) * radius);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    //draws a circle outline with a given radius and thickness
    public static void drawCircleOutline(int x, int y, float radius, float thickness, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setColor(color);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i <= 360; i++) {
            GL11.glVertex2d(x + Math.sin(i * Math.PI / 180) * radius, y + Math.cos(i * Math.PI / 180) * radius);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    //draws line from x1,y1 to x2,y2 with a given color and thickness
    public static void drawLine(float x1, float y1, float x2, float y2, float thickness, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setColor(color);
        GL11.glLineWidth(thickness);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    //draws a triangle from x1,y1 to x2,y2 to x3,y3
    public static void drawTriangle(int x, int y, int x2, int y2, int x3, int y3, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x3, y3);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    //draws a triangle outline from x1,y1 to x2,y2 to x3,y3
    public static void drawTriangleOutline(int x, int y, int x2, int y2, int x3, int y3, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setColor(color);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x3, y3);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    //draws an arc with a given radius, thickness, start angle, and end angle
    public static void drawArc(float x, float y, float radius, float thickness, float startAngle, float endAngle, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setColor(color);
        GL11.glLineWidth(thickness);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = (int) startAngle; i <= endAngle; i++) {
            GL11.glVertex2d(x + Math.sin(i * Math.PI / 180) * radius, y + Math.cos(i * Math.PI / 180) * radius);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    //draw a loading circle with a given radius, thickness, and speed
    public static void drawLoadingCircle(float x, float y, float radius, float thickness, float speed, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setColor(color);
        GL11.glLineWidth(thickness);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i <= 360; i++) {
            GL11.glVertex2d(x + Math.sin((i + speed) * Math.PI / 180) * radius, y + Math.cos((i + speed) * Math.PI / 180) * radius);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    //draws a rounded rectangle with a given radius and color and size
    /* i dont what i did wrong
    public static void drawRoundedRect(int x, int y, int width, int height, int radius, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setColor(color);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x + radius, y);
        GL11.glVertex2d(x + width - radius, y);
        GL11.glVertex2d(x + width - radius, y + height);
        GL11.glVertex2d(x + radius, y + height);
        GL11.glVertex2d(x, y + radius);
        GL11.glVertex2d(x + width, y + radius);
        GL11.glVertex2d(x + width, y + height - radius);
        GL11.glVertex2d(x, y + height - radius);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x + radius, y + radius);
        for (int i = 0; i <= 90; i++) {
            GL11.glVertex2d(x + radius + Math.sin(i * Math.PI / 180) * radius, y + radius + Math.cos(i * Math.PI / 180) * radius);
        }
        GL11.glEnd();
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x + width - radius, y + radius);
        for (int i = 90; i <= 180; i++) {
            GL11.glVertex2d(x + width - radius + Math.sin(i * Math.PI / 180) * radius, y + radius + Math.cos(i * Math.PI / 180) * radius);
        }
        GL11.glEnd();
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x + width - radius, y + height - radius);
        for (int i = 180; i <= 270; i++) {
            GL11.glVertex2d(x + width - radius + Math.sin(i * Math.PI / 180) * radius, y + height - radius + Math.cos(i * Math.PI / 180) * radius);
        }
        GL11.glEnd();
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x + radius, y + height - radius);
        for (int i = 270; i <= 360; i++) {
            GL11.glVertex2d(x + radius + Math.sin(i * Math.PI / 180) * radius, y + height - radius + Math.cos(i * Math.PI / 180) * radius);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
     */

    //draws a gradient rectangle with a given color and size
    public static void drawGradientRect(int x, int y, int width, int height, Color color1, Color color2) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        setColor(color1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        setColor(color2);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }


    public static void drawString(String text, int x, int y, Color color) {
        mc.fontRendererObj.drawString(text, x, y, color.getRGB());
    }

    public static void drawStringWithShadow(String text, int x, int y, Color color) {
        mc.fontRendererObj.drawStringWithShadow(text, x, y, color.getRGB());
    }

    public static void drawCenteredString(String text, int x, int y, Color color) {
        mc.fontRendererObj.drawString(text, x - mc.fontRendererObj.getStringWidth(text) / 2, y, color.getRGB());
    }

    public static void drawCenteredStringWithShadow(String text, int x, int y, Color color) {
        mc.fontRendererObj.drawStringWithShadow(text, x - mc.fontRendererObj.getStringWidth(text) / 2, y, color.getRGB());
    }

    //draw string with custom scale
    public static void drawString(String text, int x, int y, Color color, float scale) {
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);
        mc.fontRendererObj.drawString(text, x, y, color.getRGB());
        GL11.glPopMatrix();
    }

    //draw string with custom scale and shadow
    public static void drawStringWithShadow(String text, int x, int y, Color color, float scale) {
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);
        mc.fontRendererObj.drawStringWithShadow(text, x, y, color.getRGB());
        GL11.glPopMatrix();
    }

    //set color
    private static void setColor(int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    private static void setColor(Color color) {
        setColor(color.getRGB());
    }

    //rotate
    public static void rotate(float angle) {
        GL11.glRotatef(angle, 0.0F, 0.0F, 1.0F);
    }
}
