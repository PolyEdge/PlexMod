package pw.ipex.plex.core.mineplex;

public enum PlexCoreLobbyType {
	SERVER_UNDETERMINED,
	SERVER_UNKNOWN,
	SERVER_HUB,
	CLANS_HUB,
	CLANS_SERVER,
	GAME_LOBBY,
	GAME_INGAME, 
	OFFLINE,

	// Only used in firing events
	E_SWITCHED_SERVERS,
	E_GAME_UPDATED,
}
