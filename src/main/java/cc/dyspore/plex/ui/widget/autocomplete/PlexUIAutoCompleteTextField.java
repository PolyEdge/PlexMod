package cc.dyspore.plex.ui.widget.autocomplete;

import cc.dyspore.plex.core.util.PlexUtilRender;
import cc.dyspore.plex.core.util.PlexUtil;
import cc.dyspore.plex.core.util.PlexUtilColour;
import cc.dyspore.plex.ui.widget.itemlist.PlexUIScrolledItemList;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Boolean clearFiltersOnComplete = true;

    public List<String> previousSentMessages = new ArrayList<>();
    public Integer currentPreviousSentMessageIndex = 0;

    public List<? extends PlexUIAutoCompleteItem> allItems = new ArrayList<>();
    public List<? extends PlexUIAutoCompleteItem> visibleItems = new ArrayList<>();

    public Set<String> groupsInclude = new HashSet<>();
    public Set<String> groupsExclude = new HashSet<>();

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
        this.autoCompleteList = new PlexUIScrolledItemList(this.allItems, 0, 0, 0, 0);
        this.autoCompleteList.setPadding(10, 0);
        this.autoCompleteList.defaultEntryHeight = 10;
        this.autoCompleteList.scrollbar.hiddenForcedScroll = 0.0F;
        this.setAutocompleteListPosition(this.xPosition, this.yPosition - 50, this.xPosition + this.itemWidth, this.yPosition - 1);

        this.previousSentMessages.add("");
    }

    public void addToSentMessages(String message) {
        for (int index = 1; index < this.previousSentMessages.size(); index++) {
            if (this.previousSentMessages.get(index).equals(message)) {
                this.previousSentMessages.remove(index);
                index -= 1;
            }
        }
        this.previousSentMessages.add(1, message);
    }

    public void resetSentMessagesIndex() {
        this.currentPreviousSentMessageIndex = 0;
    }

    public List<String> getPreviousSentMessages() {
        return this.previousSentMessages;
    }

    public void setPreviousSentMessages(List<String> messages) {
        if (messages.size() == 0) {
            messages.add("");
        }
        if (!messages.get(0).equals("")) {
            messages.add(0, "");
        }
        this.previousSentMessages = messages;
    }

    public void setAutocompleteListPosition(int x, int y, int endX, int endY) {
        this.autoCompleteList.setPosition(x, y - 15, endX, endY - 15);
    }

    public void setAutoCompleteListVisible(boolean visible) {
        this.autoCompleteListVisible = visible;
    }

    public void setAutoCompleteItems(List<? extends PlexUIAutoCompleteItem> items) {
        this.allItems = items;
    }

    public void includeGroup(String group) {
        this.unexcludeGroup(group);
        this.groupsInclude.add(group);
    }

    public void unincludeGroup(String group) {
        this.groupsInclude.remove(group);
    }

    public void includeAll() {
        this.groupsInclude.clear();
    }

    public void excludeGroup(String group) {
        this.unincludeGroup(group);
        this.groupsExclude.add(group);
    }

    public void unexcludeGroup(String group) {
        this.groupsExclude.remove(group);
    }

    public void unexcludeAll() {
        this.groupsExclude.clear();
    }

    public void clearInclusionFilters() {
        this.groupsInclude.clear();
        this.groupsExclude.clear();
    }

    public List<? extends PlexUIAutoCompleteItem> getVisibleItems() {
        List<PlexUIAutoCompleteItem> items = new ArrayList<>();
        for (PlexUIAutoCompleteItem item : this.getItemsMatching(this.getLastWordInBox())) {
            if (this.groupsInclude.size() > 0) {
                if (!this.groupsInclude.contains(item.group)) {
                    continue;
                }
            }
            if (this.groupsExclude.contains(item.group)) {
                continue;
            }
            items.add(item);
        }

        return items;
    }

    private void updateItems() {
        List<? extends PlexUIAutoCompleteItem> visibleItems = this.getVisibleItems();
        this.visibleItems = visibleItems;

        PlexUIAutoCompleteItem lastVisibleItem = null;
        PlexUIAutoCompleteItem scrollTo = null;
        boolean selectedFound = false;
        boolean selectNextVisible = false;

        for (PlexUIAutoCompleteItem item : this.allItems) {
            if (item.selected) {
                this.autoCompleteWithItem(item);
                item.selected = false;
            }
            if (selectNextVisible && visibleItems.contains(item)) {
                item.softSelected = true;
                selectNextVisible = false;
                scrollTo = item;
                continue;
            }
            if (selectedFound) {
                item.softSelected = false;
                continue;
            }
            if (item.softSelected) {
                selectedFound = true;
                if (!visibleItems.contains(item)) {
                    item.softSelected = false;
                    if (lastVisibleItem != null) {
                        lastVisibleItem.softSelected = true;
                    }
                    else {
                        selectNextVisible = true;
                    }
                }
            }
            if (visibleItems.contains(item)) {
                lastVisibleItem = item;
            }
        }

        selectedFound = false;
        for (PlexUIAutoCompleteItem item : visibleItems) {
            if (item.softSelected) {
                selectedFound = true;
            }
        }

        this.autoCompleteList.items = visibleItems;

        if (!selectedFound && visibleItems.size() != 0) {
            visibleItems.get(0).softSelected = true;
            scrollTo = visibleItems.get(0);
        }
        if (scrollTo != null) {
            this.autoCompleteList.scrollToItemIfNotCompletelyInView(scrollTo);
        }
    }

    public boolean getAutoCompleteListVisible() {
        if (!this.autoCompleteListVisible) {
            return false;
        }
        this.updateItems();
        String lastWord = this.getLastWordInBox();
        if (lastWord.trim().equals("")) {
            //return false;
        }
        return this.autoCompleteList.getItemsMatchingSearchTerm(lastWord).size() != 0;
    }

    public String getLastWordInBox() {
        if (this.text.getText().endsWith(" ")) {
            return "";
        }
        String[] words = this.text.getText().split("\\s", -1);
        if (words.length == 0) {
            return "";
        }
        return words[words.length - 1];
    }

    public List<? extends PlexUIAutoCompleteItem> getItemsMatching(String text) {
        if (text.trim().equals("")) {
            return this.allItems;
        }
        return this.autoCompleteList.getItemsMatchingSearchTerm(this.allItems, text, 0);
    }

    public void setSelectedItem(int index) {
        List<? extends PlexUIAutoCompleteItem> items = this.getVisibleItems();
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            items.get(itemIndex).softSelected = false;
        }
        int selectedIndex = PlexUtil.clamp(index, 0, this.allItems.size() - 1);
        items.get(selectedIndex).softSelected = true;
    }

    public PlexUIAutoCompleteItem getSelectedItem() {
        List<? extends PlexUIAutoCompleteItem> items = this.getVisibleItems();
        for (PlexUIAutoCompleteItem item : items) {
            if (item.softSelected) {
                return item;
            }
        }
        return null;
    }

    public void moveSelectedItem(int by) {
        int selectedIndex = -1;
        List<? extends PlexUIAutoCompleteItem> items = this.getVisibleItems();
        if (items.size() == 0) {
            return;
        }
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            if (items.get(itemIndex).softSelected && selectedIndex == -1) {
                selectedIndex = itemIndex;
            }
            items.get(itemIndex).softSelected = false;
        }
        selectedIndex = PlexUtil.clamp(selectedIndex + by, 0, items.size() - 1);
        items.get(selectedIndex).softSelected = true;
        this.autoCompleteList.scrollToItemIfNotCompletelyInView(items.get(selectedIndex));
    }

    public void moveItemInMessageHistory(int by) {
        if (this.autoCompleteListVisible) {
            this.setAutoCompleteListVisible(false);
        }
        this.currentPreviousSentMessageIndex += by;
        if (this.currentPreviousSentMessageIndex <= 0) {
            this.currentPreviousSentMessageIndex = 0;
        }
        else if (this.currentPreviousSentMessageIndex >= this.previousSentMessages.size()) {
            this.currentPreviousSentMessageIndex = this.previousSentMessages.size() - 1;
        }
        this.text.setText(this.previousSentMessages.get(this.currentPreviousSentMessageIndex));
        this.text.setCursorPositionEnd();
    }

    public boolean keyTyped(char par1, int par2) {
        if (par2 == 15 && this.text.isFocused() && !this.autoCompleteListVisible) {
            this.setAutoCompleteListVisible(true);
            return true;
        }
        if (par2 == 15 && this.text.isFocused() && this.getAutoCompleteListVisible()) {
            PlexUIAutoCompleteItem item = this.getSelectedItem();
            if (item != null) {
                this.autoCompleteWithItem(item);
            }
            this.setAutoCompleteListVisible(false);
            return true;
        }
        if (par2 == 1 && this.text.isFocused() && this.getAutoCompleteListVisible()) {
            this.setAutoCompleteListVisible(false);
            return true;
        }
        if (par2 == 200 && this.text.isFocused() && this.getAutoCompleteListVisible()) {
            this.moveSelectedItem(-1);
            return true;
        }
        else if (par2 == 200 && this.text.isFocused()) {
            this.moveItemInMessageHistory(1);
        }
        if (par2 == 208 && this.text.isFocused() && this.getAutoCompleteListVisible()) {
            this.moveSelectedItem(1);
            return true;
        }
        else if (par2 == 208 && this.text.isFocused()) {
            this.moveItemInMessageHistory(-1);
        }
        if (par2 == 28 && this.text.isFocused() && this.getAutoCompleteListVisible()) {
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

    public void mouseClicked(int mouseX, int mouseY, int btn) {
        if (this.autoCompleteListVisible) {
            if (!(mouseX >= this.autoCompleteList.startX && mouseY >= this.autoCompleteList.startY - 15 && mouseX <= this.autoCompleteList.endX && mouseY <= this.autoCompleteList.endY + 15)) {
                this.autoCompleteListVisible = false;
            }
        }
        this.autoCompleteList.mouseClicked(mouseX, mouseY, btn);
        this.text.mouseClicked(mouseX, mouseY, btn);
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
        StringBuilder output = new StringBuilder();
        String[] words = this.text.getText().split("\\s", -1);
        //if (this.text.getText().endsWith(" ")) {
        //
        //}
        for (int wordIndex = 0; wordIndex < words.length - 1; wordIndex++) {
            output.append(words[wordIndex]).append(" ");
        }
        output.append(item.autoCompleteText).append(" ");
        item.selected = false;
        this.text.setText(output.toString());
        this.text.setCursorPositionEnd();
        if (this.clearFiltersOnComplete) {
            this.clearInclusionFilters();
        }
        this.updateItems();
    }

    public void drawScreen(int par1, int par2, float par3) {
        //Plex.logger.info("lw " + this.getLastWordInBox());
        //Plex.logger.info("ai " + this.allItems.size());
        this.updateItems();
        this.text.drawTextBox();
        //GlStateManager.disableColorLogic();
        //GlStateManager.enableTexture2D();
        boolean listVisible = this.getAutoCompleteListVisible();
        this.autoCompleteList.setVisible(listVisible);

        if (listVisible) {
            PlexUtilRender.drawGradientRect(this.autoCompleteList.startX, this.autoCompleteList.startY - 2, this.autoCompleteList.endX, this.autoCompleteList.endY + 2, PlexUtilColour.setAlpha(this.listBackgroundColour,  230), PlexUtilColour.setAlpha(this.listBackgroundColour, 230));
        }

        this.autoCompleteList.drawScreen(par1, par2, par3);

        if (listVisible) {
            PlexUtilRender.drawGradientRect(this.autoCompleteList.startX - 1, this.autoCompleteList.startY - 15, this.autoCompleteList.endX + 1 , this.autoCompleteList.startY - 2,  PlexUtilColour.setAlpha(this.listBackgroundColour,  255), PlexUtilColour.setAlpha(this.listBackgroundColour,  255));
            PlexUtilRender.drawGradientRect(this.autoCompleteList.startX - 1, this.autoCompleteList.startY - 2, this.autoCompleteList.endX + 1, this.autoCompleteList.startY, PlexUtilColour.setAlpha(this.listBackgroundColour, 255), PlexUtilColour.setAlpha(this.listBackgroundColour, 0));
            PlexUtilRender.drawGradientRect(this.autoCompleteList.startX - 1, this.autoCompleteList.endY + 2, this.autoCompleteList.endX + 1, this.autoCompleteList.endY + 15, PlexUtilColour.setAlpha(this.listBackgroundColour,  255), PlexUtilColour.setAlpha(this.listBackgroundColour,  255));
            PlexUtilRender.drawGradientRect(this.autoCompleteList.startX - 1, this.autoCompleteList.endY, this.autoCompleteList.endX + 1, this.autoCompleteList.endY + 2, PlexUtilColour.setAlpha(this.listBackgroundColour, 0), PlexUtilColour.setAlpha(this.listBackgroundColour, 255));

            PlexUtilRender.drawScaledHorizontalLine(this.autoCompleteList.startX - 1, this.autoCompleteList.endX, this.autoCompleteList.startY - 15, 0, 1.0F, this.listBorderColour);
            PlexUtilRender.drawScaledHorizontalLine(this.autoCompleteList.startX - 1, this.autoCompleteList.endX, this.autoCompleteList.endY + 15, 0, 1.0F, this.listBorderColour);
            PlexUtilRender.drawScaledVerticalLine(this.autoCompleteList.startX - 1, this.autoCompleteList.startY - 15, this.autoCompleteList.endY + 15, 0, 1.0F, this.listBorderColour);
            PlexUtilRender.drawScaledVerticalLine(this.autoCompleteList.endX, this.autoCompleteList.startY - 15, this.autoCompleteList.endY + 15, 0, 1.0F, this.listBorderColour);

        }
        //Plex.logger.info("test");
    }
}
