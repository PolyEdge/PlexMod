package pw.ipex.plex.mods.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pw.ipex.plex.core.PlexCoreChatRegex;
import pw.ipex.plex.core.PlexCoreChatRegexEntry;

public class PlexMessagingChatMessageAdapter {
	public Map<String, String> messageTags = new HashMap<String, String>();
	public String regexEntryName = "";
	public Class<? extends PlexMessagingChannelBase> channelClass;
	public PlexCoreChatRegexEntry regexEntry;
	public String formatString = "";
	public String chatGroup = "";
	public String recipientEntityName = "";
	public String channelName;
	public List<PlexMessagingMessageClickCallback> callbacks = new ArrayList<PlexMessagingMessageClickCallback>();
	public int defaultMessageType = 1;
	public int defaultMessageSide = 0;
	
	public PlexMessagingChatMessageAdapter(String group, String regexEntryName, String formatString) {
		this.chatGroup = group;
		this.regexEntryName = regexEntryName;
		this.formatString = formatString;
		this.regexEntry = PlexCoreChatRegex.getEntryNamed(regexEntryName);
	}
	
	public PlexMessagingChatMessageAdapter(String group, String regexEntryName, String formatString, String channelName) {
		this.chatGroup = group;
		this.regexEntryName = regexEntryName;
		this.formatString = formatString;
		this.channelName = channelName;
		this.regexEntry = PlexCoreChatRegex.getEntryNamed(regexEntryName);
	}
	
	public PlexMessagingChatMessageAdapter addCallback(PlexMessagingMessageClickCallback callback) {
		this.callbacks.add(callback);
		return this;
	}
	
	public PlexMessagingChatMessageAdapter setChannelClass(Class<? extends PlexMessagingChannelBase> channelClass) {
		this.channelClass = channelClass;
		return this;
	}
	
	public Class<? extends PlexMessagingChannelBase> getChannelClass() {
		return this.channelClass;
	}
	
	public PlexMessagingChatMessageAdapter setDefaultMessageType(int defaultType) {
		this.defaultMessageType = defaultType;
		return this;
	}
	
	public PlexMessagingChatMessageAdapter addTag(String key, String value) {
		this.messageTags.put(key, value);
		return this;
	}
	
	public String getTag(String text, String key) {
		if (this.regexEntry == null) {
			return "";
		}
		return this.regexEntry.formatStringWithGroups(text, key);
	}
	
	public String getChannelName(String text) {
		if (this.regexEntry == null) {
			return "";
		}
		return this.regexEntry.formatStringWithGroups(text, this.channelName);
	}
	
	public String getRecipientEntityName(String text) {
		if (this.regexEntry == null) {
			return "";
		}
		return this.regexEntry.formatStringWithGroups(text, this.recipientEntityName);
	}
	
	public boolean matchesMessage(String message) {
		if (this.regexEntry == null) {
			return false;
		}
		return this.regexEntry.matches(message);
	}
	
	public boolean regexEntryHasTag(String tag) {
		if (this.regexEntry == null) {
			return false;
		}
		return this.regexEntry.hasTag(tag);
	}
	
	public PlexMessagingMessage getIncompleteMessageFromText(String text) {
		if (!this.matchesMessage(text)) {
			return null;
		}
		PlexMessagingMessage message = new PlexMessagingMessage();
		return null; // TODO: finish this
	}
}
