package pw.ipex.plex.mods.messaging.channel;

import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexMessagingPrivateMessagesChannel extends PlexMessagingChannelBase {

	@Override
	public void channelInit() {
	}
	
	@Override
	public void chatMessage(IChatComponent message) {
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
		return PlexCoreUtils.getSkin(this.recipientEntityName);
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
