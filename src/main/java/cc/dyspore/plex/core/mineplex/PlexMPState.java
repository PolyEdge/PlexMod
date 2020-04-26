package cc.dyspore.plex.core.mineplex;

import java.time.OffsetDateTime;
import java.util.HashMap;

public class PlexMPState {
	public boolean isMultiplayer = false;
	public boolean isMineplex = false;

	public long joinTimeMS;
	public OffsetDateTime joinTimeDT;

	public PlexMPLobby currentLobby;

	public String multiplayerServerIP;
	public String multiplayerServerHostname;

	public String currentClansSeason = null;

	public boolean emotesDisallowed = false;
	public HashMap<String, String> emotesList = new HashMap<>();
	
	public void reset() {
		this.isMineplex = false;
		this.isMultiplayer = false;
		this.currentLobby = null;

		this.multiplayerServerIP = null;
		this.multiplayerServerHostname = null;
	}
}
