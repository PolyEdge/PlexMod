package cc.dyspore.plex.mods.messagingscreen;

import cc.dyspore.plex.core.mineplex.PlexLobbyType;
import cc.dyspore.plex.core.util.PlexUtilChat;
import cc.dyspore.plex.mods.messagingscreen.channel.PlexMessagingChannelBase;
import cc.dyspore.plex.mods.messagingscreen.translate.PlexMessagingChatMessageAdapter;
import cc.dyspore.plex.ui.widget.autocomplete.PlexUIAutoCompleteContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.PlexModBase;
import cc.dyspore.plex.mods.messagingscreen.channel.PlexMessagingCommunityChatChannel;
import cc.dyspore.plex.mods.messagingscreen.channel.PlexMessagingSingleEntityChannel;
import cc.dyspore.plex.mods.messagingscreen.translate.PlexMessagingChatMessageConstructor;
import cc.dyspore.plex.ui.widget.autocomplete.PlexUIAutoCompleteItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlexMessagingMod extends PlexModBase {
	//private static ResourceLocation sendIcon = new ResourceLocation("PolyEdge_Plex", "chat/send.png");

	public PlexMessagingChannelManager channelManager = new PlexMessagingChannelManager();
	public KeyBinding toggleChatUI;
	public KeyBinding quickChat;
	public PlexUIAutoCompleteContainer autoCompleteContainer = new PlexUIAutoCompleteContainer();
	
	@Override
	public String getModName() {
		return "Direct Messaging";
	}
	
	@Override
	public void modInit() {
	    toggleChatUI = new KeyBinding("Open Chat UI", 157, "Plex Mod");
		quickChat = new KeyBinding("Quick Chat UI", 21, "Plex Mod");
	    ClientRegistry.registerKeyBinding(toggleChatUI);
		ClientRegistry.registerKeyBinding(quickChat);
	    
	    PlexCore.registerUiTab("Messaging", PlexMessagingUIScreen.class);
	}

	@Override
	public void saveModConfig() {
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onChat(ClientChatReceivedEvent event) {
		String chatMessageContent = event.message.getFormattedText();
		if (PlexUtilChat.chatMinimalizeLowercase(event.message.getFormattedText()).startsWith("communities> you are now chatting to")) {
			this.channelManager.unreadyChannelsByClass(PlexMessagingCommunityChatChannel.class, true);
		}
		this.channelManager.chatEvent(event);
		this.handleMessage(chatMessageContent);
	}

	public void refreshAutoCompleteList() {
		PlexUIAutoCompleteItem serverItem = autoCompleteContainer.getItemOrNew("server", "server_id");
		serverItem.setAutoCompleteText("null").setSearchText("@server").setDisplayText(PlexUtilChat.chatFromAmpersand("&dCurrent Server - &3" + "null")).setGlobalSortingIndex(0);
		autoCompleteContainer.addItem(serverItem);

		if (Plex.gameState.currentLobby.name != null) {
			serverItem.setAutoCompleteText(Plex.gameState.currentLobby.name).setDisplayText(PlexUtilChat.chatFromAmpersand("&dCurrent Server - &3" + Plex.gameState.currentLobby.name));
		}
		else {
			serverItem.setAutoCompleteText("").setDisplayText(PlexUtilChat.chatFromAmpersand("&dCurrent Server - &e" + "determining..."));
		}

		for (String emoteName : Plex.gameState.emotesList.keySet()) {
			PlexUIAutoCompleteItem emoteItem = autoCompleteContainer.getItemOrNew("emote", emoteName);
			emoteItem.setAutoCompleteText(":" + emoteName + ":").setSearchText(":" + emoteName + ":").setDisplayText(PlexUtilChat.chatFromAmpersand("&7:" + emoteName + ": &e" + Plex.gameState.emotesList.get(emoteName)));
			autoCompleteContainer.addItem(emoteItem);
		}

		List<String> playerNameList = PlexCore.getPlayerIGNTabList();
		for (String playerName : playerNameList) {
			PlexUIAutoCompleteItem playerItem = autoCompleteContainer.getItemOrNew("tabPlayer", playerName);
			playerItem.setAutoCompleteText(playerName).setSearchText(playerName).setDisplayText(playerName).setHead(playerName).setGlobalSortingIndex(10);
			autoCompleteContainer.addItem(playerItem);
		}

		List<PlexUIAutoCompleteItem> removeItems = new ArrayList<>();
		for (PlexUIAutoCompleteItem item : autoCompleteContainer.getItemsByGroup("tabPlayer")) {
			if (!playerNameList.contains(item.id)) {
				removeItems.add(item);
			}
		}
		autoCompleteContainer.autoCompleteItems.removeAll(removeItems);
		autoCompleteContainer.sortItems();
	}
	
	public void handleMessage(String message) {
		this.handleChatMessasge(message);
		this.handleOtherMessage(message);
	}
	
	public void handleChatMessasge(String chatMessage) {
		List<PlexMessagingChatMessageAdapter> messageAdapters = PlexMessagingChatMessageConstructor.getAdaptersForChatMessageWithRegexTag(chatMessage, "chatMessage");
		for (PlexMessagingChatMessageAdapter adapter : messageAdapters) {
			PlexMessagingMessage message = this.processChatMessageWithAdapter(chatMessage, adapter);
			if (message == null) {
				continue;
			}
			message.channel.addMessage(message);
		}
	}
	
	public void handleOtherMessage(String chatMessage) {
		for (PlexMessagingChatMessageAdapter messageAdapter : PlexMessagingChatMessageConstructor.getAllAdaptersForChatMessage(chatMessage)) {
			if (messageAdapter.chatGroup.equals("chatMessage")) {
				continue;
			}
			PlexMessagingMessage message = this.processChatMessageWithAdapter(chatMessage, messageAdapter);
			if (message != null) {
				message.channel.addMessage(message);
				//this.channelManager.bumpChannelToTop(message.channel);
			}
		}
	}
	
	public PlexMessagingMessage processChatMessageWithAdapter(String chatMessage, PlexMessagingChatMessageAdapter messageAdapter) {
		if (messageAdapter == null) {
			return null;
		}

		String channelName = messageAdapter.getChannelName(chatMessage);
		Class<? extends PlexMessagingChannelBase> channelClass = messageAdapter.getChannelClass();
		String recipientEntityName = messageAdapter.getRecipientEntityName(chatMessage);

		if (!messageAdapter.meetsConditions(chatMessage)) {
			return null;
		}

		if (messageAdapter.regexEntryName.equals("direct_message")) {
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

		PlexMessagingChannelBase channel;
		if (channelName == null) { // null when sending to open channel
			if (this.channelManager.selectedChannel == null) {
				return null;
			}
			channel = this.channelManager.selectedChannel;
		}
		else {
			if (!channelExists(channelName, channelClass) && !PlexMessagingSingleEntityChannel.class.isAssignableFrom(channelClass) && (recipientEntityName == null || recipientEntityName.equals(""))) {
				return null;
			}
			if (!channelExists(channelName, channelClass) && messageAdapter.requiresChannelExists) {
				return null;
			}

			channel = getChannel(channelName, channelClass, recipientEntityName);
			if (!channel.recipientEntityName.equals(recipientEntityName) && channel.recipientEntityName.equalsIgnoreCase(recipientEntityName) && messageAdapter.updateRecipientEntityNameCase) {
				channel.recipientEntityName = recipientEntityName;
			}
		}

		messageAdapter.applyChannelTags(chatMessage, channel);
		if (!messageAdapter.meetsRequirements(PlexMessagingUIScreen.isChatOpen(), PlexMessagingMod.this.channelManager.selectedChannel, channel)) {
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
		return getChannel(name, type, null);
	}

	public boolean channelExists(String name, Class<? extends PlexMessagingChannelBase> type) {
		return this.channelManager.getChannel(name) != null;
	}
	
	public PlexMessagingChannelBase getChannel(String name, Class<? extends PlexMessagingChannelBase> type, String recipientEntityName) {
		if (recipientEntityName == null) {
			recipientEntityName = "";
		}
		if (this.channelManager.getChannel(name) == null) {
			PlexMessagingChannelBase channel;
			try {
				channel = type.newInstance();
				channel.setName(name);
				channel.setRecipientEntityName(recipientEntityName);
				this.channelManager.addChannel(channel);
			} 
			catch (InstantiationException | IllegalAccessException e) {}
		}
		return this.channelManager.getChannel(name);
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if (Plex.minecraft.inGameHasFocus && toggleChatUI.isPressed()) {
			PlexCore.displayUIScreen(new PlexMessagingUIScreen());
		}
		if (Plex.minecraft.inGameHasFocus && quickChat.isPressed()) {
			PlexCore.displayUIScreen(new PlexMessagingUIScreen().setQuickChat());
		}
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (PlexMessagingUIScreen.isChatOpen()) {
			this.refreshAutoCompleteList();
		}
	}

	@Override
	public void lobbyUpdated(PlexLobbyType type) {
		if (type.equals(PlexLobbyType.E_LOBBY_SWITCH)) {
			this.channelManager.unreadyChannelsByClass(PlexMessagingCommunityChatChannel.class, false);
			final PlexMessagingChannelManager finalManager = this.channelManager;
			if (finalManager.selectedChannel != null) {
				if (!finalManager.selectedChannel.awaitingReady && !finalManager.selectedChannel.channelReady) {
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							finalManager.selectedChannel.getChannelReady();
						}
					}, 500L);
				}
			}
		}
 	}
}
