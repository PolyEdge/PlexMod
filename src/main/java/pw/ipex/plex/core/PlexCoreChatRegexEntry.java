package pw.ipex.plex.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlexCoreChatRegexEntry {
	public String entryName = "";
	public String regexString = "";
	public Pattern regexPattern = null;
	public Map<String, Integer> patternNames = new HashMap<String, Integer>();
	public List<String> identifierTags = new ArrayList<String>();
	
	public String formatInput(String input) {
		if (input.indexOf((char) 167) == -1) {
			input = input.replace('&', (char) 167);
		}
		return input;
	}
	
	public PlexCoreChatRegexEntry(String name, String pattern) {
		pattern = this.formatInput(pattern);
		this.entryName = name;
		this.regexString = pattern;
		this.regexPattern = Pattern.compile(pattern);
	}
	
	public PlexCoreChatRegexEntry(String idTag, String name, String pattern) {
		pattern = this.formatInput(pattern);
		this.entryName = name;
		this.regexString = pattern;
		this.regexPattern = Pattern.compile(pattern);
		this.addIdTag(idTag);
	}
	
	public PlexCoreChatRegexEntry addIdTag(String name) {
		this.identifierTags.add(name);
		return this;
	}
	
	public boolean hasIdTag(String name) {
		return this.identifierTags.contains(name);
	}
	
	public PlexCoreChatRegexEntry addGroup(int group, String name) {
		this.patternNames.put(name, group);
		return this;
	}
	
	public boolean matches(String string) {
		return string.matches(this.regexString);
	}
	
	public String getValue(String input, String groupName) {
		try {
			return this.getValues(input).get(groupName);
		}
		catch (Throwable e) {
			return null;
		}
	}
	
	public Map<String, String> getValues(String input) {
		Map<String, String> output = new HashMap<String, String>();
		Matcher matcher = this.regexPattern.matcher(input);
		matcher.find();
		for (String groupName : patternNames.keySet()) {
			try {
				output.put(groupName, matcher.group(this.patternNames.get(groupName)));
			}
			catch (Throwable e) {
				output.put(groupName, null);
			}			
		}
		return output;		
	}
	
	public String formatStringWithGroups(String groupsInput, String formattingString) {
		groupsInput = this.formatInput(groupsInput);
		formattingString = this.formatInput(formattingString);
		Map<String, String> groups = this.getValues(groupsInput);
		for (String groupName : groups.keySet()) {
			formattingString = formattingString.replace("{" + groupName + "}", groups.get(groupName) != null ? groups.get(groupName) : "");
		}
		return formattingString;
	}
}
