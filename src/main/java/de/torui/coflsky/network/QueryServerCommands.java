package de.torui.coflsky.network;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.minecraft_integration.CoflSessionManager;
import de.torui.coflsky.minecraft_integration.PlayerDataProvider;
import org.apache.commons.io.IOUtils;

public class QueryServerCommands {
	
	private static final Gson gson = new GsonBuilder().create();
	
	public static String QueryCommands() {
		
		String queryResult = GetRequest(CoflSky.CommandUri);
		
		if(queryResult != null) {
			CommandInfo[] commands = gson.fromJson(queryResult, CommandInfo[].class);

			CoflSky.logger.debug(">>> "+Arrays.toString(commands));
			
			StringBuilder sb = new StringBuilder();
			
			if(commands.length>0) {
				for(CommandInfo cm : commands) {
					sb.append(cm).append("\n");
				}
			}
			return sb.toString().trim();
			
		}
		
		return "§4ERROR: Could not connect to command server!";
	}
	
	private static class CommandInfo {
		
		public final String subCommand;
		public final String description;
		

		public CommandInfo(String subCommand, String description) {
			super();
			this.subCommand = subCommand;
			this.description = description;
		}

		@Override
		public String toString() {
			return subCommand + ": " + description;
		}
		
		
		
	}
	private static String GetRequest(String uri) {
		
		try {
			URL url = new URL(uri);
	    	HttpURLConnection con;
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			
			//con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("User-Agent", "CoflMod");
			con.setDoInput(true);

			// ...

			/*OutputStream os = con.getOutputStream();
			byte[] bytes = ("[\"" + getUsername() + "\"]").getBytes("UTF-8");
			os.write(bytes);
			os.close();
			*/
			CoflSky.logger.debug("InputStream");
			 InputStream in = new BufferedInputStream(con.getInputStream());
			 ByteArrayOutputStream result = new ByteArrayOutputStream();
			 IOUtils.copy(in,result);
			 in.close();
			 // StandardCharsets.UTF_8.name() > JDK 7
			 String resString =  result.toString("UTF-8");

			 CoflSky.logger.debug("Result= " + resString);
			 return resString;
		} catch (IOException e) {
			CoflSky.logger.error("Error getting request! "+e);
		}
		
		return null;
	}
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
			con.setRequestProperty("conId", CoflSessionManager.GetCoflSession(username).sessionUUID);
			con.setRequestProperty("uuid",username);
			con.setDoInput(true);
			con.setDoOutput(true);
			// ...

			OutputStream os = con.getOutputStream();
			byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
			os.write(bytes);
			os.close();

			InputStream in = new BufferedInputStream(con.getInputStream());

			ByteArrayOutputStream result = new ByteArrayOutputStream();
			IOUtils.copy(in, result);
			in.close();
			// StandardCharsets.UTF_8.name() > JDK 7
			return result.toString("UTF-8");
		} catch (IOException e) {
			CoflSky.logger.error("Error POSTing request! "+e);
		}

		return null;
	}
}
