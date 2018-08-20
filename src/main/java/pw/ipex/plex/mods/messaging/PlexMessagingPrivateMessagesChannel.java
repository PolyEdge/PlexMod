package pw.ipex.plex.mods.messaging;

import net.minecraft.util.IChatComponent;
import pw.ipex.plex.Plex;

public class PlexMessagingPrivateMessagesChannel extends PlexMessagingChannelBase {

	@Override
	public void channelInit() {
	}
	
	@Override
	public void chatMessage(IChatComponent message) {
	}
	
	@Override
	public String getDisplayName() {
		return "PMs - " + this.recipientEntityName;
	}

	@Override
	public void readyChannel() {
		this.setReady();
	}

	@Override
	public void sendMessage(String message) {
		Plex.minecraft.thePlayer.sendChatMessage("/s " + this.recipientEntityName + " " + message);

	}

	@Override
	public Integer getMaxMessageLength() {
		return 100 - ("/w " + this.recipientEntityName + " ").length();
	}
}
