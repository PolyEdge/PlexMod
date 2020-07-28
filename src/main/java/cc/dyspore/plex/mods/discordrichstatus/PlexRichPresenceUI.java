package cc.dyspore.plex.mods.discordrichstatus;

import net.minecraft.client.gui.GuiButton;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.ui.PlexUIBase;
import cc.dyspore.plex.ui.PlexUIModMenu;
import cc.dyspore.plex.ui.widget.PlexUISlider;

public class PlexRichPresenceUI extends PlexUIBase {

	@Override
	public String getTitle() {
		return "Discord Integration";
	}

	@Override
	public void initScreen(PlexUIModMenu ui) {
		int top = ui.startingYPos(135);
		int paneSize = ui.centeredPaneSize(1, 20, 160);
		int pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		PlexNewRichPresenceMod instance = PlexCore.modInstance(PlexNewRichPresenceMod.class);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, enabledDisabled("Rich Presence", instance.modEnabled)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, shownHidden("Current Server", instance.displayLobbyName)));
		ui.addElement(new GuiButton(7, pane1Pos + 5, top + 46, paneSize - 10, 20, shownHidden("IGN", instance.displayIGN)));
		ui.addElement(new GuiButton(8, pane1Pos + 5, top + 69, paneSize - 10, 20, timerMode()));
		ui.addElement(new GuiButton(9, pane1Pos + 5, top + 92, paneSize - 10, 20, enabledDisabled("AFK Status", instance.showAfk)));
		ui.addElement(new GuiButton(10, pane1Pos + 5, top + 115, paneSize - 10, 20, shownHidden("Server IP", instance.showIP)));
	}

	public String shownHidden(String prefix, Boolean shown) {
		return prefix + ": " + (shown ? "Shown" : "Hidden");
	}	
	
	public String enabledDisabled(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}
	
	public String timerMode() {
		String prefix = "Timer: ";
		Integer mode = PlexCore.modInstance(PlexNewRichPresenceMod.class).timerMode;
		if (mode.equals(0)) {
			return prefix + "Disabled";
		}
		if (mode.equals(1)) {
			return prefix + "Current Game";
		}
		if (mode.equals(2)) {
			return prefix + "Time Online";
		}
		return "null";
	}
	
	@Override
	public void onSliderInteract(PlexUISlider slider) {
	}

	@Override
	public void onButtonInteract(GuiButton button) {
		PlexNewRichPresenceMod instance = PlexCore.modInstance(PlexNewRichPresenceMod.class);
		if (button.id == 5) {
			instance.modEnabled = !instance.modEnabled;
			button.displayString = enabledDisabled("Rich Presence", instance.modEnabled);
		}
		if (button.id == 6) {
			instance.displayLobbyName = !instance.displayLobbyName;
			button.displayString = shownHidden("Current Server", instance.displayLobbyName);
		}
		if (button.id == 7) {
			instance.displayIGN = !instance.displayIGN;
			button.displayString = shownHidden("IGN", instance.displayIGN);
		}
		if (button.id == 8) {
			instance.timerMode = (instance.timerMode + 1) % PlexNewRichPresenceMod.MAX_TIMER_MODE;
			button.displayString = timerMode();
		}
		if (button.id == 9) {
			instance.showAfk = !instance.showAfk;
			button.displayString = enabledDisabled("AFK Status", instance.showAfk);
		}
		if (button.id == 10) {
			instance.showIP = !instance.showIP;
			button.displayString = shownHidden("Server IP", instance.showIP);
		}
	}

	@Override
	public int pageForegroundColour() {
		return 0xff7289DA;
	}
	
	@Override
	public int pageBackgroundColour() {
		return 0xff7289DA;
	}

}
