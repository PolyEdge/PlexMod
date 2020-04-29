package cc.dyspore.plex.mods.replycommand;

import net.minecraft.client.gui.GuiButton;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.ui.PlexUIBase;
import cc.dyspore.plex.ui.PlexUIModMenuScreen;
import cc.dyspore.plex.ui.widget.PlexUISlider;

public class PlexBetterReplyUI extends PlexUIBase {
	@Override
	public String uiGetTitle() {
		return "Better Private Messaging";
	}

	@Override
	public void initGui(PlexUIModMenuScreen ui) {
		PlexBetterReplyMod instance = PlexCore.modInstance(PlexBetterReplyMod.class);
		int top = ui.startingYPos(52);
		int paneSize = ui.centeredPaneSize(1, 20, 160);
		int pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("Better Reply", instance.modEnabled)));
		ui.addElement(new PlexUISlider(this, 6, pane1Pos + 5, top + 23, paneSize - 10, 20, (float)instance.replyTimeoutSeconds / PlexBetterReplyMod.MAX_REPLY_TIMEOUT, timeoutDisplayString()));
	}
	
	public String buttonDisplayString(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}

	public String timeoutDisplayString() {
		PlexBetterReplyMod instance = PlexCore.modInstance(PlexBetterReplyMod.class);
		Integer secondsValue = instance.replyTimeoutSeconds;
		return "Reply Timeout: " + (secondsValue.equals(PlexBetterReplyMod.MAX_REPLY_TIMEOUT) ? "Unlimited" : (secondsValue + "s"));
	}
	
	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
		PlexBetterReplyMod instance = PlexCore.modInstance(PlexBetterReplyMod.class);
		if (slider.id == 6) {
			instance.replyTimeoutSeconds = Math.round(slider.sliderValue * PlexBetterReplyMod.MAX_REPLY_TIMEOUT);
			slider.displayString = timeoutDisplayString();
		}
	}
	
	@Override
	public int pageForegroundColour() {
		return 0xff0cff28;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		PlexBetterReplyMod instance = PlexCore.modInstance(PlexBetterReplyMod.class);
		if (button.id == 5) {
			instance.modEnabled = !instance.modEnabled;
			button.displayString = buttonDisplayString("Better Reply", instance.modEnabled);
		}
	}
}
