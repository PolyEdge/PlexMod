package cc.dyspore.plex.core.mineplex;

import cc.dyspore.plex.core.util.PlexUtil;
import net.minecraft.client.Minecraft;

import java.time.OffsetDateTime;

public class PlexMPLobby {
    public LobbyType type;
    public String name;

    public long joinTimeMs;
    public OffsetDateTime joinTimeDT;

    public PlexMPGame currentGame;

    public PlexMPLobby() {
        this.type = LobbyType.UNDETERMINED;

        this.joinTimeMs = Minecraft.getSystemTime();
        this.joinTimeDT = PlexUtil.getCurrentTime();
    }

    public enum LobbyType {
        UNDETERMINED,
        UNKNOWN,
        MAIN_HUB,
        GAME_LOBBY,
        GAME_INGAME,
        CLANS_HUB,
        CLANS_SERVER,

        // Only used in firing events
        E_WORLD_CHANGE,
        E_LOBBY_SWITCH,
        E_LOBBY_NAME_UPDATED,
        E_GAME_UPDATED,
        E_GAME_ENDED
    }
}
