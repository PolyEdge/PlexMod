package pw.ipex.plex.mods.messaging;

import java.util.ArrayList;
//import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.mods.messaging.channel.PlexMessagingChannelBase;

public class PlexMessagingChannelManager {
	public PlexMessagingChannelBase selectedChannel = null;
	public List<PlexMessagingChannelBase> channels = new ArrayList<PlexMessagingChannelBase>();
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
	
	public void setSelectedChannel(PlexMessagingChannelBase channel) {
		if (this.selectedChannel != null) {
			this.selectedChannel.deselected();
		}
		this.selectedChannel = channel;
		if (channel != null) {
			channel.channelLastSwitchedTo = Minecraft.getSystemTime();
			channel.selected();		
			channel.getChannelReady();
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
	
	public void bumpChannelToTop(PlexMessagingChannelBase channel) {
		List<PlexMessagingChannelBase> remove = new ArrayList<PlexMessagingChannelBase>();
		remove.add(channel);
		this.channels.removeAll(remove);
		this.channels.add(0, channel);
	}
	
	public void addPassiveMessageToChannel(PlexMessagingChannelBase channel, PlexMessagingMessage message) {
		channel.addPassiveMessage(message);
	}
	
	public void addAgressiveMessageToChannel(PlexMessagingChannelBase channel, PlexMessagingMessage message) {
		channel.addAgressiveMessage(message);
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