package pw.ipex.plex.mods.messaging;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mod.PlexModBase;
import pw.ipex.plex.mods.messaging.channel.PlexMessagingChannelBase;

public class PlexMessagingMod extends PlexModBase {
	//private static ResourceLocation sendIcon = new ResourceLocation("PolyEdge_Plex", "chat/send.png");

	public static PlexMessagingChannelManager channelManager = new PlexMessagingChannelManager();
	public static KeyBinding toggleChatUI;
	
	@Override
	public String getModName() {
		return "Direct Messaging";
	}
	
	@Override
	public void modInit() {
	    toggleChatUI = new KeyBinding("Open Chat UI", 157, "Plex Mod");
	    ClientRegistry.registerKeyBinding(toggleChatUI);
	    
	    PlexCore.registerUiTab("Messaging", PlexMessagingUIScreen.class);
	}

	@Override
	public void saveModConfig() {
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		String chatMessageContent = PlexCoreUtils.condenseChatFilter(event.message.getFormattedText());
		this.handleMessage(chatMessageContent);
	}
	
	public void handleMessage(String message) {
		this.handleChatMessasge(message);
		this.handleOtherMessage(message);
	}
	
	public void handleChatMessasge(String chatMessage) {
		PlexMessagingChatMessageAdapter messageAdapter = PlexMessagingChatMessageConstructor.getAdapterForChatMessageWithRegexTag(chatMessage, "chatMessage");
		PlexMessagingMessage message = this.processChatMessageWithAdapter(chatMessage, messageAdapter);
		if (message == null) {
			return;
		}
		message.channel.addAgressiveMessage(message);
		channelManager.bumpChannelToTop(message.channel);
	}
	
	public void handleOtherMessage(String chatMessage) {
		for (PlexMessagingChatMessageAdapter messageAdapter : PlexMessagingChatMessageConstructor.getAllAdaptersForChatMessage(chatMessage)) {
			if (messageAdapter.chatGroup.equals("chatMessage")) {
				continue;
			}
			PlexMessagingMessage message = this.processChatMessageWithAdapter(chatMessage, messageAdapter);
			if (message != null) {
				message.channel.addAgressiveMessage(message);
				//channelManager.bumpChannelToTop(message.channel);
			}
		}
	}
	
	public PlexMessagingMessage processChatMessageWithAdapter(String chatMessage, PlexMessagingChatMessageAdapter messageAdapter) {
		if (messageAdapter == null) {
			return null;
		}
		String channelName = messageAdapter.getChannelName(chatMessage);
		String recipientEntityName = messageAdapter.getRecipientEntityName(chatMessage);
		if (channelName == null) {
			if (PlexMessagingMod.channelManager.selectedChannel == null) {
				return null;
			}
			channelName = PlexMessagingMod.channelManager.selectedChannel.name;
		}
		else if (messageAdapter.regexEntryName.equals("direct_message")) {
			recipientEntityName = messageAdapter.formatStringWithGroups("{author}", chatMessage);
			channelName = messageAdapter.formatStringWithGroups("{author}", chatMessage);
			if (recipientEntityName.equalsIgnoreCase(PlexCore.getPlayerIGN())) {
				recipientEntityName = messageAdapter.formatStringWithGroups("{destination}", chatMessage);
			}
			if (channelName.equalsIgnoreCase(PlexCore.getPlayerIGN())) {
				channelName = messageAdapter.formatStringWithGroups("{destination}", chatMessage);
			}	
			channelName = "PM." + channelName;
		}
		PlexMessagingChannelBase channel = getChannel(channelName, messageAdapter.getChannelClass(), recipientEntityName);
		if (!messageAdapter.meetsRequirements(PlexMessagingUIScreen.isChatOpen(), PlexMessagingMod.channelManager.selectedChannel, channel)) {
			return null;
		}
		PlexMessagingMessage message = messageAdapter.getIncompleteMessageFromText(chatMessage).setNow().setHead(messageAdapter.formatStringWithGroups("{author}", chatMessage));
		if (messageAdapter.formatStringWithGroups("{author}", chatMessage).equals(PlexCore.getPlayerIGN())) {
			message.setRight();
		}
		message.setChannel(channel);
		return message;
	}
	
	public PlexMessagingChannelBase getChannel(String name, Class<? extends PlexMessagingChannelBase> type) {
		return this.getChannel(name, type, null);
	}
	
	public PlexMessagingChannelBase getChannel(String name, Class<? extends PlexMessagingChannelBase> type, String recipientEntityName) {
		if (recipientEntityName == null) {
			recipientEntityName = "";
		}
		if (channelManager.getChannel(name) == null) {
			PlexMessagingChannelBase channel;
			try {
				channel = type.newInstance();
				channel.setName(name);
				channel.setRecipientEntityName(recipientEntityName);
				channelManager.addChannel(channel);
			} 
			catch (InstantiationException | IllegalAccessException e) {}
		}
		return channelManager.getChannel(name);
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if (Plex.minecraft.inGameHasFocus && toggleChatUI.isPressed()) {
			PlexCore.displayUIScreen(new PlexMessagingUIScreen());
		}
	}
	
	@Override
	public void switchedLobby(PlexCoreLobbyType type) {
	}

}
