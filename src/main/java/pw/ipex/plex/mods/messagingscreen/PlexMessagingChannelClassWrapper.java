package pw.ipex.plex.mods.messagingscreen;

import pw.ipex.plex.mods.messagingscreen.channel.PlexMessagingChannelBase;
import pw.ipex.plex.ui.widget.itemlist.PlexUIScrolledItem;

public class PlexMessagingChannelClassWrapper implements PlexUIScrolledItem {
    public boolean selected = false;
    public String channelName = "";
    public String description = "";
    public String recipientEntityName = "";
    public String channelNameFormat;
    public String autoCommand = null;
    public int foregroundColour = 0xffffff;
    public Class<? extends PlexMessagingChannelBase> channelClass;

    public PlexMessagingChannelClassWrapper(Class<? extends PlexMessagingChannelBase> channelClass, String channelNameFormat) {
        this.channelClass = channelClass;
        this.channelNameFormat = channelNameFormat;
    }

    public PlexMessagingChannelClassWrapper setName(String name) {
        this.channelName = name;
        return this;
    }

    public PlexMessagingChannelClassWrapper setDescription(String description) {
        this.description = description;
        return this;
    }

    public PlexMessagingChannelClassWrapper setRecipientEntityName(String name) {
        this.recipientEntityName = name;
        return this;
    }

    public PlexMessagingChannelClassWrapper setAutoCommand(String command) {
        this.autoCommand = command;
        return this;
    }

    public PlexMessagingChannelClassWrapper setForegroundColour(int colour) {
        this.foregroundColour = colour;
        return this;
    }

    public Class<? extends PlexMessagingChannelBase> getChannelClass() {
        return channelClass;
    }

    public String getChannelNameFromText(String text) {
        return this.channelNameFormat.replace("{name}", text);
    }

    public String getAutoCommandFromText(String text) {
        if (this.autoCommand == null) {
            return null;
        }
        return this.autoCommand.replace("{name}", text);
    }

    public String getRecipientEntityNameFromText(String text) {
        return this.recipientEntityName.replace("{name}", text);
    }

    @Override
    public boolean listItemIsSelected() {
        return this.selected;
    }

    @Override
    public void listItemClick() {
        this.selected = true;
    }

    @Override
    public void listItemOtherItemClicked() {
        this.selected = false;
    }

    @Override
    public void listItemSelect() {
    }

    @Override
    public int listItemGetHeight() {
        return -1;
    }

    @Override
    public String listItemGetText() {
        return this.channelName;
    }

    @Override
    public String listItemGetSearchText() {
        return "";
    }

    @Override
    public void listItemRenderText(int x, int y, int cellWidth, int cellHeight, float alpha, boolean selected, boolean mouseOver) {
    }

    @Override
    public int listItemGetForegroundColour() {
        return this.foregroundColour;
    }
}
