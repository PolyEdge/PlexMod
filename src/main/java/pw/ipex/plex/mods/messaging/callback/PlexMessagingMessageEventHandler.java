package pw.ipex.plex.mods.messaging.callback;

import pw.ipex.plex.mods.messaging.render.PlexMessagingMessageHoverState;

public abstract class PlexMessagingMessageEventHandler {
	public void onClick(PlexMessagingMessageHoverState hoverState, int button) {}

	public void onHover(PlexMessagingMessageHoverState hoverState) {}
}
