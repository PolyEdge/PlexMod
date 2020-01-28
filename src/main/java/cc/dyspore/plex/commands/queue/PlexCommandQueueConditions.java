package cc.dyspore.plex.commands.queue;

import cc.dyspore.plex.Plex;
import net.minecraft.client.Minecraft;

public class PlexCommandQueueConditions implements Cloneable {
    private long commandDelay = 1600L;
    private long lobbySwitchDelay = 4000L;
    private long joinServerDelay = 4000L;
    private long chatOpenDelay = 1000L;
    private boolean sendableOffMineplex = false;

    public PlexCommandQueueConditions clone() {
        try {
            return (PlexCommandQueueConditions) super.clone();
        }
        catch (CloneNotSupportedException e) {
            return new PlexCommandQueueConditions();
        }
    }

    private long processDelta(long delta) {
        return delta <= 0 ? -10000L : delta;
    }

    public PlexCommandQueueConditions afterCommand(long commandDelay) {
        this.commandDelay = this.processDelta(commandDelay);
        return this;
    }

    public PlexCommandQueueConditions afterLobbyChange(long lobbySwitchDelay) {
        this.lobbySwitchDelay = this.processDelta(lobbySwitchDelay);
        return this;
    }

    public PlexCommandQueueConditions afterLogon(long joinServerDelay) {
        this.joinServerDelay = this.processDelta(joinServerDelay);
        return this;
    }

    public PlexCommandQueueConditions afterChatOpen(long chatOpenDelay) {
        this.chatOpenDelay = this.processDelta(chatOpenDelay);
        return this;
    }

    public PlexCommandQueueConditions canSendOffMineplex(boolean sendableOffMineplex) {
        this.sendableOffMineplex = sendableOffMineplex;
        return this;
    }

    public boolean sendable() {
        long time = Minecraft.getSystemTime();
        if (!Plex.gameState.isMineplex && !this.sendableOffMineplex) {
            return false;
        }
        if (time < Plex.queue.lastCommandSent + commandDelay) {
            return false;
        }
        if (time < Plex.gameState.currentLobby.joinTimeMs + lobbySwitchDelay) {
            return false;
        }
        if (time < Plex.gameState.joinTimeMS + joinServerDelay) {
            return false;
        }
        if (time < Plex.listeners.getChatOpenTime() + chatOpenDelay) {
            return false;
        }
        return true;
    }
}
