package pw.ipex.plex.mods.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pw.ipex.plex.core.PlexCoreChatRegex;
import pw.ipex.plex.core.PlexCoreChatRegexEntry;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexMessagingChatMessageAdapter {
	public Map<String, String> messageTags = new HashMap<String, String>();
	public String regexEntryName = "";
	public Class<? extends PlexMessagingChannelBase> channelClass;
	public PlexCoreChatRegexEntry regexEntry;
	public String contentFormatString = "";
	public String chatGroup = "";
	public String recipientEntityName = "";
	public String channelName = "";
	public String author;
	public List<PlexMessagingMessageClickCallback> callbacks = new ArrayList<PlexMessagingMessageClickCallback>();
	public int defaultMessageType = 1;
	public int defaultMessageSide = 0;
	
	public PlexMessagingChatMessageAdapter(String group, String regexEntryName, String formatString) {
		this.chatGroup = group;
		this.regexEntryName = regexEntryName;
		this.contentFormatString = formatString;
		this.regexEntry = PlexCoreChatRegex.getEntryNamed(regexEntryName);
	}
	
	public PlexMessagingChatMessageAdapter(String group, String regexEntryName, String formatString, String channelName) {
		this.chatGroup = group;
		this.regexEntryName = regexEntryName;
		this.contentFormatString = formatString;
		this.channelName = channelName;
		this.regexEntry = PlexCoreChatRegex.getEntryNamed(regexEntryName);
	}
	
	public String formatStringWithGroups(String stringToBeFormatted, String textMatchingRegex) {
		if (this.regexEntry == null || stringToBeFormatted == null || textMatchingRegex == null) {
			return "";
		}
		return this.regexEntry.formatStringWithGroups(textMatchingRegex, stringToBeFormatted);
	}
	
	public PlexMessagingChatMessageAdapter addCallback(PlexMessagingMessageClickCallback callback) {
		this.callbacks.add(callback);
		return this;
	}
	
	public PlexMessagingChatMessageAdapter addMessageTag(String key, String value) {
		this.messageTags.put(key, value);
		return this;
	}
	
	public PlexMessagingChatMessageAdapter setChannelClass(Class<? extends PlexMessagingChannelBase> channelClass) {
		this.channelClass = channelClass;
		return this;
	}
	
	public PlexMessagingChatMessageAdapter setAuthor(String author) {
		this.author = author;
		return this;
	}
	
	public PlexMessagingChatMessageAdapter setDefaultMessageType(int defaultType) {
		this.defaultMessageType = defaultType;
		return this;
	}
	
	public PlexMessagingChatMessageAdapter setDefaultMessagePosition(int defaultPos) {
		this.defaultMessageSide = defaultPos;
		return this;
	}
	

	public Class<? extends PlexMessagingChannelBase> getChannelClass() {
		return this.channelClass;
	}
	
	public String getMessageTag(String key, String text) {
		return this.formatStringWithGroups(this.messageTags.get(key), text);
	}
	
	public String getChannelName(String text) {
		return this.formatStringWithGroups(this.channelName, text);
	}
	
	public String getRecipientEntityName(String text) {
		return this.formatStringWithGroups(this.recipientEntityName, text);
	}
	
	public String getMessageContent(String text) {
		return this.formatStringWithGroups(this.contentFormatString, text);	
	}
	
	public String getAuthor(String text) {
		return this.formatStringWithGroups(this.author, text);
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
		message.type = this.defaultMessageType;
		message.position = this.defaultMessageSide;
		for (PlexMessagingMessageClickCallback callback : this.callbacks) {
			message.addCallback(callback);
		}
		for (String tag : this.messageTags.keySet()) {
			message.setTag(tag, this.getMessageTag(text, tag));
		}
		message.setContent(this.getMessageContent(text));
		String theAuthor = this.getAuthor(text);
		if (theAuthor != null) {
			message.fromUser = theAuthor;
		}
		return message;
	}
}
