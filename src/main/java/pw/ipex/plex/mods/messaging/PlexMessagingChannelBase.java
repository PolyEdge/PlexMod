package pw.ipex.plex.mods.messaging;

import java.util.ArrayList;
import java.util.List;
//import java.util.ListIterator;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreRenderUtils;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.ui.PlexUIScrolledItem;

public abstract class PlexMessagingChannelBase implements PlexUIScrolledItem {
	public Long lastPassiveActivity = 0L;
	public Long lastAgressiveActivity = 0L;
	public Long lastChannelRead = 0L;
	public String name = "channel" + Minecraft.getSystemTime();
	public String recipientEntityName = "";
	public String lastTextTyped = "";
	public Float lastMessagesScroll = 1.0F;
	public Boolean awaitingReady = false;
	public Boolean channelReady = false;
	public Long selectTime = null;
	public Long deselectTime = null;
	public Long readyTime;
	public Long channelLastSwitchedTo;
	public Float lastChannelScroll = 1.0F;
	
	public List<PlexMessagingMessage> channelMessages = new ArrayList<PlexMessagingMessage>();
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setRecipientEntityName(String name) {
		this.recipientEntityName = name;
	}
	
	public abstract void channelInit();
	
	public abstract void chatMessage(IChatComponent message);
	
	public abstract void readyChannel();
	
	public abstract void sendMessage(String message);
	
	public abstract Integer getMaxMessageLength();
	
	public void channelSelected() {}
	
	public void channelDeselected() {}
	
	public String getDisplayName() {
		return "";
	}
	
	public Integer getDisplayColour() {
		return 0xffffff;
	}
	
	public Integer getMessageBackgroundColour() {
		return 0x65757575;
	}
	
	protected void selected() {
		this.selectTime = Minecraft.getSystemTime();
		this.awaitingReady = false;
		this.channelReady = false;
		this.channelSelected();
	}
	
	protected void deselected() {
		this.selectTime = Minecraft.getSystemTime();
		this.awaitingReady = false;
		this.channelReady = false;
		this.channelDeselected();
	}
	
	protected void getChannelReady() {
		this.awaitingReady = true;
		this.channelReady = false;
		this.readyChannel();
	}
	
	public void setReady() {
		if (!this.channelReady) {
			this.readyTime = Minecraft.getSystemTime();
		}
		this.awaitingReady = false;
		this.channelReady = true;
	}
	
	public void addPassiveMessage(PlexMessagingMessage message) {
		channelMessages.add(message);
		lastPassiveActivity = Minecraft.getSystemTime();
	}
	
	public void addAgressiveMessage(PlexMessagingMessage message) {
		channelMessages.add(message);
		lastAgressiveActivity = Minecraft.getSystemTime();
	}
	
	public void readingChannel() {
		this.lastChannelRead = Minecraft.getSystemTime();
	}
	
	public List<PlexMessagingMessage> getMessagesSinceLastRead() {
		List<PlexMessagingMessage> messages = new ArrayList<PlexMessagingMessage>();
		//ListIterator<PlexMessagingMessage> totalMessages = messages.listIterator(messages.size());

		for (PlexMessagingMessage message : this.channelMessages) {
			if (message.time > this.lastChannelRead) {
				messages.add(message);
				continue;
			}
			//break;
		}		
		
//		while (totalMessages.hasPrevious()) {
//			PlexMessagingMessage message = totalMessages.previous();
//			if (message.time > lastChannelRead) {
//				messages.add(message);
//				continue;
//			}
//			break;
//		}
		return messages;
	}

	@Override
	public boolean listItemIsSelected() {
		return PlexMessagingMod.channelManager.selectedChannel == this;
	}

	@Override
	public void listItemSelect() {
		if (this.listItemIsSelected()) {
			PlexMessagingMod.channelManager.setSelectedChannel(null);
		}
		else {
			PlexMessagingMod.channelManager.setSelectedChannel(this);
		}
	}

	@Override
	public int listItemGetHeight() {
		return -1;
	}

	@Override
	public String listItemGetText() {
		return null;
	}

	@Override
	public void listItemRenderText(int x, int y, int cellWidth, int cellHeight, boolean selected, boolean mouseOver) {
		int channelNameStartX = x;
		int foregroundColour = this.getDisplayColour();
		int unreadMessages = this.getMessagesSinceLastRead().size();
		if (unreadMessages > 0) {
			channelNameStartX = x + PlexCoreRenderUtils.calculateScaledStringWidth(PlexCoreUtils.ampersandToFormatCharacter("&l[" + String.valueOf(unreadMessages) + "] "), 1.0F);
		}
		
		if (selected) {
			foregroundColour = PlexCoreUtils.multiplyColour(foregroundColour, 1.60F);
		}
		if (mouseOver) {
			foregroundColour = PlexCoreUtils.multiplyColour(foregroundColour, 1.20F);
		}
		if (unreadMessages > 0) {
			PlexCoreRenderUtils.drawScaledStringLeftSide(PlexCoreUtils.ampersandToFormatCharacter("&l[" + String.valueOf(unreadMessages) + "] "), x, y + ((cellHeight / 2) - (Plex.minecraft.fontRendererObj.FONT_HEIGHT / 2)), 0xff0202, 1.0F);				
		}
		String finalText = Plex.minecraft.fontRendererObj.trimStringToWidth(this.getDisplayName(), (x + cellWidth) - channelNameStartX);
		PlexCoreRenderUtils.drawScaledStringLeftSide(finalText, channelNameStartX, y + ((cellHeight / 2) - (Plex.minecraft.fontRendererObj.FONT_HEIGHT / 2)), this.getDisplayColour(), 1.0F);		
	}

	@Override
	public int listItemGetForegroundColour() {
		return 0;
	}
}
