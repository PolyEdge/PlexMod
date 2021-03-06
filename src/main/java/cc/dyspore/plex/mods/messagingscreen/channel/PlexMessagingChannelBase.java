package cc.dyspore.plex.mods.messagingscreen.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.ListIterator;

import cc.dyspore.plex.core.util.PlexUtilRender;
import cc.dyspore.plex.core.util.PlexUtilChat;
import cc.dyspore.plex.core.util.PlexUtilColour;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import cc.dyspore.plex.Plex;

import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.mods.messagingscreen.PlexMessagingMessage;
import cc.dyspore.plex.mods.messagingscreen.PlexMessagingMod;
import cc.dyspore.plex.ui.widget.itemlist.PlexUIScrolledItem;

public abstract class PlexMessagingChannelBase implements PlexUIScrolledItem, Comparable<PlexMessagingChannelBase> {
	public Long lastChannelActivity = 0L;
	public Long lastChannelRead = 0L;
	public String name = "channel" + Minecraft.getSystemTime();
	public Map<String, String> channelTags = new HashMap<>();
	public String recipientEntityName = "";

	public Boolean awaitingReady = false;
	public Boolean channelReady = false;
	public Boolean connectFailed = false;

	public Boolean hiddenFromList = false;

	public String lastTextTyped = "";
	public Long lastChannelSwitchedTo;
	public Float lastChannelScroll = 1.0F;

	public Long selectTime = null;
	public Long readyTime = null;

	public int maxConnectionAttempts = 3;
	public long connectionAttemptDelay = 1750;
	public boolean errorOnMaxAttempts = true;

	public long lastConnectionAttempt = 0L;
	public int connectionAttempts = 0;

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
	
	public abstract int getMaxMessageLength();
	
	public void channelSelected() {}
	
	public void channelDeselected() {}
	
	public ResourceLocation getAttachedPlayerHead() {
		return null;
	}
	
	public String getDisplayName() {
		return "";
	}
	
	public int getDisplayColour() {
		return 0xffffff;
	}
	
	public int getMessageBackgroundColour() {
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
		this.connectionAttempts = 0;
		this.lastConnectionAttempt = 0;
		this.channelDeselected();
	}
	
	public void getChannelReady() {
		this.awaitingReady = true;
		this.channelReady = false;
		this.connectFailed = false;
		this.lastConnectionAttempt = Minecraft.getSystemTime();
		this.connectionAttempts += 1;
		this.readyChannel();
	}
	
	public void setReady() {
		if (!this.channelReady) {
			this.readyTime = Minecraft.getSystemTime();
		}
		this.awaitingReady = false;
		this.channelReady = true;
	}

	public void setUnready() {
		this.channelReady = false;
		this.connectionAttempts = 0;
		this.lastConnectionAttempt = 0;
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

	public void loopReady() {
		if (!this.awaitingReady && !this.channelReady && !this.connectFailed) {
			if (Minecraft.getSystemTime() > this.lastConnectionAttempt + this.connectionAttemptDelay) {
				if (this.connectionAttempts < this.maxConnectionAttempts) {
					this.getChannelReady();
				}
				else {
					if (errorOnMaxAttempts) {
						this.setError();
					}
				}
			}
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
		return PlexCore.modInstance(PlexMessagingMod.class).channelManager.selectedChannel == this;
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
			PlexCore.modInstance(PlexMessagingMod.class).channelManager.setSelectedChannel(null);
		}
		else {
			PlexCore.modInstance(PlexMessagingMod.class).channelManager.setSelectedChannel(this);
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
	public void listItemRenderText(int x, int y, int cellWidth, int cellHeight, float alpha, boolean selected, boolean mouseOver) {
		int channelNameStartX = x;
		int foregroundColour = this.getDisplayColour();
		int unreadMessages = this.getMessagesSinceLastRead().size();
		boolean playerHead = false;
		int playerHeadSize = (int) ((float) cellHeight * 0.75F);
		int playerHeadX = (x + cellWidth) - (playerHeadSize + ((cellHeight - playerHeadSize) / 2));
		int playerHeadY = y + ((cellHeight - playerHeadSize) / 2);
		
		if (unreadMessages > 0) {
			channelNameStartX = x + PlexUtilRender.calculateScaledStringWidth(PlexUtilChat.chatFromAmpersand("&l[" + String.valueOf(unreadMessages) + "] "), 1.0F);
		}
		
		if (this.getAttachedPlayerHead() != null) {
			playerHead = true;
		}
		
		if (selected) {
			foregroundColour = PlexUtilColour.multiply(foregroundColour, 1.60F);
		}
		if (mouseOver) {
			foregroundColour = PlexUtilColour.multiply(foregroundColour, 1.20F);
		}
		if (unreadMessages > 0) {
			PlexUtilRender.drawScaledStringLeftSide(PlexUtilChat.chatFromAmpersand("&l[" + String.valueOf(unreadMessages) + "] "), x, y + ((cellHeight / 2) - (Plex.minecraft.fontRendererObj.FONT_HEIGHT / 2)), 0xff0202, 1.0F);
		}
		String finalText = Plex.minecraft.fontRendererObj.trimStringToWidth(this.getDisplayName(), (x + cellWidth) - channelNameStartX - (playerHead ? cellHeight : 0));
		PlexUtilRender.drawScaledStringLeftSide(finalText, channelNameStartX, y + ((cellHeight / 2) - (Plex.minecraft.fontRendererObj.FONT_HEIGHT / 2)), this.getDisplayColour(), 1.0F);
		if (playerHead) {
			PlexUtilRender.drawGradientRect(playerHeadX, playerHeadY, playerHeadX + playerHeadSize, playerHeadY + playerHeadSize, 0xffffffff, 0xffffffff);
			PlexUtilRender.drawPlayerHead(this.getAttachedPlayerHead(), playerHeadX, playerHeadY, playerHeadSize);
		}
	}

	@Override
	public int listItemGetForegroundColour() {
		return 0;
	}
}
