package pw.ipex.plex.mods.autothank;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIMenuScreen;
import pw.ipex.plex.ui.PlexUISlider;

public class PlexAutoThankUI extends PlexUIBase {
	@Override
	public String uiGetTitle() {
		return "Auto Thank Amplifiers";
	}

	@Override
	public void uiAddButtons(PlexUIMenuScreen ui) {
		Integer top = ui.startingYPos(41);
		Integer paneSize = ui.centeredPaneSize(1, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("Auto Thank", PlexCore.getSharedValue("autoThank_enabled").booleanValue)));
	}
	
	public String buttonDisplayString(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}

	
	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
	}
	
	@Override
	public Integer pageForegroundColour() {
		return 0xff00ffc3;
	}

	@Override
	public String uiGetSliderDisplayString(PlexUISlider slider) {
		return null;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		if (button.id == 5) {
			PlexCore.getSharedValue("autoThank_enabled").set(!PlexCore.getSharedValue("autoThank_enabled").booleanValue);
			button.displayString = buttonDisplayString("Auto Thank", PlexCore.getSharedValue("autoThank_enabled").booleanValue);
		}
	}
}
