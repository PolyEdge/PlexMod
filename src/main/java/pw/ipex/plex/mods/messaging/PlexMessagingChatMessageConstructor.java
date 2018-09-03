package pw.ipex.plex.mods.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.mods.messaging.channel.*;

public class PlexMessagingChatMessageConstructor {
	public static List<PlexMessagingChatMessageAdapter> messageHandlers = new ArrayList<PlexMessagingChatMessageAdapter>();
	public static Map<String, Class<? extends PlexMessagingChannelBase>> groupClassMapping = new HashMap<String, Class<? extends PlexMessagingChannelBase>>();
	
	static {
		addGroupChannelClass("party", PlexMessagingPartyChatChannel.class);
		addGroupChannelClass("team", PlexMessagingTeamChatChannel.class);
		addGroupChannelClass("community", PlexMessagingCommunityChatChannel.class);
		addGroupChannelClass("direct_message", PlexMessagingPrivateMessagesChannel.class);
		
		addAdapter("chatMessage", "party_chat", "{$message}", "@Party").setChannelClass(getGroupChannelClass("party")).setDefaultMessageType(0).setAuthor("{author}");
		addAdapter("chatMessage", "team_chat", "{$message}", "#Team").setChannelClass(getGroupChannelClass("team")).setDefaultMessageType(0).setAuthor("{author}");
		addAdapter("chatMessage", "community_chat", "{$message}", "!{community}").setChannelClass(getGroupChannelClass("community")).setDefaultMessageType(0).setAuthor("{author}").setRecipientEntityName("{community}").setChannelTag("comColour", "{com_name_colour}");
		addAdapter("chatMessage", "direct_message", "{$message}", "PM.{author}").setChannelClass(getGroupChannelClass("direct_message")).setDefaultMessageType(0).setAuthor("{author}");
		
		addAdapter("party", "party_invite", "&7Party invite from {sender}\n&a&lACCEPT  &c&lDENY", "@Party").addMessageTag("invitation_sender_ign", "{ign}");
		addAdapter("party", "party_invited", "&e{sender} &7has invited &e{invited_player} &7to the party.", "@Party");
		addAdapter("party", "party_join", "&e{ign} &7joined the party.", "@Party").condition("{ign} !equals " + PlexCore.getPlayerIGN());
		addAdapter("party", "party_left", "&e{ign} &7left the party.", "@Party");
		addAdapter("party", "party_leave", "&7You left your current party.", "@Party");
		addAdapter("party", "party_remove", "&e{ign} &7was removed from your party.", "@Party");
	}
	
	public static PlexMessagingChatMessageAdapter addAdapter(String group, String regexID, String formatString, String channelName) {
		PlexMessagingChatMessageAdapter adapter = new PlexMessagingChatMessageAdapter(group, regexID, formatString, channelName);
		if (groupClassMapping.containsKey(adapter.chatGroup)) {
			adapter.setChannelClass(groupClassMapping.get(adapter.chatGroup));
		}
		messageHandlers.add(adapter);
		return adapter;
	}
	
	public static void addGroupChannelClass(String groupName, Class<? extends PlexMessagingChannelBase> channelClass) {
		groupClassMapping.put(groupName, channelClass);
	}
	
	public static Class<? extends PlexMessagingChannelBase> getGroupChannelClass(String groupName) {
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
