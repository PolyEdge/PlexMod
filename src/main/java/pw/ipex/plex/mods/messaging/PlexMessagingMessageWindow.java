package pw.ipex.plex.mods.messaging;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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
			totalHeight += authorExtra;
			renderData.textBackdropY = totalHeight;
			textLines = PlexCoreRenderUtils.textWrapScaledString(message.content, this.getMaxChatMessageWidth() - (this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) * 2) - playerHeadExtra, this.messageTextScale);
			int y = authorExtra;
			for (String line : textLines) {
				int lineSize = PlexCoreRenderUtils.calculateScaledStringWidth(line, this.messageTextScale); // text width
				lineSize += (this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) * 2); // padding
				renderData.addTextLine(line, this.messageTextScale, this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale), y, lineSize);
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
			
			if (message.position == message.POSITION_LEFT) {
				renderData.relativeX = 0;
				if (message.playerHead == null) {
					maxWidth = backdropWidth;
					renderData.textBackdropX = 0;
				}
				else {
					int headSize = getDefaultBackdropSizeByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale);
					maxWidth = backdropWidth + headSize + this.playerHeadMessageSpacing;
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
				renderData.addTextLine(line, this.messageTextScale, lineSize / 2, y, lineSize);
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
	
	public int getTotalChatHeight() {
		int totalHeight = 0;
		String lastUser = null;
		for (PlexMessagingMessage message : this.displayedChannel.channelMessages) {
			if (message.type == message.TYPE_SYSTEM_MESSAGE) {
				this.addToNonZero(totalHeight, this.messageSpacingDifferentAuthor);
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
				this.addToNonZero(totalHeight, author ? this.messageSpacingDifferentAuthor : this.messageSpacingSameAuthor);
				totalHeight += this.getRenderData(message, author).totalHeight;
				lastUser = message.fromUser;
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
		
		return renderData.totalHeight;
	}
	
	public void drawScreen(int mouseX, int mouseY, float par3) {
		if (!this.isEnabled) {
			GlStateManager.color(0.75F, 0.75F, 0.75F, 1.0F);
		}
		int totalHeight = this.getTotalChatHeight();
		this.scrollbar.updateVelocity();
		this.scrollbar.setContentScale((float)this.getChatAreaHeight() / (float) totalHeight);
		int scrollRange = totalHeight - this.getChatAreaHeight();
		int viewportTop = (int)(scrollRange * this.scrollbar.scrollValue + 0);
		int viewportBottom = (int)(scrollRange * this.scrollbar.scrollValue + this.getChatAreaHeight());
		for (int i = this.displayedChannel.channelMessages.size() - 1; i >= 0 ; i--) {
			
		}
	}
}
