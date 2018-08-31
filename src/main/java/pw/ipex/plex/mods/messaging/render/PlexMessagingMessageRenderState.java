package pw.ipex.plex.mods.messaging.render;

public class PlexMessagingMessageRenderState {
	public boolean RENDER_HEADS_SHOWN = false;
	public boolean RENDER_HEAD_ENABLED = false;
	public boolean RENDER_AUTHOR_ENABLED = false;
	
	public PlexMessagingMessageRenderState setHeadsShown(boolean headsShown) {
		this.RENDER_HEADS_SHOWN = headsShown;
		return this;
	}
	
	public PlexMessagingMessageRenderState setHeadEnabled(boolean headEnabled) {
		this.RENDER_HEAD_ENABLED = headEnabled;
		return this;
	}
	
	public PlexMessagingMessageRenderState setAuthorEnabled(boolean authorEnabled) {
		this.RENDER_AUTHOR_ENABLED = authorEnabled;
		return this;
	}
	
	public boolean matches(PlexMessagingMessageRenderState state) {
		return (state.RENDER_AUTHOR_ENABLED == this.RENDER_AUTHOR_ENABLED) && (state.RENDER_HEAD_ENABLED == this.RENDER_HEAD_ENABLED) && (state.RENDER_HEADS_SHOWN == this.RENDER_HEADS_SHOWN);
	}
}
