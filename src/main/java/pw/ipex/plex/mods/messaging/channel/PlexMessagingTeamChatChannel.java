package pw.ipex.plex.mods.messaging.channel;

import net.minecraft.util.IChatComponent;
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
	public void chatMessage(IChatComponent message) {
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
