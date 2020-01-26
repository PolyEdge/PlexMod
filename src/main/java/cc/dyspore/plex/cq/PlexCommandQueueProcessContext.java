package cc.dyspore.plex.cq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlexCommandQueueProcessContext {
    public Set<String> blockingGroups = new HashSet<>();
    public boolean sendError = false;
}
