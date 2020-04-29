package cc.dyspore.plex.mods.messagingscreen.channel;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import cc.dyspore.plex.Plex;

public class PlexMessagingTeamChatChannel extends PlexMessagingSingleEntityChannel {

	@Override
	public void channelInit() {
	}
	
	@Override
	public String getDisplayName() {
		return "Team";
	}
	
	@Override
	public void chatMessage(ClientChatReceivedEvent event) {
	}
	
	@Override
	public int getDisplayColour() {
		return 0xff4286f4;
	}

	@Override
	public void readyChannel() {
		this.setReady();		
	}

	@Override
	public void sendMessage(String message) {
		Plex.minecraft.thePlayer.sendChatMessage("#" + message);

	}

	@Override
	public int getMaxMessageLength() {
		return 99;
	}
}
