package cc.dyspore.plex.core.mineplex;

import cc.dyspore.plex.core.util.PlexUtil;
import net.minecraft.client.Minecraft;

import java.time.OffsetDateTime;

public class PlexGame {
    public String name;
    public boolean spectating;
    public boolean ended;

    public long startTimeMS;
    public OffsetDateTime startTimeDT;

    public PlexGame(String name, boolean spectating) {
        this.name = name;
        this.spectating = spectating;

        this.startTimeMS = Minecraft.getSystemTime();
        this.startTimeDT = PlexUtil.getCurrentTime();
    }
}
