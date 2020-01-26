package cc.dyspore.plex.ui.widget.autocomplete;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlexUIAutoCompleteContainer {
    public List<PlexUIAutoCompleteItem> autoCompleteItems = new ArrayList<>();
    public Map<String, Integer> groupSortIndex = new ConcurrentHashMap<String, Integer>() {
    };

    public PlexUIAutoCompleteItem addItem(PlexUIAutoCompleteItem item) {
        if (!this.autoCompleteItems.contains(item)) {
            this.autoCompleteItems.add(item);
        }
        return item;
    }

    public PlexUIAutoCompleteItem removeItem(PlexUIAutoCompleteItem item) {
        while (this.autoCompleteItems.contains(item)) {
            this.autoCompleteItems.remove(item);
        }
        return item;
    }

    @Deprecated
    public PlexUIAutoCompleteItem getItemById(String id) {
        for (PlexUIAutoCompleteItem item : this.autoCompleteItems) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        return null;
    }

    public List<PlexUIAutoCompleteItem> getItemsByGroup(String group) {
        List<PlexUIAutoCompleteItem> items = new ArrayList<>();
        for (PlexUIAutoCompleteItem item : this.autoCompleteItems) {
            if (item.group.equals(group)) {
                items.add(item);
            }
        }
        return items;
    }

    public PlexUIAutoCompleteItem getItem(String group, String id) {
        for (PlexUIAutoCompleteItem item : this.autoCompleteItems) {
            if (item.id.equals(id) && item.group.equals(group)) {
                return item;
            }
        }
        return null;
    }

    public PlexUIAutoCompleteItem getItemOrNew(String group, String id) {
        for (PlexUIAutoCompleteItem item : this.autoCompleteItems) {
            if (item.id.equals(id) && item.group.equals(group)) {
                return item;
            }
        }
        return new PlexUIAutoCompleteItem(id, group);
    }

    public void sortItems() {
        Collections.sort(this.autoCompleteItems);
    }
}
