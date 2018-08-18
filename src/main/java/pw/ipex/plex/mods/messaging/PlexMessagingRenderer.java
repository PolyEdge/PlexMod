package pw.ipex.plex.mods.messaging;

import org.lwjgl.opengl.GL11;

import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIMenuScreen;

public class PlexMessagingRenderer {
	PlexUIBase parent;
	
	public int paddingX;
	public int paddingY;
	
	public int startX;
	public int startY;
	public int endX;
	public int endY;
	
	public int messageBalloonPadding = 3;
	public int lineSpacing = 10;
	
	public int textWrapMode = 0;
	
	public int contacts_entryHeight = 16;
	
	public PlexMessagingRenderer(PlexUIBase parent) {
		this.parent = parent;
	}
	
	public void setPosition(int sx, int sy, int ex, int ey) {
		this.startX = sx;
		this.startY = sy;
		this.endX = ex;
		this.endY = ey;
	}
		
	public void setPadding(int px, int py) {
		this.paddingX = px;
		this.paddingY = py;
	}
	
	public int getSizeX() {
		return this.endX - this.startX;
	}
	
	public int getSizeY() {
		return this.endY - this.startY;
	}
	
	public void setTextWrapLeft() {
		this.textWrapMode = 0;
	}
	
	public void setTextWrapRight() {
		this.textWrapMode = 1;
	}
	
	public void calculateRenderHeightForMessage(PlexMessagingMessage message) {
		//this.parent.parentUI.getFontRenderer().listFormattedStringToWidth(message.content, wrapWidth)
	}
	
	public int multiplyColour(int colour, float mul) {
		return multiplyColour(colour, mul, false);
	}
	
	public int multiplyColour(int colour, float mul, boolean alpha) {
		Integer[] newColour = PlexCoreUtils.rgbCodeFrom(colour);
		return PlexCoreUtils.colourCodeFrom(PlexCoreUtils.intRange((int)(newColour[0] * mul), 0, 255), PlexCoreUtils.intRange((int)(newColour[1] * mul), 0, 255), PlexCoreUtils.intRange((int)(newColour[2] * mul), 0, 255), alpha ? PlexCoreUtils.intRange((int)(newColour[3] * mul), 0, 255) : newColour[3]);
	}
	
	public void drawScaledString(String text, float x, float y, int colour, float scale, boolean shadow) {
	    GL11.glPushMatrix();
	    GL11.glScaled(scale, scale, scale);
	    this.parent.parentUI.getFontRenderer().drawString(text, x / scale, y / scale, colour, shadow);
	    GL11.glPopMatrix();		
	}
	
	public void drawScaledStringRightSide(String text, int x, int y, int colour, float scale, boolean shadow) {
	    GL11.glPushMatrix();
	    GL11.glScaled(scale, scale, scale);
	    this.parent.parentUI.getFontRenderer().drawString(text, x / scale - this.parent.parentUI.getFontRenderer().getStringWidth(text), y, colour, shadow);
	    GL11.glPopMatrix();
	}
	
	public void drawScaledStringLeftSide(String text, int x, int y, int colour, float scale, boolean shadow) {
		drawScaledString(text, x, y, colour, scale, shadow);
	}
	
	public void drawScaledStringRightSide(String text, int x, int y, int colour, float scale) {
		drawScaledStringRightSide(text, x, y, colour, scale, false);
	}
	
	public void drawScaledStringLeftSide(String text, int x, int y, int colour, float scale) {
		drawScaledStringLeftSide(text, x, y, colour, scale, false);
	}
	
	public int calculateScaledStringWidth(String text, float scale) {
		return (int)(this.parent.parentUI.getFontRenderer().getStringWidth(text) * scale);
	}
	
	public int getDmContactsListTotalPixels(PlexMessagingChannelManager manager) {
		return manager.channels.size() * this.contacts_entryHeight;
	}
	
	public PlexMessagingChannelBase getMouseOverChannel(PlexMessagingChannelManager manager, float scrollPercentage, int mouseX, int mouseY, int rangeCutoff) {
		int scrollRange = this.getDmContactsListTotalPixels(manager) - this.getSizeY();
		int viewportTop = (int)(scrollRange * scrollPercentage + 0);
		int viewportBottom = (int)(scrollRange * scrollPercentage + this.getSizeY());
		if (!(mouseX > this.startX && mouseX < this.endX)) {
			return null;
		}
		int currentY = -contacts_entryHeight;
		for (PlexMessagingChannelBase channel : manager.channels) {
			currentY += contacts_entryHeight;
			if (currentY + contacts_entryHeight < viewportTop - rangeCutoff) {
				continue;
			}
			if (currentY > viewportBottom + rangeCutoff) {
				break;
			}
			int itemYposition = this.startY + (currentY - viewportTop);
			if (mouseY > itemYposition && mouseY < itemYposition + contacts_entryHeight) {
				return channel;
			}
		}
		return null;
	}
	
	public void renderDmContactsList(PlexMessagingChannelManager manager, float scrollPercentage, int mouseX, int mouseY, int rangeCutoff) {
		int scrollRange = this.getDmContactsListTotalPixels(manager) - this.getSizeY();
		int viewportTop = (int)(scrollRange * scrollPercentage + 0);
		int viewportBottom = (int)(scrollRange * scrollPercentage + this.getSizeY());
		int currentY = -contacts_entryHeight;
		for (PlexMessagingChannelBase channel : manager.channels) {
			currentY += contacts_entryHeight;
			if (currentY + contacts_entryHeight < viewportTop - rangeCutoff) {
				continue;
			}
			if (currentY > viewportBottom + rangeCutoff) {
				break;
			}
			int itemYposition = this.startY + (currentY - viewportTop);
			int itemForegroundColour;
			if (channel.getDisplayColour() != null) {
				itemForegroundColour = channel.getDisplayColour();
			}
			else {
				itemForegroundColour = 0xffffffff;
			}
			int itemBackgroundColour = 0x60777777;
			boolean isMouseOver = false;
			boolean isSelected = false;
			int unreadMessages = channel.getMessagesSinceLastRead().size();
			int entryStartX = this.startX + this.paddingX;
			int channelNameStartX = entryStartX;
			if (mouseX > this.startX && mouseX < this.endX && mouseY > itemYposition && mouseY < itemYposition + contacts_entryHeight) {
				isMouseOver = true;
			}
			if (manager.selectedChannel == channel) {
				isSelected = true;
			}
			if (unreadMessages > 0) {
				channelNameStartX = entryStartX + calculateScaledStringWidth("[" + String.valueOf(unreadMessages) + "]  ", 1.0F);
			}
			
			if (isSelected) {
				itemForegroundColour = multiplyColour(itemForegroundColour, 1.60F);
				itemBackgroundColour = multiplyColour(itemBackgroundColour, 1.60F);
			}
			if (isMouseOver) {
				itemForegroundColour = multiplyColour(itemForegroundColour, 1.20F);
				itemBackgroundColour = multiplyColour(itemBackgroundColour, 1.20F);
			}
			if (unreadMessages > 0) {
				drawScaledStringLeftSide("[" + String.valueOf(unreadMessages) + "]", entryStartX, itemYposition + ((contacts_entryHeight / 2) - (this.parent.parentUI.getFontRenderer().FONT_HEIGHT / 2)), 0xff0202, 1.0F);				
			}
			PlexUIMenuScreen.drawRect(this.startX, itemYposition, this.endX, itemYposition + contacts_entryHeight, itemBackgroundColour);
			String finalText = this.parent.parentUI.getFontRenderer().trimStringToWidth(channel.displayName, this.endX - channelNameStartX - this.paddingX);
			drawScaledStringLeftSide(finalText, channelNameStartX, itemYposition + ((contacts_entryHeight / 2) - (this.parent.parentUI.getFontRenderer().FONT_HEIGHT / 2)), itemForegroundColour, 1.0F);
		}
	}
}
