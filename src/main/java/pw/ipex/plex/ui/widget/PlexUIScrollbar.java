package pw.ipex.plex.ui.widget;

import net.minecraft.client.Minecraft;
import pw.ipex.plex.core.render.PlexCoreRenderUtils;

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
	
	public Integer toYPostion(float sliderValue) {
		return this.getScaledBarRangeTopY() + (int)((getScaledBarRangeBottomY() - getScaledBarRangeTopY()) * sliderValue);
	}
	
	public Float toSliderValue(Integer yPosition) {
		return (float)(yPosition - this.getScaledBarRangeTopY()) / (float)(getScaledBarRangeBottomY() - getScaledBarRangeTopY());
	}
	
	public Integer getScaledBarSize() {
		Integer totalPixels = this.barBottom - this.barTop;
		return (int)(totalPixels * this.barScale);		
	}
	
	public Integer getScaledBarRangeTopY() {
		return this.barTop + (this.getScaledBarSize() / 2);
	}
	
	public Integer getScaledBarRangeBottomY() {
		return this.barBottom - (this.getScaledBarSize() / 2);
	}
	
	public Integer getScaledBarSliderTopY() {
		return getScaledBarRangeTopY() + (this.getScaledBarSize() / 2);
	}
	
	public Integer getScaledBarSliderBottomY() {
		return this.barBottom - (this.getScaledBarSize() / 2);
	}
	
	public Integer getBarYPos() {
		if (this.velocityEnabled) {
			return this.toYPostion(this.targetPos); 
		}
		return this.toYPostion(this.scrollValue);
	}
	
	public Integer[] getBarWidgetPosition() {
		return new Integer[] {this.barTop, this.barBottom, this.barXPosition, this.barXPosition + this.barWidth};
	}	

	public Integer[] getBarSliderPosition() {
		Integer barSize = getScaledBarSize();
		Integer rangeTop = getScaledBarRangeTopY();
		Integer rangeBottom = getScaledBarRangeBottomY();
		Integer barSizeRange = rangeBottom - rangeTop;
		
		float scrollVal;
		if (this.velocityEnabled) {
			scrollVal = this.targetPos;
		}
		else {
			scrollVal = this.scrollValue;
		}
		
		Integer barCenterY = rangeTop + (int)(barSizeRange * scrollVal);
		Integer barTopY = barCenterY - (barSize / 2);
		Integer barBottomY = barCenterY + (barSize / 2);
		return new Integer[] {barTopY, barBottomY, this.barXPosition, this.barXPosition + this.barWidth};
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
		//Tessellator tessellator = Tessellator.getInstance();
		//WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		
		Integer[] widgetPos = getBarWidgetPosition();
		int top = widgetPos[0];
		int bottom = widgetPos[1];
		int left = widgetPos[2];
		int right = widgetPos[3];
		
		Integer[] barPos = getBarSliderPosition();
		int sbarTop = barPos[0];
		int sbarBottom = barPos[1];
		int sbarLeft = barPos[2];
		int sbarRight = barPos[3];
		
        PlexCoreRenderUtils.drawRect(left, top, right, bottom, 0xff000000);
		PlexCoreRenderUtils.drawRect(sbarLeft, sbarTop, sbarRight, sbarBottom, 0xff808080);
		PlexCoreRenderUtils.drawRect(sbarLeft, sbarTop, sbarRight - 1, sbarBottom - 1, 0xffc0c0c0);

        
        // original source just in case
//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
//        GlStateManager.disableAlpha();
//        GlStateManager.shadeModel(7425);
//        GlStateManager.disableTexture2D(); 
//        
//        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//		worldrenderer.pos(left, bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
//		worldrenderer.pos(right, bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
//		worldrenderer.pos(right, top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
//		worldrenderer.pos(left, top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
//		tessellator.draw();
//		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//		worldrenderer.pos(sbarLeft, sbarBottom, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
//		worldrenderer.pos(sbarRight, sbarBottom, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
//		worldrenderer.pos(sbarRight, sbarTop, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
//		worldrenderer.pos(sbarLeft, sbarTop, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
//		tessellator.draw();
//		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//		worldrenderer.pos(sbarLeft, sbarBottom + 1.0D, 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
//		worldrenderer.pos(sbarRight - 1.0D, sbarBottom + 1.0D, 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
//		worldrenderer.pos(sbarRight - 1.0D, sbarTop, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
//		worldrenderer.pos(sbarLeft, sbarTop + 1.0D, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
//		tessellator.draw();
//        
//		GlStateManager.enableTexture2D();
//        GlStateManager.shadeModel(7424);
//        GlStateManager.enableAlpha();
//        GlStateManager.disableBlend();
	}

	public boolean mouseOverSlider(int mouseX, int mouseY) {
		Integer[] barPos = getBarSliderPosition();
		int sbarTop = barPos[0];
		int sbarBottom = barPos[1];
		int sbarLeft = barPos[2];
		int sbarRight = barPos[3];
		return (mouseX < sbarRight) && (mouseX > sbarLeft) && (mouseY > sbarTop) && (mouseY < sbarBottom);
	}

	public boolean mouseOverWidget(int mouseX, int mouseY) {
		Integer[] widgetPos = getBarWidgetPosition();
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

//	public void mouseInput()
//	{
//		int wheel = Mouse.getEventDWheel();
//		if (wheel > 0) {
//			this.scrollY += this.speed;
//		} else if (wheel < 0) {
//			this.scrollY -= this.speed;
//		}
//		if (wheel != 0) {
//			checkOutOfBorders();
//		}
//	}
}
