package cc.dyspore.plex.commands.queue;

import java.util.HashSet;
import java.util.Set;

public class PlexCommandQueueProcessContext {
    public Set<String> blockingGroups = new HashSet<>();
    public boolean sendError = false;
}
