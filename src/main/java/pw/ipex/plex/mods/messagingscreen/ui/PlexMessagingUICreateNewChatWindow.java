package pw.ipex.plex.mods.messagingscreen.ui;

import net.minecraft.client.gui.GuiScreen;
import pw.ipex.plex.Plex;
import pw.ipex.plex.mods.messagingscreen.PlexMessagingChannelClassWrapper;
import pw.ipex.plex.ui.widget.itemlist.PlexUIScrolledItemList;

import java.util.List;

public final class PlexMessagingUICreateNewChatWindow extends GuiScreen {
	public int startX;
	public int startY;
	public int endX;
	public int endY;

	public int minTopListPadding = 15;
	public int minBottomListPadding = 15;

	public String headerText = "";
	public int headerColour = 0xffffff;
	public int descriptionColour = 0xffffff;

	public boolean isEnabled = true;

	public PlexUIScrolledItemList channelSelection;
	public List<PlexMessagingChannelClassWrapper> items;

	public PlexMessagingUICreateNewChatWindow(int startX, int startY, int endX, int endY) {
		this.channelSelection = new PlexUIScrolledItemList(this.items, 0, 0, 0, 0);
		this.channelSelection.paddingX = 5;
		this.setPosition(startX, startY, endX, endY);
		this.updateChannelSelectionPosition(0, 0);
	}
	
	public void setPosition(int startX, int startY, int endX, int endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.updateChannelSelectionPosition(0, 0);
	}
	
	public void updateChannelSelectionPosition(int titleHeight, int descriptionHeight) {
		titleHeight = Math.max(titleHeight, minTopListPadding);
		descriptionHeight = Math.max(titleHeight, minBottomListPadding);
		int width = this.getWidth() / 3;
		int sidePadding = (this.getWidth() - width) / 2;
		this.channelSelection.setPosition(this.startX + sidePadding, this.startY + (int) (titleHeight * 1.2), this.endX - sidePadding, this.endY - (int) (descriptionHeight * 1.2));
	}

	public void setItems(List<PlexMessagingChannelClassWrapper> items) {
		this.items = items;
	}

	public PlexMessagingChannelClassWrapper getSelectedItem() {
		PlexMessagingChannelClassWrapper foundItem = null;
		for (PlexMessagingChannelClassWrapper item : this.items) {
			if (item.selected) {
				if (foundItem != null) {
					item.selected = false;
				}
				else {
					foundItem = item;
				}
			}
		}
		return foundItem;
	}
	
	public int getWidth() {
		return this.endX - this.startX;
	}
	
	public int getHeight() {
		return this.endY - this.startY;
	}

	public void drawScreen(int mouseX, int mouseY, float par3) {
		if (!this.isEnabled) {
			return;
		}
		this.channelSelection.items = this.items;
		PlexMessagingChannelClassWrapper selectedChannelType = this.getSelectedItem();

		this.channelSelection.drawScreen(mouseX, mouseY, par3);
		int headerSize = Plex.renderUtils.drawCenteredTextWrapScaledString(this.headerText, this.startX + this.getWidth() / 2, this.startY, (int) (this.getWidth() * 0.75F),1.5F, this.headerColour, 2);
		int footerSize = 0;
		String description = "";
		if (selectedChannelType != null) {
			description = selectedChannelType.description;
			footerSize = Plex.renderUtils.calculateCenteredTextWrapScaledStringHeight(description, (int) (this.getWidth() * 0.75F),1.0F, 1);
		}
		int footerLocation = this.endY - footerSize - 1;

		this.updateChannelSelectionPosition(headerSize, footerSize);
		this.channelSelection.drawScreen(mouseX, mouseY, par3);
		Plex.renderUtils.drawCenteredTextWrapScaledString(description, this.startX + this.getWidth() / 2, footerLocation, (int) (this.getWidth() * 0.75F),1.0F, this.descriptionColour, 1);
	}
	
	@Override
	public void mouseClicked(int par1, int par2, int btn) {
		if (!this.isEnabled) {
			return;
		}
		this.channelSelection.mouseClicked(par1, par2, btn);
		
		//PlexUIScrolledItem hoverItem = this.getMouseOverItem(par1, par2);
		//if (hoverItem != null) {
		//	hoverItem.listItemSelect();
		//}
	}
	
	public void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (!this.isEnabled) {
			return;
		}
		this.channelSelection.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		this.channelSelection.mouseReleased(mouseX, mouseY, state);
	}
	
	public void handleMouseInput(int x, int y) {
		if (!this.isEnabled) {
			return;
		}
		this.channelSelection.handleMouseInput(x, y);
	}
}
