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
	public int paddingRightWithScrollbar = 0;
	
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
		return this.getChatEndX() - this.getChatStartX();
	}
	
	public int getChatAreaHeight() {
		return this.getChatEndY() - this.getChatStartY();
	}
	
	public int getChatStartX() {
		return this.startX + this.paddingLeft;
	}
	
	public int getChatEndX() {
		return this.endX - (this.scrollbar.barVisible() ? 8 + this.paddingRightWithScrollbar : this.paddingRight); //this.endX - this.paddingRight - (this.scrollbar.barVisible() ? 8 : 0);
	}
	
	public int getChatStartY() {
		return this.startY + this.paddingTop;
	}
	
	public int getChatEndY() {
		return this.endY - this.paddingBottom; //this.endX - this.paddingBottom;
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
		return (int) ((textHeight * scale) + this.getYPaddingByTextHeight((int) (Plex.minecraft.fontRendererObj.FONT_HEIGHT * scale)) * 2) ;
	}
	
	public int addToNonZero(int number, int add) {
		return number == 0 ? 0 : number + add; 
	}
	
	public boolean channelContainsHeads() {
		if (this.displayedChannel == null) {
			return false;
		}
		for (PlexMessagingMessage message : this.displayedChannel.channelMessages) {
			if (message.playerHead != null) {
				return true;
			}
		}
		return false;
	}
	
	public PlexMessagingMessageRenderData calculateMessageRenderData(PlexMessagingMessage message, PlexMessagingMessageRenderState renderState) {
		int maxWidth = 0;
		int totalHeight = 0;
		List<String> textLines = new ArrayList<String>();
		PlexMessagingMessageRenderData renderData = new PlexMessagingMessageRenderData();
		renderData.textColour = message.colour;
		renderData.backdropColour = message.backgroundColour;
		if (message.type == message.TYPE_CHAT_MESSAGE) {
			int backdropWidth = 0;
			int headSize = getDefaultBackdropSizeByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale);
			boolean headsShown = renderState.RENDER_HEADS_SHOWN;
			int authorExtra = renderState.RENDER_AUTHOR_ENABLED ? (int)(Plex.minecraft.fontRendererObj.FONT_HEIGHT * this.authorTextScale) + this.messageAuthorSeparator : 0;
			int authorWidth = renderState.RENDER_AUTHOR_ENABLED ? PlexCoreRenderUtils.calculateScaledStringWidth(message.fromUser, this.authorTextScale) : 0;
			int playerHeadExtra = !headsShown ? 0 : getDefaultBackdropSizeByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) + this.playerHeadMessageSpacing;
			
			renderData.displayBackdrop = true;
			renderData.headsShown = headsShown;
			renderData.authorVisible = renderState.RENDER_AUTHOR_ENABLED;
			renderData.authorName = message.fromUser;
			renderData.authorScale = this.authorTextScale;
			
			totalHeight += authorExtra;
			renderData.textBackdropY = totalHeight;
			
			List<String> textLinesSplit = new ArrayList<String>();
			for (String line : message.content.split("\n")) {
				textLinesSplit.add(line);
			}
			for (String line : textLinesSplit) {
				List<String> textWrapLines = PlexCoreRenderUtils.textWrapScaledString(line, this.getMaxChatMessageWidth() - (this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) * 2) - playerHeadExtra, this.messageTextScale);
				for (String wrapLine : textWrapLines) {
					textLines.add(wrapLine);
				}
			}

			int y = authorExtra;
			for (String line : textLines) {
				int lineSize = PlexCoreRenderUtils.calculateScaledStringWidth(line, this.messageTextScale); // text width
				lineSize += (this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) * 2); // padding
				renderData.addTextLine(line, this.messageTextScale, this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) + (message.position == message.POSITION_LEFT ? playerHeadExtra : 0), y + this.getYPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale), lineSize, message.colour);
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
				if (message.playerHead == null || !renderState.RENDER_HEADS_SHOWN) {
					maxWidth = backdropWidth;
					renderData.textBackdropX = 0;
				}
				else {
					maxWidth = backdropWidth + headSize + this.playerHeadMessageSpacing;
					renderData.authorX = headSize + this.playerHeadMessageSpacing;
					renderData.textBackdropX = headSize + this.playerHeadMessageSpacing;
					if (renderState.RENDER_HEAD_ENABLED) {
						renderData.playerHead = message.playerHead;
						renderData.playerHeadSize = headSize;
						renderData.playerHeadX = 0;
						renderData.playerHeadY = authorExtra;						
					}
				}
			}
			else if (message.position == message.POSITION_RIGHT) {
				if (authorWidth > backdropWidth) {
					renderData.authorX -= authorWidth - backdropWidth;
				}
				if (message.playerHead == null || !renderState.RENDER_HEADS_SHOWN) {
					maxWidth = backdropWidth;
					renderData.relativeX = 0 - backdropWidth;
					renderData.textBackdropX = 0;
				}
				else {
					maxWidth = backdropWidth + this.playerHeadMessageSpacing + headSize;
					renderData.relativeX = 0 - backdropWidth - this.playerHeadMessageSpacing - headSize;
					renderData.textBackdropX = 0;
					if (renderState.RENDER_HEAD_ENABLED) {
						renderData.playerHead = message.playerHead;
						renderData.playerHeadSize = headSize;
						renderData.playerHeadX = backdropWidth + this.playerHeadMessageSpacing;
						renderData.playerHeadY = authorExtra;						
					}
				}
			}
		}
		else if (message.type == message.TYPE_SYSTEM_MESSAGE) {
			renderData.relativeX = null; // centered
			//totalHeight += this.getYPaddingByTextHeight((int) (Plex.minecraft.fontRendererObj.FONT_HEIGHT * this.messageTextScale));
			List<String> textLinesSplit = new ArrayList<String>();
			for (String line : message.content.split("\n")) {
				textLinesSplit.add(line);
			}
			for (String line : textLinesSplit) {
				List<String> textWrapLines = PlexCoreRenderUtils.textWrapScaledString(line, this.getMaxSystemMessageWidth() - (this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) * 2), this.messageTextScale);
				for (String wrapLine : textWrapLines) {
					textLines.add(wrapLine);
				}
			}
			int width = 0;
			int y = 0;
			for (String line : textLines) {
				int lineSize = PlexCoreRenderUtils.calculateScaledStringWidth(line, this.messageTextScale); // text width
				//lineSize += (this.getXPaddingByScaledTextHeight(Plex.minecraft.fontRendererObj.FONT_HEIGHT, this.messageTextScale) * 2); // padding
				renderData.addTextLine(line, this.messageTextScale, 0 - (lineSize / 2), y, lineSize, message.colour);
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
	
	public PlexMessagingMessageRenderData getRenderData(PlexMessagingMessage message, PlexMessagingMessageRenderState renderState) {
		if (message.cachedRenderData != null) {
			if (message.cachedRenderData.renderState.matches(renderState)) {
				return message.cachedRenderData;
			}
		}
		message.cachedRenderData = this.calculateMessageRenderData(message, renderState);
		message.cachedRenderData.renderState = renderState;
		return message.cachedRenderData;
	}
	
	public int oldGetTotalChatHeight() {
		int totalHeight = 0;
		String lastUser = null;
		if (this.displayedChannel == null) {
			return 0;
		}
		boolean headsEnabled = this.channelContainsHeads();
		for (PlexMessagingMessage message : this.displayedChannel.channelMessages) {
			PlexMessagingMessageRenderState messageState = new PlexMessagingMessageRenderState();
			messageState.setHeadsShown(headsEnabled);
			if (message.type == message.TYPE_SYSTEM_MESSAGE) {
				messageState.setAuthorEnabled(false);
				messageState.setHeadEnabled(false);
				totalHeight = this.addToNonZero(totalHeight, this.messageSpacingDifferentAuthor);
				totalHeight += this.getRenderData(message, messageState).totalHeight;
				lastUser = null;
			}
			if (message.type == message.TYPE_CHAT_MESSAGE) {
				boolean author = true;
				if (lastUser != null) {
					if (lastUser.equals(message.fromUser)) {
						author = false;
					}
				}
				messageState.setAuthorEnabled(author);
				messageState.setHeadEnabled(false);
				totalHeight = this.addToNonZero(totalHeight, author ? this.messageSpacingDifferentAuthor : this.messageSpacingSameAuthor);
				totalHeight += this.getRenderData(message, messageState).totalHeight;
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
		boolean headsEnabled = this.channelContainsHeads();
		for (int i = 0; i < this.displayedChannel.channelMessages.size(); i++) {
			PlexMessagingMessage previousMessage = i - 1 >= 0 ? this.displayedChannel.channelMessages.get(i - 1) : null;
			PlexMessagingMessage message = this.displayedChannel.channelMessages.get(i);
			PlexMessagingMessageRenderState messageState = new PlexMessagingMessageRenderState();
			messageState.setHeadsShown(headsEnabled);
			boolean renderItem = false;
			boolean author = false;
			if (message.type == message.TYPE_SYSTEM_MESSAGE) {
				renderItem = true;
				messageState.setAuthorEnabled(false);
				messageState.setHeadEnabled(false);
				totalHeight = this.addToNonZero(totalHeight, this.messageSpacingDifferentAuthor);
				//totalHeight += this.getRenderData(message, messageState).totalHeight;
			}
			if (message.type == message.TYPE_CHAT_MESSAGE) {
				renderItem = true;
				author = true;
				if (previousMessage != null) {
					if (previousMessage.fromUser.equals(message.fromUser) && previousMessage.position.equals(message.position)) {
						author = false;
					}
				}
				messageState.setAuthorEnabled(author);
				messageState.setHeadEnabled(author);
				totalHeight = this.addToNonZero(totalHeight, author ? this.messageSpacingDifferentAuthor : this.messageSpacingSameAuthor);
			}
			if (renderItem) {
				int itemTotalHeight = this.getRenderData(message, messageState).totalHeight;
				totalHeight += itemTotalHeight;
			}
		}
		return totalHeight;
	}
	
	public PlexMessagingMessageHoverState getMouseOverMessage(int mouseX, int mouseY) {		
		if (this.displayedChannel == null) {
			return null;
		}
		int totalHeight = this.getTotalChatHeight();
		
		int scrollRange = totalHeight - this.getChatAreaHeight();
		int viewportTop = (int)(scrollRange * this.scrollbar.scrollValue + 0);
		
		int currentY = 0;
		//for (int i = this.displayedChannel.channelMessages.size() - 1; i >= 0 ; i--) {
		boolean headsEnabled = this.channelContainsHeads();
		for (int i = 0; i < this.displayedChannel.channelMessages.size(); i++) {
			PlexMessagingMessage previousMessage = i - 1 >= 0 ? this.displayedChannel.channelMessages.get(i - 1) : null;
			PlexMessagingMessage message = this.displayedChannel.channelMessages.get(i);
			PlexMessagingMessageRenderState messageState = new PlexMessagingMessageRenderState();
			messageState.setHeadsShown(headsEnabled);
			boolean renderItem = false;
			boolean author = false;
			if (message.type == message.TYPE_SYSTEM_MESSAGE) {
				renderItem = true;
				messageState.setAuthorEnabled(false);
				messageState.setHeadEnabled(false);
				currentY = this.addToNonZero(currentY, this.messageSpacingDifferentAuthor);
				//totalHeight += this.getRenderData(message, messageState).totalHeight;
			}
			if (message.type == message.TYPE_CHAT_MESSAGE) {
				renderItem = true;
				author = true;
				if (previousMessage != null) {
					if (previousMessage.fromUser.equals(message.fromUser) && previousMessage.position.equals(message.position)) {
						author = false;
					}
				}
				messageState.setAuthorEnabled(author);
				messageState.setHeadEnabled(author);
				currentY = this.addToNonZero(currentY, author ? this.messageSpacingDifferentAuthor : this.messageSpacingSameAuthor);
			}
			if (renderItem) {
				int itemStartY = this.getChatStartY() + (currentY - viewportTop);
				PlexMessagingMessageHoverState hoverState = this.getMessageHoverState(message, itemStartY, messageState, mouseX, mouseY);
				if (hoverState.IS_SELECTED) {
					return hoverState;
				}
				int itemTotalHeight = this.getRenderData(message, messageState).totalHeight;
				currentY += itemTotalHeight;
			}
		}
		return null;
	}
	
	public PlexMessagingMessageHoverState getMessageHoverState(PlexMessagingMessage message, int positionY, PlexMessagingMessageRenderState messageState, int mouseX, int mouseY) {
		PlexMessagingMessageHoverState hoverState = new PlexMessagingMessageHoverState().setMessage(message);
		if (mouseY < this.getRenderBorderTop() || mouseY > this.getRenderBorderBottom()) {
			return hoverState;
		}
		PlexMessagingMessageRenderData renderData = this.getRenderData(message, messageState);
		if (renderData.playerHead != null && messageState.RENDER_HEAD_ENABLED) {
			int headX = renderData.getItemXPosition(this.getChatStartX(), this.getChatEndX(), renderData.playerHeadX);
			int headY = renderData.getItemYPosition(positionY, renderData.playerHeadY);
			if (mouseX > headX  && mouseY > headY && mouseX < headX + renderData.playerHeadSize && mouseY < headY + renderData.playerHeadSize) {
				return hoverState.setHeadSelected(true);
			}
		}
		if (renderData.authorVisible && messageState.RENDER_AUTHOR_ENABLED) {
			int authorX = renderData.getItemXPosition(this.getChatStartX(), this.getChatEndX(), renderData.authorX);
			int authorY = renderData.getItemYPosition(positionY, renderData.authorY);
			int authorWidth = PlexCoreRenderUtils.calculateScaledStringWidth(renderData.authorName, renderData.authorScale);
			int authorHeight = (int) (Plex.minecraft.fontRendererObj.FONT_HEIGHT * renderData.authorScale);
			if (mouseX > authorX && mouseY > authorY && mouseX < authorX + authorWidth && mouseY < authorY + authorHeight) {
				return hoverState.setAuthorSelected(true);
			}
		}
		for (PlexMessagingMessageTextData line : renderData.textLines) {
			int lineX = renderData.getItemXPosition(this.getChatStartX(), this.getChatEndX(), line.x);
			int lineY = renderData.getItemYPosition(positionY, line.y);
			int lineHeight = line.getHeight();
			int wordSpaceWidth = PlexCoreRenderUtils.calculateScaledStringWidth(" ", line.scale);
			int wordX = lineX;
			for (String word : line.text.split(" ")) {
				int wordWidth = PlexCoreRenderUtils.calculateScaledStringWidth(word, line.scale);
				if (mouseX > wordX && mouseY > lineY && mouseX < wordX + wordWidth && mouseY < lineY + lineHeight) {
					return hoverState.setSelectedWord(word).setSelectedLine(line);
				}
				wordX += wordWidth + wordSpaceWidth;
			}
			if (mouseX > lineX && mouseY > lineY && mouseX < lineX + line.width && mouseY < lineY + lineHeight) {
				hoverState.setMessageSelected(true).setSelectedLine(line);
			}
		}
		if (renderData.displayBackdrop) {
			int bdStartX = renderData.getItemXPosition(this.getChatStartX(), this.getChatEndX(), renderData.textBackdropX);
			int bdEndX = bdStartX + renderData.textBackdropWidth;
			int bdTop = renderData.getItemYPosition(positionY, renderData.textBackdropY); 
			int bdBottom = bdTop + renderData.textBackdropHeight;
			if (mouseX > bdStartX && mouseY > bdTop && mouseX < bdEndX && mouseY < bdBottom) {
				return hoverState.setMessageSelected(true);
			}
		}
		return hoverState;
	}
	
	public void processMouseClick(PlexMessagingMessage message, int positionY, PlexMessagingMessageRenderState messageState, int mouseX, int mouseY, List<PlexMessagingMessageClickCallback> callbacks) {
		PlexMessagingMessageHoverState hoverState = this.getMessageHoverState(message, positionY, messageState, mouseX, mouseY);
		for (PlexMessagingMessageClickCallback callback : callbacks) {
			callback.clickCallback(hoverState);
		}
	}
	
	public int drawMessage(PlexMessagingMessage message, int positionY, PlexMessagingMessageRenderState messageState) {
		PlexMessagingMessageRenderData renderData = this.getRenderData(message, messageState);
		if (renderData.playerHead != null && messageState.RENDER_HEAD_ENABLED) {
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
		if (renderData.authorVisible && messageState.RENDER_AUTHOR_ENABLED) {
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
		//PlexCoreRenderUtils.drawScaledString("" + totalHeight, this.getChatStartX() + 5, this.getChatStartY() + 5, 0xffffff, 0.5F, false);
		//PlexCoreRenderUtils.drawScaledString("" + scrollRange, this.getChatStartX() + 5, this.getChatStartY() + 15, 0xffffff, 0.5F, false);
		//PlexCoreRenderUtils.drawScaledString("" + viewportTop, this.getChatStartX() + 5, this.getChatStartY() + 20, 0xffffff, 0.5F, false);
		//PlexCoreRenderUtils.drawScaledString("" + this.getChatAreaHeight(), this.getChatStartX() + 5, this.getChatStartY() + 25, 0xffffff, 0.5F, false);
		boolean headsEnabled = this.channelContainsHeads();
		for (int i = 0; i < this.displayedChannel.channelMessages.size(); i++) {
			PlexMessagingMessage previousMessage = i - 1 >= 0 ? this.displayedChannel.channelMessages.get(i - 1) : null;
			PlexMessagingMessage message = this.displayedChannel.channelMessages.get(i);
			PlexMessagingMessageRenderState messageState = new PlexMessagingMessageRenderState();
			messageState.setHeadsShown(headsEnabled);
			boolean renderItem = false;
			boolean author = false;
			if (message.type == message.TYPE_SYSTEM_MESSAGE) {
				renderItem = true;
				messageState.setAuthorEnabled(false);
				messageState.setHeadEnabled(false);
				currentY = this.addToNonZero(currentY, this.messageSpacingDifferentAuthor);
				//currentY += this.getRenderData(message, false).totalHeight;
			}
			if (message.type == message.TYPE_CHAT_MESSAGE) {
				renderItem = true;
				author = true;
				if (previousMessage != null) {
					if (previousMessage.fromUser.equals(message.fromUser) && previousMessage.position.equals(message.position)) {
						author = false;
					}
				}
				messageState.setAuthorEnabled(author);
				messageState.setHeadEnabled(author);
				currentY = this.addToNonZero(currentY, author ? this.messageSpacingDifferentAuthor : this.messageSpacingSameAuthor);
			}
			if (renderItem) {
				int itemStartY = this.getChatStartY() + (currentY - viewportTop);
				int itemTotalHeight = this.getRenderData(message, messageState).totalHeight;
				int itemEndY = itemStartY + itemTotalHeight;
				currentY += itemTotalHeight;
				if (itemEndY > this.getRenderBorderTop() && itemStartY < this.getRenderBorderBottom()) {
					this.drawMessage(message, itemStartY, messageState);
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
