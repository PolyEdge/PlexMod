package pw.ipex.plex.ui;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreRenderUtils;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexUIScrolledItemList extends GuiScreen {	
	public int startX;
	public int startY;
	public int endX;
	public int endY;
	public int paddingX;
	public int paddingY;
	
	public boolean isEnabled = true;
	
	public int renderBorderTop = 5;
	public int renderBorderBottom = 5;
	public int defaultEntryHeight = 16;
	public int mouseWheelScrollPixelAmount = 40;
	
	public List<? extends PlexUIScrolledItem> items;
	
	public PlexUIScrollbar scrollbar;
	
	public <T> PlexUIScrolledItemList(List<? extends PlexUIScrolledItem> itemsList, int startX, int startY, int endX, int endY) {
		this.items = itemsList;
		this.scrollbar = new PlexUIScrollbar(startY, endY, endX - 8, 6);
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
	
	public int getTotalListPixels() {
		int height = 0;
		for (PlexUIScrolledItem item : this.items) {
			height += heightOrDefault(item);
		}
		return height;
	}
	
	public PlexUIScrolledItem getMouseOverItem(int mouseX, int mouseY) {
		int scrollRange = this.getTotalListPixels() - this.getSizeY();
		int viewportTop = (int)(scrollRange * this.scrollbar.scrollValue + 0);
		int viewportBottom = (int)(scrollRange * this.scrollbar.scrollValue + this.getSizeY());
		if (!(mouseX > this.startX && mouseX < this.getEndXWithScrollbar())) {
			return null;
		}
		if (this.items.size() == 0) {
			return null;
		}
		int currentY = -heightOrDefault(this.items.get(0));
		for (PlexUIScrolledItem item : this.items) {
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
	
	public void drawScreen(int mouseX, int mouseY, float par3) {
		if (!this.isEnabled) {
			GlStateManager.color(0.75F, 0.75F, 0.75F, 1.0F);
		}
		this.scrollbar.updateVelocity();
		this.scrollbar.setContentScale((float)this.getSizeY() / (float) this.getTotalListPixels());
		int scrollRange = this.getTotalListPixels() - this.getSizeY();
		int viewportTop = (int)(scrollRange * this.scrollbar.scrollValue + 0);
		int viewportBottom = (int)(scrollRange * this.scrollbar.scrollValue + this.getSizeY());
		if (this.items.size() == 0) {
			return;
		}
		int currentY = -heightOrDefault(this.items.get(0));
		for (PlexUIScrolledItem item : this.items) {
			currentY += heightOrDefault(item);
			if (currentY + heightOrDefault(item) < viewportTop - this.renderBorderTop) {
				continue;
			}
			if (currentY > viewportBottom + this.renderBorderBottom) {
				break;
			}
			int itemYposition = this.startY + (currentY - viewportTop);
			
			boolean isMouseOver = false;
			boolean isSelected = false;
			if (mouseX > this.startX && mouseX < this.endX && mouseY > itemYposition && mouseY < itemYposition + heightOrDefault(item) && this.isEnabled) {
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
			
			PlexUIMenuScreen.drawRect(this.startX, itemYposition, this.endX, itemYposition + heightOrDefault(item), itemBackgroundColour);
	
			if (itemText != null) {
				String finalText = Plex.minecraft.fontRendererObj.trimStringToWidth(itemText, this.getEndXWithScrollbar() - this.paddingX * 2);
				PlexCoreRenderUtils.drawScaledStringLeftSide(finalText, this.startX + this.paddingX, itemYposition + (heightOrDefault(item) - (Plex.minecraft.fontRendererObj.FONT_HEIGHT / 2)), itemForegroundColour, 1.0F);
			}
			else {
				item.listItemRenderText(this.startX + this.paddingX, itemYposition, this.getSizeXWithScrollbar() - this.paddingX * 2, heightOrDefault(item), isSelected, isMouseOver);
			}
		}
		this.scrollbar.drawScreen(mouseX, mouseY, par3);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	public void mouseClicked(int par1, int par2, int btn) {
		if (!this.isEnabled) {
			return;
		}
		this.scrollbar.mousePressed(par1, par2, btn);
		
		PlexUIScrolledItem hoverItem = this.getMouseOverItem(par1, par2);
		if (hoverItem != null) {
			hoverItem.listItemSelect();
		}
	}
	
	public void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (!this.isEnabled) {
			return;
		}
		this.scrollbar.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		this.scrollbar.mouseReleased(mouseX, mouseY);
	}
	
	public void handleMouseInput(int x, int y) {
		if (!this.isEnabled) {
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
