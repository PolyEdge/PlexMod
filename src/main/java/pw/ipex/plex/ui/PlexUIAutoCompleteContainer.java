package pw.ipex.plex.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlexUIAutoCompleteContainer {
    public List<PlexUIAutoCompleteItem> autoCompleteItems = new ArrayList<>();

    public PlexUIAutoCompleteItem addItem(PlexUIAutoCompleteItem item) {
        this.autoCompleteItems.add(item);
        return item;
    }

    public PlexUIAutoCompleteItem getItemById(String id) {
        for (PlexUIAutoCompleteItem item : this.autoCompleteItems) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        return null;
    }

    public void sortItemsById() {
        Collections.sort(this.autoCompleteItems, (item1, item2) -> item1.id.compareTo(item2.id));
    }
}
