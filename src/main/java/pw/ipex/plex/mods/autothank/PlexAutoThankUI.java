package pw.ipex.plex.mods.autothank;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIModMenuScreen;
import pw.ipex.plex.ui.widget.PlexUISlider;

import java.util.Objects;

public class PlexAutoThankUI extends PlexUIBase {
	@Override
	public String uiGetTitle() {
		return "AutoThank";
	}

	@Override
	public void uiAddButtons(PlexUIModMenuScreen ui) {
		Integer top = ui.startingYPos(41);
		Integer paneSize = ui.centeredPaneSize(1, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("AutoThank", PlexCore.modInstance(PlexAutoThankMod.class).modEnabled)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, buttonDisplayString("Compact Messages", PlexCore.modInstance(PlexAutoThankMod.class).compactMessagesEnabled)));

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
	public void uiButtonClicked(GuiButton button) {
		if (button.id == 5) {
			PlexCore.modInstance(PlexAutoThankMod.class).modEnabled = !PlexCore.modInstance(PlexAutoThankMod.class).modEnabled;
			button.displayString = buttonDisplayString("AutoThank", PlexCore.modInstance(PlexAutoThankMod.class).modEnabled);
		}
		if (button.id == 6) {
			PlexCore.modInstance(PlexAutoThankMod.class).compactMessagesEnabled = !PlexCore.modInstance(PlexAutoThankMod.class).compactMessagesEnabled;
			button.displayString = buttonDisplayString("Compact Messages", PlexCore.modInstance(PlexAutoThankMod.class).compactMessagesEnabled);
		}
	}
}
