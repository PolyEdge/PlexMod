package cc.dyspore.plex.mods.messagingscreen.render;

import cc.dyspore.plex.Plex;

public class PlexMessagingMessageTextData {
	public String text = "";
	public float scale = 1.0F;
	public int x = 0;
	public int y = 0;
	public int width = 0;
	public Integer colour = null;
	public int stringOffset = -1;
	
	public int getHeight() {
		return (int) (this.scale * Plex.minecraft.fontRendererObj.FONT_HEIGHT);
	}
}
