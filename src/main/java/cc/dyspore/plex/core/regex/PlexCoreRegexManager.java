package cc.dyspore.plex.core.regex;

import net.minecraft.util.IChatComponent;
import cc.dyspore.plex.core.regex.chat.PlexCoreRegexChatMatch;

import java.util.*;

public class PlexCoreRegexManager {
    public static int MAX_CACHE = 2;
    private static List<Map.Entry<IChatComponent, PlexCoreRegexChatMatch>> cache = new ArrayList<>();

    private static void cleanCache() {
        while (cache.size() > MAX_CACHE) {
            cache.remove(0);
        }
    }

    public static PlexCoreRegexChatMatch getChatMatch(IChatComponent component) {
        for (Map.Entry<IChatComponent, PlexCoreRegexChatMatch> item : cache) {
            if (item.getKey() == component) {
                return item.getValue();
            }
        }
        PlexCoreRegexChatMatch match = new PlexCoreRegexChatMatch(component);
        cache.add(new AbstractMap.SimpleEntry<>(component, match));
        cleanCache();
        return match;
    }
}
