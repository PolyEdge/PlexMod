package pw.ipex.plex.mods.friendslistenhancements;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIMenuScreen;
import pw.ipex.plex.ui.PlexUISlider;

public class PlexFriendsListEnhancementsUI extends PlexUIBase {
	@Override
	public String uiGetTitle() {
		return "Friends List Enhancements";
	}

	@Override
	public void uiAddButtons(PlexUIMenuScreen ui) {
		Integer top = ui.startingYPos(72);
		Integer paneSize = ui.centeredPaneSize(1, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("Incoming", !PlexCore.getSharedValue("friendsListEnhancements_hideIncoming").booleanValue)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, buttonDisplayString("Outgoing", !PlexCore.getSharedValue("friendsListEnhancements_hideOutgoing").booleanValue)));
		ui.addElement(new GuiButton(7, pane1Pos + 5, top + 46, paneSize - 10, 20, buttonDisplayString("Offline", !PlexCore.getSharedValue("friendsListEnhancements_hideOffline").booleanValue)));
	}
	
	public String buttonDisplayString(String prefix, Boolean shown) {
		return prefix + ": " + (shown ? "Shown" : "Hidden");
	}

	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
	}

	@Override
	public String uiGetSliderDisplayString(PlexUISlider slider) {
		return null;
	}
	
	@Override
	public Integer pageForegroundColour() {
		return 0xff8a16ff;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		if (button.id == 5) {
			PlexCore.getSharedValue("friendsListEnhancements_hideIncoming").set(!PlexCore.getSharedValue("friendsListEnhancements_hideIncoming").booleanValue);
			button.displayString = buttonDisplayString("Incoming", !PlexCore.getSharedValue("friendsListEnhancements_hideIncoming").booleanValue);
		}
		if (button.id == 6) {
			PlexCore.getSharedValue("friendsListEnhancements_hideOutgoing").set(!PlexCore.getSharedValue("friendsListEnhancements_hideOutgoing").booleanValue);
			button.displayString = buttonDisplayString("Outgoing", !PlexCore.getSharedValue("friendsListEnhancements_hideOutgoing").booleanValue);
		}
		if (button.id == 7) {
			PlexCore.getSharedValue("friendsListEnhancements_hideOffline").set(!PlexCore.getSharedValue("friendsListEnhancements_hideOffline").booleanValue);
			button.displayString = buttonDisplayString("Offline", !PlexCore.getSharedValue("friendsListEnhancements_hideOffline").booleanValue);
		}
	}

}
