package de.torui.coflsky.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

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
    		 String uuid = CoflSky.PlayerUUID;
    		try {
				socket = new WSClient(new URI(uri + uuid));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		thread = new Thread(socket);
    		thread.start();
    		isRunning=true;
    	}
    }
    
    public synchronized void stop() {
    	if(isRunning) {
    		try {
				socket.closeBlocking();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		isRunning = false;
    		socket = null;
    		socket = null;
    	}
    }
    
    public synchronized void SendMessage(Command cmd){
    	this.socket.SendCommand(cmd);
    }
}
