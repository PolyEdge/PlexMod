package pw.ipex.plex.mods.messaging;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreRenderUtils;
import pw.ipex.plex.ui.PlexUIScrollbar;

public final class PlexMessagingMessageWindow extends GuiScreen {
	public int startX;
	public int startY;
	public int endX;
	public int endY;
	
	public int paddingTop = 0;
	public int paddingBottom = 5;
	public int paddingLeft = 0;
	public int paddingRight = 0;
	
	public int renderBorderTop = 5;
	public int renderBorderBottom = 5;
	
	public int backgroundColour = 0x99252525;
	public float messageTextScale = 1.0F;
	public float authorTextScale = 0.6F;
	public float maxChatMessageWidthPercent = 0.75F;
	public float maxSystemMessageWidthPercent = 0.5F;
	public float messageContentPaddingPercent = 0.75F; // message is 75% text 25% padding - relative to FontRenderer.FONT_HEIGHT
	public int extraMessageXPadding = 2;
	public int messageAuthorSeparator = 1;
	public float messageLineSpacingFontHeightPercent = 1.0F;
	public int extraChatMessageLineSpacing = 1;
	public int extraSystemMessageLineSpacing = 1;
	public int messageSpacingDifferentAuthor = 3;
	public int messageSpacingSameAuthor = 2;
	
	public int playerHeadMessageSpacing = 2;

	public int mouseWheelScrollPixelAmount = 40;
	
	public boolean isEnabled = true;
	
	public PlexUIScrollbar scrollbar;
	
	public PlexMessagingChannelBase displayedChannel = null;
	
	public PlexMessagingMessageWindow(int startX, int startY, int endX, int endY) {
		this.scrollbar = new PlexUIScrollbar(startY, endY, endX - 7, 6);
		this.setPosition(startX, startY, endX, endY);
		this.updateScrollbarPosition();
	}
	
	public void setPosition(int startX, int startY, int endX, int endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}
	
	public void updateScrollbarPosition() {
		this.scrollbar = new PlexUIScrollbar(this.startY, this.endY, this.endX - 7, 6);
	}
	
	public void setChannel(PlexMessagingChannelBase channel) {
		this.displayedChannel = channel;
	}
	
	public int getWidth() {
		return this.endX - this.startX;
	}
	
	public int getHeight() {
		return this.endY - this.startY;
	}
	
	public int getChatAreaWidth() {
		return this.endX - this.startX - (this.scrollbar.barVisible() ? 8 : 0) - this.paddingLeft - this.paddingRight;
	}
	
	public int getChatAreaHeight() {
		return this.endY - this.startY - this.paddingTop - this.paddingBottom;
	}
	
	public int getChatStartX() {
		return this.startX + this.paddingLeft;
	}
	
	public int getChatEndX() {
		return this.endX - this.paddingRight;
	}
	
	public int getChatStartY() {
		return this.startY + this.paddingTop;
	}
	
	public int getChatEndY() {
		return this.endX - this.paddingBottom;
	}
	
	public int getRenderBorderTop() {
		return this.startY - this.renderBorderTop;
	}
	
	public int getRenderBorderBottom() {
		return this.endY + this.renderBorderBottom;
	}
	
	public int getMaxChatMessageWidth() {
		return (int) (this.getChatAreaWidth() * this.maxChatMessageWidthPercent);
	}
	
	public int getMaxSystemMessageWidth() {
		return (int) (this.getChatAreaWidth() * this.maxSystemMessageWidthPercent);
	}

	public int getXPaddingByTextHeight(int textHeight) {
		return this.getYPaddingByTextHeight(textHeight) + this.extraMessageXPadding;
	}
	
	public int getYPaddingByTextHeight(int textHeight) {
		return (int)(((textHeight / this.messageContentPaddingPercent) - textHeight) / 2);
	}
	
	public int getDefaultBackdropSizeByTextHeight(int textHeight) {
		return (int) (textHeight / this.messageContentPaddingPercent);
	}
	
	public int getXPaddingByScaledTextHeight(int textHeight, float scale) {
		return this.getYPaddingByTextHeight((int) (textHeight * scale)) + this.extraMessageXPadding;
	}
	
	public int getYPaddingByScaledTextHeight(int textHeight, float scale) {
		return (int)((((textHeight * scale) / this.messageContentPaddingPercent) - textHeight) / 2);
	}
	
	public int getDefaultBackdropSizeByScaledTextHeight(int textHeight, float scale) {
		return (int) ((textHeight * scale) / this.messageContentPaddingPercent);
	}
	
	public int addToNonZero(int number, int add) {
		return number == 0 ? 0 : number + add; 
	}
	
	public boolean channelContainsHeads() {
		return false;
	}
	
	public PlexMessagingMessageRenderData calculateMessageRenderData(PlexMessagingMessage message, boolean includeAuthor) {
		int maxWidth = 0;
		int totalHeight = 0;
		List<String> textLines = new ArrayList<String>();
		PlexMessagingMessageRenderData renderData = new PlexMessagingMessageRenderData();
		renderData.textColour = message.colour;
		renderData.backdropColour = message.backgroundColour;
		if (message.type == message.TYPE_CHAT_MESSAGE) {
			int backdropWidth = 0;
			renderData.displayBackdrop = true;
			int authorExtra = includeAuthor ? (int)(Plex.minecraft.fontRendererObj.FONT_HEIGHT * this.authorTextScale) + this.messageAuthorSeparator : 0;
			int playerHeadExtra = message.playerHead == null ? 0 : getDefaultBackdropSizeByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) + this.playerHeadMessageSpacing;
			renderData.authorVisible = includeAuthor;
			renderData.authorName = message.fromUser;
			renderData.authorScale = this.authorTextScale;
			totalHeight += authorExtra;
			renderData.textBackdropY = totalHeight;
			textLines = PlexCoreRenderUtils.textWrapScaledString(message.content, this.getMaxChatMessageWidth() - (this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) * 2) - playerHeadExtra, this.messageTextScale);
			int y = authorExtra;
			for (String line : textLines) {
				int lineSize = PlexCoreRenderUtils.calculateScaledStringWidth(line, this.messageTextScale); // text width
				lineSize += (this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) * 2); // padding
				renderData.addTextLine(line, this.messageTextScale, this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale), y + this.getYPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale), lineSize, message.colour);
				if (lineSize > backdropWidth) {
					backdropWidth = lineSize;
				}
				y += (Plex.minecraft.fontRendererObj.FONT_HEIGHT * this.messageTextScale * this.messageLineSpacingFontHeightPercent) + this.extraChatMessageLineSpacing;
			}
			int backdropHeight = (int) ((Plex.minecraft.fontRendererObj.FONT_HEIGHT * this.messageTextScale * this.messageLineSpacingFontHeightPercent) * textLines.size()); // text line spacing
			backdropHeight += (this.extraChatMessageLineSpacing * (textLines.size() - 1)); // extra line spacing
			backdropHeight += this.getYPaddingByTextHeight((int) (Plex.minecraft.fontRendererObj.FONT_HEIGHT * this.messageTextScale)) * 2; // top and bottom padding
			renderData.textBackdropHeight = backdropHeight;
			renderData.textBackdropWidth = backdropWidth;
			totalHeight += backdropHeight;
			
			renderData.authorX = 0;
			renderData.authorY = 0;
			if (message.position == message.POSITION_LEFT) {
				renderData.relativeX = 0;
				if (message.playerHead == null) {
					maxWidth = backdropWidth;
					renderData.textBackdropX = 0;
				}
				else {
					int headSize = getDefaultBackdropSizeByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale);
					maxWidth = backdropWidth + headSize + this.playerHeadMessageSpacing;
					renderData.authorX = headSize + this.playerHeadMessageSpacing;
					renderData.textBackdropX = headSize + this.playerHeadMessageSpacing;
					renderData.playerHead = message.playerHead;
					renderData.playerHeadSize = headSize;
					renderData.playerHeadX = 0;
					renderData.playerHeadY = authorExtra;
				}
			}
			else if (message.position == message.POSITION_RIGHT) {
				if (message.playerHead == null) {
					maxWidth = backdropWidth;
					renderData.relativeX = 0 - backdropWidth;
					renderData.textBackdropX = 0;
				}
				else {
					int headSize = getDefaultBackdropSizeByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale);
					maxWidth = backdropWidth + this.playerHeadMessageSpacing + headSize;
					renderData.relativeX = 0 - backdropWidth - this.playerHeadMessageSpacing - headSize;
					renderData.textBackdropX = 0;
					renderData.playerHead = message.playerHead;
					renderData.playerHeadSize = headSize;
					renderData.playerHeadX = backdropWidth + this.playerHeadMessageSpacing;
					renderData.playerHeadY = authorExtra;
				}
			}
		}
		else if (message.type == message.TYPE_SYSTEM_MESSAGE) {
			renderData.relativeX = null; // centered
			//totalHeight += this.getYPaddingByTextHeight((int) (Plex.minecraft.fontRendererObj.FONT_HEIGHT * this.messageTextScale));
			textLines = PlexCoreRenderUtils.textWrapScaledString(message.content, this.getMaxSystemMessageWidth() - (this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) * 2), this.messageTextScale);
			int width = 0;
			int y = 0;
			for (String line : textLines) {
				int lineSize = PlexCoreRenderUtils.calculateScaledStringWidth(line, this.messageTextScale); // text width
				//lineSize += (this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) * 2); // padding
				renderData.addTextLine(line, this.messageTextScale, lineSize / 2, y, lineSize, message.colour);
				if (lineSize > width) {
					width = lineSize;
				}
				y += (Plex.minecraft.fontRendererObj.FONT_HEIGHT * this.messageTextScale * this.messageLineSpacingFontHeightPercent) + this.extraSystemMessageLineSpacing;
			}
			totalHeight += (Plex.minecraft.fontRendererObj.FONT_HEIGHT * this.messageTextScale * this.messageLineSpacingFontHeightPercent) * textLines.size();
			totalHeight += (this.extraSystemMessageLineSpacing * (textLines.size() - 1));
			maxWidth = width;
			//totalHeight += this.getYPaddingByTextHeight((int) (Plex.minecraft.fontRendererObj.FONT_HEIGHT * this.messageTextScale));
		}
		renderData.totalHeight = totalHeight;
		renderData.maxWidth = maxWidth;

		return renderData;
	}
	
	public PlexMessagingMessageRenderData getRenderData(PlexMessagingMessage message, boolean showAuthor) {
		if (message.cachedRenderData != null) {
			if (message.cachedRenderData.authorVisible == showAuthor) {
				return message.cachedRenderData;
			}
		}
		message.cachedRenderData = this.calculateMessageRenderData(message, showAuthor);
		return message.cachedRenderData;
	}
	
	public int oldGetTotalChatHeight() {
		int totalHeight = 0;
		String lastUser = null;
		if (this.displayedChannel == null) {
			return 0;
		}
		for (PlexMessagingMessage message : this.displayedChannel.channelMessages) {
			if (message.type == message.TYPE_SYSTEM_MESSAGE) {
				totalHeight = this.addToNonZero(totalHeight, this.messageSpacingDifferentAuthor);
				totalHeight += this.getRenderData(message, false).totalHeight;
				lastUser = null;
			}
			if (message.type == message.TYPE_CHAT_MESSAGE) {
				boolean author = true;
				if (lastUser != null) {
					if (lastUser.equals(message.fromUser)) {
						author = false;
					}
				}
				totalHeight = this.addToNonZero(totalHeight, author ? this.messageSpacingDifferentAuthor : this.messageSpacingSameAuthor);
				totalHeight += this.getRenderData(message, author).totalHeight;
				lastUser = message.fromUser;
			}
		}
		return totalHeight;
	}
	
	public int getTotalChatHeight() {		
		if (this.displayedChannel == null) {
			return 0;
		}

		int totalHeight = 0;
		//for (int i = this.displayedChannel.channelMessages.size() - 1; i >= 0 ; i--) {
		for (int i = 0; i < this.displayedChannel.channelMessages.size(); i++) {
			PlexMessagingMessage previousMessage = i - 1 >= 0 ? this.displayedChannel.channelMessages.get(i - 1) : null;
			PlexMessagingMessage message = this.displayedChannel.channelMessages.get(i);
			boolean renderItem = false;
			boolean author = false;
			if (message.type == message.TYPE_SYSTEM_MESSAGE) {
				renderItem = true;
				totalHeight = this.addToNonZero(totalHeight, this.messageSpacingDifferentAuthor);
				totalHeight += this.getRenderData(message, false).totalHeight;
			}
			if (message.type == message.TYPE_CHAT_MESSAGE) {
				renderItem = true;
				author = true;
				if (previousMessage != null) {
					if (previousMessage.fromUser.equals(message.fromUser)) {
						author = false;
					}
				}
				totalHeight = this.addToNonZero(totalHeight, author ? this.messageSpacingDifferentAuthor : this.messageSpacingSameAuthor);
			}
			if (renderItem) {
				int itemTotalHeight = this.getRenderData(message, author).totalHeight;
				totalHeight += itemTotalHeight;
			}
		}
		return totalHeight;
	}
	
	public int drawMessage(PlexMessagingMessage message, int positionY, boolean showAuthor) {
		PlexMessagingMessageRenderData renderData = this.getRenderData(message, showAuthor);
		if (renderData.playerHead != null) {
			int headX = renderData.getItemXPosition(this.getChatStartX(), this.getChatEndX(), renderData.playerHeadX);
			int headY = renderData.getItemYPosition(positionY, renderData.playerHeadY);
			if (headY + renderData.playerHeadSize > this.getRenderBorderTop() && headY < this.getRenderBorderBottom()) {
				PlexCoreRenderUtils.drawPlayerHead(renderData.playerHead, headX, headY, renderData.playerHeadSize);
			}
		}
		if (renderData.displayBackdrop) {
			int bdStartX = renderData.getItemXPosition(this.getChatStartX(), this.getChatEndX(), renderData.textBackdropX);
			int bdEndX = bdStartX + renderData.textBackdropWidth;
			int bdTop = renderData.getItemYPosition(positionY, renderData.textBackdropY); 
			int bdBottom = bdTop + renderData.textBackdropHeight;
			if (bdTop < this.getRenderBorderTop()) {
				bdTop = this.getRenderBorderTop();
			}
			if (bdBottom > this.getRenderBorderBottom()) {
				bdBottom = this.getRenderBorderBottom();
			}
			PlexCoreRenderUtils.staticDrawGradientRect(bdStartX, bdTop, bdEndX, bdBottom, renderData.backdropColour, renderData.backdropColour);
		}
		if (renderData.authorVisible) {
			int authorX = renderData.getItemXPosition(this.getChatStartX(), this.getChatEndX(), renderData.authorX);
			int authorY = renderData.getItemYPosition(positionY, renderData.authorY);
			if (authorY + (Plex.minecraft.fontRendererObj.FONT_HEIGHT * renderData.authorScale) > this.getRenderBorderTop() && authorY < this.getRenderBorderBottom()) {
				PlexCoreRenderUtils.drawScaledString(renderData.authorName, authorX, authorY, 0xffffff, renderData.authorScale, false);
			}
		}
		for (PlexMessagingMessageTextData line : renderData.textLines) {
			int lineX = renderData.getItemXPosition(this.getChatStartX(), this.getChatEndX(), line.x);
			int lineY = renderData.getItemYPosition(positionY, line.y);
			if (lineY + (Plex.minecraft.fontRendererObj.FONT_HEIGHT * line.scale) > this.getRenderBorderTop() && lineY < this.getRenderBorderBottom()) {
				PlexCoreRenderUtils.drawScaledString(line.text, lineX, lineY, line.colour, line.scale, false);
			}			
		}
		return renderData.totalHeight;
	}
	
	public void drawScreen(int mouseX, int mouseY, float par3) {
		if (!this.isEnabled) {
			GlStateManager.color(0.75F, 0.75F, 0.75F, 1.0F);
		}			
		if (this.displayedChannel == null) {
			return;
		}
		int totalHeight = this.getTotalChatHeight();
		this.scrollbar.updateVelocity();
		this.scrollbar.setContentScale((float)this.getChatAreaHeight() / (float) totalHeight);			
		int scrollRange = totalHeight - this.getChatAreaHeight();
		int viewportTop = (int)(scrollRange * this.scrollbar.scrollValue + 0);
		//int viewportBottom = (int)(scrollRange * this.scrollbar.scrollValue + this.getChatAreaHeight());
		int currentY = 0;
		for (int i = 0; i < this.displayedChannel.channelMessages.size(); i++) {
			PlexMessagingMessage previousMessage = i - 1 >= 0 ? this.displayedChannel.channelMessages.get(i - 1) : null;
			PlexMessagingMessage message = this.displayedChannel.channelMessages.get(i);
			boolean renderItem = false;
			boolean author = false;
			if (message.type == message.TYPE_SYSTEM_MESSAGE) {
				renderItem = true;
				currentY = this.addToNonZero(currentY, this.messageSpacingDifferentAuthor);
				currentY += this.getRenderData(message, false).totalHeight;
			}
			if (message.type == message.TYPE_CHAT_MESSAGE) {
				renderItem = true;
				author = true;
				if (previousMessage != null) {
					if (previousMessage.fromUser.equals(message.fromUser)) {
						author = false;
					}
				}
				currentY = this.addToNonZero(currentY, author ? this.messageSpacingDifferentAuthor : this.messageSpacingSameAuthor);
			}
			if (renderItem) {
				int itemStartY = this.getChatStartY() + (currentY - viewportTop);
				int itemTotalHeight = this.getRenderData(message, author).totalHeight;
				int itemEndY = itemStartY + itemTotalHeight;
				currentY += itemTotalHeight;
				if (itemEndY > this.getRenderBorderTop() && itemStartY < this.getRenderBorderBottom()) {
					this.drawMessage(message, itemStartY, author);
				}
			}
		}
		this.scrollbar.drawScreen(mouseX, mouseY, par3);
	}
	
	@Override
	public void mouseClicked(int par1, int par2, int btn) {
		if (!this.isEnabled) {
			return;
		}
		this.scrollbar.mousePressed(par1, par2, btn);
		
		//PlexUIScrolledItem hoverItem = this.getMouseOverItem(par1, par2);
		//if (hoverItem != null) {
		//	hoverItem.listItemSelect();
		//}
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
				this.scrollbar.scrollByPixels((0 - (scrollWheel / Math.abs(scrollWheel))) * this.mouseWheelScrollPixelAmount, this.getTotalChatHeight(), this.getChatAreaHeight());
			}
		}
	}

}
