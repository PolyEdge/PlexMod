package cc.dyspore.plex.core.mineplex;

import cc.dyspore.plex.core.util.PlexUtil;
import net.minecraft.client.Minecraft;

import java.time.OffsetDateTime;

public class PlexLobby {
    public PlexLobbyType type;
    public String name;

    public long joinTimeMs;
    public OffsetDateTime joinTimeDT;

    public PlexGame currentGame;

    public PlexLobby() {
        this.type = PlexLobbyType.UNDETERMINED;

        this.joinTimeMs = Minecraft.getSystemTime();
        this.joinTimeDT = PlexUtil.getCurrentTime();
    }
}
