package pw.ipex.plex.mods.messagingscreen.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.ListIterator;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import pw.ipex.plex.Plex;

import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mods.messagingscreen.PlexMessagingMessage;
import pw.ipex.plex.mods.messagingscreen.PlexMessagingMod;
import pw.ipex.plex.ui.widget.itemlist.PlexUIScrolledItem;

public abstract class PlexMessagingChannelBase implements PlexUIScrolledItem, Comparable<PlexMessagingChannelBase> {
	public Long lastChannelActivity = 0L;
	public Long lastChannelRead = 0L;
	public String name = "channel" + Minecraft.getSystemTime();
	public Map<String, String> channelTags = new HashMap<>();
	public String recipientEntityName = "";
	public String lastTextTyped = "";
	public Float lastMessagesScroll = 1.0F;
	public Boolean awaitingReady = false;
	public Boolean channelReady = false;
	public Boolean connectFailed = false;
	public Boolean hiddenFromList = false;
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
	
	public abstract void chatMessage(ClientChatReceivedEvent event);
	
	public abstract void readyChannel();
	
	public abstract void sendMessage(String message);
	
	public abstract Integer getMaxMessageLength();
	
	public void channelSelected() {}
	
	public void channelDeselected() {}
	
	public ResourceLocation getAttachedPlayerHead() {
		return null;
	}
	
	public String getDisplayName() {
		return "";
	}
	
	public Integer getDisplayColour() {
		return 0xffffff;
	}
	
	public Integer getMessageBackgroundColour() {
		return 0x65757575;
	}
	
	public void selected() {
		this.selectTime = Minecraft.getSystemTime();
		this.awaitingReady = false;
		this.channelReady = false;
		this.connectFailed = false;
		this.channelSelected();
	}
	
	public void deselected() {
		this.selectTime = null;
		this.awaitingReady = false;
		this.channelReady = false;
		this.channelDeselected();
	}
	
	public void getChannelReady() {
		this.awaitingReady = true;
		this.channelReady = false;
		this.connectFailed = false;
		this.readyChannel();
	}
	
	public void setReady() {
		if (!this.channelReady) {
			this.readyTime = Minecraft.getSystemTime();
		}
		this.awaitingReady = false;
		this.channelReady = true;
	}

	public void setError() {
		this.readyTime = null;
		this.connectFailed = true;
		this.awaitingReady = false;
	}

	public long getSelectTime() {
		if (this.selectTime == null) {
			this.selectTime = Minecraft.getSystemTime();
		}
		return this.selectTime;
	}
	
	public void addMessage(PlexMessagingMessage message) {
		channelMessages.add(message);
		message.channel = this;
		if (message.updatesChannelActivity) {
			this.bumpActivityNow();
		}
	}

	public void bumpActivityNow() {
		lastChannelActivity = Minecraft.getSystemTime();
	}

	@Override
	public int compareTo(PlexMessagingChannelBase item) {
		return (int) (this.lastChannelActivity - item.lastChannelActivity);
	}
	
	public void readingChannel() {
		this.lastChannelRead = Minecraft.getSystemTime();
	}

	public void readyIfNeeded() {
		if (!this.awaitingReady && !this.channelReady) {
			this.getChannelReady();
		}
	}
	
	public List<PlexMessagingMessage> getAllMessagesSinceLastRead() {
		List<PlexMessagingMessage> messages = new ArrayList<PlexMessagingMessage>();

		for (PlexMessagingMessage message : this.channelMessages) {
			if (message.time > this.lastChannelRead) {
				messages.add(message);
			}
		}
		return messages;
	}

	public List<PlexMessagingMessage> getMessagesSinceLastRead() {
		List<PlexMessagingMessage> messages = new ArrayList<PlexMessagingMessage>();

		for (PlexMessagingMessage message : this.channelMessages) {
			if (message.time > this.lastChannelRead && message.countsAsUnread) {
				messages.add(message);
			}
		}
		return messages;
	}

		public void addTag(String key, String val) {
		this.channelTags.put(key, val);
	}

	public String getTag(String key) {
		return this.channelTags.get(key);
	}

	@Override
	public boolean listItemIsSelected() {
		return PlexMessagingMod.channelManager.selectedChannel == this;
	}

	@Override
	public void listItemClick() {
	}

	@Override
	public void listItemOtherItemClicked() {
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
	public String listItemGetSearchText() {
		return this.name;
	}

	@Override
	public void listItemRenderText(int x, int y, int cellWidth, int cellHeight, boolean selected, boolean mouseOver) {
		int channelNameStartX = x;
		int foregroundColour = this.getDisplayColour();
		int unreadMessages = this.getMessagesSinceLastRead().size();
		boolean playerHead = false;
		int playerHeadSize = (int) ((float) cellHeight * 0.75F);
		int playerHeadX = (x + cellWidth) - (playerHeadSize + ((cellHeight - playerHeadSize) / 2));
		int playerHeadY = y + ((cellHeight - playerHeadSize) / 2);
		
		if (unreadMessages > 0) {
			channelNameStartX = x + Plex.renderUtils.calculateScaledStringWidth(PlexCoreUtils.chatFromAmpersand("&l[" + String.valueOf(unreadMessages) + "] "), 1.0F);
		}
		
		if (this.getAttachedPlayerHead() != null) {
			playerHead = true;
		}
		
		if (selected) {
			foregroundColour = PlexCoreUtils.multiplyColour(foregroundColour, 1.60F);
		}
		if (mouseOver) {
			foregroundColour = PlexCoreUtils.multiplyColour(foregroundColour, 1.20F);
		}
		if (unreadMessages > 0) {
			Plex.renderUtils.drawScaledStringLeftSide(PlexCoreUtils.chatFromAmpersand("&l[" + String.valueOf(unreadMessages) + "] "), x, y + ((cellHeight / 2) - (Plex.minecraft.fontRendererObj.FONT_HEIGHT / 2)), 0xff0202, 1.0F);
		}
		String finalText = Plex.minecraft.fontRendererObj.trimStringToWidth(this.getDisplayName(), (x + cellWidth) - channelNameStartX - (playerHead ? cellHeight : 0));
		Plex.renderUtils.drawScaledStringLeftSide(finalText, channelNameStartX, y + ((cellHeight / 2) - (Plex.minecraft.fontRendererObj.FONT_HEIGHT / 2)), this.getDisplayColour(), 1.0F);
		if (playerHead) {
			Plex.renderUtils.staticDrawGradientRect(playerHeadX, playerHeadY, playerHeadX + playerHeadSize, playerHeadY + playerHeadSize, 0xffffffff, 0xffffffff);
			Plex.renderUtils.drawPlayerHead(this.getAttachedPlayerHead(), playerHeadX, playerHeadY, playerHeadSize);
		}
	}

	@Override
	public int listItemGetForegroundColour() {
		return 0;
	}
}
