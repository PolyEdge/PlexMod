package pw.ipex.plex.core.mineplex;

import java.time.OffsetDateTime;
import java.util.HashMap;

import net.minecraft.client.Minecraft;

public class PlexCoreServerState {
	
	public boolean onMineplex = false;
	public long serverJoinTime = -1L;
	public long lastServerJoin = -1L;
	public OffsetDateTime serverJoinDateTime = null;
	public String serverIP = "";
	public String serverHostname = "";
	
	public PlexCoreLobbyType currentLobbyType = PlexCoreLobbyType.OFFLINE;
	public long lastLobbySwitch = -1L;
	public String currentLobbyName = null;
	public String updatedLobbyName = null;
	
	public String currentGameName = null;
	public long gameStartTime = -1;
	public OffsetDateTime gameStartDateTime = null;
	public boolean isGameSpectator = false;
	
	public long lastControlInput;
	public long lastChatOpen = -1L;

	public boolean canUseEmotes = true;

	public HashMap<String, String> emotesList = new HashMap<>();
	
	
	public void setToOnline() {
		this.onMineplex = true;
		this.serverJoinDateTime = OffsetDateTime.now();
		this.lastServerJoin = Minecraft.getSystemTime();
		this.currentLobbyType = PlexCoreLobbyType.SERVER_UNDETERMINED;
		this.currentGameName = null;
		this.currentLobbyName = null;
		this.gameStartTime = -1L;
		this.gameStartDateTime = null;
		this.isGameSpectator = false;
		this.lastLobbySwitch = Minecraft.getSystemTime();
	}
	
	public void resetToOffline() {
		this.onMineplex = false;
		this.currentLobbyType = PlexCoreLobbyType.OFFLINE;
		this.currentGameName = null;
		this.currentLobbyName = null;
		this.gameStartTime = -1L;
		this.gameStartDateTime = null;
		this.isGameSpectator = false;
		this.lastLobbySwitch = -1L;
		this.serverIP = "";
		this.serverHostname = "";
	}
}
