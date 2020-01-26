package cc.dyspore.plex.mods.messagingscreen.channel;

import cc.dyspore.plex.core.util.PlexUtilMojang;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import cc.dyspore.plex.Plex;

public class PlexMessagingPrivateMessagesChannel extends PlexMessagingChannelBase {

	@Override
	public void channelInit() {
	}
	
	@Override
	public void chatMessage(ClientChatReceivedEvent event) {
	}
	
	@Override
	public String getDisplayName() {
		return this.recipientEntityName;
	}

	@Override
	public void readyChannel() {
		this.setReady();
	}
	
	@Override
	public Integer getDisplayColour() {
		return 0xffffaa00;
	}
	
	@Override
	public ResourceLocation getAttachedPlayerHead() {
		if (this.recipientEntityName == null) {
			return null;
		}
		if (this.recipientEntityName.trim().equals("")) {
			return null;
		}
		return PlexUtilMojang.getSkin(this.recipientEntityName);
	}

	@Override
	public void sendMessage(String message) {
		Plex.minecraft.thePlayer.sendChatMessage("/w " + this.recipientEntityName + " " + message);

	}

	@Override
	public Integer getMaxMessageLength() {
		return 100 - ("/w " + this.recipientEntityName + " ").length();
	}
}
