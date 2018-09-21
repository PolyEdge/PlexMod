package pw.ipex.plex.mods.richpresence;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIModMenuScreen;
import pw.ipex.plex.ui.PlexUISlider;

public class PlexRichPresenceUI extends PlexUIBase {

	@Override
	public String uiGetTitle() {
		return "Discord Integration";
	}

	@Override
	public void uiAddButtons(PlexUIModMenuScreen ui) {
		Integer top = ui.startingYPos(107);
		Integer paneSize = ui.centeredPaneSize(1, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, enabledDisabled("Rich Presence", PlexCore.getSharedValue("richPresence_enabled").booleanValue)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, shownHidden("Current Server", PlexCore.getSharedValue("richPresence_showLobbies").booleanValue)));
		ui.addElement(new GuiButton(7, pane1Pos + 5, top + 46, paneSize - 10, 20, shownHidden("IGN", PlexCore.getSharedValue("richPresence_showIGN").booleanValue)));
		ui.addElement(new GuiButton(8, pane1Pos + 5, top + 69, paneSize - 10, 20, timerMode()));
		ui.addElement(new GuiButton(9, pane1Pos + 5, top + 92, paneSize - 10, 20, enabledDisabled("AFK Status", PlexCore.getSharedValue("richPresence_showAFK").booleanValue)));

	}

	public String shownHidden(String prefix, Boolean shown) {
		return prefix + ": " + (shown ? "Shown" : "Hidden");
	}	
	
	public String enabledDisabled(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}
	
	public String timerMode() {
		String prefix = "Timer: ";
		Integer mode = PlexCore.getSharedValue("richPresence_timerMode").integerValue;
		if (mode.equals(0)) {
			return prefix + "Disabled";
		}
		if (mode.equals(1)) {
			return prefix + "Current Game";
		}
		if (mode.equals(2)) {
			return prefix + "Server Time";
		}
		return "null";
	}
	
	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		if (button.id == 5) {
			PlexCore.getSharedValue("richPresence_enabled").set(!PlexCore.getSharedValue("richPresence_enabled").booleanValue);
			button.displayString = enabledDisabled("Rich Presence", PlexCore.getSharedValue("richPresence_enabled").booleanValue);
		}
		if (button.id == 6) {
			PlexCore.getSharedValue("richPresence_showLobbies").set(!PlexCore.getSharedValue("richPresence_showLobbies").booleanValue);
			button.displayString = shownHidden("Current Server", PlexCore.getSharedValue("richPresence_showLobbies").booleanValue);
		}
		if (button.id == 7) {
			PlexCore.getSharedValue("richPresence_showIGN").set(!PlexCore.getSharedValue("richPresence_showIGN").booleanValue);
			button.displayString = shownHidden("IGN", PlexCore.getSharedValue("richPresence_showIGN").booleanValue);
		}
		if (button.id == 8) {
			PlexCore.getSharedValue("richPresence_timerMode").set((Integer)((PlexCore.getSharedValue("richPresence_timerMode").integerValue) + 1) % PlexNewRichPresenceMod.MAX_TIMER_MODE);
			button.displayString = timerMode();
		}
		if (button.id == 9) {
			PlexCore.getSharedValue("richPresence_showAFK").set(!PlexCore.getSharedValue("richPresence_showAFK").booleanValue);
			button.displayString = enabledDisabled("AFK Status", PlexCore.getSharedValue("richPresence_showAFK").booleanValue);
		}
	}

	@Override
	public Integer pageForegroundColour() {
		return 0xff7289DA;
	}
	
	@Override
	public Integer pageBackgroundColour() {
		return 0xff7289DA;
	}

}
