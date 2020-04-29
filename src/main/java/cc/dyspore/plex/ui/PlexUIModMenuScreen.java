package cc.dyspore.plex.ui;

import java.io.IOException;
import java.util.*;

import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.util.PlexUtilRender;
import cc.dyspore.plex.core.util.PlexUtil;
import cc.dyspore.plex.core.util.PlexUtilTextures;
import cc.dyspore.plex.core.util.PlexUtilColour;
import cc.dyspore.plex.mods.plexmod.PlexMod;
import cc.dyspore.plex.mods.plexmod.PlexModSocialMedia;
import cc.dyspore.plex.mods.plexmod.PlexModUI;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import cc.dyspore.plex.Plex;

public class PlexUIModMenuScreen extends GuiScreen {
	public static List<SocialMediaButton> socialMediaButtons = new ArrayList<>();
	public static List<String> ees = new ArrayList<>();
	public static Random random = new Random();
	public static long paletteFadeTime = 500L;

	public GuiScreen parent;
	public PlexUIBase childUI;
	public PlexUtilColour.ColourPalette palette;

	public List<GuiButton> plexInterfaceButtonList = new ArrayList<>();
	public Map<Integer, PlexCore.PlexUITab> plexInterfaceUiTabs = new HashMap<>();

	public String ee;
	public boolean eee;
	
	public PlexUIModMenuScreen(PlexUIBase screen, GuiScreen parent, PlexUtilColour.ColourPalette palette) {
		this.childUI = screen;
		this.parent = parent;
		this.palette = palette != null ? palette : new PlexUtilColour.ColourPalette(4, 0xffffffff, null, paletteFadeTime);
		this.updateGuiState();

		this.ee = ees.get(random.nextInt(ees.size()));
		this.eee = false;
	}

	public PlexUIModMenuScreen(PlexUIBase screen) {
		this(screen, null, null);
	}

	public PlexUIModMenuScreen(GuiScreen parent) {
		this(new PlexModUI(), parent, null);
	}

	// positioning
	
	public int startingYPos(int contentHeight) {
		int heightRange = verticalPixelCount();
		return zoneStartY() + Math.max(heightRange - contentHeight, 0) / 4;
	}
	
	public int horizontalPixelCount() {
		return (zoneEndX() - zoneStartX());
	}
	
	public int verticalPixelCount() {
		return (zoneEndY() - zoneStartY());
	}
	
	public int zoneStartX() {
		if (this.childUI.disableSidebar() || this.childUI.disablePlexUi()) {
			return 0;
		}
		return (this.width / 6);
	}
	
	public int zoneStartY() {
		if (this.childUI.disablePlexUi()) {
			return 0;
		}
		return 50;
	}
	
	public int zoneEndX() {
		return this.width;
	}
	
	public int zoneEndY() {
		if (this.childUI.disableDoneButton()) {
			return this.height;
		}
		return this.height - 30;
	}
	
	public int zoneCenterX() {
		return zoneStartX() + (horizontalPixelCount() / 2);
	}
	
	public int zoneCenterY() {
		return zoneStartY() + (verticalPixelCount() / 2);
	}
	
	public int centeredPanePos(int paneNumber, int paneCount, int minSize, int maxSize) {
		return (paneCount % 2 == 0 ? centeredPanePosEven(paneNumber, paneCount, minSize, maxSize) : centeredPanePosOdd(paneNumber, paneCount, minSize, maxSize));
	}
	
	public int centeredPanePosEven(int paneNumber, int paneCount, int minSize, int maxSize) {
		return zoneCenterX() + paneNumber * centeredPaneSize(paneCount, minSize, maxSize);
	}
	
	public int centeredPanePosOdd(int paneNumber, int paneCount, int minSize, int maxSize) {
		return zoneCenterX() + paneNumber * centeredPaneSize(paneCount, minSize, maxSize) - centeredPaneSize(paneCount, minSize, maxSize) / 2;
	}
	
	public int centeredPaneSize(int paneCount, int minSize, int maxSize) {
		return PlexUtil.clamp(horizontalPixelCount() / paneCount, minSize, maxSize);
	}

	public int panePosition(int paneNumber, int paneCount) {
		return zoneStartX() + (panePixelCount(paneCount) * paneCount) - (panePixelCount(paneCount) / 2);
	}
	
	public int panePixelCount(int columns) {
		return horizontalPixelCount() / columns;
	}
	
	public int getCenteredStartY(int height) {
		return this.zoneCenterY() - (height);
	}

	//

	@Override
	public void initGui() {
		this.buttonList.clear();
		this.plexInterfaceButtonList.clear();
		this.initializePlexInterface();
		this.childUI.initGui(this);
		this.childUI.uiOpened();
	}

	public <T extends GuiButton> void addElement(T item) {
		this.buttonList.add(item);
	}

	public void close() {
		this.mc.displayGuiScreen(this.parent);
	}
	
	private void initializePlexInterface() {
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
	
	private void updatePlexInterface() {
		for (GuiButton button : this.plexInterfaceButtonList) {
			if (button.id == 1) {
				button.visible = !(this.childUI.disableDoneButton() || this.childUI.disablePlexUi());
			}
			else if (this.plexInterfaceUiTabs.containsKey(button.id)) {
				button.visible = !(this.childUI.disableSidebar() || this.childUI.disablePlexUi());
				button.enabled = !(this.plexInterfaceUiTabs.get(button.id).getGuiClass().equals(this.childUI.getClass()));
			}
		}
	}

	private void updateGuiState() {
		this.childUI.guiScreen = this;
		this.palette.setColour(0, this.childUI.pageForegroundColour(), this.childUI.pageForegroundState());
		this.palette.setColour(1, this.childUI.pageBackgroundColour(), this.childUI.pageBackgroundState());
		this.palette.setColour(2, PlexUtilColour.fromRGB(0, 0, 0, this.childUI.pageBackgroundTransparency()), PlexUtilColour.PaletteState.FIXED);
	}
	
	@Override
	public void actionPerformed(GuiButton button) {
		this.childUI.uiButtonClicked(button);
	}
	
	private void plexInterfaceButtonPressed(GuiButton button) {
		if (button.id == 1) {
			PlexCore.saveAllConfig();
			this.close();
		}
		else if (this.plexInterfaceUiTabs.containsKey(button.id) && !this.childUI.disablePlexUi()) {
			PlexCore.saveAllConfig();
			try {
				this.mc.displayGuiScreen(new PlexUIModMenuScreen(this.plexInterfaceUiTabs.get(button.id).getGuiClass().newInstance(), this.parent, this.palette));
			}
			catch (InstantiationException | IllegalAccessException ignored) {}
		}
	}
	
	@Override
	protected void keyTyped(char par1, int par2) throws IOException {
		if (par2 == 1) {
			if (this.childUI.escapeTyped()) {
				PlexCore.saveAllConfig();
				this.close();
			}
			return;
		}
		super.keyTyped(par1, par2);
		this.childUI.keyTyped(par1, par2);
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
		this.childUI.mouseClicked(par1, par2, btn);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		this.childUI.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		this.childUI.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		this.childUI.updateScreen();
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		this.childUI.handleMouseInput(x, y);
	}
	
	@Override
	public void onGuiClosed() {
		this.childUI.uiClosed();
	}

	public FontRenderer getFontRenderer() {
		return this.fontRendererObj;
	}
	
	private int getForegroundColour() {
		return this.palette.getActiveColour(0);
	}
	
	private int getBackgroundColour() {
		return PlexUtilColour.replace(this.palette.getActiveColour(1), -1, -1, -1, PlexUtilColour.channel(this.palette.getActiveColour(2), 3));
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
						PlexUtil.openURL(PlexMod.socialMediaLinkMapping.get(item) + PlexMod.socialMediaLinks.get(item));
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
	
	private void drawHeaderImage() {
		GL11.glPushMatrix();
		Plex.minecraft.renderEngine.bindTexture(PlexUtilTextures.GUI_BANNER_TEXTURE);
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
		this.updateGuiState();

		int foregroundColour = getForegroundColour();
		int backgroundColour = getBackgroundColour();
		
		if (!this.childUI.disablePlexUi()) {
			drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680); // screen fill	
			drawRect(this.zoneStartX(), this.zoneStartY(), this.zoneEndX(), this.zoneEndY(), backgroundColour); // zone fill			
		}

		this.childUI.drawScreen(mouseX, mouseY, par3);
		
		if (!this.childUI.disablePlexUi()) {
			drawGradientRect(0, 0, this.width, 25, 0xaa10100f, 0xaa10100f); // top bar
			
			if (!this.childUI.disableSidebar()) {
				drawGradientRect(0, 25, this.width / 6, this.height, 0xaa10100f, 0xaa10100f); // side bar
			}
			
			drawRect(this.zoneStartX(), 25, this.zoneEndX(), this.zoneStartY(), 0x7710100f); // local title bar background 
			
			if (!this.childUI.disableDoneButton()) {
				drawRect(this.zoneStartX(), this.zoneEndY(), this.zoneEndX(), this.height, 0x7710100f); // done button bar background
			}
			
			drawHorizontalLine(0, this.width, 25, foregroundColour); // top bar border
			
			if (!this.childUI.disableSidebar()) {
				drawVerticalLine(this.width / 6, 25, this.height, foregroundColour); // side bar border
			}

			drawCenteredString(this.fontRendererObj, this.childUI.uiGetTitle(), this.zoneCenterX(), 35, 16777215); // Local title
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
				this.eee = true;
			}
			else if (this.eee) {
				this.ee = ees.get(random.nextInt(ees.size()));
				this.eee = false;
			}

			PlexUtilRender.drawScaledStringRightSide(lobbyName, this.getSocialMediaStartX() - 5, 8, 0xdf8214, 1.0F, false);
		}

		
		for (GuiButton button : this.plexInterfaceButtonList) {
			button.drawButton(Plex.minecraft, mouseX, mouseY);
		}
     
		super.drawScreen(mouseX, mouseY, par3);
	}

	public static void updateSocialMedia() {
		socialMediaButtons.clear();
		int iconSize = 16;
		int topBarHeight = 25;

		int positionIncrement = iconSize + (iconSize / 6) + 2;
		int positionX = -((topBarHeight - iconSize) / 2) - iconSize;
		int positionY = ((topBarHeight - iconSize) / 2);

		List<PlexModSocialMedia> socialMediaList = Arrays.asList(PlexModSocialMedia.values());
		Collections.reverse(socialMediaList);

		for (PlexModSocialMedia socialMedia : socialMediaList) {
			if (!socialMedia.available) {
				continue;
			}
			//socialMediaButtons.add(new SocialMediaButton())
			//if (PlexMod.socialMediaLinks.containsKey(item)) {
			//	Plex.minecraft.renderEngine.bindTexture(PlexMod.socialMediaRenderInformation.get(item));
			//	GuiScreen.drawScaledCustomSizeModalRect(positionX, positionY, 0.0F, 0.0F, 256, 256, iconSize, iconSize, 256.0F, 256.0F);
			//	positionX -= positionIncrement;
			//}
		}
	}

	static {
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
	}

	static class SocialMediaButton {
		private int x;
		private int y;
		public int width;
		public int height;

		public PlexModSocialMedia socialMedia;

		public SocialMediaButton(int x, int y, int width, int height, PlexModSocialMedia socialMedia) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.socialMedia = socialMedia;
		}
	}
}