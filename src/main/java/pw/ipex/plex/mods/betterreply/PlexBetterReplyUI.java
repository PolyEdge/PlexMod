package pw.ipex.plex.mods.betterreply;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIMenuScreen;
import pw.ipex.plex.ui.PlexUISlider;

public class PlexBetterReplyUI extends PlexUIBase {
	@Override
	public String uiGetTitle() {
		return "Better Private Messaging";
	}

	@Override
	public void uiAddButtons(PlexUIMenuScreen ui) {
		Integer top = ui.startingYPos(52);
		Integer paneSize = ui.centeredPaneSize(1, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("Better Reply", PlexCore.getSharedValue("betterReply_enabled").booleanValue)));
		ui.addElement(new PlexUISlider(this, 6, pane1Pos + 5, top + 23, paneSize - 10, 20, Float.valueOf(PlexCore.getSharedValue("betterReply_replyTimeout").integerValue) / PlexBetterReplyMod.MAX_REPLY_TIMEOUT, timeoutDisplayString()));
	}
	
	public String buttonDisplayString(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}

	public String timeoutDisplayString() {
		Integer secondsValue = PlexCore.getSharedValue("betterReply_replyTimeout").integerValue;
		return "Reply Timeout: " + (secondsValue.equals(PlexBetterReplyMod.MAX_REPLY_TIMEOUT) ? "Unlimited" : (secondsValue + "s"));
	}
	
	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
		if (slider.id == 6) {
			PlexCore.getSharedValue("betterReply_replyTimeout").set(Math.round(slider.sliderValue * PlexBetterReplyMod.MAX_REPLY_TIMEOUT));
			slider.displayString = timeoutDisplayString();
		}
	}
	
	@Override
	public Integer pageForegroundColour() {
		return 0xff0cff28;
	}

	@Override
	public String uiGetSliderDisplayString(PlexUISlider slider) {
		return null;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		if (button.id == 5) {
			PlexCore.getSharedValue("betterReply_enabled").set(!PlexCore.getSharedValue("betterReply_enabled").booleanValue);
			button.displayString = buttonDisplayString("Better Reply", PlexCore.getSharedValue("betterReply_enabled").booleanValue);
			PlexCore.getCommandListener("r").setDisabled(!PlexCore.getSharedValue("betterReply_enabled").booleanValue);
		}
	}
}
