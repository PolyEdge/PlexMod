package cc.dyspore.plex.ui;

import java.io.IOException;
import java.util.*;

import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.util.PlexUtilRender;
import cc.dyspore.plex.core.util.PlexUtil;
import cc.dyspore.plex.core.util.PlexUtilTextures;
import cc.dyspore.plex.core.util.PlexUtilColour;
import cc.dyspore.plex.mods.plexmod.PlexModSocialMedia;
import cc.dyspore.plex.mods.plexmod.PlexModUI;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import cc.dyspore.plex.Plex;

public class PlexUIModMenu extends GuiScreen {
	public static final List<SocialMediaButton> socialMediaButtons = Collections.synchronizedList(new ArrayList<>());
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
	
	public PlexUIModMenu(PlexUIBase screen, GuiScreen parent, PlexUtilColour.ColourPalette palette) {
		this.childUI = screen;
		this.parent = parent;
		this.palette = palette != null ? palette : new PlexUtilColour.ColourPalette(4, 0xffffffff, null, paletteFadeTime);
		this.updateGuiState();
		updateSocialMedia();

		this.ee = ees.get(random.nextInt(ees.size()));
		this.eee = false;
	}

	public PlexUIModMenu(PlexUIBase screen) {
		this(screen, null, null);
	}

	public PlexUIModMenu(GuiScreen parent) {
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

	//

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
				this.mc.displayGuiScreen(new PlexUIModMenu(this.plexInterfaceUiTabs.get(button.id).getGuiClass().newInstance(), this.parent, this.palette));
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
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
		if (button == 0) {
			for (GuiButton guiButton : this.plexInterfaceButtonList) {
				if (guiButton.mousePressed(this.mc, mouseX, mouseY)) {
					guiButton.playPressSound(this.mc.getSoundHandler());
					this.plexInterfaceButtonPressed(guiButton);
				}
			}
		}		
		synchronized (socialMediaButtons) {
			for (SocialMediaButton socialMediaButton : socialMediaButtons) {
				socialMediaButton.click(this, mouseX, mouseY);
			}
		}
		super.mouseClicked(mouseX, mouseY, button);
		this.childUI.mouseClicked(mouseX, mouseY, button);
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

	//

	public FontRenderer getFontRenderer() {
		return this.fontRendererObj;
	}
	
	private int getForegroundColour() {
		return this.palette.getActiveColour(0);
	}
	
	private int getBackgroundColour() {
		return PlexUtilColour.replace(this.palette.getActiveColour(1), -1, -1, -1, PlexUtilColour.channel(this.palette.getActiveColour(2), 3));
	}
	
	private void drawHeaderImage() {
		//GL11.glPushMatrix();
		Plex.minecraft.renderEngine.bindTexture(PlexUtilTextures.GUI_BANNER_TEXTURE);
		GlStateManager.resetColor();
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GuiScreen.drawScaledCustomSizeModalRect(20, 0, 0.0F, 0.0F, 880, 200, 110, 25, 880.0F, 200.0F);
		//GL11.glPopMatrix();
	}

	private void drawSocialMedia(int mouseX, int mouseY) {
		//GL11.glPushMatrix();
		GlStateManager.resetColor();
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		synchronized (socialMediaButtons) {
			for (SocialMediaButton button : socialMediaButtons) {
				button.draw(this, mouseX, mouseY);
			}
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		//GL11.glPopMatrix();
	}

	private int getPaddedSocialMediaLocation() {
		synchronized (socialMediaButtons) {
			if (socialMediaButtons.size() == 0) {
				return this.width;
			}
			return socialMediaButtons.get(0).getX(this);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float par3) {
		this.updatePlexInterface();
		this.updateGuiState();

		int foregroundColour = this.getForegroundColour();
		int backgroundColour = this.getBackgroundColour();
		
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
			this.drawHeaderImage();
			this.drawSocialMedia(mouseX, mouseY);

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

			if (mouseX > this.getPaddedSocialMediaLocation() - 30 && mouseX < this.getPaddedSocialMediaLocation() - 5 && mouseY > 2 && mouseY < 24 && Mouse.isButtonDown(1)) {
				lobbyName = ee;
				this.eee = true;
			}
			else if (this.eee) {
				this.ee = ees.get(random.nextInt(ees.size()));
				this.eee = false;
			}

			PlexUtilRender.drawScaledStringRightSide(lobbyName, this.getPaddedSocialMediaLocation() - 2, 8, 0xdf8214, 1.0F, false);
		}

		
		for (GuiButton button : this.plexInterfaceButtonList) {
			button.drawButton(Plex.minecraft, mouseX, mouseY);
		}
     
		super.drawScreen(mouseX, mouseY, par3);
	}

	static {
		ees.add("Hey There!!!");
		ees.add("Subscribe to PewDiePie");
		ees.add("Worpp was here");
		ees.add("what u lookin at");
		ees.add(":)");
		ees.add("moo");
		ees.add("insert witty easter egg here");
		ees.add("<o/");
		ees.add("22.flp");
		ees.add("swag");
		ees.add("oh yeah yeah");
		ees.add("void u prolly find this first");
	}

	public static void updateSocialMedia() {
		synchronized (socialMediaButtons) {
			doUpdateSocialMedia();
		}
	}

	private static void doUpdateSocialMedia() {
		socialMediaButtons.clear();
		int iconSize = 16;
		int topBarHeight = 25;

		int positionY = ((topBarHeight - iconSize) / 2);
		int positionX = -positionY;
		int positionIncrement = iconSize + (iconSize / 6) + 2;

		List<PlexModSocialMedia> socialMediaList = Arrays.asList(PlexModSocialMedia.values());
		Collections.reverse(socialMediaList);

		for (PlexModSocialMedia socialMedia : socialMediaList) {
			if (!socialMedia.available) {
				continue;
			}
			socialMediaButtons.add(new SocialMediaButton(positionX - iconSize, positionY, iconSize, iconSize, socialMedia));
			positionX -= positionIncrement;
		}
		socialMediaButtons.add(0, new SocialMediaButton(positionX));
	}

	static class SocialMediaButton {
		private boolean isDummy;
		private int x;
		private int y;
		public int width;
		public int height;

		public PlexModSocialMedia socialMedia;

		private SocialMediaButton(int x) {
			this(x, 0, 0, 0, null);
			this.isDummy = true;
		}

		private SocialMediaButton(int x, int y, int width, int height, PlexModSocialMedia socialMedia) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.socialMedia = socialMedia;
		}

		public int getX(GuiScreen guiScreen) {
			return guiScreen.width + this.x;
		}

		public int getY(GuiScreen guiScreen) {
			return this.y;
		}

		public boolean isHovered(GuiScreen guiScreen, int mouseX, int mouseY) {
			int x = this.getX(guiScreen);
			int y = this.getY(guiScreen);
			return mouseX >= x && mouseY >= y && mouseX <= x + this.width && mouseY <= y + this.width && !this.isDummy;
		}

		public void click(GuiScreen guiScreen, int mouseX, int mouseY) {
			if (!this.isDummy && this.isHovered(guiScreen, mouseX, mouseY)) {
				PlexUtil.openURL(this.socialMedia.link);
			}
		}

		public void draw(GuiScreen guiScreen, int mouseX, int mouseY) {
			if (this.isDummy || this.socialMedia == null || !this.socialMedia.available) {
				return;
			}

			Plex.minecraft.renderEngine.bindTexture(socialMedia.icon);

			if (this.isHovered(guiScreen, mouseX, mouseY)) {
				GlStateManager.color(0.85F, 0.85F, 0.85F, 1.0F);
			}
			else {
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
			GuiScreen.drawScaledCustomSizeModalRect(this.getX(guiScreen), this.getY(guiScreen), 0.0F, 0.0F, socialMedia.iconWidth, socialMedia.iconHeight, this.width, this.height, socialMedia.iconWidth, socialMedia.iconHeight);
		}
	}
}