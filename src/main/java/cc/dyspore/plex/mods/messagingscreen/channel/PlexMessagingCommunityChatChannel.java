package cc.dyspore.plex.mods.messagingscreen.channel;

import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.util.PlexUtilChat;
import cc.dyspore.plex.cq.PlexCommandQueue;
import cc.dyspore.plex.cq.PlexCommandQueueCommand;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class PlexMessagingCommunityChatChannel extends PlexMessagingChannelBase {

	public static PlexCommandQueue commandQueue = new PlexCommandQueue("communityChatChannel", Plex.queue);

	@Override
	public void channelInit() {
		commandQueue.setPriority(2);
		commandQueue.conditions
				.afterChatOpen(0)
				.afterLobbyChange(0)
				.afterLogon(500)
				.afterCommand(900);
	}
	
	@Override
	public String getDisplayName() {
		return this.recipientEntityName;
	}
	
	@Override
	public void chatMessage(ClientChatReceivedEvent event) {
		String condensedMessage = PlexUtilChat.chatMinimalizeLowercase(event.message.getFormattedText());
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
		return PlexUtilChat.colourCode.get(this.getTag("comColour"));
	}

	@Override
	public void readyChannel() {
		PlexCommandQueueCommand comCommand = new PlexCommandQueueCommand("communityChatChannel", "/com chat " + this.recipientEntityName);
		comCommand.setCompleteOnSend(true);
		commandQueue.cancelAll();
		commandQueue.addCommand(comCommand);
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
