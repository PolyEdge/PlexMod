package cc.dyspore.plex.core.mineplex;

public enum PlexLobbyType {
	UNDETERMINED,
	UNKNOWN,
	MAIN_HUB,
	GAME_LOBBY,
	GAME_INGAME,
	CLANS_HUB,
	CLANS_SERVER,

	// Only used in firing events
	E_LOBBY_SWITCH,
	E_LOBBY_NAME_UPDATED,
	E_GAME_UPDATED,
}
