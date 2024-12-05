package de.torui.coflsky.network;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketState;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.WSCommandHandler;
import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.JsonStringCommand;
import de.torui.coflsky.commands.RawCommand;
import net.minecraft.client.Minecraft;

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
	private WebSocket socket;
	public boolean shouldRun = false;
	public WebSocketState currentState = WebSocketState.CLOSED;
	
	public WSClient(URI uri) {
		this.uri = uri;
		
	}
	
	public synchronized void start() throws IOException, WebSocketException, NoSuchAlgorithmException {
		WebSocketFactory factory = new WebSocketFactory();
		
		/*// Create a custom SSL context.
		SSLContext context = NaiveSSLContext.getInstance("TLS");

		// Set the custom SSL context.
		factory.setSSLContext(context);

		// Disable manual hostname verification for NaiveSSLContext.
		//
		// Manual hostname verification has been enabled since the
		// version 2.1. Because the verification is executed manually
		// after Socket.connect(SocketAddress, int) succeeds, the
		// hostname verification is always executed even if you has
		// passed an SSLContext which naively accepts any server
		// certificate. However, this behavior is not desirable in
		// some cases and you may want to disable the hostname
		// verification. You can disable the hostname verification
		// by calling WebSocketFactory.setVerifyHostname(false).
		factory.setVerifyHostname(false);
		factory.*/
		factory.setVerifyHostname(false);
		factory.setSSLContext(NaiveSSLContext.getInstance("TLSv1.2"));
		factory.setConnectionTimeout(5*1000);
		this.socket = factory.createSocket(uri);
		this.socket.addListener(this);
		this.socket.connect();
	}
	
	public void stop() {
		System.out.println("Closing Socket");
		if(socket == null)
			return;
		socket.clearListeners();
		socket.disconnect();
		System.out.println("Socket closed");

	}
	
	@Override
	public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
		System.out.println("WebSocket Changed state to: " + newState);
		currentState = newState;
		
		boolean isActiveSocket = CoflSky.Wrapper.socket == this;
		if(newState == WebSocketState.CLOSED && shouldRun && isActiveSocket) {
			CoflSky.Wrapper.restartWebsocketConnection();
		}
		if(!isActiveSocket){
			websocket.clearListeners();
		}
		
		super.onStateChanged(websocket, newState);
	}

	
	
	 @Override
	    public void onTextMessage(WebSocket websocket, String text) throws Exception{
		
		//super.onTextMessage(websocket, text);
		 System.out.println("Received: "+ text);
		JsonStringCommand cmd = gson.fromJson(text, JsonStringCommand.class);
		//System.out.println(cmd);
		try {
			WSCommandHandler.HandleCommand(cmd, Minecraft.getMinecraft().thePlayer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void SendCommand(Command cmd) {
		SendCommand(new RawCommand(cmd.getType().ToJson(),gson.toJson(cmd.getData())));
	}
	public void SendCommand(RawCommand cmd) {
		Send(cmd);
	}
	
	public synchronized void Send(Object obj) {
		String json = gson.toJson(obj);
		System.out.println("###Sending message of json value " + json);
		if(this.socket == null)
			try 
			{
				start();
			} catch(Exception e)
			{
		 		System.out.println("Ran into an error on implicit start for send: "+ e);
			}
		this.socket.sendText(json);
	}
		
	
	
}