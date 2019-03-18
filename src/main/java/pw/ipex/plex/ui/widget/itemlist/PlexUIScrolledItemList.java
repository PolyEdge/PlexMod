package pw.ipex.plex.ui.widget.itemlist;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import pw.ipex.plex.Plex;

import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.ui.PlexUIModMenuScreen;
import pw.ipex.plex.ui.widget.PlexUIScrollbar;

public class PlexUIScrolledItemList extends GuiScreen {	
	public int startX;
	public int startY;
	public int endX;
	public int endY;
	public int paddingX;
	public int paddingY;
	
	public boolean isEnabled = true;
	public boolean isVisible = true;
	
	public int renderBorderTop = 5;
	public int renderBorderBottom = 5;
	public boolean renderBorderTransition = false;
	public int renderBorderTransitionDistance = 10;
	public int defaultEntryHeight = 16;
	public int mouseWheelScrollPixelAmount = 40;
	
	public List<? extends PlexUIScrolledItem> items;
	public String searchText = "";
	public int searchMode = 0; // 0 - contains 1 - starts with
	
	public PlexUIScrollbar scrollbar;
	
	public PlexUIScrolledItemList(List<? extends PlexUIScrolledItem> itemsList, int startX, int startY, int endX, int endY) {
		this.items = itemsList;
		this.scrollbar = new PlexUIScrollbar(startY, endY, endX - 8, 6);
		this.scrollbar.hiddenForcedScroll = 0.0F;
		this.setPosition(startX, startY, endX, endY);
	}
	
	public void setPosition(int sx, int sy, int ex, int ey) {
		this.startX = sx;
		this.startY = sy;
		this.endX = ex;
		this.endY = ey;
		this.updateScrollbarPos(); 
	}
		
	public void setPadding(int px, int py) {
		this.paddingX = px;
		this.paddingY = py;
	}
	
	public void setRenderBorder(int top, int bottom) {
		this.renderBorderTop = top;
		this.renderBorderBottom = bottom;
	}
	
	public void updateScrollbarPos() {
		this.scrollbar.setPosition(this.startY, this.endY, this.endX - 8, 6);
	}
	
	public void setEnabled(boolean enabled) {
		this.isEnabled = enabled;
		this.scrollbar.setEnabled(enabled);
	}

	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}

	public void setRenderBorderTransition(int size) {
		this.renderBorderTransition = true;
		this.renderBorderTransitionDistance = size;
	}

	public void disableRenderBorderTransition() {
		this.renderBorderTransition = false;
	}
	
	public int getSizeX() {
		return this.endX - this.startX;
	}
	
	public int getSizeXWithScrollbar() {
		return this.getEndXWithScrollbar() - this.startX;
	}
	
	public int getSizeY() {
		return this.endY - this.startY;
	}

	public int getEndXWithScrollbar() {
		return this.endX - (this.scrollbar.barVisible() ? 8 : 0);
	}
	
	public int heightOrDefault(PlexUIScrolledItem item) {
		Integer itemHeight = item.listItemGetHeight();
		return itemHeight <= 0 ? defaultEntryHeight : itemHeight;
	}
	
	public void scrollListByPixels(int pixels) {
		if (!this.scrollbar.barVisible()) {
			return;
		}
		this.scrollbar.scrollByPixels(pixels, this.getTotalListPixels(), this.getSizeY());
	}

	public List<? extends PlexUIScrolledItem> getAllItems() {
		return this.items;
	}

	public List<? extends PlexUIScrolledItem> getVisibleItems() {
		return this.getItemsMatchingSearchTerm(this.searchText);
	}
	
	public List<? extends PlexUIScrolledItem> getItemsMatchingSearchTerm(String searchTerm) {
		return this.getItemsMatchingSearchTerm(this.items, searchTerm, this.searchMode);
 	}

	public <T> List<T> getItemsMatchingSearchTerm(List<T> items, String searchTerm, int searchMode) {
		if (searchTerm == null) {
			return items;
		}
		if (searchTerm.equals("")) {
			return items;
		}
		List<T> searchItems = new ArrayList<>();
		for (int index = 0; index < items.size(); index++) {
			try {
				PlexUIScrolledItem item = ((PlexUIScrolledItem) items.get(index));
				if (item.listItemGetSearchText() == null) {
					continue;
				}
				if (item.listItemGetSearchText().equals("")) {
					continue;
				}
				if (item.listItemGetSearchText().toLowerCase().contains(searchTerm.toLowerCase()) && searchMode == 0) {
					searchItems.add(items.get(index));
				}
				if (item.listItemGetSearchText().toLowerCase().startsWith(searchTerm.toLowerCase()) && searchMode == 1) {
					searchItems.add(items.get(index));
				}
			}
			catch (Throwable e) {}
		}
		return searchItems;
	}
	
	public int getTotalListPixels() {
		return getTotalListPixels(this.getVisibleItems());
	}

	public int getTotalListPixels(List<? extends PlexUIScrolledItem> items) {
		int height = 0;
		for (PlexUIScrolledItem item : items) {
			height += heightOrDefault(item);
		}
		return height;
	}

	public PlexUIScrolledItem getMouseOverItem(int mouseX, int mouseY) {
		int scrollRange = this.getTotalListPixels(this.getVisibleItems()) - this.getSizeY();
		int viewportTop = (int)(scrollRange * this.scrollbar.scrollValue + 0);
		int viewportBottom = (int)(scrollRange * this.scrollbar.scrollValue + this.getSizeY());
		if (!(mouseX > this.startX && mouseX < this.getEndXWithScrollbar())) {
			return null;
		}
		List<? extends PlexUIScrolledItem> itemsSearch = this.getVisibleItems();
		if (itemsSearch.size() == 0) {
			return null;
		}
		int currentY = -heightOrDefault(itemsSearch.get(0));
		for (PlexUIScrolledItem item : itemsSearch) {
			currentY += heightOrDefault(item);
			if (currentY + heightOrDefault(item) < viewportTop - this.renderBorderTop) {
				continue;
			}
			if (currentY > viewportBottom + this.renderBorderBottom) {
				break;
			}
			int itemYposition = this.startY + (currentY - viewportTop);
			if (mouseY > itemYposition && mouseY < itemYposition + heightOrDefault(item)) {
				return item;
			}
		}
		return null;
	}

	public void updateScreen() {
		this.scrollbar.updateScreen();
	}

	public int getListYPositionOfItem(PlexUIScrolledItem searchItem) {
		List<? extends PlexUIScrolledItem> itemsSearch = this.getVisibleItems();
		int scrollRange = this.getTotalListPixels() - this.getSizeY();

		if (itemsSearch.size() == 0) {
			return -1;
		}
		int currentY = -heightOrDefault(itemsSearch.get(0));
		for (PlexUIScrolledItem item : itemsSearch) {
			currentY += heightOrDefault(item);
			if (item == searchItem) {
				return currentY;
			}
		}
		return -1;
	}

	public float getScrollLocationOfItem(PlexUIScrolledItem item) {
		int scrollRange = this.getTotalListPixels() - this.getSizeY();
		int ypos = this.getListYPositionOfItem(item);
		if (ypos == -1) {
			return 0.0F;
		}
		return PlexCoreUtils.clamp((float) ypos / (float) scrollRange, 0.0F, 1.0F);
	}

	public boolean isItemVisibleInList(PlexUIScrolledItem scrollItem) {
		if (!this.isVisible) {
			return false;
		}
		List<? extends PlexUIScrolledItem> itemsSearch = this.getVisibleItems();
		int scrollRange = this.getTotalListPixels() - this.getSizeY();
		int viewportTop = (int)(scrollRange * this.scrollbar.realScrollValue + 0);
		int viewportBottom = (int)(scrollRange * this.scrollbar.realScrollValue + this.getSizeY());
		int itemYPos = this.getListYPositionOfItem(scrollItem);
		if (itemYPos == -1) {
			return false;
		}
		return itemYPos + heightOrDefault(scrollItem) >= viewportTop && itemYPos <= viewportBottom;
	}

	public boolean isItemFullyVisibleInList(PlexUIScrolledItem scrollItem) {
		if (!this.isVisible) {
			return false;
		}
		List<? extends PlexUIScrolledItem> itemsSearch = this.getVisibleItems();
		int scrollRange = this.getTotalListPixels() - this.getSizeY();
		int viewportTop = (int)(scrollRange * this.scrollbar.realScrollValue + 0);
		int viewportBottom = (int)(scrollRange * this.scrollbar.realScrollValue + this.getSizeY());
		int itemYPos = this.getListYPositionOfItem(scrollItem);
		if (itemYPos == -1) {
			return false;
		}
		return itemYPos >= viewportTop && itemYPos + heightOrDefault(scrollItem) <= viewportBottom;
	}

	public void scrollToItemIfNotInView(PlexUIScrolledItem item) {
		if (!this.isItemVisibleInList(item)) {
			this.scrollbar.setScroll(this.getScrollLocationOfItem(item), false);
		}
	}

	public void scrollToItemIfNotCompletelyInView(PlexUIScrolledItem item) {
		if (!this.isItemFullyVisibleInList(item)) {
			this.scrollbar.setScroll(this.getScrollLocationOfItem(item), false);
		}
	}
	
	public void drawScreen(int mouseX, int mouseY, float par3) {
		if (!this.isVisible) {
			return;
		}
		if (!this.isEnabled) {
			GlStateManager.color(0.75F, 0.75F, 0.75F, 1.0F);
		}
		List<? extends PlexUIScrolledItem> itemsSearch = this.getVisibleItems();
		this.scrollbar.setContentScale((float)this.getSizeY() / (float) this.getTotalListPixels());
		int scrollRange = this.getTotalListPixels() - this.getSizeY();
		int viewportTop = (int)(scrollRange * this.scrollbar.scrollValue + 0);
		int viewportBottom = (int)(scrollRange * this.scrollbar.scrollValue + this.getSizeY());
		if (itemsSearch.size() == 0) {
			return;
		}
		int currentY = -heightOrDefault(itemsSearch.get(0));
		for (PlexUIScrolledItem item : itemsSearch) {
			float alpha = 1.0F;
			currentY += heightOrDefault(item);
			if (currentY + heightOrDefault(item) < viewportTop - this.renderBorderTop) {
				if (this.renderBorderTransition && currentY + heightOrDefault(item) >= viewportTop - this.renderBorderTop - this.renderBorderTransitionDistance) {
					alpha = 1.0F - PlexCoreUtils.clamp(Math.abs((float)((viewportTop - this.renderBorderTop) - (currentY + heightOrDefault(item)))) / (float)this.renderBorderTransitionDistance, 0.0F, 1.0F);
				}
				else {
					continue;
				}
			}
			if (currentY > viewportBottom + this.renderBorderBottom) {
				if (this.renderBorderTransition && currentY <= viewportBottom + this.renderBorderBottom + this.renderBorderTransitionDistance) {
					alpha = 1.0F - PlexCoreUtils.clamp(Math.abs((float)((viewportBottom + this.renderBorderBottom) - currentY)) / (float)this.renderBorderTransitionDistance, 0.0F, 1.0F);
					Plex.renderUtils.drawScaledString("" + alpha, 150.0F, 60.0F, 0xffffff, 0.75F, false);
					Plex.renderUtils.drawScaledString("" + (viewportBottom + this.renderBorderBottom) , 150.0F, 66.0F, 0xffffff, 0.75F, false);
					Plex.renderUtils.drawScaledString("" + currentY, 150.0F, 72.0F, 0xffffff, 0.75F, false);
				}
				else {
					break;
				}
			}
			int itemYposition = this.startY + (currentY - viewportTop);
			
			boolean isMouseOver = false;
			boolean isSelected = false;
			if (mouseX > this.startX && mouseX < this.getEndXWithScrollbar() && mouseY > itemYposition && mouseY < itemYposition + heightOrDefault(item) && this.isEnabled) {
				isMouseOver = true;
			}
			if (item.listItemIsSelected()) {
				isSelected = true;
			}
			
			String itemText = item.listItemGetText();
			int itemForegroundColour = item.listItemGetForegroundColour();
			int itemBackgroundColour = 0x60777777;
			if ((Integer)itemForegroundColour == null) {
				itemForegroundColour = 0xffffffff;
			}
			
			if (isSelected) {
				itemForegroundColour = PlexCoreUtils.multiplyColour(itemForegroundColour, 1.60F);
				itemBackgroundColour = PlexCoreUtils.multiplyColour(itemBackgroundColour, 1.60F);
			}
			if (isMouseOver) {
				itemForegroundColour = PlexCoreUtils.multiplyColour(itemForegroundColour, 1.20F);
				itemBackgroundColour = PlexCoreUtils.multiplyColour(itemBackgroundColour, 1.20F);
			}

			itemBackgroundColour = PlexCoreUtils.multiplyColour(itemBackgroundColour, 1.0F, 1.0F, 1.0F, alpha);

			
			Plex.renderUtils.staticDrawGradientRect(this.startX, itemYposition, this.endX, itemYposition + heightOrDefault(item), itemBackgroundColour, itemBackgroundColour);
	
			if (itemText != null) {
				String finalText = Plex.minecraft.fontRendererObj.trimStringToWidth(itemText, this.getEndXWithScrollbar() - this.paddingX * 2);
				Plex.renderUtils.drawScaledStringLeftSide(finalText, this.startX + this.paddingX, itemYposition + ((heightOrDefault(item) / 2) - (Plex.minecraft.fontRendererObj.FONT_HEIGHT / 2)), itemForegroundColour, 1.0F);
			}
			else {
				item.listItemRenderText(this.startX + this.paddingX, itemYposition, this.getSizeXWithScrollbar() - this.paddingX * 2, heightOrDefault(item), alpha, isSelected, isMouseOver);
			}
		}
		this.scrollbar.drawScreen(mouseX, mouseY, par3);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	public void mouseClicked(int par1, int par2, int btn) {
		if (!this.isEnabled || !this.isVisible) {
			return;
		}
		this.scrollbar.mousePressed(par1, par2, btn);
		
		PlexUIScrolledItem hoverItem = this.getMouseOverItem(par1, par2);
		if (hoverItem != null) {
			for (PlexUIScrolledItem item : this.items) {
				if (item != hoverItem) {
					item.listItemOtherItemClicked();
				}
			}
			hoverItem.listItemSelect();
			hoverItem.listItemClick();
		}
	}
	
	public void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (!this.isEnabled || !this.isVisible) {
			return;
		}
		this.scrollbar.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		this.scrollbar.mouseReleased(mouseX, mouseY);
	}
	
	public void handleMouseInput(int x, int y) {
		if (!this.isEnabled || !this.isVisible) {
			return;
		}
		int scrollWheel = Mouse.getEventDWheel();
		if ((x > this.startX) && (x < this.endX) && (y > this.startY) && (y < this.endY)) {
			if (scrollWheel != 0) {
				this.scrollbar.scrollByPixels((0 - (scrollWheel / Math.abs(scrollWheel))) * this.mouseWheelScrollPixelAmount, this.getTotalListPixels(), this.getSizeY());
			}
		}
	}
}
