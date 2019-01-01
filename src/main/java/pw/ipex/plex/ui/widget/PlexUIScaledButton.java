package pw.ipex.plex.ui.widget;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class PlexUIScaledButton extends GuiButton {
	public int realX;
	public int realY;
	public int realWidth;
	public int realHeight;
	public float scale;
	public Float realScale;
	
	public String realText;
	public boolean isTextScaled = true;
	
	public PlexUIScaledButton(int buttonId, int x, int y, int widthIn, int heightIn, Float scale, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.realX = x;
		this.realY = y;
		this.realWidth = widthIn;
		this.realHeight = heightIn;
		this.realScale = scale;
		this.realText = buttonText;
		this.scaledDimensions();
	}
	
	public PlexUIScaledButton(int buttonId, int x, int y, int widthIn, int heightIn, Float scale, String buttonText, Boolean textScaled) {
		this(buttonId, x, y, widthIn, heightIn, scale, buttonText);
		this.isTextScaled = textScaled;
		this.scaledDimensions();
	}

	public void scaledDimensions() {
		this.scale = this.realScale != null ? this.realScale : (float) this.realHeight / 20.0F;
		this.xPosition = (int) (realX / this.scale);
		this.yPosition = (int) (realY / this.scale);
		this.width = (int) (this.realWidth / this.scale);
		this.height = (int) (this.realHeight / this.scale);
		this.displayString = this.isTextScaled ? this.realText : "";
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		this.scaledDimensions();
		GL11.glPushMatrix();
		GL11.glScaled(this.scale, this.scale, this.scale);
		super.drawButton(mc, (int) (mouseX / this.scale), (int) (mouseY / this.scale));
		GL11.glPopMatrix();
		if (!this.isTextScaled) {
			int colour = this.packedFGColour != 0 ? this.packedFGColour : (!this.enabled ? 10526880 : (this.hovered ? 16777120 : 14737632));
			this.drawCenteredString(mc.fontRendererObj, this.realText, this.realX + this.realWidth / 2, this.realY + (this.realHeight - 8) / 2, colour);
		}
	}
	
	@Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		this.scaledDimensions();
		mouseX = (int) (mouseX / this.scale);
		mouseY = (int) (mouseY / this.scale);
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }
}
