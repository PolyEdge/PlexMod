package pw.ipex.plex.core;

public enum PlexCoreLobbyType {
	SERVER_UNDETERMINED,
	SERVER_UNKNOWN,
	SERVER_HUB,
	CLANS_HUB,
	CLANS_SERVER,
	GAME_LOBBY,
	GAME_INGAME, 
	OFFLINE,
	
	SWITCHED_SERVERS // Only used in firing events
}
