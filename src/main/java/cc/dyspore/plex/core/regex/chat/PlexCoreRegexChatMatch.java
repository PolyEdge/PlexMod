package cc.dyspore.plex.core.regex.chat;

import net.minecraft.util.IChatComponent;
import cc.dyspore.plex.core.regex.PlexCoreRegex;
import cc.dyspore.plex.core.regex.PlexCoreRegexEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlexCoreRegexChatMatch {
    private Map<String, PlexCoreRegexChatMatchItem> entries = new HashMap<>();

    public PlexCoreRegexChatMatch(IChatComponent component) {
        for (PlexCoreRegexEntry entry : PlexCoreRegex.getEntriesMatchingText(component.getFormattedText())) {
            this.entries.put(entry.entryName, new PlexCoreRegexChatMatchItem(component.getFormattedText(), entry));
        }
    }

    public boolean matches(String entryName) {
        return this.entries.containsKey(entryName);
    }

    public PlexCoreRegexChatMatchItem get(String entryName) {
        return this.entries.get(entryName);
    }

    public List<PlexCoreRegexChatMatchItem> getEntries() {
        return new ArrayList<>(this.entries.values());
    }
}
