package cc.dyspore.plex.mods.messagingscreen.callback;

import cc.dyspore.plex.mods.messagingscreen.render.PlexMessagingMessageHoverState;

public abstract class PlexMessagingMessageEventHandler {
	public void onClick(PlexMessagingMessageHoverState hoverState, int button) {}

	public void onHover(PlexMessagingMessageHoverState hoverState) {}
}
