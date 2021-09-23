package de.torui.coflsky.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import com.neovisionaries.ws.client.WebSocketException;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.core.Command;


public class WSClientWrapper {
    public WSClient socket;
    public Thread thread;
    public boolean isRunning;
    public String uri = "";
    
    public WSClientWrapper(String uri) {
    	this.uri = uri;
    }
    
    public synchronized void start() {
    	if(!isRunning) {
    		try {
    			
				socket = new WSClient(new URI(uri));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		/*thread = new Thread(socket);
    		thread.start();
    		isRunning=true;*/
    		isRunning = true;
    		try {
				socket.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WebSocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public synchronized void stop() {
    	if(isRunning) {
    	/*	try {
				//socket.closeBlocking();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
    		socket.stop();
    		isRunning = false;
    		socket = null;
    	}
    }
    
    public synchronized void SendMessage(Command cmd){
    	this.socket.SendCommand(cmd);
    }
}
