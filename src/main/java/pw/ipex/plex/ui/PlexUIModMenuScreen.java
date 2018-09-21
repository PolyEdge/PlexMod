package pw.ipex.plex.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreRenderUtils;
import pw.ipex.plex.core.PlexCoreTextures;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mods.plexmod.PlexPlexMod;

public class PlexUIModMenuScreen extends GuiScreen {
	public PlexUIBase baseUiScreen;
	public PlexUIColourState colourState = new PlexUIColourState();
	public PlexUIColourState oldColourState = null;
	public List<GuiButton> internalButtonList = new ArrayList<GuiButton>();
	public List<PlexUITabContainer> uiTabs = new ArrayList<PlexUITabContainer>();
	public Long initializationTime;
	public Long colourFadeTime = 500L;
	
	public PlexUIModMenuScreen(PlexUIBase base) {
		this.baseUiScreen = base;
		this.initializationTime = Minecraft.getSystemTime();
		this.updateColourState();
		this.updateChild();
	}
	
	public PlexUIModMenuScreen(PlexUIBase base, PlexUIColourState oldColourState) {
		this(base);
		this.oldColourState = oldColourState;
	}
	
	public void updateChild() {
		this.baseUiScreen.parentUI = this;
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
		if (this.baseUiScreen.disableSidebar() || this.baseUiScreen.disablePlexUi()) {
			return 0;
		}
		return (this.width / 6);
	}
	
	public Integer zoneStartY() {
		if (this.baseUiScreen.disablePlexUi()) {
			return 0;
		}
		return 50;
	}
	
	public Integer zoneEndX() {
		return this.width;
	}
	
	public Integer zoneEndY() {
		if (this.baseUiScreen.disableDoneButton()) {
			return this.height;
		}
		return this.height - 30;
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
		this.internalButtonList.clear();
		this.addPlexUi();			
		this.baseUiScreen.uiOpened();
		this.baseUiScreen.uiAddButtons(this);
	}
	
	public void addPlexUi() {
		this.internalButtonList.add(new GuiButton(1, (this.width / 6) + ((this.width - (this.width / 6)) / 2) - 40, this.height - 25, 80, 20, "Done"));			
		this.uiTabs.clear();
		int y_count = 0;
		int tabID = -1;
		for (PlexUITabContainer tab : PlexCore.getUiTabList()) {
			uiTabs.add(tab.getShallowCopy().setID(tabID));
			GuiButton button = new GuiButton(tabID, 10, 35 + (y_count * 25), this.width / 6 - 20, 20, tab.getLabel());
			this.internalButtonList.add(button);
			y_count += 1;
			tabID -= 1;
		}
	}
	
	public void updatePlexUi() {
		for (GuiButton button : this.internalButtonList) {
			if (button.id == 1) {
				button.visible = !(this.baseUiScreen.disableDoneButton() || this.baseUiScreen.disablePlexUi());
			}
			else if (button.id < 0) {
				button.visible = !(this.baseUiScreen.disableSidebar() || this.baseUiScreen.disablePlexUi());
				for (PlexUITabContainer tab : this.uiTabs) {
					if (button.id == tab.getID()) {
						button.enabled = !(tab.uiClass.equals(this.baseUiScreen.getClass()));
					}
				}
			}
		}
	}
	
	public void addElement(GuiButton item) {
		this.buttonList.add(item);
	}
	
	public void addElement(PlexUISlider slider) {
		this.buttonList.add(slider);
	}

	public List<GuiButton> getButtonList() {
		return this.buttonList;
	}
	
	public FontRenderer getFontRenderer() {
		return this.fontRendererObj;
	}
	
	@Override
	public void actionPerformed(GuiButton button) {
		this.baseUiScreen.uiButtonClicked(button);
	}
	
	public void internalActionPerformed (GuiButton button) {
		if (button.id == 1) {
			PlexCore.saveAllConfig();
			this.mc.displayGuiScreen((GuiScreen) null);
		}
		else if (button.id < 0 && !this.baseUiScreen.disablePlexUi()) {
			for (PlexUITabContainer tab : this.uiTabs) {
				if (button.id == tab.getID()) {
					PlexCore.saveAllConfig();
					try {
						this.mc.displayGuiScreen(new PlexUIModMenuScreen(tab.uiClass.newInstance(), this.colourState));
						return;
					} 
					catch (InstantiationException e) {} 
					catch (IllegalAccessException e) {}
				}
			}
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
		if (btn == 0) {
			for (GuiButton button : this.internalButtonList) {
				if (button.mousePressed(this.mc, par1, par2)) {
					button.playPressSound(this.mc.getSoundHandler());
					this.internalActionPerformed(button);
				}
			}
		}		

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
	
	public void updateColourState() {
		this.colourState.setColour("foreground", this.baseUiScreen.pageForegroundColour());
		this.colourState.setColour("background", this.baseUiScreen.pageBackgroundColour());
		this.colourState.setColour("background_transparency", PlexCoreUtils.colourCodeFrom(0, 0, 0, this.baseUiScreen.pageBackgroundTransparency()));
	}
	
	public Integer getForeground() {
		float fadeTime = PlexCoreUtils.floatRange((float) ((Minecraft.getSystemTime() - this.initializationTime) / ((double) this.colourFadeTime)), 0.0F, 1.0F);
		return this.colourState.colourBetweenStates("foreground", this.oldColourState, this.colourState, fadeTime);
	}
	
	public Integer getBackground() {
		float fadeTime = PlexCoreUtils.floatRange((float) ((Minecraft.getSystemTime() - this.initializationTime) / ((double) this.colourFadeTime)), 0.0F, 1.0F);
		return PlexCoreUtils.replaceColour(this.colourState.colourBetweenStates("background", this.oldColourState, this.colourState, fadeTime), null, null, null, PlexCoreUtils.rgbCodeFrom(this.colourState.colourBetweenStates("background_transparency", this.oldColourState, this.colourState, fadeTime))[3]);
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

	public int getSocialMediaStartX() {
		int iconSize = 16;
		int barHeight = 25;
		int positionIncrement = iconSize + (iconSize / 6) + 2;
		int positionX = this.width - ((barHeight - iconSize) / 2) - iconSize;
		int positionY = ((barHeight - iconSize) / 2);
		for (String item : PlexPlexMod.socialMediaRenderInformation.keySet()) {
			if (PlexPlexMod.socialMediaLinks.containsKey(item)) {
				positionX -= positionIncrement;
			}
		}
		return positionX + positionIncrement;
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
		
		this.updatePlexUi();
		this.updateColourState();

		Integer foregroundColour = getForeground();
		Integer backgroundColour = getBackground();
		
		if (!this.baseUiScreen.disablePlexUi()) {
			drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680); // screen fill	
			drawRect(this.zoneStartX(), this.zoneStartY(), this.zoneEndX(), this.zoneEndY(), backgroundColour); // zone fill			
		}

		this.baseUiScreen.drawScreen(par1, par2, par3);
		
		if (!this.baseUiScreen.disablePlexUi()) {
			drawGradientRect(0, 0, this.width, 25, 0xaa10100f, 0xaa10100f); // top bar
			
			if (!this.baseUiScreen.disableSidebar()) {
				drawGradientRect(0, 25, this.width / 6, this.height, 0xaa10100f, 0xaa10100f); // side bar
			}
			
			drawRect(this.zoneStartX(), 25, this.zoneEndX(), this.zoneStartY(), 0x7710100f); // local title bar background 
			
			if (!this.baseUiScreen.disableDoneButton()) {
				drawRect(this.zoneStartX(), this.zoneEndY(), this.zoneEndX(), this.height, 0x7710100f); // done button bar background
			}
			
			drawHorizontalLine(0, this.width, 25, foregroundColour); // top bar border
			
			if (!this.baseUiScreen.disableSidebar()) {
				drawVerticalLine(this.width / 6, 25, this.height, foregroundColour); // side bar border
			}

			drawCenteredString(this.fontRendererObj, this.baseUiScreen.uiGetTitle(), this.zoneCenterX(), 35, 16777215); // Local title
			drawHeaderImage();
			drawSocialMedia();

			String lobbyName = Plex.serverState.updatedLobbyName;
			if (lobbyName == null) {
				lobbyName = "...";
			}

			PlexCoreRenderUtils.drawScaledStringRightSide(lobbyName, this.getSocialMediaStartX() - 5, 8, 0xdf8214, 1.0F, false);
		}

		
		for (GuiButton button : this.internalButtonList) {
			button.drawButton(Plex.minecraft, par1, par2);
		}
     
		super.drawScreen(par1, par2, par3);
	}
}