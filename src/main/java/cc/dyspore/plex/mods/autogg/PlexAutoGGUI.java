package cc.dyspore.plex.mods.autogg;

import cc.dyspore.plex.core.util.PlexUtil;
import net.minecraft.client.gui.GuiButton;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.ui.PlexUIBase;
import cc.dyspore.plex.ui.PlexUIModMenuScreen;
import cc.dyspore.plex.ui.widget.PlexUISlider;
import cc.dyspore.plex.ui.widget.PlexUITextField;
import cc.dyspore.plex.ui.widget.itemlist.PlexUIScrolledItemList;

import java.util.List;

public class PlexAutoGGUI extends PlexUIBase {
	public PlexUIScrolledItemList ggMessagesList;
	public PlexUITextField ggMessageEdit;

	public GuiButton newGGButton;
	public GuiButton removeGGButton;

	public PlexAutoGGMessage oldSelectedMessage = null;
	
	@Override
	public String uiGetTitle() {
		return "Auto GG";
	}

	@Override
	public void initGui(PlexUIModMenuScreen ui) {
		int top = ui.startingYPos(180);
		int paneSize = ui.centeredPaneSize(2, 20, 160);
		int pane1Pos = ui.centeredPanePos(-1, 2, 20, 160);
		int pane2Pos = ui.centeredPanePos(0, 2, 20, 160);

		PlexAutoGGMod modInstance = PlexCore.modInstance(PlexAutoGGMod.class);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, this.enabledDisabled("AutoGG", modInstance.modEnabled)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, this.beforeAfter("Chat Silence", modInstance.ggWaitUntilSilenceEnd)));

		ui.addElement(new PlexUISlider(this, 7, pane2Pos + 5, top + 0, paneSize - 10, 20, (float) (modInstance.ggDelay / (modInstance.MAX_DELAY - modInstance.MIN_DELAY)), this.delayDisplayString()));
		ui.addElement(new GuiButton(8, pane2Pos + 5, top + 23, paneSize - 10, 20, this.ggModeDisplayString()));

		this.ggMessagesList = new PlexUIScrolledItemList(modInstance.ggMessages, pane1Pos + 5, top + 46, pane2Pos + paneSize - 5, top + 151);
		this.ggMessagesList.setRenderBorder(-10, -10);
		this.ggMessagesList.setPadding(5, 0);
		this.ggMessagesList.setRenderBorderTransition(15);

		this.ggMessageEdit = new PlexUITextField(9, Plex.minecraft.fontRendererObj, pane1Pos + 5, top + 154, paneSize * 2 - 5 - 3 - 20 - 3 - 20 - 5, 20);
		this.ggMessageEdit.text.setMaxStringLength(100);
		this.ggMessageEdit.text.setCanLoseFocus(false);
		this.ggMessageEdit.text.setFocused(true);

		ui.addElement(this.newGGButton = new GuiButton(10, pane2Pos + paneSize - 20 - 20 - 3 - 5, top + 154, 20, 20, "+"));
		ui.addElement(this.removeGGButton = new GuiButton(11, pane2Pos + paneSize - 20 - 5, top + 154, 20, 20, "-"));

		this.deselectAll();
		this.updateSelectedItem();
	}

	public PlexAutoGGMessage getSelectedItem() {
		PlexAutoGGMod modInstance = PlexCore.modInstance(PlexAutoGGMod.class);
		PlexAutoGGMessage selectedMessage = null;
		for (PlexAutoGGMessage message : modInstance.ggMessages) {
			if (message.selected && selectedMessage == null) {
				selectedMessage = message;
			}
			else {
				message.selected = false;
			}
		}
		return selectedMessage;
	}

	public void updateSelectedItem() {
		PlexAutoGGMessage selectedMessage = this.getSelectedItem();
		if (selectedMessage == null) {
			this.ggMessageEdit.text.setText("");
			this.removeGGButton.enabled = false;
			this.oldSelectedMessage = null;
			return;
		}
		else if (selectedMessage != this.oldSelectedMessage) {
			this.ggMessageEdit.text.setText(selectedMessage.message);
			this.oldSelectedMessage = selectedMessage;
		}
		this.ggMessageEdit.text.setEnabled(true);
		this.removeGGButton.enabled = true;
	}

	public void deselectAll() {
		PlexAutoGGMod modInstance = PlexCore.modInstance(PlexAutoGGMod.class);
		for (PlexAutoGGMessage message : modInstance.ggMessages) {
			message.selected = false;
		}
	}

	public void newItem() {
		PlexAutoGGMod modInstance = PlexCore.modInstance(PlexAutoGGMod.class);
		PlexAutoGGMessage newMessage = new PlexAutoGGMessage(this.ggMessageEdit.text.getText());
		this.deselectAll();
		newMessage.selected = true;
		modInstance.ggMessages.add(newMessage);
		this.ggMessagesList.scrollToItemIfNotCompletelyInView(newMessage);
	}

	public void deleteItem(PlexAutoGGMessage message) {
		PlexAutoGGMod modInstance = PlexCore.modInstance(PlexAutoGGMod.class);
		if (message == null) {
			return;
		}
		PlexAutoGGMessage messageBefore = null;
		PlexAutoGGMessage messageAfter = null;
		boolean messageFound = false;
		for (PlexAutoGGMessage ggMessage : modInstance.ggMessages) {
			if (ggMessage == message) {
				messageFound = true;
				continue;
			}
			if (!messageFound) {
				messageBefore = ggMessage;
			}
			else {
				messageAfter = ggMessage;
			}
			if (messageFound && messageAfter != null) {
				break;
			}
		}
		modInstance.ggMessages.remove(message);
		deselectAll();
		PlexAutoGGMessage newSelectedMessage = null;
		if (messageAfter != null) {
			newSelectedMessage = messageAfter;
		}
		else if (messageBefore != null) {
			newSelectedMessage = messageBefore;
		}

		if (newSelectedMessage != null) {
			newSelectedMessage.selected = true;
			this.ggMessagesList.scrollToItemIfNotCompletelyInView(newSelectedMessage);
		}
	}

	public void moveSelection(int indexes) {
		PlexAutoGGMod modInstance = PlexCore.modInstance(PlexAutoGGMod.class);
		List<PlexAutoGGMessage> messages = modInstance.ggMessages;
		if (messages.size() == 0) {
			return;
		}

		PlexAutoGGMessage selectedItem = this.getSelectedItem();
		if (selectedItem == null) {
			selectedItem = messages.get(0);
		}
		selectedItem.selected = false;

		int selectedIndex = -1;
		for (int itemIndex = 0; itemIndex < messages.size(); itemIndex++) {
			if (messages.get(itemIndex) == selectedItem) {
				selectedIndex = itemIndex;
			}
		}
		selectedIndex = PlexUtil.clamp(selectedIndex + indexes, 0, messages.size() - 1);
		messages.get(selectedIndex).selected = true;
		this.ggMessagesList.scrollToItemIfNotCompletelyInView(messages.get(selectedIndex));
	}
	
	@Override
	public void mouseClicked(int par1, int par2, int btn) {
		this.ggMessagesList.mouseClicked(par1, par2, btn);
		this.ggMessageEdit.mouseClicked(par1, par2, btn);
		this.updateSelectedItem();
	}

	@Override
	public void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		this.ggMessagesList.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		this.ggMessagesList.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void handleMouseInput(int x, int y) {
		this.ggMessagesList.handleMouseInput(x, y);
	}
	
	@Override
	public void updateScreen() {
		this.ggMessagesList.updateScreen();
		this.ggMessageEdit.updateScreen();
	}
	
	@Override
	public void keyTyped(char par1, int par2) {
		if (par2 == 200) {
			this.moveSelection(-1);
		}
		else if (par2 == 208) {
			this.moveSelection(1);
		}
		else if (par2 == 28) {
			this.newItem();
		}
		else {
			this.ggMessageEdit.keyTyped(par1, par2);
		}
		this.updateSelectedItem();
		PlexAutoGGMessage selectedMessage = this.getSelectedItem();
		if (selectedMessage != null) {
			selectedMessage.message = this.ggMessageEdit.text.getText();
		}
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.ggMessagesList.drawScreen(par1, par2, par3);
		this.ggMessageEdit.drawScreen(par1, par2, par3);
	}

	public String ggModeDisplayString() {
		PlexAutoGGMod modInstance = PlexCore.modInstance(PlexAutoGGMod.class);
		if (modInstance.ggMode == 0) {
			return "Messages: In Order";
		}
		if (modInstance.ggMode == 1) {
			return "Messages: Random";
		}
		return "";
	}
	
	public String enabledDisabled(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}
	
	public String beforeAfter(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "After" : "Before");
	}
	
	public String delayDisplayString() {
		PlexAutoGGMod modInstance = PlexCore.modInstance(PlexAutoGGMod.class);
		return "Delay: " + (Math.round(modInstance.ggDelay * 10.0D) / 10.D) + "s";
	}

	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
		PlexAutoGGMod modInstance = PlexCore.modInstance(PlexAutoGGMod.class);
		if (slider.id == 7) {
			modInstance.ggDelay = (modInstance.MIN_DELAY + (slider.sliderValue * (modInstance.MAX_DELAY - modInstance.MIN_DELAY)));
			slider.displayString = delayDisplayString();
		}
	}
	
	@Override
	public Integer pageForegroundColour() {
		return 0xffe0c908;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		PlexAutoGGMod modInstance = PlexCore.modInstance(PlexAutoGGMod.class);
		if (button.id == 5) {
			modInstance.modEnabled = !modInstance.modEnabled;
			button.displayString = enabledDisabled("AutoGG", modInstance.modEnabled);
		}
		if (button.id == 6) {
			modInstance.ggWaitUntilSilenceEnd = !modInstance.ggWaitUntilSilenceEnd;
			button.displayString = beforeAfter("Chat Silence", modInstance.ggWaitUntilSilenceEnd);
		}
		if (button.id == 8) {
			modInstance.ggMode = (modInstance.ggMode + 1) % (modInstance.MAX_GG_MODE + 1);
			button.displayString = this.ggModeDisplayString();
		}
		if (button.id == 10) {
			this.newItem();
		}
		if (button.id == 11) {
			this.deleteItem(this.getSelectedItem());
		}
		this.updateSelectedItem();
	}
}
