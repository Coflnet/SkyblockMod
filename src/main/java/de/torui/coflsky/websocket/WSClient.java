package de.torui.coflsky.websocket;

import java.io.IOException;
import java.net.URI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketState;
import net.minecraft.client.Minecraft;

import de.torui.coflsky.WSCommandHandler;
import de.torui.coflsky.core.Command;

public class WSClient extends WebSocketAdapter {

	
	public static Gson gson;
	
	
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
	public URI uri;
	public WebSocket socket;
	
	public WSClient(URI uri) {
		this.uri = uri;
	}
	
	public void start() throws IOException, WebSocketException {
		WebSocketFactory factory = new WebSocketFactory();
		this.socket = factory.createSocket(uri);
		this.socket.addListener(this);
		this.socket.connect();
	}
	
	public void stop() {
		System.out.println("Closing Socket");
	//	socket.sendClose();
		socket.clearListeners();
	
		socket.disconnect();
		/*try {
			socket.getConnectedSocket().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WebSocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.out.println("Socket closed");

	}
	
	@Override
	public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
		System.out.println("WebSocket Changed state to: " + newState);
		super.onStateChanged(websocket, newState);
	}

	
	
	 @Override
	    public void onTextMessage(WebSocket websocket, String text) throws Exception{
		
		//super.onTextMessage(websocket, text);
		 System.out.println("Received: "+ text);
		Command cmd = gson.fromJson(text, Command.class);
		//System.out.println(cmd);
		WSCommandHandler.HandleCommand(cmd, Minecraft.getMinecraft().thePlayer);
		
	}

	public void SendCommand(Command cmd) {
		String json = gson.toJson(cmd);
		this.socket.sendText(json);
	}

		
	
	
}
	
/*
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
		})*.create();
	}
	
	public WSClient(URI serverUri) {
		super(serverUri);
		
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		
	}

	@Override
	public void onMessage(String message) {
		//System.out.println(message);
		
		Command cmd = gson.fromJson(message, Command.class);
		//System.out.println(cmd);
		WSCommandHandler.HandleCommand(cmd, Minecraft.getMinecraft().thePlayer);
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
		String json = gson.toJson(command);
		this.send(json);
	}
	
}
*/
