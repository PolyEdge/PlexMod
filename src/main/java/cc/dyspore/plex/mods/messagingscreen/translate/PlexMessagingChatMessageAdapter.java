package cc.dyspore.plex.mods.messagingscreen.translate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.dyspore.plex.mods.messagingscreen.callback.PlexMessagingMessageEventHandler;
import cc.dyspore.plex.mods.messagingscreen.channel.PlexMessagingChannelBase;
import cc.dyspore.plex.core.regex.PlexCoreRegex;
import cc.dyspore.plex.core.regex.PlexCoreRegexEntry;
import cc.dyspore.plex.mods.messagingscreen.PlexMessagingMessage;

public class PlexMessagingChatMessageAdapter {
	public Map<String, String> messageTags = new HashMap<String, String>();
	public Map<String, String> channelFields = new HashMap<String, String>();
	public String regexEntryName;
	public Class<? extends PlexMessagingChannelBase> channelClass;
	public PlexCoreRegexEntry regexEntry;
	public String contentFormatString;
	public String chatGroup;
	public String recipientEntityName = null;
	public String channelName;
	public String author;
	public boolean requiresChannelOpen = false;
	public boolean requiresChatOpen = false;
	public boolean requiresChannelExists = false;
	public boolean sendToSelectedChannel = false;
	public boolean enableFormatRegions = false;
	public boolean updateRecipientEntityNameCase = false;
	public boolean bumpsChannelActivity = false;
	public boolean countsTowardsUnread = true;
	public List<PlexMessagingMessageEventHandler> callbacks = new ArrayList<PlexMessagingMessageEventHandler>();
	public List<String> conditions = new ArrayList<String>();
	public int defaultMessageType = 1;
	public int defaultMessageSide = 0;
	public String MATCH_CONDITION = "(.+?) (!?)(startswith|endswith|startswithcase|endswithcase|equals|equalscase|contains(?:\\.[0-9]+)?|char\\.[0-9]+|regex) (.+)";
	public String MATCH_DEFAULT_BREAKDOWN = "([^ ]+ ?)";
	public Pattern PATTERN_CONDITION = Pattern.compile(MATCH_CONDITION);
	public Pattern PATTERN_DEFAULT_BREAKDOWN = Pattern.compile(MATCH_DEFAULT_BREAKDOWN);

	public PlexMessagingChatMessageAdapter(String group, String regexEntryName, String formatString, String channelName) {
		this.chatGroup = group;
		this.regexEntryName = regexEntryName;
		this.contentFormatString = formatString;
		this.channelName = channelName;
		this.regexEntry = PlexCoreRegex.getEntryNamed(regexEntryName);
	}
	
	public String formatStringWithGroups(String stringToBeFormatted, String textMatchingRegex) {
		if (this.regexEntry == null || stringToBeFormatted == null || textMatchingRegex == null) {
			return "";
		}
		return this.regexEntry.formatStringWithGroups(textMatchingRegex, stringToBeFormatted);
	}
	
	public PlexMessagingChatMessageAdapter addCallback(PlexMessagingMessageEventHandler callback) {
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
	
	public PlexMessagingChatMessageAdapter setSendToSelectedChannel(boolean send) {
		this.sendToSelectedChannel = send;
		return this;
	}
	
	public PlexMessagingChatMessageAdapter setAuthor(String author) {
		this.author = author;
		return this;
	}
	
	public PlexMessagingChatMessageAdapter setRecipientEntityName(String recipient) {
		this.recipientEntityName = recipient;
		return this;
	}

	public PlexMessagingChatMessageAdapter setUpdatesRecipientEntityNameCase(boolean update) {
		this.updateRecipientEntityNameCase = update;
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
	
	public PlexMessagingChatMessageAdapter setChannelOpenedRequired(boolean required) {
		this.requiresChannelOpen = required;
		return this;
	}

	public PlexMessagingChatMessageAdapter setChannelExistsRequired(boolean required) {
		this.requiresChannelExists = required;
		return this;
	}
	
	public PlexMessagingChatMessageAdapter setChatOpenedRequired(boolean required) {
		this.requiresChatOpen = required;
		return this;
	}

	public PlexMessagingChatMessageAdapter setUsesFormatRegions(boolean enable) {
		this.enableFormatRegions = enable;
		return this;
	}

	public PlexMessagingChatMessageAdapter setCountsAsUnread(boolean counts) {
		this.countsTowardsUnread = counts;
		return this;
	}

	public PlexMessagingChatMessageAdapter setBumpsChannelActivity(boolean bumps) {
		this.bumpsChannelActivity = bumps;
		return this;
	}

	public PlexMessagingChatMessageAdapter setChannelTag(String tag, String value) {
		this.channelFields.put(tag, value);
		return this;
	}
	
	public PlexMessagingChatMessageAdapter condition(String condition) {
		this.conditions.add(condition);
		return this;
	}

	public void applyChannelTags(String text, PlexMessagingChannelBase channel) {
		for (String tagKey : this.channelFields.keySet()) {
			channel.addTag(tagKey, this.formatStringWithGroups(this.channelFields.get(tagKey), text));
		}
	}

	public Class<? extends PlexMessagingChannelBase> getChannelClass() {
		if (this.sendToSelectedChannel) {
			return null;
		}
		return this.channelClass;
	}
	
	public String getMessageTag(String key, String text) {
		//PlexUtil.chatAddMessage(key + "> " + text + " > " + this.messageTags.get(key) + " > " + this.formatStringWithGroups(this.messageTags.get(key), text));
		return this.formatStringWithGroups(this.messageTags.get(key), text);
	}
	
	public String getChannelName(String text) {
		if (this.sendToSelectedChannel) {
			return null;
		}
		return this.formatStringWithGroups(this.channelName, text);
	}
	
	public String getRecipientEntityName(String text) {
		if (this.recipientEntityName == null) {
			return null;
		}
		return this.formatStringWithGroups(this.recipientEntityName, text);
	}
	
	public String getMessageContent(String text) {
		if (this.enableFormatRegions) {
			List<String> regions = PlexCoreRegex.splitFormatRegionString(this.contentFormatString);
			String output = "";
			for (String region : regions) {
				if (region.startsWith("$")) {
					output = output + this.formatStringWithGroups(region.substring(1), text);
				}
				else if (region.startsWith("!")) {
					output = output + this.formatStringWithGroups(region.substring(1).split(Pattern.quote("|"), 2)[1], text);
				}
			}
			//PlexUtil.chatAddMessage("cmbuilder \"" + output.replace("\n", "{NL}") + "\"");
			return output;
		}
		return this.formatStringWithGroups(this.contentFormatString, text);
	}

	public Map<Integer, String> getMessageBreakdown(String text) {
		List<Map.Entry<String, String>> splitBreakdown = new ArrayList<>();
		if (this.enableFormatRegions) {
			for (String region : PlexCoreRegex.splitFormatRegionString(this.contentFormatString)) {
				if (region.startsWith("$")) {
					splitBreakdown.add(new AbstractMap.SimpleEntry<>(region, this.formatStringWithGroups(region.substring(1), text)));
				}
				else if (region.startsWith("!")) {
					splitBreakdown.add(new AbstractMap.SimpleEntry<>(region.split("\\|", 2)[0], this.formatStringWithGroups(region.substring(1).split("\\|", 2)[1], text)));
				}
			}
		}
		else {
			Matcher matcher = PATTERN_DEFAULT_BREAKDOWN.matcher(this.contentFormatString);
			while (matcher.find()) {
				splitBreakdown.add(new AbstractMap.SimpleEntry<>("$" + matcher.group(1), this.formatStringWithGroups(matcher.group(1), text)));
			}
		}
		Map<Integer, String> breakdown = new LinkedHashMap<>();
		int pos = 0;
		for (Map.Entry<String, String> region : splitBreakdown) {
			//PlexUtil.chatAddMessage("bdbuilder \"" + region.getKey().replace("\n", "{NL}") + "\" >> \"" + region.getValue().replace("\n", "{NL}") + "\"");
			breakdown.put(pos, region.getKey());
			pos += region.getValue().length();
		}
		return breakdown;
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
	
	public boolean meetsCondition(String condition, String text) {
		condition = this.formatStringWithGroups(condition, text);
		Matcher matcher = PATTERN_CONDITION.matcher(condition);
		matcher.find();
		Boolean meetsCondition = null;
		String item1 = matcher.group(1).trim();
		String item2 = matcher.group(4).trim();
		String operator = matcher.group(3);
		if (operator.equals("startswith")) {
			meetsCondition = item1.toLowerCase().startsWith(item2.toLowerCase());
		}
		else if (operator.equals("startswithcase")) {
			meetsCondition = item1.startsWith(item2);
		}
		else if (operator.equals("endswith")) {
			meetsCondition = item1.toLowerCase().endsWith(item2.toLowerCase());
		}
		else if (operator.equals("endsswithcase")) {
			meetsCondition = item1.endsWith(item2);
		}
		else if (operator.equals("equals")) {
			meetsCondition = item1.toLowerCase().equals(item2.toLowerCase());
		}
		else if (operator.equals("equalscase")) {
			meetsCondition = item1.equals(item2);
		}
		else if (operator.split("\\.")[0].equals("contains") || operator.split("\\.")[0].equals("containscase")) {
			String[] localArgs = operator.split("\\.");
			boolean caseSensitive = localArgs[0].equals("containscase");
			int amount = 0;
			String op = ">";
			if (localArgs.length == 2) {
				if ("<>=".contains(String.valueOf(localArgs[1].charAt(0)))) {
					amount = Integer.parseInt(localArgs[1].substring(1));
					op = String.valueOf(localArgs[1].charAt(0));
				}
				else {
					amount = Integer.parseInt(localArgs[1]);
				}
			}
			
			String item1Case = caseSensitive ? item1 : item1.toLowerCase();
			String item2Case = caseSensitive ? item2 : item2.toLowerCase();
			int occurrences = 0;
			int lastOccurrenceIndex = 0;
			
			while (lastOccurrenceIndex != -1) {
				lastOccurrenceIndex = item1Case.indexOf(item2Case, lastOccurrenceIndex);
			    if (lastOccurrenceIndex != -1) {
			    	occurrences++;
			    	lastOccurrenceIndex += item2Case.length();
			    }
			}
			
			if (op.equals(">")) {
				meetsCondition = occurrences > amount;
			}
			if (op.equals("<")) {
				meetsCondition = occurrences < amount;
			}
			if (op.equals("=")) {
				meetsCondition = occurrences == amount;
			}
		}
		else if (operator.split("\\.")[0].equals("char") || operator.split("\\.")[0].equals("charcase")) {
			String[] localArgs = operator.split("\\.");
			boolean caseSensitive = localArgs[0].equals("charcase");
			int position = Integer.parseInt(localArgs[1]);
			
			String item1Case = caseSensitive ? item1 : item1.toLowerCase();
			String item2Case = caseSensitive ? item2 : item2.toLowerCase();
			
			if (position >= item1Case.length()) {
				meetsCondition = false;
			}
			else {
				meetsCondition = String.valueOf(item1Case.charAt(position)).equals(item2Case);
			}
		}
		
		return meetsCondition == null ? false : (matcher.group(2).equals("!") ? !meetsCondition : meetsCondition);
	}

	public PlexMessagingMessage updateMessageFromString(PlexMessagingMessage message, String text) {
		if (!this.matchesMessage(text)) {
			return null;
		}
		message.type = this.defaultMessageType;
		message.position = this.defaultMessageSide;
		message.countsAsUnread = this.countsTowardsUnread;
		message.parentAdapter = this;
		message.messageBreakdown = this.getMessageBreakdown(text);
		for (PlexMessagingMessageEventHandler callback : this.callbacks) {
			message.addCallback(callback);
		}
		for (String tag : this.messageTags.keySet()) {
			message.setTag(tag, this.getMessageTag(tag, text));
		}
		message.setContent(this.getMessageContent(text));
		String theAuthor = this.getAuthor(text);
		if (theAuthor != null) {
			message.setAuthor(theAuthor);
		}
		return message;
	}
	
	public PlexMessagingMessage getIncompleteMessageFromText(String text) {
		return this.updateMessageFromString(new PlexMessagingMessage(), text);
	}
	
	public boolean meetsRequirements(boolean chatOpen, PlexMessagingChannelBase selectedChannel, PlexMessagingChannelBase messageChannel) {
		boolean requiredChannelOpen = false;
		if (selectedChannel != null && messageChannel != null) {
			requiredChannelOpen = selectedChannel.equals(messageChannel);
		}
		if (this.sendToSelectedChannel) {
			if (this.requiresChatOpen) {
				return chatOpen;
			}
			return true;
		}
		if (this.requiresChannelOpen && !chatOpen) {
			return false;
		}
		if (this.requiresChannelOpen && !requiredChannelOpen) {
			return false;
		}
		return true;
	}
	
	public boolean meetsConditions(String message) {
		for (String condition : this.conditions) {
			if (!this.meetsCondition(condition, message)) {
				return false;
			}
		}
		return true;
	}
}
