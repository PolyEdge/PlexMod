package pw.ipex.plex.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

public class PlexUISlider extends GuiButton {
	public float sliderValue;
	public PlexUIBase parentUI;
	public boolean dragging;
	/*private static final String __OBFID = "CL_00000680";*/

	// GuiOptionSlider
	public PlexUISlider(PlexUIBase parent, int id, int x, int y, int width, int height, float def, String initialDisplayValue) {
		super(id, x, y, width, height, "");
		this.sliderValue = def;
		this.displayString = initialDisplayValue;
		this.parentUI = parent;
	}

	@Override
	public int getHoverState(boolean flag) {
		return 0;
	}

	@Override
	protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3) {
		if (this.visible) {
			if (this.dragging) { // dragging
				this.sliderValue = ((float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8));
				if (this.sliderValue < 0.0F) {
					this.sliderValue = 0.0F;
				}

				if (this.sliderValue > 1.0F) {
					this.sliderValue = 1.0F;
				}

				this.updateCallback();
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			drawTexturedModalRect(this.xPosition
					+ (int) (this.sliderValue * (this.width - 8)),
					this.yPosition, 0, 66, 4, 20);
			drawTexturedModalRect(this.xPosition
					+ (int) (this.sliderValue * (this.width - 8)) + 4,
					this.yPosition, 196, 66, 4, 20);
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int par2, int par3) {
		if (super.mousePressed(mc, par2, par3)) {
			this.sliderValue = ((float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8));

			if (this.sliderValue < 0.0F) {
				this.sliderValue = 0.0F;
			}

			if (this.sliderValue > 1.0F) {
				this.sliderValue = 1.0F;
			}

			this.updateCallback();
			this.dragging = true;
			return true;
		}
		return false;
	}

	public void mouseReleased(int p_146118_1_, int p_146118_2_) {
		this.dragging = false;
	}
	
	private void updateCallback() {
		this.parentUI.uiSliderInteracted(this);
	}
}