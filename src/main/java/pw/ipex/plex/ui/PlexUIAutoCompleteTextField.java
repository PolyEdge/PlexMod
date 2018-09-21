package pw.ipex.plex.ui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreRenderUtils;
import pw.ipex.plex.core.PlexCoreUtils;

import java.util.ArrayList;
import java.util.List;

public class PlexUIAutoCompleteTextField {
    public GuiTextField text;
    public PlexUIScrolledItemList autoCompleteList;
    public Integer id;
    public FontRenderer fontRendererInstance;
    public Integer xPosition;
    public Integer yPosition;
    public Integer itemWidth;
    public Integer itemHeight;

    public Boolean autoCompleteListVisible = false;

    public List<? extends PlexUIAutoCompleteItem> items = new ArrayList<>();

    public Integer listBackgroundColour = 0x000000;
    public Integer listBorderColour = 0xff757575;

    public PlexUIAutoCompleteTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        this.id = componentId;
        this.fontRendererInstance = fontrendererObj;
        this.xPosition = x;
        this.yPosition = y;
        this.itemWidth = par5Width;
        this.itemHeight = par6Height;
        this.text = new GuiTextField(this.id, this.fontRendererInstance, this.xPosition, this.yPosition, this.itemWidth, this.itemHeight);
        this.autoCompleteList = new PlexUIScrolledItemList(this.items, 0, 0, 0, 0);
        this.autoCompleteList.setPadding(10, 0);
        this.autoCompleteList.defaultEntryHeight = 10;
        this.setAutocompleteListPosition(this.xPosition, this.yPosition - 50, this.xPosition + this.itemWidth, this.yPosition - 1);
    }

    public void setAutocompleteListPosition(int x, int y, int endX, int endY) {
        this.autoCompleteList.setPosition(x, y - 15, endX, endY - 15);
    }

    public void setAutoCompleteListVisible(boolean visible) {
        this.autoCompleteListVisible = visible;
    }

    public void setAutoCompleteItems(List<? extends PlexUIAutoCompleteItem> items) {
        this.items = items;
    }

    private void updateAutoCompleteItems() {
        this.autoCompleteList.items = this.items;
    }

    public boolean getAutoCompleteListVisible() {
        if (!this.autoCompleteListVisible) {
            return false;
        }
        this.updateAutoCompleteItems();
        String lastWord = this.getLastWordInBox();
        if (lastWord.trim().equals("")) {
            return false;
        }
        return this.autoCompleteList.getItemsMatchingSearchTerm(lastWord).size() != 0;
    }

    public String getLastWordInBox() {
        if (this.text.getText().endsWith(" ")) {
            return "";
        }
        String[] words = this.text.getText().split("\\s");
        if (words.length == 0) {
            return "";
        }
        return words[words.length - 1];
    }

    public List<? extends PlexUIAutoCompleteItem> getItemsMatching(String text) {
        if (text.trim().equals("")) {
            return this.items;
        }
        return this.autoCompleteList.getItemsMatchingSearchTerm(this.items, text, 0);
    }

    public List<? extends PlexUIAutoCompleteItem> getItemsMatchingLastWord() {
        return this.getItemsMatching(this.getLastWordInBox());
    }

    public void setSelectedItem(int index) {
        List<? extends PlexUIAutoCompleteItem> items = this.getItemsMatchingLastWord();
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            items.get(itemIndex).softSelected = false;
        }
        int selectedIndex = PlexCoreUtils.intRange(index, 0, this.items.size() - 1);
        items.get(selectedIndex).softSelected = true;
    }

    public PlexUIAutoCompleteItem getSelectedItem() {
        List<? extends PlexUIAutoCompleteItem> items = this.getItemsMatchingLastWord();
        for (PlexUIAutoCompleteItem item : items) {
            if (item.softSelected) {
                return item;
            }
        }
        return null;
    }

    public void moveSelectedItem(int by) {
        int selectedIndex = -1;
        List<? extends PlexUIAutoCompleteItem> items = this.getItemsMatchingLastWord();
        if (items.size() == 0) {
            return;
        }
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            if (items.get(itemIndex).softSelected && selectedIndex == -1) {
                selectedIndex = itemIndex;
            }
            items.get(itemIndex).softSelected = false;
        }
        selectedIndex = PlexCoreUtils.intRange(selectedIndex + by, 0, items.size() - 1);
        items.get(selectedIndex).softSelected = true;
        this.autoCompleteList.scrollToItemIfNotCompletelyInView(items.get(selectedIndex));
    }

    public boolean keyTyped(char par1, int par2) {
        if (par2 == 15 && this.text.isFocused()) {
            this.setAutoCompleteListVisible(true);
            return true;
        }
        if (par2 == 1 && this.text.isFocused() && this.autoCompleteListVisible) {
            this.setAutoCompleteListVisible(false);
            return true;
        }
        if (par2 == 200 && this.text.isFocused() && this.autoCompleteListVisible) {
            this.moveSelectedItem(-1);
            return true;
        }
        if (par2 == 208 && this.text.isFocused() && this.autoCompleteListVisible) {
            this.moveSelectedItem(1);
            return true;
        }
        if (par2 == 28 && this.text.isFocused() && this.autoCompleteListVisible) {
            PlexUIAutoCompleteItem item = this.getSelectedItem();
            if (item != null) {
                this.autoCompleteWithItem(item);
            }
            this.setAutoCompleteListVisible(false);
            return true;
        }
        if (par2 == 57 && this.text.isFocused()) {
            this.setAutoCompleteListVisible(false);
        }
        this.text.textboxKeyTyped(par1, par2);
        return false;
    }

    public void mouseClicked(int par1, int par2, int btn) {
        this.autoCompleteList.mouseClicked(par1, par2, btn);
        this.text.mouseClicked(par1, par2, btn);
    }

    public void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        this.autoCompleteList.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        this.autoCompleteList.mouseReleased(mouseX, mouseY, state);
    }

    public void handleMouseInput(int x, int y) {
        this.autoCompleteList.handleMouseInput(x, y);
    }

    public void updateScreen() {
        this.text.updateCursorCounter();
        this.autoCompleteList.updateScreen();
    }

    public void autoCompleteWithItem(PlexUIAutoCompleteItem item) {
        this.setAutoCompleteListVisible(false);
        String output = "";
        String[] words = this.text.getText().split("\\s");
        for (int wordIndex = 0; wordIndex < words.length - 1; wordIndex++) {
            output = output + words[wordIndex] + " ";
        }
        output = output + item.autoCompleteText + " ";
        this.text.setText(output);
        this.text.setCursorPositionEnd();
    }

    public void drawScreen(int par1, int par2, float par3) {
        //Plex.logger.info("lw " + this.getLastWordInBox());
        //Plex.logger.info("ai " + this.items.size());
        List<? extends PlexUIAutoCompleteItem> visibleItems = this.getItemsMatchingLastWord();
        //Plex.logger.info("si " + visibleItems.size());
        for (PlexUIAutoCompleteItem item : this.items) {
            if (item.selected) {
                this.autoCompleteWithItem(item);
                item.selected = false;
            }
            if (!visibleItems.contains(item)) {
                item.softSelected = false;
            }
        }
        boolean softSelectedItem = false;
        for (PlexUIAutoCompleteItem item : this.items) {
            if (item.softSelected) {
                softSelectedItem = true;
            }
        }
        if (!softSelectedItem && visibleItems.size() != 0) {
            visibleItems.get(0).softSelected = true;
            this.autoCompleteList.scrollToItemIfNotCompletelyInView(visibleItems.get(0));
        }
        this.text.drawTextBox();
        //GlStateManager.disableColorLogic();
        //GlStateManager.enableTexture2D();
        boolean listVisible = this.getAutoCompleteListVisible();
        this.autoCompleteList.setVisible(listVisible);
        this.autoCompleteList.items = visibleItems;

        if (listVisible) {
            PlexCoreRenderUtils.staticDrawGradientRect(this.autoCompleteList.startX, this.autoCompleteList.startY - 2, this.autoCompleteList.endX, this.autoCompleteList.endY + 2, PlexCoreUtils.replaceColour(this.listBackgroundColour, null, null, null, 127), PlexCoreUtils.replaceColour(this.listBackgroundColour, null, null, null, 190));
        }

        this.autoCompleteList.drawScreen(par1, par2, par3);

        if (listVisible) {
            PlexCoreRenderUtils.staticDrawGradientRect(this.autoCompleteList.startX - 1, this.autoCompleteList.startY - 15, this.autoCompleteList.endX + 1 , this.autoCompleteList.startY - 2,  PlexCoreUtils.replaceColour(this.listBackgroundColour, null, null, null, 255), PlexCoreUtils.replaceColour(this.listBackgroundColour, null, null, null, 255));
            PlexCoreRenderUtils.staticDrawGradientRect(this.autoCompleteList.startX - 1, this.autoCompleteList.startY - 2, this.autoCompleteList.endX + 1, this.autoCompleteList.startY, PlexCoreUtils.replaceColour(this.listBackgroundColour, null, null, null, 255), PlexCoreUtils.replaceColour(this.listBackgroundColour, null, null, null, 0));
            PlexCoreRenderUtils.staticDrawGradientRect(this.autoCompleteList.startX - 1, this.autoCompleteList.endY + 2, this.autoCompleteList.endX + 1, this.autoCompleteList.endY + 15, PlexCoreUtils.replaceColour(this.listBackgroundColour, null, null, null, 255), PlexCoreUtils.replaceColour(this.listBackgroundColour, null, null, null, 255));
            PlexCoreRenderUtils.staticDrawGradientRect(this.autoCompleteList.startX - 1, this.autoCompleteList.endY, this.autoCompleteList.endX + 1, this.autoCompleteList.endY + 2, PlexCoreUtils.replaceColour(this.listBackgroundColour, null, null, null, 0), PlexCoreUtils.replaceColour(this.listBackgroundColour, null, null, null, 255));

            PlexCoreRenderUtils.drawScaledHorizontalLine(this.autoCompleteList.startX - 1, this.autoCompleteList.endX, this.autoCompleteList.startY - 15, 1.0F, this.listBorderColour);
            PlexCoreRenderUtils.drawScaledHorizontalLine(this.autoCompleteList.startX - 1, this.autoCompleteList.endX, this.autoCompleteList.endY + 15, 1.0F, this.listBorderColour);
            PlexCoreRenderUtils.drawScaledVerticalLine(this.autoCompleteList.startX - 1, this.autoCompleteList.startY - 15, this.autoCompleteList.endY + 15, 1.0F, this.listBorderColour);
            PlexCoreRenderUtils.drawScaledVerticalLine(this.autoCompleteList.endX, this.autoCompleteList.startY - 15, this.autoCompleteList.endY + 15, 1.0F, this.listBorderColour);

        }
        //Plex.logger.info("test");
    }
}
