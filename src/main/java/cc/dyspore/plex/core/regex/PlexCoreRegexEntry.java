package cc.dyspore.plex.core.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlexCoreRegexEntry {
	public String entryName;
	public String regexString;
	public Pattern regexPattern;
	public boolean hideWhite = true;
	public boolean hideReset = true;
	public Map<String, Integer> groupNames = new HashMap<>();
	public List<String> identifierTags = new ArrayList<>();

	public char FORMAT_SYMBOL_CHAR = (char) 167;
	public String FORMAT_SYMBOL = String.valueOf(FORMAT_SYMBOL_CHAR);

	public PlexCoreRegexEntry() {
		this.regexString = "";
	}

	public PlexCoreRegexEntry(String name, String pattern) {
		pattern = this.anyFromAmpersand(pattern, true);
		this.entryName = name;
		this.regexString = pattern;
		this.regexPattern = Pattern.compile(pattern);
	}

	public PlexCoreRegexEntry(String name, String pattern, String idTag) {
		this(name, pattern);
		this.tag(idTag);
	}

	public String anyFromAmpersand(String input, boolean unescape) {
		if (input.indexOf((char) 167) == -1) {
			input = input.replaceAll("(?<!\\\\)(?:((?:\\\\\\\\)*))&", "$1" + FORMAT_SYMBOL);
			if (unescape) {
				input = input.replaceAll("\\\\&", "&");
			}
		}
		return input;
	}

	public String smartFromAmpersand(String input, boolean unescape) {
		if (input.indexOf((char) 167) == -1) {
			input = input.replaceAll("(?<!\\\\)(?:((?:\\\\\\\\)*))&([0-9a-fA-FkKlLmMnNoOrR])", "$1" + FORMAT_SYMBOL + "$2");
			if (unescape) {
				input = input.replaceAll("\\\\(&[0-9a-fA-FkKlLmMnNoOrR])", "$1");
			}
		}
		return input;
	}

	public String clear(String input, boolean white, boolean reset) {
		if (white) {
			input = input.replace(FORMAT_SYMBOL + "f", "");
		}
		if (reset) {
			input = input.replace(FORMAT_SYMBOL + "r", "");
		}
		return input;
	}

	public String getMinified(String input) {
		return this.clear(input, this.hideWhite, this.hideReset);
	}

	public String prepareInputString(String input) {
		return this.getMinified(this.smartFromAmpersand(input, true));
	}

	public PlexCoreRegexEntry setHideWhite(boolean hideWhite) {
		this.hideWhite = hideWhite;
		return this;
	}

	public PlexCoreRegexEntry setHideReset(boolean hideReset) {
		this.hideReset = hideReset;
		return this;
	}

	public PlexCoreRegexEntry setMinified(boolean minified) {
		this.hideWhite = minified;
		this.hideReset = minified;
		return this;
	}

	public PlexCoreRegexEntry tag(String name) {
		this.identifierTags.add(name);
		return this;
	}

	public boolean hasTag(String name) {
		return this.identifierTags.contains(name);
	}
	
	public PlexCoreRegexEntry addField(int group, String name) {
		this.groupNames.put(name, group);
		return this;
	}

	public boolean hasField(String field) {
		return this.groupNames.keySet().contains(field);
	}

	public boolean matches(String string) {
		//Plex.logger.info("comparing " + this.entryName + ": " + this.regexString);
		//Plex.logger.info("to: " + this.prepareInputString(string));
		return this.prepareInputString(string).matches(this.regexString);
	}
	
	public String getField(String input, String field) {
		try {
			return this.getAllFields(input).get(field);
		}
		catch (Throwable e) {
			return null;
		}
	}
	
	public Map<String, String> getAllFields(String input) {
		Map<String, String> output = new HashMap<>();
		Matcher matcher = this.regexPattern.matcher(this.prepareInputString(input));
		matcher.find();
		for (String groupName : groupNames.keySet()) {
			try {
				output.put(groupName, matcher.group(this.groupNames.get(groupName)));
			}
			catch (Throwable e) {
				output.put(groupName, null);
			}			
		}
		return output;		
	}
	
	public String formatStringWithGroups(String messageInput, String formattingString) {
		messageInput = this.prepareInputString(messageInput);
		formattingString = this.smartFromAmpersand(formattingString, true);
		Map<String, String> groups = this.getAllFields(messageInput);
		for (String groupName : groups.keySet()) {
			formattingString = formattingString.replace("{" + groupName + "}", groups.get(groupName) != null ? groups.get(groupName) : "");
			formattingString = formattingString.replace("{$" + groupName + "}", groups.get(groupName) != null ? groups.get(groupName).replaceAll(FORMAT_SYMBOL + "[0-9a-fA-Fklmor]", "") : "");
		}
		return formattingString;
	}
}
