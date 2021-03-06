package cc.dyspore.plex.mods.friendslist;

import cc.dyspore.plex.ui.PlexUIModMenu;
import net.minecraft.client.gui.GuiButton;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.ui.PlexUIBase;
import cc.dyspore.plex.ui.widget.PlexUISlider;

public class PlexFriendsListUI extends PlexUIBase {
	@Override
	public String getTitle() {
		return "Friends List Enhancements";
	}

	@Override
	public void initScreen(PlexUIModMenu ui) {
		PlexFriendsListMod instance = PlexCore.modInstance(PlexFriendsListMod.class);
		int top = ui.startingYPos(72);
		int paneSize = ui.centeredPaneSize(1, 20, 160);
		int pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("Incoming", !instance.hideIncomingRequestsEnabled)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, buttonDisplayString("Outgoing", !instance.hideOutgoingRequestsEnabled)));
		ui.addElement(new GuiButton(7, pane1Pos + 5, top + 46, paneSize - 10, 20, buttonDisplayString("Offline", !instance.hideOfflineEnabled)));
	}
	
	public String buttonDisplayString(String prefix, Boolean shown) {
		return prefix + ": " + (shown ? "Shown" : "Hidden");
	}

	@Override
	public void onSliderInteract(PlexUISlider slider) {
	}
	
	@Override
	public int pageForegroundColour() {
		return 0xff8a16ff;
	}

	@Override
	public void onButtonInteract(GuiButton button) {
		PlexFriendsListMod instance = PlexCore.modInstance(PlexFriendsListMod.class);
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
