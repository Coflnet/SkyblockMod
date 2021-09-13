package de.torui.coflsky.websocket;
import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.torui.coflsky.core.Command;


public class WSClient extends WebSocketClient{
	
	public static Gson gson;
	
	public static WSClient Instancce;	
		
	
	static {
		gson = new GsonBuilder()/*.setFieldNamingStrategy(new FieldNamingStrategy() {
			@Override
			public String translateName(Field f) {
				
				String name = f.getName();
				char firstChar = name.charAt(0);
				return Character.toLowerCase(firstChar) + name.substring(1);
			}
		})*/.create();
	}
	
	public WSClient(URI serverUri) {
		super(serverUri);
		
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		
	}

	@Override
	public void onMessage(String message) {
		System.out.println(message);
		
		Command cmd = gson.fromJson(message, Command.class);

		
		System.out.println(cmd);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.printf("code: %n reason:%s remote:%b", code, reason,remote);
	}

	@Override
	public void onError(Exception ex) {
		ex.printStackTrace();		
	}
	
	public void SendCommand(Command command) {
		
	}
	
}
