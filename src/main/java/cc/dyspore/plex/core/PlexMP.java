package cc.dyspore.plex.core;

import cc.dyspore.plex.core.util.PlexUtil;
import net.minecraft.client.Minecraft;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class PlexMP {
    public static Session session = new Session();
    public static Lobby lobby = null;
    public static Game game = null;

    public static class Session {
        public boolean isMultiplayer = false;
        public boolean isMineplex = false;

        public String multiplayerServerIP;
        public String multiplayerServerHostname;
        public DT joinTime;

        public String currentClansSeason = null;

        public boolean emotesDisallowed = false;
        public Map<String, String> emoteList = new HashMap<>();

        public void join() {
            this.isMultiplayer = true;
            this.joinTime = new DT();
        }

        public void leave() {
            this.isMineplex = false;
            this.isMultiplayer = false;
            this.multiplayerServerIP = null;
            this.multiplayerServerHostname = null;
            this.joinTime = null;
        }
    }

    public static class Lobby {
        public LobbyType type;
        public String server;

        public DT joinTime;

        public Lobby() {
            this.type = LobbyType.UNDETERMINED;

            this.joinTime = new DT();
        }
    }

    public enum LobbyType {
        UNDETERMINED,
        UNKNOWN,
        MAIN_HUB,
        GAME_LOBBY,
        GAME_INGAME,
        CLANS_HUB,
        CLANS_SERVER,

        // Used in PlexModBase.onLobbyUpdate
        E_WORLD_CHANGE,
        E_LOBBY_SWITCH,
        E_LOBBY_NAME_UPDATED,
        E_GAME_UPDATED,
        E_GAME_ENDED
    }

    public static class Game {
        public String name;
        public boolean spectating;
        public boolean ended;

        public DT startTime;

        public Game(String name, boolean spectating) {
            this.name = name;
            this.spectating = spectating;

            this.startTime = new DT();
        }
    }

    public static class DT {
        public long milliseconds = Minecraft.getSystemTime();
        public OffsetDateTime dateTime = PlexUtil.getCurrentTime();
    }

    public static void reset() {
        session.leave();
        lobby = null;
        game = null;
    }
}
