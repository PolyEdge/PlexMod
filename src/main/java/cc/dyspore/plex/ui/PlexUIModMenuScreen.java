package cc.dyspore.plex.ui;

import java.io.IOException;
import java.util.*;

import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.util.PlexUtilRender;
import cc.dyspore.plex.core.util.PlexUtil;
import cc.dyspore.plex.core.util.PlexUtilTextures;
import cc.dyspore.plex.core.util.PlexUtilColour;
import cc.dyspore.plex.mods.plexmod.PlexMod;
import cc.dyspore.plex.mods.plexmod.PlexModUI;
import cc.dyspore.plex.ui.widget.PlexUISlider;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import cc.dyspore.plex.Plex;

import cc.dyspore.plex.core.render.PlexCoreRenderColourState;

public class PlexUIModMenuScreen extends GuiScreen {
	public GuiScreen parent;
	public PlexUIBase baseUiScreen;
	public PlexCoreRenderColourState colourState = new PlexCoreRenderColourState();
	public PlexCoreRenderColourState oldColourState = null;
	public List<GuiButton> plexInterfaceButtonList = new ArrayList<>();
	public Map<Integer, PlexCore.PlexUITab> plexInterfaceUiTabs = new HashMap<>();
	public long initializationTime;
	public long colourFadeTime = 500L;

	public List<String> ees = new ArrayList<>();
	public String ee;
	public boolean ee1;
	
	public PlexUIModMenuScreen(PlexUIBase base) {
		this.baseUiScreen = base;
		this.initializationTime = Minecraft.getSystemTime();
		this.updateColourState();
		this.updateChild();

		ees.add("Hey There!!!");
		ees.add("Subscribe to PewDiePie");
		ees.add("Worpp was here");
		ees.add("what u lookin at");
		ees.add("*wave check*");
		ees.add("moo");
		ees.add("insert witty easter egg here");
		ees.add("<o/");
		ees.add("22.flp");
		ees.add("swag");
		ees.add("oh yeah yeah");
		ees.add("void u prolly find this first xd");

		this.ee = ees.get(new Random().nextInt(ees.size()));
		this.ee1 = false;
	}

	public PlexUIModMenuScreen(GuiScreen parent) {
		this(new PlexModUI());
		this.parent = parent;
	}
	
	public PlexUIModMenuScreen(PlexUIBase base, PlexCoreRenderColourState oldColourState, GuiScreen parent) {
		this(base);
		this.oldColourState = oldColourState;
		this.parent = parent;
	}

	public void close() {
		this.mc.displayGuiScreen(this.parent);
	}
	
	public void updateChild() {
		this.baseUiScreen.parentUI = this;
	}
	
	public Integer startingYPos(Integer contentHeight) {
		Integer heightRange = verticalPixelCount();
		return zoneStartY() + PlexUtil.clamp(heightRange - contentHeight, 0, null) / 4;
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
		return PlexUtil.clamp(horizontalPixelCount() / paneCount, minSize, maxSize);
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
		this.plexInterfaceButtonList.clear();
		this.initializePlexInterface();
		this.baseUiScreen.initGui(this);
		this.baseUiScreen.uiOpened();
	}
	
	public void initializePlexInterface() {
		this.plexInterfaceButtonList.add(new GuiButton(1, (this.width / 6) + ((this.width - (this.width / 6)) / 2) - 40, this.height - 25, 80, 20, "Done"));
		this.plexInterfaceUiTabs.clear();

		List<PlexCore.PlexUITab> tabs = PlexCore.getUiTabList();
		for (int i = 0; i < tabs.size(); i++) {
			PlexCore.PlexUITab tab = tabs.get(i);
			this.plexInterfaceUiTabs.put(-1 - i, tab.getShallowCopy());
			GuiButton button = new GuiButton(-1 - i, 10, 35 + (i * 25), this.width / 6 - 20, 20, tab.getLabel());
			this.plexInterfaceButtonList.add(button);
		}
	}
	
	public void updatePlexInterface() {
		for (GuiButton button : this.plexInterfaceButtonList) {
			if (button.id == 1) {
				button.visible = !(this.baseUiScreen.disableDoneButton() || this.baseUiScreen.disablePlexUi());
			}
			else if (this.plexInterfaceUiTabs.containsKey(button.id)) {
				button.visible = !(this.baseUiScreen.disableSidebar() || this.baseUiScreen.disablePlexUi());
				button.enabled = !(this.plexInterfaceUiTabs.get(button.id).getGuiClass().equals(this.baseUiScreen.getClass()));
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
	
	public void plexInterfaceButtonPressed(GuiButton button) {
		if (button.id == 1) {
			PlexCore.saveAllConfig();
			this.close();
		}
		else if (this.plexInterfaceUiTabs.containsKey(button.id) && !this.baseUiScreen.disablePlexUi()) {
			PlexCore.saveAllConfig();
			try {
				this.mc.displayGuiScreen(new PlexUIModMenuScreen(this.plexInterfaceUiTabs.get(button.id).getGuiClass().newInstance(), this.colourState, this.parent));
			}
			catch (InstantiationException | IllegalAccessException ignored) {}
		}
	}
	
	@Override
	protected void keyTyped(char par1, int par2) throws IOException {
		if (par2 == 1) {
			if (this.baseUiScreen.escapeTyped()) {
				PlexCore.saveAllConfig();
				this.close();
			}
			return;
		}
		super.keyTyped(par1, par2);
		this.baseUiScreen.keyTyped(par1, par2);
	}
	
	@Override
	protected void mouseClicked(int par1, int par2, int btn) throws IOException {
		if (btn == 0) {
			for (GuiButton button : this.plexInterfaceButtonList) {
				if (button.mousePressed(this.mc, par1, par2)) {
					button.playPressSound(this.mc.getSoundHandler());
					this.plexInterfaceButtonPressed(button);
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
		this.colourState.setColour("background_transparency", PlexUtilColour.fromRGB(0, 0, 0, this.baseUiScreen.pageBackgroundTransparency()));
	}
	
	public Integer getForeground() {
		float fadeTime = PlexUtil.clamp((float) ((Minecraft.getSystemTime() - this.initializationTime) / ((double) this.colourFadeTime)), 0.0F, 1.0F);
		return this.colourState.colourBetweenStates("foreground", this.oldColourState, this.colourState, fadeTime);
	}
	
	public Integer getBackground() {
		float fadeTime = PlexUtil.clamp((float) ((Minecraft.getSystemTime() - this.initializationTime) / ((double) this.colourFadeTime)), 0.0F, 1.0F);
		return PlexUtilColour.replace(this.colourState.colourBetweenStates("background", this.oldColourState, this.colourState, fadeTime), null, null, null, PlexUtilColour.toRGB(this.colourState.colourBetweenStates("background_transparency", this.oldColourState, this.colourState, fadeTime))[3]);
	}
	
	public void clickSocialMedia(int mouseX, int mouseY) {
		int iconSize = 16;
		int barHeight = 25;
		int positionIncrement = iconSize + (iconSize / 6) + 2;
		int positionX = this.width - ((barHeight - iconSize) / 2) - iconSize;
		int positionY = ((barHeight - iconSize) / 2);
		for (String item : PlexMod.socialMediaRenderInformation.keySet()) {
			if (PlexMod.socialMediaLinks.containsKey(item)) {
				if (mouseX > positionX && mouseY > positionY && mouseX < positionX + iconSize && mouseY < positionY + iconSize) {
					if (PlexMod.socialMediaLinkMapping.containsKey(item)) {
						PlexUtil.openWebsite(PlexMod.socialMediaLinkMapping.get(item) + PlexMod.socialMediaLinks.get(item));
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
		for (String item : PlexMod.socialMediaRenderInformation.keySet()) {
			if (PlexMod.socialMediaLinks.containsKey(item)) {
				Plex.minecraft.renderEngine.bindTexture(PlexMod.socialMediaRenderInformation.get(item));
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
		for (String item : PlexMod.socialMediaRenderInformation.keySet()) {
			if (PlexMod.socialMediaLinks.containsKey(item)) {
				positionX -= positionIncrement;
			}
		}
		return positionX + positionIncrement;
	}
	
	public void drawHeaderImage() {
		GL11.glPushMatrix();
		Plex.minecraft.renderEngine.bindTexture(PlexUtilTextures.GUI_BANNER_ICON);
		GlStateManager.resetColor();
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GuiScreen.drawScaledCustomSizeModalRect(20, 0, 0.0F, 0.0F, 880, 200, 110, 25, 880.0F, 200.0F);
		GL11.glPopMatrix();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float par3) {
		
		this.updatePlexInterface();
		this.updateColourState();

		Integer foregroundColour = getForeground();
		Integer backgroundColour = getBackground();
		
		if (!this.baseUiScreen.disablePlexUi()) {
			drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680); // screen fill	
			drawRect(this.zoneStartX(), this.zoneStartY(), this.zoneEndX(), this.zoneEndY(), backgroundColour); // zone fill			
		}

		this.baseUiScreen.drawScreen(mouseX, mouseY, par3);
		
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

			String lobbyName;
			if (!Plex.gameState.isMineplex) {
				lobbyName = "[Not Online]";
			}
			else if (Plex.gameState.currentLobby == null || Plex.gameState.currentLobby.name == null) {
				lobbyName = "...";
			}
			else {
				lobbyName = Plex.gameState.currentLobby.name;
			}

			if (mouseX > this.getSocialMediaStartX() - 35 && mouseX < this.getSocialMediaStartX() - 10 && mouseY > 2 && mouseY < 24 && Mouse.isButtonDown(1)) {
				lobbyName = ee;
				this.ee1 = true;
			}
			else if (this.ee1){
				this.ee = ees.get(new Random().nextInt(ees.size()));
				this.ee1 = false;
			}

			PlexUtilRender.drawScaledStringRightSide(lobbyName, this.getSocialMediaStartX() - 5, 8, 0xdf8214, 1.0F, false);
		}

		
		for (GuiButton button : this.plexInterfaceButtonList) {
			button.drawButton(Plex.minecraft, mouseX, mouseY);
		}
     
		super.drawScreen(mouseX, mouseY, par3);
	}
}