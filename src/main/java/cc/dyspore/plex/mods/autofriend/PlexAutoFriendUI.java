package cc.dyspore.plex.mods.autofriend;

import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.ui.PlexUIBase;
import cc.dyspore.plex.ui.PlexUIModMenu;
import cc.dyspore.plex.ui.widget.PlexUISlider;
import cc.dyspore.plex.ui.widget.PlexUITextField;
import net.minecraft.client.gui.GuiButton;

public class PlexAutoFriendUI extends PlexUIBase {
	
	public PlexUITextField primaryMessageField;
	public PlexUITextField secondaryMessageField;
	
	@Override
	public String getTitle() {
		return "AutoFriend";
	}

	@Override
	public void initScreen(PlexUIModMenu ui) {
		PlexAutoFriendMod instance = PlexCore.modInstance(PlexAutoFriendMod.class);
		int top = ui.startingYPos(112);
		int paneSize = ui.centeredPaneSize(1, 20, 160);
		int pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("AutoFriend", instance.modEnabled)));

	}
	
	@Override
	public void onMousePressed(int par1, int par2, int button) {
	}
	
	@Override
	public void updateScreen() {
	}
	
	@Override
	public void onKeyPressed(char character, int keyCode) {
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
	}
	
	public String buttonDisplayString(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}
	
	public String beforeAfter(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "After" : "Before");
	}

	@Override
	public void onSliderInteract(PlexUISlider slider) {
	}
	
	@Override
	public int pageForegroundColour() {
		return 0xffe0c908;
	}

	@Override
	public void onButtonInteract(GuiButton button) {
		PlexAutoFriendMod instance = PlexCore.modInstance(PlexAutoFriendMod.class);
		if (button.id == 5) {
			instance.modEnabled = !instance.modEnabled;
			button.displayString = buttonDisplayString("AutoFriend", instance.modEnabled);
		}
	}
}
