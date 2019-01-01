package pw.ipex.plex.core.loop;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlexCoreEventLoopManager {
    private Map<String, PlexCoreEventLoop> eventLoops = new ConcurrentHashMap<>();

    public PlexCoreEventLoop get(String name) {
        if (!this.eventLoops.containsKey(name)) {
            this.eventLoops.put(name, new PlexCoreEventLoop(name));
        }
        return this.eventLoops.get(name);
    }
}
