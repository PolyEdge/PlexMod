package pw.ipex.plex.mods.messagingscreen.channel;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.cq.PlexCommandQueue;
import pw.ipex.plex.cq.PlexCommandQueueCommand;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexMessagingCommunityChatChannel extends PlexMessagingChannelBase {

	public PlexCommandQueue commandQueue = new PlexCommandQueue("communityChatChannel", Plex.plexCommandQueue);

	@Override
	public void channelInit() {
		this.commandQueue.delaySet.chatOpenDelay = -1000L;
		this.commandQueue.delaySet.lobbySwitchDelay = 0L;
		this.commandQueue.delaySet.joinServerDelay = 500L;
		this.commandQueue.delaySet.commandDelay = 900L;
	}
	
	@Override
	public String getDisplayName() {
		return this.recipientEntityName;
	}
	
	@Override
	public void chatMessage(ClientChatReceivedEvent event) {
		String condensedMessage = PlexCoreUtils.minimalize(event.message.getFormattedText());
		if (condensedMessage.startsWith("communities> you are now chatting to " + this.recipientEntityName.toLowerCase())) {
			this.setReady();
			event.setCanceled(true);
		}
		else if (condensedMessage.startsWith("communities> that community was not found")) {
			this.setError();
			event.setCanceled(true);
		}
	}
	
	@Override
	public Integer getDisplayColour() {
		if (this.getTag("comColour") == null) {
			return 0xffffffff;
		}
		return PlexCoreUtils.colourCode.get(this.getTag("comColour"));
	}

	@Override
	public void readyChannel() {
		PlexCommandQueueCommand comCommand = new PlexCommandQueueCommand("communityChatChannel", "/com chat " + this.recipientEntityName);
		comCommand.setCompleteOnSend(true);
		this.commandQueue.addCommand(comCommand);
	}

	@Override
	public void sendMessage(String message) {
		Plex.minecraft.thePlayer.sendChatMessage("!" + message);

	}

	@Override
	public Integer getMaxMessageLength() {
		return 99;
	}
}
