package de.torui.coflsky.network;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.minecraft_integration.CoflSessionManager;
import de.torui.coflsky.minecraft_integration.PlayerDataProvider;

public class QueryServerCommands {

	private static Gson gson = new GsonBuilder().create();

	public static String PostRequest(String uri,  String data) {
		try {
			String username = PlayerDataProvider.getUsername();
			URL url = new URL(uri);
			HttpURLConnection con;
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("User-Agent", "CoflMod");
			con.setRequestProperty("conId", CoflSessionManager.GetCoflSession(username).SessionUUID);
			con.setRequestProperty("uuid",username);
			con.setDoInput(true);
			con.setDoOutput(true);
			// ...

			OutputStream os = con.getOutputStream();
			byte[] bytes = data.getBytes("UTF-8");
			os.write(bytes);
			os.close();

			InputStream in = new BufferedInputStream(con.getInputStream());
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int length; (length = in.read(buffer)) != -1; ) {
				result.write(buffer, 0, length);
			}
			// StandardCharsets.UTF_8.name() > JDK 7
			String resString =  result.toString("UTF-8");

			return resString;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}