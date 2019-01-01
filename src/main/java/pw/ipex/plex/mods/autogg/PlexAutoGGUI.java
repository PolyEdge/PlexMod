package pw.ipex.plex.mods.autogg;

import net.minecraft.client.gui.GuiButton;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIModMenuScreen;
import pw.ipex.plex.ui.widget.PlexUISlider;
import pw.ipex.plex.ui.widget.PlexUITextField;

public class PlexAutoGGUI extends PlexUIBase {
	
	public PlexUITextField primaryMessageField;
	public PlexUITextField secondaryMessageField;
	
	@Override
	public String uiGetTitle() {
		return "Auto GG";
	}

	@Override
	public void uiAddButtons(PlexUIModMenuScreen ui) {
		Integer top = ui.startingYPos(112);
		Integer paneSize = ui.centeredPaneSize(1, 20, 160);
		Integer pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("AutoGG", PlexCore.getSharedValue("autoGG_enabled").booleanValue)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, beforeAfter("Chat Silence", PlexCore.getSharedValue("autoGG_waitUntilSilenceOver").booleanValue)));
		ui.addElement(new PlexUISlider(this, 7, pane1Pos + 5, top + 46, paneSize - 10, 20, (float) (PlexCore.getSharedValue("autoGG_delay").doubleValue / (PlexAutoGGMod.MAX_DELAY - PlexAutoGGMod.MIN_DELAY)), delayDisplayString()));	
		this.primaryMessageField = new PlexUITextField(8, Plex.minecraft.fontRendererObj, pane1Pos + 5, top + 69, paneSize - 10, 20);
		this.primaryMessageField.text.setMaxStringLength(100);
		this.primaryMessageField.text.setText(PlexCore.getSharedValue("autoGG_primaryMessage").stringValue);
		this.secondaryMessageField = new PlexUITextField(8, Plex.minecraft.fontRendererObj, pane1Pos + 5, top + 92, paneSize - 10, 20);
		this.secondaryMessageField.text.setMaxStringLength(100);
		this.secondaryMessageField.text.setText(PlexCore.getSharedValue("autoGG_secondaryMessage").stringValue);
	}
	
	@Override
	public void mouseClicked(int par1, int par2, int btn) {
		this.primaryMessageField.mouseClicked(par1, par2, btn);
		this.secondaryMessageField.mouseClicked(par1, par2, btn);
	}
	
	@Override
	public void updateScreen() {
		this.primaryMessageField.updateScreen();
		this.secondaryMessageField.updateScreen();
	}
	
	@Override
	public void keyTyped(char par1, int par2) {
		this.primaryMessageField.keyTyped(par1, par2);
		this.secondaryMessageField.keyTyped(par1, par2);
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.primaryMessageField.drawScreen(par1, par2, par3);
		this.secondaryMessageField.drawScreen(par1, par2, par3);
		
		PlexCore.getSharedValue("autoGG_primaryMessage").set(this.primaryMessageField.text.getText());
		PlexCore.getSharedValue("autoGG_secondaryMessage").set(this.secondaryMessageField.text.getText());
	}
	
	public String buttonDisplayString(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}
	
	public String beforeAfter(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "After" : "Before");
	}
	
	public String delayDisplayString() {
		return "Delay: " + (Math.round(PlexCore.getSharedValue("autoGG_delay").doubleValue * 10.0D) / 10.D) + "s";
	}

	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
		if (slider.id == 7) {
			PlexCore.getSharedValue("autoGG_delay").set((double) (PlexAutoGGMod.MIN_DELAY + (slider.sliderValue * (PlexAutoGGMod.MAX_DELAY - PlexAutoGGMod.MIN_DELAY))));
			slider.displayString = delayDisplayString();
		}
	}
	
	@Override
	public Integer pageForegroundColour() {
		return 0xffe0c908;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		if (button.id == 5) {
			PlexCore.getSharedValue("autoGG_enabled").set(!PlexCore.getSharedValue("autoGG_enabled").booleanValue);
			button.displayString = buttonDisplayString("AutoGG", PlexCore.getSharedValue("autoGG_enabled").booleanValue);
		}
		if (button.id == 6) {
			PlexCore.getSharedValue("autoGG_waitUntilSilenceOver").set(!PlexCore.getSharedValue("autoGG_waitUntilSilenceOver").booleanValue);
			button.displayString = beforeAfter("Chat Silence", PlexCore.getSharedValue("autoGG_waitUntilSilenceOver").booleanValue);
		}
	}
}
