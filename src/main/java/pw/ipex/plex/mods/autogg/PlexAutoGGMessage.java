package pw.ipex.plex.mods.autogg;

import pw.ipex.plex.ui.widget.itemlist.PlexUIScrolledItem;

public class PlexAutoGGMessage implements PlexUIScrolledItem {
    public String message;
    public boolean selected = false;

    public PlexAutoGGMessage(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean listItemIsSelected() {
        return this.selected;
    }

    @Override
    public void listItemSelect() {
        this.selected = true;
    }

    @Override
    public void listItemClick() {
        this.selected = !this.selected;
    }

    @Override
    public void listItemOtherItemClicked() {
        this.selected = false;
    }

    @Override
    public int listItemGetHeight() {
        return 15;
    }

    @Override
    public String listItemGetText() {
        return this.message;
    }

    @Override
    public String listItemGetSearchText() {
        return null;
    }

    @Override
    public void listItemRenderText(int x, int y, int cellWidth, int cellHeight, float alpha, boolean selected, boolean mouseOver) {

    }

    @Override
    public int listItemGetForegroundColour() {
        return 0xffffffff;
    }
}
