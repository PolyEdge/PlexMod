package pw.ipex.plex.mods.plexmod;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIModMenuScreen;
import pw.ipex.plex.ui.PlexUISlider;

public class PlexPlexUI extends PlexUIBase {

	@Override
	public String uiGetTitle() {
		// TODO Auto-generated method stub
		return "Plex";
	}

	@Override
	public void uiAddButtons(PlexUIModMenuScreen ui) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		Integer top = parentUI.getCenteredStartY(30);
		Integer paneSize = parentUI.centeredPaneSize(1, 20, 160);
		Integer pane1Pos = parentUI.centeredPanePos(0, 1, 20, 160);
		parentUI.drawCenteredString(parentUI.getFontRenderer(), "Plex Mod", pane1Pos + paneSize / 2, top + 0, 0xffffff); 
		parentUI.drawCenteredString(parentUI.getFontRenderer(), "v" + Plex.VERSION + (Plex.PATCHID == null ? "" : "-" + Plex.PATCHID), pane1Pos + paneSize / 2, top + 10, 0xffffff);
		parentUI.drawCenteredString(parentUI.getFontRenderer(), "[unreleased beta version]", pane1Pos + paneSize / 2, top + 30, 0xffe500);
		
		parentUI.drawCenteredString(parentUI.getFontRenderer(), "Command help - /plex help", pane1Pos + paneSize / 2, parentUI.zoneEndY() - 12, 0xffffff);
	}

	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		// TODO Auto-generated method stub

	}

	public Integer pageBackgroundColour() {
		return -1; // -1 = chroma
	}
	
	public Integer pageForegroundColour() {
		return -1;
	}
	
	public Integer pageBackgroundTransparency() {
		return 35;
	}
}
