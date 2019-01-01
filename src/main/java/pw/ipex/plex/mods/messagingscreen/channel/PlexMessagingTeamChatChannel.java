package pw.ipex.plex.mods.messagingscreen.channel;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import pw.ipex.plex.Plex;

public class PlexMessagingTeamChatChannel extends PlexMessagingChannelBase {

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
	public Integer getDisplayColour() {
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
	public Integer getMaxMessageLength() {
		return 99;
	}
}
