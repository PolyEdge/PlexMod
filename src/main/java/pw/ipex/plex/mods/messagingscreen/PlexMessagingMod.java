package pw.ipex.plex.mods.messagingscreen;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.mineplex.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mod.PlexModBase;
import pw.ipex.plex.mods.messagingscreen.channel.PlexMessagingChannelBase;
import pw.ipex.plex.mods.messagingscreen.channel.PlexMessagingCommunityChatChannel;
import pw.ipex.plex.mods.messagingscreen.channel.PlexMessagingSingleEntityChannel;
import pw.ipex.plex.mods.messagingscreen.translate.PlexMessagingChatMessageAdapter;
import pw.ipex.plex.mods.messagingscreen.translate.PlexMessagingChatMessageConstructor;
import pw.ipex.plex.ui.widget.autocomplete.PlexUIAutoCompleteContainer;
import pw.ipex.plex.ui.widget.autocomplete.PlexUIAutoCompleteItem;

import java.util.ArrayList;
import java.util.List;

public class PlexMessagingMod extends PlexModBase {
	//private static ResourceLocation sendIcon = new ResourceLocation("PolyEdge_Plex", "chat/send.png");

	public static PlexMessagingChannelManager channelManager = new PlexMessagingChannelManager();
	public static KeyBinding toggleChatUI;
	public static KeyBinding quickChat;
	public static PlexUIAutoCompleteContainer autoCompleteContainer = new PlexUIAutoCompleteContainer();
	
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
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		String chatMessageContent = PlexCoreUtils.condenseChatFilter(event.message.getFormattedText());
		if (PlexCoreUtils.minimalize(event.message.getFormattedText()).startsWith("communities> you are now chatting to")) {
			channelManager.unreadyChannelsByClass(PlexMessagingCommunityChatChannel.class);
		}
		this.handleMessage(chatMessageContent);
	}

	public void refreshAutoCompleteList() {
		PlexUIAutoCompleteItem serverItem = autoCompleteContainer.getItemOrNew("server", "server_id");
		serverItem.setAutoCompleteText("null").setSearchText("@server").setDisplayText(PlexCoreUtils.ampersandToFormatCharacter("&dCurrent Server - &3" + "null")).setGlobalSortingIndex(0);
		autoCompleteContainer.addItem(serverItem);

		if (Plex.serverState.currentLobbyName != null) {
			serverItem.setAutoCompleteText(Plex.serverState.currentLobbyName).setDisplayText(PlexCoreUtils.ampersandToFormatCharacter("&dCurrent Server - &3" + Plex.serverState.currentLobbyName));
		}
		else {
			serverItem.setAutoCompleteText("").setDisplayText(PlexCoreUtils.ampersandToFormatCharacter("&dCurrent Server - &e" + "determining..."));
		}

		for (String emoteName : Plex.serverState.emotesList.keySet()) {
			PlexUIAutoCompleteItem emoteItem = autoCompleteContainer.getItemOrNew("emote", emoteName);
			emoteItem.setAutoCompleteText(":" + emoteName + ":").setSearchText(":" + emoteName + ":").setDisplayText(PlexCoreUtils.ampersandToFormatCharacter("&7:" + emoteName + ": &e" + Plex.serverState.emotesList.get(emoteName)));
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
		PlexMessagingChatMessageAdapter messageAdapter = PlexMessagingChatMessageConstructor.getAdapterForChatMessageWithRegexTag(chatMessage, "chatMessage");
		PlexMessagingMessage message = this.processChatMessageWithAdapter(chatMessage, messageAdapter);
		if (message == null) {
			return;
		}
		message.channel.addMessage(message);
	}
	
	public void handleOtherMessage(String chatMessage) {
		for (PlexMessagingChatMessageAdapter messageAdapter : PlexMessagingChatMessageConstructor.getAllAdaptersForChatMessage(chatMessage)) {
			if (messageAdapter.chatGroup.equals("chatMessage")) {
				continue;
			}
			PlexMessagingMessage message = this.processChatMessageWithAdapter(chatMessage, messageAdapter);
			if (message != null) {
				message.channel.addMessage(message);
				//channelManager.bumpChannelToTop(message.channel);
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
			if (PlexMessagingMod.channelManager.selectedChannel == null) {
				return null;
			}
			channel = PlexMessagingMod.channelManager.selectedChannel;
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
	
	public static PlexMessagingChannelBase getChannel(String name, Class<? extends PlexMessagingChannelBase> type) {
		return getChannel(name, type, null);
	}

	public static boolean channelExists(String name, Class<? extends PlexMessagingChannelBase> type) {
		return channelManager.getChannel(name) != null;
	}
	
	public static PlexMessagingChannelBase getChannel(String name, Class<? extends PlexMessagingChannelBase> type, String recipientEntityName) {
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
	public void lobbyUpdated(PlexCoreLobbyType type) {
		if (type.equals(PlexCoreLobbyType.E_SWITCHED_SERVERS)) {
			channelManager.unreadyChannelsByClass(PlexMessagingCommunityChatChannel.class);
			final PlexMessagingChannelManager finalManager = channelManager;
			if (finalManager.selectedChannel != null) {
				if (!finalManager.selectedChannel.awaitingReady && !finalManager.selectedChannel.channelReady) {
					finalManager.selectedChannel.getChannelReady();
				}
			}
		}
 	}
}
