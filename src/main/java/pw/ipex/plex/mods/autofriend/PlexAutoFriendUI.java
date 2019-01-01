package pw.ipex.plex.mods.autofriend;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIModMenuScreen;
import pw.ipex.plex.ui.widget.PlexUISlider;
import pw.ipex.plex.ui.widget.PlexUITextField;

public class PlexAutoFriendUI extends PlexUIBase {
	
	public PlexUITextField primaryMessageField;
	public PlexUITextField secondaryMessageField;
	
	@Override
	public String uiGetTitle() {
		return "AutoFriend";
	}

	@Override
	public void uiAddButtons(PlexUIModMenuScreen ui) {
		Integer top = ui.startingYPos(112);
		Integer paneSize = ui.centeredPaneSize(1, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("AutoFriend", PlexCore.getSharedValue("autoFriend_enabled").booleanValue)));

	}
	
	@Override
	public void mouseClicked(int par1, int par2, int btn) {
	}
	
	@Override
	public void updateScreen() {
	}
	
	@Override
	public void keyTyped(char par1, int par2) {
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
	}
	
	public String buttonDisplayString(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}
	
	public String beforeAfter(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "After" : "Before");
	}

	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
	}
	
	@Override
	public Integer pageForegroundColour() {
		return 0xffe0c908;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		if (button.id == 5) {
			PlexCore.getSharedValue("autoFriend_enabled").set(!PlexCore.getSharedValue("autoFriend_enabled").booleanValue);
			button.displayString = buttonDisplayString("AutoFriend", PlexCore.getSharedValue("autoFriend_enabled").booleanValue);
		}
	}
}
