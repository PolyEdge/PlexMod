package pw.ipex.plex.mods.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.mods.messaging.callback.PlexMessagingMessageEventParty;
import pw.ipex.plex.mods.messaging.channel.*;

public class PlexMessagingChatMessageConstructor {
	public static List<PlexMessagingChatMessageAdapter> messageHandlers = new ArrayList<PlexMessagingChatMessageAdapter>();
	public static Map<String, PlexMessagingChannelClassWrapper> groupClassMapping = new HashMap<>();
	
	static {
		addGroupChannelClass("party", new PlexMessagingChannelClassWrapper(PlexMessagingPartyChatChannel.class, "@Party").setName("Party").setDescription("Type a name to invite them to the party.").setAutoCommand("/party invite {name}").setForegroundColour(0xffe820e8));
		addGroupChannelClass("team", new PlexMessagingChannelClassWrapper(PlexMessagingTeamChatChannel.class, "#Party").setName("Team").setDescription("Type below to use team chat (in-game only)").setForegroundColour(0xff4286f4).setAutoCommand("#{name}"));
		addGroupChannelClass("community", new PlexMessagingChannelClassWrapper(PlexMessagingCommunityChatChannel.class, "!{name}").setName("Community").setDescription("Enter a community name below to chat in it.").setRecipientEntityName("{name}"));
		addGroupChannelClass("direct_message", new PlexMessagingChannelClassWrapper(PlexMessagingPrivateMessagesChannel.class, "PM.{name}").setName("PMs").setDescription("Type a player name below to private message them.").setRecipientEntityName("{name}").setForegroundColour(0xffffaa00));
		
		addAdapter("chatMessage", "party_chat", "{$message}", "@Party").setChannelClass(getGroupChannelClass("party").channelClass).setDefaultMessageType(0).setAuthor("{author}");
		addAdapter("chatMessage", "team_chat", "{$message}", "#Team").setChannelClass(getGroupChannelClass("team").channelClass).setDefaultMessageType(0).setAuthor("{author}");
		addAdapter("chatMessage", "community_chat", "{$message}", "!{community}").setChannelClass(getGroupChannelClass("community").channelClass).setDefaultMessageType(0).setAuthor("{author}").setRecipientEntityName("{community}").setChannelTag("comColour", "{com_name_colour}").setUpdatesRecipientEntityNameCase(true);
		addAdapter("chatMessage", "direct_message", "{$message}", "PM.{author}").setChannelClass(getGroupChannelClass("direct_message").channelClass).setDefaultMessageType(0).setAuthor("{author}").setUpdatesRecipientEntityNameCase(true);

		addAdapter("party", "party_create", "&7You created a new party.", "@Party");
		addAdapter("party", "party_invite", "&7Party invite from &e{sender}&7:\n{{ACCEPT_BUTTON|&a&lACCEPT}}  {{DENY_BUTTON|&c&lDENY}}", "@Party").addMessageTag("invitation_sender_ign", "{sender}").setUsesFormatRegions(true).addCallback(new PlexMessagingMessageEventParty());
		addAdapter("party", "party_invited", "&e{sender} &7has invited &e{invited_player} &7to the party.", "@Party");
		addAdapter("party", "party_join", "&e{ign} &7joined the party.", "@Party").condition("{ign} !equals " + PlexCore.getPlayerIGN());
		addAdapter("party", "party_join", "&e{ign} &7joined the party.", "@Party").condition("{ign} equals " + PlexCore.getPlayerIGN()).setChannelOpenedRequired(true);
		addAdapter("party", "party_left", "&e{ign} &7left the party.", "@Party");
		addAdapter("party", "party_leave", "&7You left your current party.", "@Party");
		addAdapter("party", "party_remove", "&e{ign} &7was removed from your party.", "@Party");
		addAdapter("party", "party_offline", "&7Failed to invite &e{ign} &7because they are offline.", "@Party");

		addAdapter("direct_message", "direct_message_player_offline", "&7Failed to send message to &e{destination} &7because they are offline.", "PM.{destination}").setChannelOpenedRequired(true);
	}
	
	public static PlexMessagingChatMessageAdapter addAdapter(String group, String regexID, String formatString, String channelName) {
		PlexMessagingChatMessageAdapter adapter = new PlexMessagingChatMessageAdapter(group, regexID, formatString, channelName);
		if (groupClassMapping.containsKey(adapter.chatGroup)) {
			adapter.setChannelClass(groupClassMapping.get(adapter.chatGroup).channelClass);
		}
		messageHandlers.add(adapter);
		return adapter;
	}
	
	public static void addGroupChannelClass(String groupName, PlexMessagingChannelClassWrapper channelClass) {
		groupClassMapping.put(groupName, channelClass);
	}
	
	public static PlexMessagingChannelClassWrapper getGroupChannelClass(String groupName) {
		return groupClassMapping.get(groupName);
	}
	
	public static PlexMessagingChatMessageAdapter getAdapterForChatMessage(String chatMessage) {
		for (PlexMessagingChatMessageAdapter adapter : messageHandlers) {
			if (adapter.matchesMessage(chatMessage)) {
				return adapter;
			}
		}
		return null;
	}
	
	public static PlexMessagingChatMessageAdapter getAdapterForChatMessageWithRegexTag(String chatMessage, String tag) {
		for (PlexMessagingChatMessageAdapter adapter : messageHandlers) {
			if (adapter.matchesMessage(chatMessage) && adapter.regexEntryHasTag(tag)) {
				return adapter;
			}
		}
		return null;
	}
	
	public static List<PlexMessagingChatMessageAdapter> getAllAdaptersForChatMessage(String chatMessage) {
		List<PlexMessagingChatMessageAdapter> adapters = new ArrayList<PlexMessagingChatMessageAdapter>();
		for (PlexMessagingChatMessageAdapter adapter : messageHandlers) {
			if (adapter.matchesMessage(chatMessage)) {
				adapters.add(adapter);
			}
		}
		return adapters;
	}
	
	public static List<PlexMessagingChatMessageAdapter> getAllAdaptersForChatMessageWithRegexTag(String chatMessage, String tag) {
		List<PlexMessagingChatMessageAdapter> adapters = new ArrayList<PlexMessagingChatMessageAdapter>();
		for (PlexMessagingChatMessageAdapter adapter : messageHandlers) {
			if (adapter.matchesMessage(chatMessage) && adapter.regexEntryHasTag(tag)) {
				adapters.add(adapter);
			}
		}
		return adapters;
	}
	
	public static List<PlexMessagingChatMessageAdapter> adaptersWithGroup(String group) {
		List<PlexMessagingChatMessageAdapter> adapters = new ArrayList<PlexMessagingChatMessageAdapter>();
		for (PlexMessagingChatMessageAdapter adapter : messageHandlers) {
			if (adapter.chatGroup.equals(group)) {
				adapters.add(adapter);
			}
		}
		return adapters;
	}
}
