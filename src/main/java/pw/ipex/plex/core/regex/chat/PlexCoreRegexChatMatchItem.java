package pw.ipex.plex.core.regex.chat;

import pw.ipex.plex.core.regex.PlexCoreRegexEntry;

import java.util.HashMap;
import java.util.Map;

public class PlexCoreRegexChatMatchItem {
    public String chatMessage;
    public PlexCoreRegexEntry entry;
    public Map<String, String> fields = new HashMap<>();

    public PlexCoreRegexChatMatchItem(String chatMessage, PlexCoreRegexEntry entry) {
        this.entry = entry;
        this.chatMessage = chatMessage;
        this.fields = this.entry.getAllFields(this.chatMessage);
    }

    public boolean hasField(String field) {
        return this.fields.containsKey(field);
    }

    public String getField(String field) {
        return this.fields.get(field);
    }

    public boolean hasTag(String name) {
        return this.entry.hasField(name);
    }
}
