package pw.ipex.plex.mods.messagingscreen.render;

import pw.ipex.plex.mods.messagingscreen.PlexMessagingMessage;

public class PlexMessagingArbitraryMessageState {
    public PlexMessagingMessage originalMessage;
    public PlexMessagingMessageRenderState renderState;
    public PlexMessagingMessageRenderData renderData;
    public boolean isVisible;
    public int listPositionY;
    public int absolutePositionY;

    public boolean VALID_ITEM;

    public PlexMessagingArbitraryMessageState(PlexMessagingMessage originalMessage) {
        this.originalMessage = originalMessage;
        this.renderState = new PlexMessagingMessageRenderState();
        this.renderData = new PlexMessagingMessageRenderData();
    }
}
