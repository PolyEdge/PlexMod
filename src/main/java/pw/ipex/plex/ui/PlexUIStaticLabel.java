package pw.ipex.plex.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreRenderUtils;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexUIStaticLabel extends GuiScreen {
	public int startX;
	public int startY;
	public int labelHeight;
	
	public float displayX = 0.0F;
	public float displayY = 0.0F;
	public float displayHeight = 0.0F;
	public int oldDisplayX = 0;
	public int oldDisplayY = 0;
	public int oldDisplayHeight = 0;
	public Long lastPositionUpdate = null;
	public Long lastHeightUpdate = null;
	
	public String text = "";
	public String displayText = "";
	public String oldDisplayText = "";
	public int displayTextWidth = 0;
	public Long lastTextUpdate = null;
	
	public boolean visible = true;
	
	public int colour = 0xc0232323;
	public int displayColour = colour;
	public int oldDisplayColour = colour;
	public Long lastColourUpdate = null;
	
	public int textColour = 0xffffff;
	public int textDisplayColour = colour;
	public int oldTextDisplayColour = colour;
	public int targetTextWidth = 0;
	public Long lastTextColourUpdate = null;

	public int movementSpeedMs = 250;
	public int textUpdateMs = 250;
	public int colourChangeMs = 250;
	
	public float labelPaddingPercent = 0.25F;
	public int extraXPadding = 2;
	
	public PlexUIStaticLabel(int posX, int posY, int height) {
		this.setPosition(posX, posY, height, true);
	}
	
	/*public void setPosition(int posX, int posY, int width, int height) {
		this.startX = posX;
		this.startY = posY;
		this.barWidth = width;
		this.barHeight = height;
	}*/
	
	public void setMovementSpeed(int cycleMs) {
		this.movementSpeedMs = cycleMs;
	}
	
	public void setColourSpeed(int cycleMs) {
		this.colourChangeMs = cycleMs;
	}
	
	public void setTextUpdateSpeed(int cycleMs) {
		this.textUpdateMs = cycleMs;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public void setPosition(int posX, int posY, int height) {
		this.setPosition(posX, posY, false);
		this.setHeight(height, false);
	}

	public void setPosition(int posX, int posY, int height, boolean force) {
		this.setPosition(posX, posY, force);
		this.setHeight(height, force);
	}
	
	public void setPosition(int posX, int posY, boolean force) {
		if (posX == this.startX && posY == this.startY && !force) {
			return;
		}
		if (force) {
			this.displayX = posX;
			this.displayY = posY;
		}
		this.oldDisplayX = (int) this.displayX;
		this.oldDisplayY = (int) this.displayY;
		this.startX = posX;
		this.startY = posY;
		this.lastPositionUpdate = Minecraft.getSystemTime();
	}
	
	public void setHeight(int height, boolean force) {
		if (height == this.labelHeight && !force) {
			return;
		}
		if (force) {
			this.labelHeight = height;
		}
		this.oldDisplayHeight = (int) this.displayHeight;
		this.labelHeight = height;
		this.lastHeightUpdate = Minecraft.getSystemTime();
	}
	
	public void setText(String text) {
		this.setText(text, false);
	}
	
	public void setText(String text, boolean force) {
		if (this.text.equals(text) && !force) {
			return;
		}
		if (force) {
			this.displayText = text;
		}
		this.targetTextWidth = PlexCoreRenderUtils.calculateScaledStringWidth(text, 1.0F);
		this.oldDisplayText = this.displayText;
		this.text = text;
		this.lastTextUpdate = Minecraft.getSystemTime();			
	}
	
	public void setBackgroundColour(int colour) {
		this.setBackgroundColour(colour, false);
	}
	
	public void setBackgroundColour(int colour, boolean force) {
		if (this.colour == colour && !force) {
			return;
		}
		if (force) {
			this.displayColour = colour;
		}
		this.oldDisplayColour = this.displayColour;
		this.colour = colour;
		this.lastColourUpdate = Minecraft.getSystemTime();	
	}
	
	public void setTextColour(int colour) {
		this.setTextColour(colour, false);
	}
	
	public void setTextColour(int colour, boolean force) {
		if (this.textColour == colour && !force) {
			return;
		}
		if (force) {
			this.textDisplayColour = colour;
		}
		this.oldTextDisplayColour = this.textDisplayColour;
		this.textColour = colour;
		this.lastTextColourUpdate = Minecraft.getSystemTime();	
	}
	
	public void updateLabel() {
		if (this.lastPositionUpdate != null) {
			float timeBetween = ((float)PlexCoreUtils.longRange((Minecraft.getSystemTime() - this.lastPositionUpdate), 0L, (long) this.movementSpeedMs) / (float) this.movementSpeedMs);
			if (this.displayX != this.startX) {
				this.displayX = this.oldDisplayX + (float)(this.startX - this.oldDisplayX) * timeBetween;
			}
			if (this.displayY != this.startY) {
				this.displayY = this.oldDisplayY + (float)(this.startY - this.oldDisplayY) * timeBetween;
			}
		}
		else {
			this.displayX = this.startX;
			this.displayY = this.startY;
		}
		if (this.lastHeightUpdate != null) {
			float timeBetween = ((float)PlexCoreUtils.longRange((Minecraft.getSystemTime() - this.lastPositionUpdate), 0L, (long) this.movementSpeedMs) / (float) this.movementSpeedMs);
			if (this.displayHeight != this.labelHeight) {
				this.displayHeight = this.oldDisplayHeight + (float)(this.labelHeight - this.oldDisplayHeight) * timeBetween;
			}			
		}
		else {
			this.displayHeight = this.labelHeight;
		}
		if (this.lastTextUpdate != null) {
			float timeBetween = ((float)PlexCoreUtils.longRange((Minecraft.getSystemTime() - this.lastTextUpdate), 0L, (long) this.textUpdateMs) / (float) this.textUpdateMs);
			if (!this.displayText.equals(this.text) || this.displayTextWidth != this.targetTextWidth) {
				int oldWidth = PlexCoreRenderUtils.calculateScaledStringWidth(this.oldDisplayText, 1.0F);
				int newWidth = PlexCoreRenderUtils.calculateScaledStringWidth(this.text, 1.0F);
				this.displayTextWidth = (int) (oldWidth + (float)(newWidth - oldWidth) * timeBetween);
				this.displayText = PlexCoreRenderUtils.trimScaledTextToWidth(this.text, this.displayTextWidth, 1.0F);
			}
		}
		else {
			this.displayText = this.text;
			this.displayTextWidth = PlexCoreRenderUtils.calculateScaledStringWidth(this.text, 1.0F);
		}
		if (lastColourUpdate != null) {
			if (this.displayColour != this.colour) {
				this.displayColour = PlexCoreUtils.betweenColours(this.oldDisplayColour, this.colour, ((float)PlexCoreUtils.longRange((Minecraft.getSystemTime() - this.lastColourUpdate), 0L, (long) this.colourChangeMs) / (long) this.colourChangeMs));
			}
		}
		else {
			this.displayColour = this.colour;
		}
		if (lastTextColourUpdate != null) {
			if (this.textDisplayColour != this.textColour) {
				this.textDisplayColour = PlexCoreUtils.betweenColours(this.oldTextDisplayColour, this.textColour, ((float)PlexCoreUtils.longRange((Minecraft.getSystemTime() - this.lastTextColourUpdate), 0L, (long) this.colourChangeMs) / (long) this.colourChangeMs));
			}
		}
		else {
			this.textDisplayColour = this.textColour;
		}
	}
	
	public void drawScreen(int par1, int par2, float par3) {
		updateLabel();
		if (!this.visible) {
			return;
		}
		int padding = (int)((this.displayHeight * (this.labelPaddingPercent)) / 2.0F);
		float fontScale = (this.displayHeight - padding * 2) / Plex.minecraft.fontRendererObj.FONT_HEIGHT;
		//int textWidth = (int) (this.displayTextWidth * fontScale);
		PlexCoreRenderUtils.drawScaledGradientRect((int)this.displayX, (int)this.displayY, (int)this.displayX + (int)(this.displayTextWidth * fontScale) + extraXPadding * 2 + padding * 2, (int)this.displayY + this.labelHeight, 1.0F, this.displayColour, this.displayColour);
		PlexCoreRenderUtils.drawScaledString(this.displayText, (int)this.displayX + padding + extraXPadding, (int)this.displayY + padding, this.textDisplayColour, fontScale, false);
	}
}
