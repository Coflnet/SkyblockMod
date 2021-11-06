package de.torui.coflsky.network;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import com.neovisionaries.ws.client.WebSocketException;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.commands.Command;
import de.torui.coflsky.commands.JsonStringCommand;
import de.torui.coflsky.minecraft_integration.PlayerDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import de.torui.coflsky.minecraft_integration.CoflSessionManager;


public class WSClientWrapper {
    public WSClient socket;
   // public Thread thread;
    public boolean isRunning;
    
    private String[] uris;

    
    public WSClientWrapper(String[] uris) {
    	this.uris = uris;
    }
    
    public void restartWebsocketConnection() {
    	socket.socket.clearListeners();
    	socket.stop();
    	
    	System.out.println("Sleeping...");
    	Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Lost connection to Coflnet, trying to reestablish the connection in 2 Seconds..."));
    	
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	socket = new WSClient(socket.uri);
    	isRunning = false;    	
    	start();
    }
    
    
    public boolean startConnection() {
    	
    	if(isRunning)
    		return false;
    	
    	for(String s : uris) {
    		
    		System.out.println("Trying connection with uri=" + s);
    		
    		if(initializeNewSocket(s)) {
    			return true;
    		}
    	}
    	
    	Minecraft.getMinecraft().thePlayer.addChatMessage(
    			new ChatComponentText("Cofl could not establish a connection to any server!"+
    	"\nIf you think this is a bug. Please report it on our Discord and include the ")
    			.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
    			.appendSibling(new ChatComponentText("logs!")
    							.setChatStyle(new ChatStyle()
								.setUnderlined(true)
								.setColor(EnumChatFormatting.BLUE)
    							.setChatClickEvent(new ClickEvent(Action.OPEN_URL, "https://www.youtube.com/watch?v=9Kb29wEcMcs")))));
    	
    	return false;
    	//throw new Error("Could not connect to any websocket remote!");
    	
    }
    
    
    
    public boolean initializeNewSocket(String uriPrefix) {
    	
    	
    	String uri = uriPrefix;
    	uri += "?version=" + CoflSky.VERSION;
    	
    	String username = PlayerDataProvider.getUsername();
    	uri += "&player=" + username;
    	
    	//Generate a CoflSession
    	
    	try {
			CoflSessionManager.UpdateCoflSessions();
			String coflSessionID = CoflSessionManager.GetCoflSession(username).SessionUUID;
			
			uri += "&SId=" + coflSessionID;	
	    	
			socket = new WSClient(URI.create(uri));
			
			boolean successfull = start();
			if(successfull) {
				socket.shouldRun = true;
			}
			return successfull;
    	} catch(IOException e) {
    		e.printStackTrace();
    	}			

		return false;   	
    	
    }
    
    private synchronized boolean start() {
    	if(!isRunning) {
    		try {
    			
				socket.start();
				isRunning = true;

				return true;
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
    		return false;
    	}
		return false;
    }
    
    public synchronized void stop() {
    	if(isRunning) {
    		socket.shouldRun = false;
    		socket.stop();
    		isRunning = false;
    		socket = null;
    	}
    }
    
    public synchronized void SendMessage(Command cmd){
    	if(this.isRunning) {
    		this.socket.SendCommand(cmd);
    	} else {
    		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("tried sending a callback to coflnet but failed. the connection must be closed."));
    	}
    	
    }

	
	public String GetStatus() {
		return "" + isRunning + " " +  
	    (this.socket!=null ? this.socket.currentState.toString() : "NOT_INITIALIZED");
	}
}
