package pw.ipex.plex.mods.chatmod;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIModMenuScreen;
import pw.ipex.plex.ui.widget.PlexUISlider;

public class PlexChatStreamUI extends PlexUIBase {
	@Override
	public String uiGetTitle() {
		return "Chat Options";
	}

	@Override
	public void uiAddButtons(PlexUIModMenuScreen ui) {
		Integer top = ui.startingYPos(94);
		Integer paneSize = ui.centeredPaneSize(2, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(-1, 2, 20, 160);
		Integer pane2Pos = ui.centeredPanePos(0, 2, 20, 160);

		PlexChatStreamMod modInstance = PlexCore.modInstance(PlexChatStreamMod.class);
		
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, shownHidden("Player Chat", !modInstance.hidePlayerChat)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, shownHidden("Party Chat", !modInstance.hidePartyChat)));
		ui.addElement(new GuiButton(7, pane1Pos + 5, top + 46, paneSize - 10, 20, shownHidden("Team Chat", !modInstance.hideTeamChat)));
		ui.addElement(new GuiButton(8, pane1Pos + 5, top + 69, paneSize - 10, 20, shownHidden("Community Chat", !modInstance.hideComChat)));

		ui.addElement(new GuiButton(9, pane2Pos + 5, top + 0, paneSize - 10, 20, shownHidden("Community Invites", !modInstance.hideCommunityInvites)));
		ui.addElement(new GuiButton(10, pane2Pos + 5, top + 23, paneSize - 10, 20, enabledDisabled("Adblocking", modInstance.adblocking)));
		ui.addElement(new PlexUISlider(this, 11, pane2Pos + 5, top + 46, paneSize - 10, 20, (float)modInstance.MAX_LOBBY_FILTRATION / modInstance.MAX_LOBBY_FILTRATION, filtrationLevelDisplayString()));
		ui.addElement(new GuiButton(14, pane2Pos + 5, top + 69, paneSize - 10, 20, shownHidden("Gadget Disable", !modInstance.hideGadgetDisable)));

		//ui.addElement(new PlexUISlider(this, 12, pane2Pos + 5, top + 69, paneSize - 10, 20, Float.valueOf(PlexCore.getSharedValue("hideStream_barReductionIndex").integerValue) / PlexChatStreamMod.MAX_BAR_REDUCTION, barReductionDisplayString()));

	}

	public String shownHidden(String prefix, Boolean shown) {
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
		PlexChatStreamMod modInstance = PlexCore.modInstance(PlexChatStreamMod.class);
		Integer level = PlexCore.getSharedValue("hideStream_barReductionIndex").integerValue;
		java.util.Map.Entry<Integer, Integer> barReductionValues = modInstance.BAR_REDUCTION.get(level);
		return "Bar Reduction: " + (level.equals(0) ? "Disabled" : ((float) barReductionValues.getValue() / (float) barReductionValues.getKey() * 100) + "%");
	}
	
	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
		PlexChatStreamMod modInstance = PlexCore.modInstance(PlexChatStreamMod.class);
		if (slider.id == 11) {
			modInstance.lobbyFiltrationLevel = Math.round(slider.sliderValue * modInstance.MAX_LOBBY_FILTRATION);
			slider.displayString = filtrationLevelDisplayString();
		}
		if (slider.id == 12) {
			modInstance.barReductionIndex = Math.round(slider.sliderValue * modInstance.MAX_BAR_REDUCTION);
			slider.displayString = barReductionDisplayString();
		}
	}

	@Override
	public Integer pageForegroundColour() {
		return 0xff0cb2ff;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		PlexChatStreamMod modInstance = PlexCore.modInstance(PlexChatStreamMod.class);
		if (button.id == 5) {
			modInstance.hidePlayerChat = !modInstance.hidePlayerChat;
			button.displayString = shownHidden("Player Chat", !modInstance.hidePlayerChat);
		}
		if (button.id == 6) {
			modInstance.hidePartyChat = !modInstance.hidePartyChat;
			button.displayString = shownHidden("Party Chat", !modInstance.hidePartyChat);
		}
		if (button.id == 7) {
			modInstance.hideTeamChat = !modInstance.hideTeamChat;
			button.displayString = shownHidden("Team Chat", !modInstance.hideTeamChat);
		}
		if (button.id == 8) {
			modInstance.hideComChat = !modInstance.hideComChat;
			button.displayString = shownHidden("Community Chat", !modInstance.hideComChat);
		}
		if (button.id == 9) {
			modInstance.hideCommunityInvites = !modInstance.hideCommunityInvites;
			button.displayString = shownHidden("Community Invites", !modInstance.hideCommunityInvites);
		}
		if (button.id == 10) {
			modInstance.adblocking = !modInstance.adblocking;
			button.displayString = enabledDisabled("Adblocking", modInstance.adblocking);
		}
		if (button.id == 14) {
			modInstance.hideGadgetDisable = !modInstance.hideGadgetDisable;
			button.displayString = shownHidden("Gadget Disable", !modInstance.hideGadgetDisable);
		}
	}
}
