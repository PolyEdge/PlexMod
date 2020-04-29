package cc.dyspore.plex.ui;

import cc.dyspore.plex.core.util.PlexUtilColour;
import cc.dyspore.plex.ui.widget.PlexUISlider;
import net.minecraft.client.gui.GuiButton;

public abstract class PlexUIBase {
	public PlexUIModMenuScreen guiScreen;
	
	public abstract String uiGetTitle();
	
	public abstract void initGui(PlexUIModMenuScreen ui);
	
	public abstract void uiSliderInteracted(PlexUISlider slider);
	
	public abstract void uiButtonClicked(GuiButton button);
	
	public int pageForegroundColour() {
		return 0xffffffff;
	}

	public int pageBackgroundColour() {
		return 0x00ffffff;
	}

	public PlexUtilColour.PaletteState pageForegroundState() {
		return PlexUtilColour.PaletteState.FIXED;
	}


	public PlexUtilColour.PaletteState pageBackgroundState() {
		return PlexUtilColour.PaletteState.FIXED;
	}

	public int pageBackgroundTransparency() {
		return 35;
	}

	public boolean disableDoneButton() {
		return false;
	}

	public boolean disablePlexUi() {
		return false;
	}

	public boolean disableSidebar() {
		return false;
	}

	public void keyTyped(char character, int keyCode) {}

	public boolean escapeTyped() {
		return true;
	}
	
	public void mouseClicked(int x, int y, int button) {}
	
	public void mouseReleased(int mouseX, int mouseY, int state) {}
	
	public void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
	
	public void handleMouseInput(int x, int y) {}
	
	public void updateScreen() {}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {}
	
	public void uiOpened() {}
	
	public void uiClosed() {}
}
