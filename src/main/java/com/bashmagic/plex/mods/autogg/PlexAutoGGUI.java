package com.bashmagic.plex.mods.autogg;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIMenuScreen;
import pw.ipex.plex.ui.PlexUISlider;

public class PlexAutoGGUI extends PlexUIBase {

	public static final int BUTTON_TOGGLE_AUTOGG_ID = 5;

	private PlexAutoGGMod mod;

	@Override
	public void uiOpened() {
		mod = PlexAutoGGMod.getInstance();
	}

	@Override
	public String uiGetTitle() {
		return PlexAutoGGMod.UI_TITLE;
	}

	@Override
	public void uiAddButtons(PlexUIMenuScreen ui) {
		GuiButton toggleAutoGG = new GuiButton(BUTTON_TOGGLE_AUTOGG_ID, 0, 0,
				"Auto GG: " + (mod.isAutoGGEnabled() ? "Enabled" : "Disabled"));
		toggleAutoGG.xPosition = ui.zoneCenterX() - toggleAutoGG.width / 2;
		toggleAutoGG.yPosition = ui.zoneCenterY() - toggleAutoGG.height / 2;
		ui.addElement(toggleAutoGG);
	}

	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
	}

	@Override
	public String uiGetSliderDisplayString(PlexUISlider slider) {
		return null;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		if (button.id == BUTTON_TOGGLE_AUTOGG_ID) {
			boolean prev = mod.isAutoGGEnabled();
			mod.setAutoGGEnabled(!prev);
			button.displayString = "Auto GG: " + (!prev ? "Enabled" : "Disabled");
		}
	}

}
