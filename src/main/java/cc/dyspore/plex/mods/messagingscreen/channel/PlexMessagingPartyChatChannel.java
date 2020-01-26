package cc.dyspore.plex.mods.messagingscreen.channel;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import cc.dyspore.plex.Plex;

public class PlexMessagingPartyChatChannel extends PlexMessagingSingleEntityChannel {
	@Override
	public void channelInit() {
	}
	
	@Override
	public void chatMessage(ClientChatReceivedEvent e) {
	}
	
	@Override
	public String getDisplayName() {
		return "Party";
	}
	
	@Override
	public Integer getDisplayColour() {
		return 0xffe820e8;
	}

	@Override
	public void readyChannel() {
		this.setReady();		
	}

	@Override
	public void sendMessage(String message) {
		Plex.minecraft.thePlayer.sendChatMessage("@" + message);

	}

	@Override
	public Integer getMaxMessageLength() {
		return 99;
	}
}
