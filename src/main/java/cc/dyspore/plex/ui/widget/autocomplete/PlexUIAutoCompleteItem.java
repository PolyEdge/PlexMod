package cc.dyspore.plex.ui.widget.autocomplete;

import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.util.PlexUtilRender;
import cc.dyspore.plex.ui.widget.itemlist.PlexUIScrolledItem;

public class PlexUIAutoCompleteItem implements PlexUIScrolledItem, Comparable<PlexUIAutoCompleteItem> {
    public boolean softSelected = false;
    public boolean selected = false;
    public String attachedPlayerHead = null;
    public String displayText = "";
    public String searchText = "";
    public String autoCompleteText = "";
    public Integer localSortingIndex = null;
    public Integer groupSortingIndex = null;
    public String sortingString;
    public String id;
    public String group;
    public int defaultColour = 0xffffff;

    public PlexUIAutoCompleteItem(String id, String group) {
        this.id = id;
        this.group = group;
    }

    public PlexUIAutoCompleteItem setDisplayText(String text) {
        this.displayText = text;
        return this;
    }

    public PlexUIAutoCompleteItem setSearchText(String text) {
        this.searchText = text;
        return this;
    }

    public PlexUIAutoCompleteItem setAutoCompleteText(String text) {
        this.autoCompleteText = text;
        return this;
    }

    public PlexUIAutoCompleteItem setHead(String player) {
        this.attachedPlayerHead = player;
        return this;
    }

    public PlexUIAutoCompleteItem setLocalSortingIndex(int index) {
        this.localSortingIndex = index;
        return this;
    }

    public PlexUIAutoCompleteItem setGlobalSortingIndex(int index) {
        this.groupSortingIndex = index;
        return this;
    }

    public PlexUIAutoCompleteItem setSortingString(String sortString) {
        this.sortingString = sortString;
        return this;
    }

    public String getSortingString() {
        return this.sortingString == null || this.sortingString.trim().equals("") ? this.id : this.sortingString;
    }

    @Override
    public boolean listItemIsSelected() {
        return this.softSelected || this.selected;
    }

    @Override
    public void listItemClick() {
        this.selected = true;
    }

    @Override
    public void listItemOtherItemClicked() {}

    @Override
    public void listItemSelect() {
        this.softSelected = true;
    }

    @Override
    public int listItemGetHeight() {
        return -1;
    }

    @Override
    public String listItemGetText() {
        return null;
    }

    @Override
    public String listItemGetSearchText() {
        return this.searchText;
    }

    @Override
    public void listItemRenderText(int x, int y, int cellWidth, int cellHeight, float alpha, boolean selected, boolean mouseOver) {
        int startX = x;
        boolean playerHead = false;
        int playerHeadSize = (int) ((float) cellHeight * 0.75F);
        int playerHeadX = x; //(x + cellWidth) - (playerHeadSize + ((cellHeight - playerHeadSize) / 2));
        int playerHeadY = y + ((cellHeight - playerHeadSize) / 2);

        if (this.attachedPlayerHead != null) {
            playerHead = true;
            startX = (int) (x + playerHeadSize + playerHeadSize * 0.45F);
        }

        String finalText = Plex.minecraft.fontRendererObj.trimStringToWidth(this.displayText, (x + cellWidth) - startX);
        PlexUtilRender.drawScaledStringLeftSide(finalText, startX, y + ((cellHeight / 2) - (Plex.minecraft.fontRendererObj.FONT_HEIGHT / 2)), this.defaultColour, 1.0F);
        if (playerHead) {
            PlexUtilRender.drawGradientRect(playerHeadX, playerHeadY, playerHeadX + playerHeadSize, playerHeadY + playerHeadSize, 0xffffffff, 0xffffffff);
            PlexUtilRender.drawPlayerHead(this.attachedPlayerHead, playerHeadX, playerHeadY, playerHeadSize);
        }
    }

    @Override
    public int listItemGetForegroundColour() {
        return 0;
    }

    @Override
    public int compareTo(PlexUIAutoCompleteItem item) {
        if (this.group.equals(item.group)) {
            return this.compareAsGroup(this, item);
        }

        if (this.groupSortingIndex != null || item.groupSortingIndex != null) {
            if (this.groupSortingIndex != null && item.groupSortingIndex != null) {
                if (this.groupSortingIndex.equals(item.groupSortingIndex)) {
                    return this.compareAsGroup(this, item);
                }
                return this.groupSortingIndex.compareTo(item.groupSortingIndex);
            }
            return this.localSortingIndex == null ? 1 : -1;
        }
        return this.group.compareTo(item.group);
    }

    public int compareAsGroup(PlexUIAutoCompleteItem item1, PlexUIAutoCompleteItem item2) {
        if (item1.localSortingIndex == null && item2.localSortingIndex == null) {
            return item1.getSortingString().compareTo(item2.getSortingString());
        }
        if (item1.localSortingIndex == null || item2.localSortingIndex == null) {
            return item1.localSortingIndex == null ? 1 : -1;
        }
        if (!item1.localSortingIndex.equals(item2.localSortingIndex))  {
            return item1.localSortingIndex.compareTo(item2.localSortingIndex);
        }
        return item1.getSortingString().compareTo(item2.getSortingString());
    }
}
