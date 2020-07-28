package cc.dyspore.plex.mods.plexmod;

import cc.dyspore.plex.core.util.PlexUtilColour;
import net.minecraft.client.gui.GuiButton;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.ui.PlexUIBase;
import cc.dyspore.plex.ui.PlexUIModMenu;
import cc.dyspore.plex.ui.widget.PlexUISlider;

@SuppressWarnings("ConstantConditions")
public class PlexModUI extends PlexUIBase {

	@Override
	public String getTitle() {
		return "Plex";
	}

	@Override
	public void initScreen(PlexUIModMenu ui) {
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int top = guiScreen.getCenteredStartY(30);
		int paneSize = guiScreen.centeredPaneSize(1, 20, 160);
		int pane1Pos = guiScreen.centeredPanePos(0, 1, 20, 160);
		guiScreen.drawCenteredString(guiScreen.getFontRenderer(), "Plex Mod", pane1Pos + paneSize / 2, top + 0, 0xffffff);
		guiScreen.drawCenteredString(guiScreen.getFontRenderer(), "v" + Plex.VERSION + (Plex.PATCHID == null ? "" : "-" + Plex.PATCHID), pane1Pos + paneSize / 2, top + 10, 0xffffff);
		if (Plex.NOTICE != null) {
			guiScreen.drawCenteredString(guiScreen.getFontRenderer(), "[" + Plex.NOTICE + "]", pane1Pos + paneSize / 2, top + 30, 0xffe500);
			
		}
		
		guiScreen.drawCenteredString(guiScreen.getFontRenderer(), "Command list - /plex help", pane1Pos + paneSize / 2, guiScreen.zoneEndY() - 12, 0xffffff);
	}

	@Override
	public void onSliderInteract(PlexUISlider slider) {
	}

	@Override
	public void onButtonInteract(GuiButton button) {

	}

	//@Override
	//public int pageForegroundColour() {
	//	return -1;
	//}
//
	//@Override
	//public int pageBackgroundColour() {
	//	return -1; // -1 = chroma
	//}

	@Override
	public PlexUtilColour.PaletteState pageForegroundState() {
		return PlexUtilColour.PaletteState.CHROMA;
	}

	@Override
	public PlexUtilColour.PaletteState pageBackgroundState() {
		return PlexUtilColour.PaletteState.CHROMA;
	}

	@Override
	public int pageBackgroundTransparency() {
		return 35;
	}


}
