package pw.ipex.plex.ui;

import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreRenderUtils;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.ui.PlexUIScrolledItem;

public class PlexUIAutoCompleteItem implements PlexUIScrolledItem {
    public boolean softSelected = false;
    public boolean selected = false;
    public String attachedPlayerHead = null;
    public String displayText = "";
    public String searchText = "";
    public String autoCompleteText = "";
    public String id = "";
    public int defaultColour = 0xffffff;

    @Override
    public boolean listItemIsSelected() {
        return this.softSelected || this.selected;
    }

    @Override
    public void listItemClick() {
        this.selected = true;
    }

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
    public void listItemRenderText(int x, int y, int cellWidth, int cellHeight, boolean selected, boolean mouseOver) {
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
        PlexCoreRenderUtils.drawScaledStringLeftSide(finalText, startX, y + ((cellHeight / 2) - (Plex.minecraft.fontRendererObj.FONT_HEIGHT / 2)), this.defaultColour, 1.0F);
        if (playerHead) {
            PlexCoreRenderUtils.staticDrawGradientRect(playerHeadX, playerHeadY, playerHeadX + playerHeadSize, playerHeadY + playerHeadSize, 0xffffffff, 0xffffffff);
            PlexCoreRenderUtils.drawPlayerHead(this.attachedPlayerHead, playerHeadX, playerHeadY, playerHeadSize);
        }
    }

    @Override
    public int listItemGetForegroundColour() {
        return 0;
    }
}
