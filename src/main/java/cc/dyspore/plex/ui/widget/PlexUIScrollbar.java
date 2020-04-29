package cc.dyspore.plex.ui.widget;

import cc.dyspore.plex.core.util.PlexUtilRender;
import net.minecraft.client.Minecraft;

//import org.lwjgl.input.Mouse;
//import org.lwjgl.opengl.GL11;

//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.client.renderer.WorldRenderer;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class PlexUIScrollbar {
	public float scrollValue = 0.0F;
	public float realScrollValue = 0.0F;
	
	public int barTop;
	public int barBottom;
	public int barXPosition;
	public int barWidth;
	public float barScale = 1.0F;
	
	public boolean isEnabled = true;
	public boolean isHeld = false;
	public int heldOffsetY;

	public Float hiddenForcedScroll = null;
	
	public boolean velocityEnabled = true;
	private boolean velocityWasEnabled = true;
	public long lastVelocityUpdate = 0L;
	public float targetPos = 0.0F;
	public int velocityCurve = 4;
	public int velocityUpdateSpeed = 25;

	public int updateMethod = 0; // 0 - drawScreen;  1 - updateScreen
	
	public PlexUIScrollbar(int top, int bottom, int x, int width) {
		this.barTop = top;
		this.barBottom = bottom;
		this.barXPosition = x;
		this.barWidth = width;
	}

	public void setPosition(int top, int bottom, int x, int width) {
		this.barTop = top;
		this.barBottom = bottom;
		this.barXPosition = x;
		this.barWidth = width;
	}
	
	public void setScroll(float value) {
		this.setScroll(value, false);
	}
	
	public void setScroll(float value, boolean force) {
		if (value > 1.0F) {
			value = 1.0F;
		}
		if (value < 0.0F) {
			value = 0.0F;
		}
		this.realScrollValue = value;
		if (this.velocityEnabled) {
			this.targetPos = value;
			if (force) {
				this.scrollValue = value;
			}
		}
		else {
			this.scrollValue = value;
		}
	}
	
	public void scrollBy(float value) {
		this.setScroll(this.realScrollValue + value);
	}
	
	public void setContentScale(float scale) {
		if (scale > 1.0) {
			this.barScale = 1.0F;
		}
		else if (scale < 0.0) {
			this.barScale = 0.0F; // xd
		}
		else {
			this.barScale = scale;
		}
	}
	
	public void scrollByPixels(int pixels, int contentSizePixels, int viewportPixels) {
		if ((contentSizePixels - viewportPixels) <= 0) {
			return;
		}
		this.scrollBy((float) pixels / (float) (contentSizePixels - viewportPixels));
	}
	
	public void setEnabled(boolean enabled) {
		this.isEnabled = enabled;
	}
	
	public boolean barVisible() {
		if (this.barScale >= 1) {
			return false;
		}
		return true;
	}
	
	public void updateVelocity() {
		if (this.velocityEnabled) {
			if (!this.velocityWasEnabled) {
				this.targetPos = this.scrollValue;
				this.velocityWasEnabled = true;
			}
			if (Minecraft.getSystemTime() > lastVelocityUpdate + velocityUpdateSpeed) {
				this.scrollValue = this.scrollValue + (this.targetPos - this.scrollValue) / this.velocityCurve;
				lastVelocityUpdate = Minecraft.getSystemTime();
			}
		}
		if (!this.velocityEnabled && this.velocityWasEnabled) {
			this.velocityWasEnabled = false;
		}
	}
	
	public int toYPostion(float sliderValue) {
		return this.getScaledBarRangeTopY() + (int)((getScaledBarRangeBottomY() - getScaledBarRangeTopY()) * sliderValue);
	}
	
	public float toSliderValue(int yPosition) {
		return (float)(yPosition - this.getScaledBarRangeTopY()) / (float)(getScaledBarRangeBottomY() - getScaledBarRangeTopY());
	}
	
	public int getScaledBarSize() {
		int totalPixels = this.barBottom - this.barTop;
		return (int)(totalPixels * this.barScale);		
	}
	
	public int getScaledBarRangeTopY() {
		return this.barTop + (this.getScaledBarSize() / 2);
	}
	
	public int getScaledBarRangeBottomY() {
		return this.barBottom - (this.getScaledBarSize() / 2);
	}
	
	public int getScaledBarSliderTopY() {
		return getScaledBarRangeTopY() + (this.getScaledBarSize() / 2);
	}
	
	public int getScaledBarSliderBottomY() {
		return this.barBottom - (this.getScaledBarSize() / 2);
	}
	
	public int getBarYPos() {
		if (this.velocityEnabled) {
			return this.toYPostion(this.targetPos); 
		}
		return this.toYPostion(this.scrollValue);
	}
	
	public int[] getBarWidgetPosition() {
		return new int[] {this.barTop, this.barBottom, this.barXPosition, this.barXPosition + this.barWidth};
	}	

	public int[] getBarSliderPosition() {
		int barSize = getScaledBarSize();
		int rangeTop = getScaledBarRangeTopY();
		int rangeBottom = getScaledBarRangeBottomY();
		int barSizeRange = rangeBottom - rangeTop;
		
		float scrollVal;
		if (this.velocityEnabled) {
			scrollVal = this.targetPos;
		}
		else {
			scrollVal = this.scrollValue;
		}
		
		int barCenterY = rangeTop + (int)(barSizeRange * scrollVal);
		int barTopY = barCenterY - (barSize / 2);
		int barBottomY = barCenterY + (barSize / 2);
		return new int[] {barTopY, barBottomY, this.barXPosition, this.barXPosition + this.barWidth};
	}

	public void update() {
		this.updateVelocity();
		if (!this.barVisible() && this.hiddenForcedScroll != null) {
			this.setScroll(this.hiddenForcedScroll, false);
		}
	}

	public void updateScreen() {
		if (this.updateMethod == 1) {
			this.update();
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		if (this.updateMethod == 0) {
			this.update();
		}
		if (!barVisible()) {
			return;
		}
		
		int[] widgetPos = getBarWidgetPosition();
		int top = widgetPos[0];
		int bottom = widgetPos[1];
		int left = widgetPos[2];
		int right = widgetPos[3];
		
		int[] barPos = getBarSliderPosition();
		int sbarTop = barPos[0];
		int sbarBottom = barPos[1];
		int sbarLeft = barPos[2];
		int sbarRight = barPos[3];
		
        PlexUtilRender.drawRect(left, top, right, bottom, 0xff000000);
		PlexUtilRender.drawRect(sbarLeft, sbarTop, sbarRight, sbarBottom, 0xff808080);
		PlexUtilRender.drawRect(sbarLeft, sbarTop, sbarRight - 1, sbarBottom - 1, 0xffc0c0c0);
	}

	public boolean mouseOverSlider(int mouseX, int mouseY) {
		int[] barPos = getBarSliderPosition();
		int sbarTop = barPos[0];
		int sbarBottom = barPos[1];
		int sbarLeft = barPos[2];
		int sbarRight = barPos[3];
		return (mouseX < sbarRight) && (mouseX > sbarLeft) && (mouseY > sbarTop) && (mouseY < sbarBottom);
	}

	public boolean mouseOverWidget(int mouseX, int mouseY) {
		int[] widgetPos = getBarWidgetPosition();
		int top = widgetPos[0];
		int bottom = widgetPos[1];
		int left = widgetPos[2];
		int right = widgetPos[3];
		return (mouseX < right) && (mouseX > left) && (mouseY > top) && (mouseY < bottom);
	}

	public void mousePressed(int mouseX, int mouseY, int mouseButton) {
		if (!this.isEnabled) {
			return;
		}
		if (!this.barVisible()) {
			return;
		}
		if (this.mouseOverSlider(mouseX, mouseY)) {
			this.isHeld = true;
			this.heldOffsetY = this.getBarYPos() - mouseY;
		}
		else if (this.mouseOverWidget(mouseX, mouseY)) {
			this.setScroll(this.toSliderValue(mouseY + this.heldOffsetY));
		}
	}
	
	public void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (!this.isEnabled) {
			return;
		}
		if (!this.barVisible()) {
			return;
		}
		if (this.isHeld) {
			this.setScroll(this.toSliderValue(mouseY + this.heldOffsetY));
		}
	}

	public void mouseReleased(int p_146118_1_, int p_146118_2_) {
		this.isHeld = false;
	}
}
