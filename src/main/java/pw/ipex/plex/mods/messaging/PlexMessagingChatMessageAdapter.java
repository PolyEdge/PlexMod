package pw.ipex.plex.mods.messaging;

import java.util.ArrayList;
import java.util.List;

public class PlexMessagingChatMessageAdapter {
	public String regexEntryName = "";
	public String formatString = "";
	public String chatGroup = "";
	public String recipientEntityGroupName = null;
	public List<PlexMessagingMessageClickCallback> callbacks = new ArrayList<PlexMessagingMessageClickCallback>();
	public int defaultMessageType = 1;
	public int defaultMessageSide = 0;
	
	public PlexMessagingChatMessageAdapter(String group, String regexEntryName, String formatString) {
		this.chatGroup = group;
		this.regexEntryName = regexEntryName;
		this.formatString = formatString;
	}
	
	public PlexMessagingChatMessageAdapter(String group, String regexEntryName, String formatString, String recipientEntityGroupName) {
		this.chatGroup = group;
		this.regexEntryName = regexEntryName;
		this.formatString = formatString;
		this.recipientEntityGroupName = recipientEntityGroupName;
	}
}
