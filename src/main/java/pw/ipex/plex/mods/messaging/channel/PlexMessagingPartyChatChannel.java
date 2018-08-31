package pw.ipex.plex.mods.messaging.channel;

import net.minecraft.util.IChatComponent;
import pw.ipex.plex.Plex;

public class PlexMessagingPartyChatChannel extends PlexMessagingChannelBase {

	@Override
	public void channelInit() {
	}
	
	@Override
	public void chatMessage(IChatComponent message) {
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
