package cc.dyspore.plex.mods.friendslist;

import net.minecraft.client.gui.GuiButton;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.ui.PlexUIBase;
import cc.dyspore.plex.ui.PlexUIModMenuScreen;
import cc.dyspore.plex.ui.widget.PlexUISlider;

public class PlexFriendsListEnhancementsUI extends PlexUIBase {
	@Override
	public String uiGetTitle() {
		return "Friends List Enhancements";
	}

	@Override
	public void initGui(PlexUIModMenuScreen ui) {
		PlexFriendsListEnhancementsMod instance = PlexCore.modInstance(PlexFriendsListEnhancementsMod.class);
		Integer top = ui.startingYPos(72);
		Integer paneSize = ui.centeredPaneSize(1, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("Incoming", !instance.hideIncomingRequestsEnabled)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, buttonDisplayString("Outgoing", !instance.hideOutgoingRequestsEnabled)));
		ui.addElement(new GuiButton(7, pane1Pos + 5, top + 46, paneSize - 10, 20, buttonDisplayString("Offline", !instance.hideOfflineEnabled)));
	}
	
	public String buttonDisplayString(String prefix, Boolean shown) {
		return prefix + ": " + (shown ? "Shown" : "Hidden");
	}

	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
	}
	
	@Override
	public Integer pageForegroundColour() {
		return 0xff8a16ff;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		PlexFriendsListEnhancementsMod instance = PlexCore.modInstance(PlexFriendsListEnhancementsMod.class);
		if (button.id == 5) {
			instance.hideIncomingRequestsEnabled = !instance.hideIncomingRequestsEnabled;
			button.displayString = buttonDisplayString("Incoming", !instance.hideIncomingRequestsEnabled);
		}
		if (button.id == 6) {
			instance.hideOutgoingRequestsEnabled = !instance.hideOutgoingRequestsEnabled;
			button.displayString = buttonDisplayString("Outgoing", !instance.hideOutgoingRequestsEnabled);
		}
		if (button.id == 7) {
			instance.hideOfflineEnabled = !instance.hideOfflineEnabled;
			button.displayString = buttonDisplayString("Offline", !instance.hideOfflineEnabled);
		}
	}

}
