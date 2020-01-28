package cc.dyspore.plex.mods.autofriend;

import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.ui.PlexUIBase;
import cc.dyspore.plex.ui.PlexUIModMenuScreen;
import cc.dyspore.plex.ui.widget.PlexUISlider;
import cc.dyspore.plex.ui.widget.PlexUITextField;
import net.minecraft.client.gui.GuiButton;

public class PlexAutoFriendUI extends PlexUIBase {
	
	public PlexUITextField primaryMessageField;
	public PlexUITextField secondaryMessageField;
	
	@Override
	public String uiGetTitle() {
		return "AutoFriend";
	}

	@Override
	public void initGui(PlexUIModMenuScreen ui) {
		PlexAutoFriendMod instance = PlexCore.modInstance(PlexAutoFriendMod.class);
		Integer top = ui.startingYPos(112);
		Integer paneSize = ui.centeredPaneSize(1, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("AutoFriend", instance.modEnabled)));

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
		PlexAutoFriendMod instance = PlexCore.modInstance(PlexAutoFriendMod.class);
		if (button.id == 5) {
			instance.modEnabled = !instance.modEnabled;
			button.displayString = buttonDisplayString("AutoFriend", instance.modEnabled);
		}
	}
}
