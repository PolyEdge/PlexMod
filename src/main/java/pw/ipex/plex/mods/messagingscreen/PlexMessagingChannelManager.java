package pw.ipex.plex.mods.messagingscreen;

import java.util.ArrayList;
//import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.mods.messagingscreen.channel.PlexMessagingChannelBase;

public class PlexMessagingChannelManager {
	public PlexMessagingChannelBase selectedChannel = null;
	public boolean autoReady = true;
	public List<PlexMessagingChannelBase> channels = new ArrayList<>();
	public List<PlexMessagingChannelBase> displayedChannels = new ArrayList<>();
	public Long lastChannelChange = 0L;
	
	public PlexMessagingChannelManager() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onMessage(ClientChatReceivedEvent e) {
		if (!Plex.serverState.onMineplex || selectedChannel == null) {
			return;
		}
		if (selectedChannel.awaitingReady) {
			selectedChannel.chatMessage(e);
		}
	}
	
	public void addChannel(PlexMessagingChannelBase channel) {
		this.channels.add(channel);
		channel.channelInit();
	}
	
	public PlexMessagingChannelBase getChannel(String name) { 
		for (PlexMessagingChannelBase channel : this.channels) {
			if (channel.name.equalsIgnoreCase(name)) {
				return channel;
			}
		}
		return null;
	}

	public void updateDisplayedChannels() {
		List<PlexMessagingChannelBase> nonHidden = new ArrayList<>();
		for (PlexMessagingChannelBase channel : this.channels) {
			if (!channel.hiddenFromList) {
				nonHidden.add(channel);
			}
		}
		Collections.sort(nonHidden);
		Collections.reverse(nonHidden);
		this.displayedChannels.clear();
		this.displayedChannels.addAll(nonHidden);
	}
	
	public void setSelectedChannel(PlexMessagingChannelBase channel) {
		if (this.selectedChannel != null) {
			this.selectedChannel.deselected();
		}
		this.selectedChannel = channel;
		if (channel != null) {
			channel.lastChannelSwitchedTo = Minecraft.getSystemTime();
			channel.selected();
			if (this.autoReady) {
				channel.getChannelReady();
			}
		}
		lastChannelChange = Minecraft.getSystemTime();
	}
	
	public void deselectChannel() {
		this.setSelectedChannel((PlexMessagingChannelBase) null);
	}
	
	public void deleteMessageRenderCache() {
		for (PlexMessagingChannelBase channel : this.channels) {
			for (PlexMessagingMessage message : channel.channelMessages) {
				message.cachedRenderData = null;
			}
		}
	}
	
	public void addMessageToChannel(PlexMessagingChannelBase channel, PlexMessagingMessage message) {
		channel.addMessage(message);
	}
	
	public void updateChannelActivity(PlexMessagingChannelBase channel) {
		channel.bumpActivityNow();
	}

	public void unreadyChannelsByClass(Class<? extends PlexMessagingChannelBase> channelClass) {
		for (PlexMessagingChannelBase channel : this.channels) {
			if (channel.getClass().equals(channelClass)) {
				channel.awaitingReady = false;
				channel.channelReady = false;
				channel.selectTime = null;
			}
		}
	}
}