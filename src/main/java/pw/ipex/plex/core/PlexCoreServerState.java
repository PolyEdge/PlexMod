package pw.ipex.plex.core;

import java.time.OffsetDateTime;

import net.minecraft.client.Minecraft;

public class PlexCoreServerState {
	
	public Boolean onMineplex = false;
	public Long serverJoinTime = null;
	public Long lastServerJoin = 0L;
	public OffsetDateTime serverJoinDateTime = null;
	public String serverIP = "";
	
	public PlexCoreLobbyType currentLobbyType = PlexCoreLobbyType.OFFLINE;
	public Long lastLobbySwitch = 0L;
	public String currentLobbyName = null;
	
	public String currentGameName = null;
	public Long gameStartEpoch = null;
	public OffsetDateTime gameStartDateTime = null;
	public Boolean isGameSpectator = false;
	
	public Long lastControlInput = null;
	public Long lastChatOpen = 0L;
	
	
	public void setToOnline() {
		this.onMineplex = true;
		this.serverJoinDateTime = OffsetDateTime.now();
		this.lastServerJoin = Minecraft.getSystemTime();
		this.currentLobbyType = PlexCoreLobbyType.SERVER_UNDETERMINED;
		this.currentGameName = null;
		this.currentLobbyName = null;
		this.gameStartEpoch = null;
		this.gameStartDateTime = null;
		this.isGameSpectator = false;
		this.lastLobbySwitch = Minecraft.getSystemTime();
	}
	
	public void resetToOffline() {
		this.onMineplex = false;
		this.currentLobbyType = PlexCoreLobbyType.OFFLINE;
		this.currentGameName = null;
		this.currentLobbyName = null;
		this.gameStartEpoch = null;
		this.gameStartDateTime = null;
		this.isGameSpectator = false;
		this.lastLobbySwitch = 0L;
	}
}
