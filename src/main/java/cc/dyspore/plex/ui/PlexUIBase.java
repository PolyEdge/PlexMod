package cc.dyspore.plex.ui;

import cc.dyspore.plex.core.util.PlexUtilColour;
import cc.dyspore.plex.ui.widget.PlexUISlider;
import net.minecraft.client.gui.GuiButton;

public abstract class PlexUIBase {
	public PlexUIModMenuScreen parentUI;
	
	public abstract String uiGetTitle();
	
	public abstract void initGui(PlexUIModMenuScreen ui);
	
	public abstract void uiSliderInteracted(PlexUISlider slider);
	
	public abstract void uiButtonClicked(GuiButton button);
	
	public String customGlobalTitle() {
		return "Plex Mod";
	}
	
	public Integer titleFadeColour1() {
		return 0x00e69500;
	}
	
	public Integer titleFadeColour2() {
		return 0x00e69500;
	}
	
	public Integer pageBackgroundColour() {
		return PlexUtilColour.fromRGB(255, 255, 255, 0);
	}
	
	public Integer pageForegroundColour() {
		return -1;
	}
	
	public Integer pageBackgroundTransparency() {
		return 35;
	}
	
	public Boolean disableDoneButton() {
		return false;
	}
	
	public Boolean disableUiFade() {
		return false;
	}

	public Boolean disablePlexUi() {
		return false;
	}
	
	public Boolean disableSidebar() {
		return false;
	}
	
	public void keyTyped(char par1, int par2) {}

	public boolean escapeTyped() {
		return true;
	}
	
	public void mouseClicked(int x, int y, int btn) {}
	
	public void mouseReleased(int mouseX, int mouseY, int state) {}
	
	public void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
	
	public void handleMouseInput(int x, int y) {}
	
	public void updateScreen() {}
	
	public void drawScreen(int par1, int par2, float par3) {}
	
	public void uiOpened() {}
	
	public void uiClosed() {}
}
