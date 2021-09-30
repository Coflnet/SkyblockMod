package de.torui.coflsky.minecraft_integration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraftforge.fml.common.Loader;

public class TemporarySession {
	private static Gson gson = new GsonBuilder()  .registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
        @Override
        public void write(JsonWriter out, ZonedDateTime value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public ZonedDateTime read(JsonReader in) throws IOException {
            return ZonedDateTime.parse(in.nextString());
        }
    })
    .enableComplexMapKeySerialization().create();
	public static class TempSession {
		
		public String SessionUUID;
		public ZonedDateTime timestampCreated;
		public TempSession() {}
		public TempSession(String sessionUUID, ZonedDateTime timestampCreated) {
			super();
			SessionUUID = sessionUUID;
			this.timestampCreated = timestampCreated;
		}
		
	}
	
	public static void UpdateSessions() throws IOException {
		Map<String, TempSession> sessions = GetSessions();
		
		for (String username : sessions.keySet()) {
			if(!isValidSession(sessions.get(username))) {
				DeleteTempSession(username);
			}
		}
	}
	
	public static Path GetTempFileFolder() {
		
		Path dataPath = Paths.get(Loader.instance().getConfigDir().getPath(), "CoflSky", "sessions");
		dataPath.toFile().mkdirs();
		
		return dataPath;
	}
	
	public static Map<String, TempSession> GetSessions() throws IOException{
		
		File[] sessions = GetTempFileFolder().toFile().listFiles();
		
		Map<String, TempSession> map = new HashMap<>();
		
		for (int i= 0; i<sessions.length;i++) {
			map.put(sessions[i].getName(),  GetSession(sessions[i].getName()));
		}
		
		return map;
	}
	
	public static boolean isValidSession(TempSession session) {
		if(session.timestampCreated.plus(Duration.ofDays(7)).isAfter(ZonedDateTime.now())) {
			return true;
		}
		return false;
	}
	
	private static Path GetUserPath(String username) {
		return Paths.get(GetTempFileFolder().toString() + "/" + username);
	}
	public static void DeleteTempSession(String username) {
		Path path =GetUserPath(username);
		path.toFile().delete();
	}
	
	public static TempSession GetSession(String username) throws IOException {
		Path path = GetUserPath(username);
		File file = path.toFile();
		
		if(!file.exists()) {
			TempSession session = new TempSession(UUID.randomUUID().toString(), ZonedDateTime.now());
			OverwriteTempSession(username, session);
			return session;
		}
		
		BufferedReader reader = new BufferedReader( new InputStreamReader(new FileInputStream(file)));
		String raw = reader.lines().collect(Collectors.joining("\n"));
		
		reader.close();
		TempSession session = gson.fromJson(raw, TempSession.class);
		return session;
	}
	
	public static boolean OverwriteTempSession(String username, TempSession session) throws IOException {
		
		
		Path path = GetUserPath(username);
		File file = path.toFile();
		file.createNewFile();
		
		String data = gson.toJson(session);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		bw.append(data);
		bw.flush();
		bw.close();
		
		return true;
	}
}
