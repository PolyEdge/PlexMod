package cc.dyspore.plex.ui.widget;

import cc.dyspore.plex.core.util.PlexUtilRender;
import cc.dyspore.plex.core.util.PlexUtil;
import cc.dyspore.plex.core.util.PlexUtilColour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class PlexUIProgressBar extends GuiScreen {
	public int startX;
	public int startY;
	public int barWidth;
	public int barHeight;
	
	public float progress = 0.0F;
	public float displayProgress = 0.0F;
	public float oldProgress = 0.0F;
	public Long lastProgressUpdate = null;
	public boolean visible = true;
	public int colour = 0x1eff43;
	public Long lastColourUpdate = null;
	public int displayColour = colour;
	public int oldDisplayColour = colour;

	public int loadCycleSpeedMs = 250;
	public int colourChangeMs = 250;
	
	public PlexUIProgressBar(int posX, int posY, int width, int height) {
		this.setPosition(posX, posY, width, height);
	}
	
	public void setPosition(int posX, int posY, int width, int height) {
		this.startX = posX;
		this.startY = posY;
		this.barWidth = width;
		this.barHeight = height;
	}
	
	public void setProgress(float progress) {
		this.setProgress(progress, false);
	}
	
	public void setProgress(float progress, boolean force) {
		if (progress == this.progress && !force) {
			return;
		}
		if (force) {
			this.displayProgress = progress;
		}
		this.oldProgress = this.displayColour;
		this.progress = progress;
		this.lastProgressUpdate = Minecraft.getSystemTime();
	}
	
	public void towardsProgress(float progress, int speed) {
		this.setProgress(this.progress + (progress - this.progress) / (float) speed);
	}
	
	public void setBarSpeed(int cycleMs) {
		this.loadCycleSpeedMs = cycleMs;
	}
	
	public void setColourSpeed(int cycleMs) {
		this.colourChangeMs = cycleMs;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public void setColour(int colour) {
		this.setColour(colour, false);
	}
	
	public void setColour(int colour, boolean force) {
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
	
	public void updateProgressBar() {
		this.progress = PlexUtil.clamp(this.progress, 0.0F, 1.0F);
		if (this.lastProgressUpdate != null) {
			if (this.displayProgress != this.progress) {
				this.displayProgress = this.oldProgress + (this.progress - this.oldProgress) * ((float) PlexUtil.clamp((Minecraft.getSystemTime() - this.lastProgressUpdate), 0L, (long) this.loadCycleSpeedMs) / (float) this.loadCycleSpeedMs);
			}			
		}
		else {
			this.displayProgress = this.progress;
		}
		//this.displayProgress = this.displayProgress + (this.progress - this.displayProgress) / this.updateSpeed;
		if (lastColourUpdate != null) {
			if (this.displayColour != this.colour) {
				this.displayColour = PlexUtilColour.between(this.oldDisplayColour, this.colour, ((float) PlexUtil.clamp((Minecraft.getSystemTime() - this.lastColourUpdate), 0L, (long) this.colourChangeMs) / (long) this.colourChangeMs));
			}
		}
		else {
			this.displayColour = this.colour;
		}
	}
	
	public void drawScreen(int par1, int par2, float par3) {
		updateProgressBar();
		PlexUtilRender.drawScaledGradientRect(this.startX, this.startY, (int) (this.startX + (this.barWidth * this.displayProgress)), this.startY + this.barHeight, 0, 1.0F, (int) PlexUtilColour.replace(this.displayColour, null, null, null, 255), (int) PlexUtilColour.replace(this.displayColour, null, null, null, 255));
	}
}
