package pw.ipex.plex.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreTextures;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mods.plexmod.PlexPlexMod;

public class PlexUIModMenuScreen extends GuiScreen {
	public PlexUIBase baseUiScreen;
	public PlexUIModMenuScreen oldUiScreen;
	public List<Class<? extends PlexUIBase>> ownUiData = new ArrayList<Class<? extends PlexUIBase>>();
	public List<Gui> otherGuiItems = new ArrayList<Gui>();
	public Long colourFadeTime = 500L;
	public Long colourFade = null;
	public Integer oldForeground = null;
	public Integer oldBackground = null;
	public Boolean doneButtonShown = true;
	
	public PlexUIModMenuScreen(PlexUIBase base) {
		this.baseUiScreen = base;
		updateChild();
	}
	
	public PlexUIModMenuScreen(PlexUIBase base, PlexUIModMenuScreen oldScreen) {
		this.baseUiScreen = base;
		this.colourFade = Minecraft.getSystemTime();
		this.oldUiScreen = oldScreen;
		updateChild();
	}
	
	public void updateChild() {
		this.baseUiScreen.parentUI = this;
		doneButtonShown = !this.baseUiScreen.disableDoneButton();
	}
	
	public Boolean showDoneButton() {
		return !this.baseUiScreen.disableDoneButton();
	}
	
	public Integer startingYPos(Integer contentHeight) {
		Integer heightRange = verticalPixelCount();
		return zoneStartY() + PlexCoreUtils.intRange(heightRange - contentHeight, 0, null) / 4;
	}
	
	public Integer horizontalPixelCount() {
		return (zoneEndX() - zoneStartX());
	}
	
	public Integer verticalPixelCount() {
		return (zoneEndY() - zoneStartY());
	}
	
	public Integer zoneStartX() {
		return (this.width / 6);
	}
	
	public Integer zoneStartY() {
		return 50;
	}
	
	public Integer zoneEndX() {
		return this.width;
	}
	
	public Integer zoneEndY() {
		if (showDoneButton()) {
			return this.height - 30;
		}
		return this.height;
	}
	
	public Integer zoneCenterX() {
		return zoneStartX() + (horizontalPixelCount() / 2);
	}
	
	public Integer zoneCenterY() {
		return zoneStartY() + (verticalPixelCount() / 2);
	}
	
	public Integer centeredPanePos(Integer paneNumber, Integer paneCount, Integer minSize, Integer maxSize) {
		return (paneCount % 2 == 0 ? centeredPanePosEven(paneNumber, paneCount, minSize, maxSize) : centeredPanePosOdd(paneNumber, paneCount, minSize, maxSize));
	}
	
	public Integer centeredPanePosEven(Integer paneNumber, Integer paneCount, Integer minSize, Integer maxSize) {
		return zoneCenterX() + paneNumber * centeredPaneSize(paneCount, minSize, maxSize);
	}
	
	public Integer centeredPanePosOdd(Integer paneNumber, Integer paneCount, Integer minSize, Integer maxSize) {
		return zoneCenterX() + paneNumber * centeredPaneSize(paneCount, minSize, maxSize) - centeredPaneSize(paneCount, minSize, maxSize) / 2;
	}
	
	public Integer centeredPaneSize(Integer paneCount, Integer minSize, Integer maxSize) {
		return PlexCoreUtils.intRange(horizontalPixelCount() / paneCount, minSize, maxSize);
	}

	public Integer panePosition(Integer paneNumber, Integer paneCount) {
		return zoneStartX() + (panePixelCount(paneCount) * paneCount) - (panePixelCount(paneCount) / 2);
	}
	
	public Integer panePixelCount(Integer columns) {
		return horizontalPixelCount() / columns;
	}
	
	public Integer getCenteredStartY(Integer height) {
		return this.zoneCenterY() - (height);
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		this.addPlexUi();
		this.baseUiScreen.uiOpened();
		this.baseUiScreen.uiAddButtons(this);
	}
	
	public void addPlexUi() {
		if (showDoneButton()) {
			this.buttonList.add(new GuiButton(1, (this.width / 6) + ((this.width - (this.width / 6)) / 2) - 40, this.height - 25, 80, 20, "Done"));			
		}
		ownUiData.clear();
		Integer y_count = 0;
		for (Entry<String, Class<? extends PlexUIBase>> uiData : PlexCore.getUiTabList()) {
			Class<? extends PlexUIBase> uiClass = uiData.getValue();
			String tabName = uiData.getKey();
			ownUiData.add(uiClass);
			GuiButton button = new GuiButton(200 + y_count, 10, 35 + (y_count * 25), this.width / 6 - 20, 20, tabName);
			if (uiClass.equals(this.baseUiScreen.getClass())) {
				button.enabled = false;
			}
			this.buttonList.add(button);
			y_count += 1;
		}
	}
	
	public void addElement(GuiButton item) {
		this.buttonList.add(item);
	}
	
	public void addElement(PlexUISlider slider) {
		this.buttonList.add(slider);
	}

	public void addOtherElement(Gui item) {
		this.otherGuiItems.add(item);
	}
	
	public List<GuiButton> getButtonList() {
		return this.buttonList;
	}
	
	public FontRenderer getFontRenderer() {
		return this.fontRendererObj;
	}
	
	@Override
	public void actionPerformed(GuiButton button) {
		if (button.id == 1) {
			PlexCore.saveAllConfig();
			this.mc.displayGuiScreen((GuiScreen) null);
		}
		else if ((button.id >= 200) && (button.id <= 299)) {
			if (!(button.id-200 < ownUiData.size())) {
				return;
			}
			PlexCore.saveAllConfig();
			try {
				this.mc.displayGuiScreen(new PlexUIModMenuScreen(ownUiData.get(button.id-200).newInstance(), this));
			} 
			catch (InstantiationException e) {} 
			catch (IllegalAccessException e) {}
		}
		else {
			this.baseUiScreen.uiButtonClicked(button);
		}
	}
	
	@Override
	protected void keyTyped(char par1, int par2) throws IOException {
		super.keyTyped(par1, par2);
		if (par2 == 1) {
			PlexCore.saveAllConfig();
			this.mc.displayGuiScreen((GuiScreen) null);
		}
		this.baseUiScreen.keyTyped(par1, par2);
	}
	
	@Override
	protected void mouseClicked(int par1, int par2, int btn) throws IOException {
		clickSocialMedia(par1, par2);
		super.mouseClicked(par1, par2, btn);
		this.baseUiScreen.mouseClicked(par1, par2, btn);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		this.baseUiScreen.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		this.baseUiScreen.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		this.baseUiScreen.updateScreen();
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		this.baseUiScreen.handleMouseInput(x, y);
	}
	
	@Override
	public void onGuiClosed() {
		this.baseUiScreen.uiClosed();
	}
	
//    public void drawHorizontalLine(int startX, int endX, int y, int color) {
//        if (endX < startX) {
//            int i = startX;
//            startX = endX;
//            endX = i;
//        }
//
//        drawRect(startX, y, endX + 1, y + 1, color);
//    }
//
//    public void drawVerticalLine(int x, int startY, int endY, int color) {
//        if (endY < startY) {
//            int i = startY;
//            startY = endY;
//            endY = i;
//        }
//
//        drawRect(x, startY + 1, x + 1, endY, color);
//    }
//
//    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
//    	super.drawGradientRect(left, top, right, bottom, startColor, endColor);
//    }
	
	//public Integer globalChroma() {
	//	Integer[] chromaRGB = PlexCoreUtils.getChromaRGB((Minecraft.getSystemTime() / 100.0D * 20.0D));
	//	return PlexCoreUtils.colourCodeFrom(chromaRGB[0], chromaRGB[1], chromaRGB[2], 255);
	//}
	
	public Integer getForeground() {
		Integer ownForeground = this.baseUiScreen.pageForegroundColour();
		if (ownForeground.equals(-1)) {
			ownForeground = PlexCoreUtils.globalChromaCycle();
		}
		Integer foregroundColour = ownForeground;
		if (this.colourFade != null) {
			Double fadeMultiplier = (Minecraft.getSystemTime() - this.colourFade) / ((double) this.colourFadeTime);
			if (oldForeground == null) {
				oldForeground = this.oldUiScreen.getForeground();
			}
			if (fadeMultiplier < 1) {
				foregroundColour = PlexCoreUtils.betweenColours(oldForeground, ownForeground, fadeMultiplier);
			}
		}
		return foregroundColour;
	}
	
	public Integer getBackground() {
		Integer ownBackground = this.baseUiScreen.pageBackgroundColour();
		if (ownBackground.equals(-1)) {
			ownBackground = PlexCoreUtils.globalChromaCycle();
		}
		ownBackground = PlexCoreUtils.replaceColour(ownBackground, null, null, null, this.baseUiScreen.pageBackgroundTransparency());
		Integer backgroundColour = ownBackground;
		if (this.colourFade != null) {
			Double fadeMultiplier = (Minecraft.getSystemTime() - this.colourFade) / ((double) this.colourFadeTime);
			if (oldBackground == null) {
				oldBackground = this.oldUiScreen.getBackground();
			}
			if (fadeMultiplier < 1) {
				backgroundColour = PlexCoreUtils.betweenColours(oldBackground, ownBackground, fadeMultiplier);
			}
		}
		return backgroundColour;
	}
	
	public void clickSocialMedia(int mouseX, int mouseY) {
		int iconSize = 16;
		int barHeight = 25;
		int positionIncrement = iconSize + (iconSize / 6) + 2;
		int positionX = this.width - ((barHeight - iconSize) / 2) - iconSize;
		int positionY = ((barHeight - iconSize) / 2);
		for (String item : PlexPlexMod.socialMediaRenderInformation.keySet()) {
			if (PlexPlexMod.socialMediaLinks.containsKey(item)) {
				if (mouseX > positionX && mouseY > positionY && mouseX < positionX + iconSize && mouseY < positionY + iconSize) {
					if (PlexPlexMod.socialMediaLinkMapping.containsKey(item)) {
						PlexCoreUtils.openWebsite(PlexPlexMod.socialMediaLinkMapping.get(item) + PlexPlexMod.socialMediaLinks.get(item));
						return;
					}
				}
				positionX -= positionIncrement;
			}
		}		
	}
	
	public void drawSocialMedia() {
		int iconSize = 16;
		int barHeight = 25;
		int positionIncrement = iconSize + (iconSize / 6) + 2;
		int positionX = this.width - ((barHeight - iconSize) / 2) - iconSize;
		int positionY = ((barHeight - iconSize) / 2);
		GL11.glPushMatrix();
		GlStateManager.resetColor();
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		for (String item : PlexPlexMod.socialMediaRenderInformation.keySet()) {
			if (PlexPlexMod.socialMediaLinks.containsKey(item)) {
				Plex.minecraft.renderEngine.bindTexture(PlexPlexMod.socialMediaRenderInformation.get(item));
				GuiScreen.drawScaledCustomSizeModalRect(positionX, positionY, 0.0F, 0.0F, 256, 256, iconSize, iconSize, 256.0F, 256.0F);
				positionX -= positionIncrement;
			}
		}
		GL11.glPopMatrix();
	}
	
	public void drawHeaderImage() {
		GL11.glPushMatrix();
		Plex.minecraft.renderEngine.bindTexture(PlexCoreTextures.GUI_BANNER_ICON);
		GlStateManager.resetColor();
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GuiScreen.drawScaledCustomSizeModalRect(20, 0, 0.0F, 0.0F, 880, 200, 110, 25, 880.0F, 200.0F);
		GL11.glPopMatrix();
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		//Double timeSin = (Math.sin(Minecraft.getSystemTime() * 0.0075) + 1) / 2;
		//Integer titleColour = PlexCoreUtils.betweenColours(this.baseUiScreen.titleFadeColour1(), this.baseUiScreen.titleFadeColour2(), timeSin);
		Integer foregroundColour = getForeground();
		Integer backgroundColour = getBackground();
		drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680); // fill	
		drawRect(this.zoneStartX(), this.zoneStartY(), this.zoneEndX(), this.zoneEndY(), backgroundColour);
		this.baseUiScreen.drawScreen(par1, par2, par3);
		drawGradientRect(0, 0, this.width, 25, 0xaa10100f, 0xaa10100f); // top bar
		drawGradientRect(0, 25, this.width / 6, this.height, 0xaa10100f, 0xaa10100f); // side bar
		//GuiScreen.drawScaledCustomSizeModalRect((int) (20 / scale), (int) (0 / scale), 0.0F, 0.0F, 920, 170, 920, 170, 920.0F, 170.0F);		
		//GuiScreen.drawModalRectWithCustomSizedTexture((int) (20 / scale), (int) (5 / scale), 0.0F, 0.0F, 110, 25, 880.0F, 200.0F);
		drawHorizontalLine(0, this.width, 25, foregroundColour); // top bar border
		drawVerticalLine(this.width / 6, 25, this.height, foregroundColour); // side bar border
		//drawVerticalLine(25, this.height, this.width / 6, 0xffffffff); // side bar border
		//drawCenteredString(this.fontRendererObj, this.baseUiScreen.customGlobalTitle(), this.width / 2, 8, titleColour); // Global title
		drawCenteredString(this.fontRendererObj, this.baseUiScreen.uiGetTitle(), this.zoneCenterX(), 35, 16777215); // Local title
		drawHeaderImage();
		drawSocialMedia();
		super.drawScreen(par1, par2, par3);
	}
}