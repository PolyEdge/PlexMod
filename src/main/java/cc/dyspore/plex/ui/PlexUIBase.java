package cc.dyspore.plex.ui;

import cc.dyspore.plex.core.util.PlexUtilColour;
import cc.dyspore.plex.ui.widget.PlexUISlider;
import net.minecraft.client.gui.GuiButton;

public abstract class PlexUIBase {
	public PlexUIModMenu guiScreen;

	public abstract void initScreen(PlexUIModMenu ui, PlexUIModMenu.GuiState state);

	public abstract void update(PlexUIModMenu.GuiState state);

	public void onScreenOpened() {}

	public void onScreenClosed() {}

	public void onButtonInteract(GuiButton button) {}

	public void onSliderInteract(PlexUISlider slider) {}

	public void onKeyPressed(char character, int keyCode) {}

	public boolean onEscapePressed() {
		return true;
	}
	
	public void onMousePressed(int x, int y, int button) {}
	
	public void onMouseReleased(int mouseX, int mouseY, int state) {}
	
	public void onMouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
	
	public void onMouseMoved(int x, int y) {}
	
	public void updateScreen() {}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {}
}
