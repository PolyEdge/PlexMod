package pw.ipex.plex.mods.hidestream;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIMenuScreen;
import pw.ipex.plex.ui.PlexUISlider;

public class PlexHideStreamUI extends PlexUIBase {
	@Override
	public String uiGetTitle() {
		return "Chat Options";
	}

	@Override
	public void uiAddButtons(PlexUIMenuScreen ui) {
		Integer top = ui.startingYPos(94);
		Integer paneSize = ui.centeredPaneSize(2, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(-1, 2, 20, 160);
		Integer pane2Pos = ui.centeredPanePos(0, 2, 20, 160);
		
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("Player Chat", !PlexCore.getSharedValue("hideStream_hidePlayerChat").booleanValue)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, buttonDisplayString("Party Chat", !PlexCore.getSharedValue("hideStream_hidePartyChat").booleanValue)));
		ui.addElement(new GuiButton(7, pane1Pos + 5, top + 46, paneSize - 10, 20, buttonDisplayString("Team Chat", !PlexCore.getSharedValue("hideStream_hideTeamChat").booleanValue)));
		ui.addElement(new PlexUISlider(this, 8, pane1Pos + 5, top + 69, paneSize - 10, 20, Float.valueOf(PlexCore.getSharedValue("hideStream_lobbyFiltrationLevel").integerValue) / PlexHideStreamMod.MAX_LOBBY_FILTRATION, filtrationLevelDisplayString()));
		
		ui.addElement(new GuiButton(9, pane2Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("Community Invites", !PlexCore.getSharedValue("hideStream_hideInvites").booleanValue)));
		ui.addElement(new GuiButton(10, pane2Pos + 5, top + 23, paneSize - 10, 20, enabledDisabled("Adblocking", PlexCore.getSharedValue("hideStream_adBlock").booleanValue)));
		ui.addElement(new GuiButton(11, pane2Pos + 5, top + 46, paneSize - 10, 20, enabledDisabled("MPS Support", PlexCore.getSharedValue("hideStream_mpsSupportEnabled").booleanValue)));
		//ui.addElement(new PlexUISlider(this, 12, pane2Pos + 5, top + 69, paneSize - 10, 20, Float.valueOf(PlexCore.getSharedValue("hideStream_barReductionIndex").integerValue) / PlexHideStreamMod.MAX_BAR_REDUCTION, barReductionDisplayString()));

	}

	public String buttonDisplayString(String prefix, Boolean shown) {
		return prefix + ": " + (shown ? "Shown" : "Hidden");
	}
	
	public String enabledDisabled(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}
	
	public String filtrationLevelDisplayString() {
		Integer level = PlexCore.getSharedValue("hideStream_lobbyFiltrationLevel").integerValue;
		return "Lobby Chat Filter: " + (level.equals(0) ? "Disabled" : (level));
	}
	
	public String barReductionDisplayString() {
		Integer level = PlexCore.getSharedValue("hideStream_barReductionIndex").integerValue;
		java.util.Map.Entry<Integer, Integer> barReductionValues = PlexHideStreamMod.BAR_REDUCTION.get(level);
		return "Bar Reduction: " + (level.equals(0) ? "Disabled" : ((float) barReductionValues.getValue() / (float) barReductionValues.getKey() * 100) + "%");
	}
	
	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
		if (slider.id == 8) {
			PlexCore.getSharedValue("hideStream_lobbyFiltrationLevel").set(Math.round(slider.sliderValue * PlexHideStreamMod.MAX_LOBBY_FILTRATION));
			slider.displayString = filtrationLevelDisplayString();
		}
		if (slider.id == 12) {
			PlexCore.getSharedValue("hideStream_barReductionIndex").set(Math.round(slider.sliderValue * PlexHideStreamMod.MAX_BAR_REDUCTION));
			slider.displayString = barReductionDisplayString();
		}
	}

	@Override
	public String uiGetSliderDisplayString(PlexUISlider slider) {
		return null;
	}
	
	@Override
	public Integer pageForegroundColour() {
		return 0xff0cb2ff;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		if (button.id == 5) {
			PlexCore.getSharedValue("hideStream_hidePlayerChat").set(!PlexCore.getSharedValue("hideStream_hidePlayerChat").booleanValue);
			button.displayString = buttonDisplayString("Player Chat", !PlexCore.getSharedValue("hideStream_hidePlayerChat").booleanValue);
		}
		if (button.id == 6) {
			PlexCore.getSharedValue("hideStream_hidePartyChat").set(!PlexCore.getSharedValue("hideStream_hidePartyChat").booleanValue);
			button.displayString = buttonDisplayString("Party Chat", !PlexCore.getSharedValue("hideStream_hidePartyChat").booleanValue);
		}
		if (button.id == 7) {
			PlexCore.getSharedValue("hideStream_hideTeamChat").set(!PlexCore.getSharedValue("hideStream_hideTeamChat").booleanValue);
			button.displayString = buttonDisplayString("Team Chat", !PlexCore.getSharedValue("hideStream_hideTeamChat").booleanValue);
		}
		if (button.id == 9) {
			PlexCore.getSharedValue("hideStream_hideInvites").set(!PlexCore.getSharedValue("hideStream_hideInvites").booleanValue);
			button.displayString = buttonDisplayString("Community Invites", !PlexCore.getSharedValue("hideStream_hideInvites").booleanValue);
		}
		if (button.id == 10) {
			PlexCore.getSharedValue("hideStream_adBlock").set(!PlexCore.getSharedValue("hideStream_adBlock").booleanValue);
			button.displayString = enabledDisabled("[B] Adblocking", PlexCore.getSharedValue("hideStream_adBlock").booleanValue);
		}
		if (button.id == 11) {
			PlexCore.getSharedValue("hideStream_mpsSupportEnabled").set(!PlexCore.getSharedValue("hideStream_mpsSupportEnabled").booleanValue);
			button.displayString = enabledDisabled("MPS Support", PlexCore.getSharedValue("hideStream_mpsSupportEnabled").booleanValue);
		}
	}
}
