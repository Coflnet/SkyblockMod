package de.torui.coflsky.minecraft_integration;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import de.torui.coflsky.network.WSClient;
import net.minecraft.client.Minecraft;

public class PlayerDataProvider {
	
	private static class UUIDHelper {
		public String id;
		public String name;
	}
	
	public static class PlayerPosition
	{
		public double X;
		public double Y;
		public double Z;
		public float Yaw;
		public float Pitch;

	}
	
	public static String getUsername() {
		String username = Minecraft.getSessionInfo().get("X-Minecraft-Username");
		return username;
	}

	public static PlayerPosition getPlayerPosition() {
		PlayerPosition pos = new PlayerPosition();
		pos.X = Minecraft.getMinecraft().thePlayer.posX;
		pos.Y = Minecraft.getMinecraft().thePlayer.posY;
		pos.Z = Minecraft.getMinecraft().thePlayer.posZ;
		pos.Yaw = Minecraft.getMinecraft().thePlayer.rotationYaw;
		pos.Pitch = Minecraft.getMinecraft().thePlayer.rotationPitch;
		return pos;
	}
	
}
