package pw.ipex.plex.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlexCoreChatRegexEntry {
	public String entryName = "";
	public String regexString = "";
	public Pattern regexPattern = null;
	public Map<String, Integer> patternNames = new HashMap<String, Integer>();
	
	public PlexCoreChatRegexEntry(String name, String pattern) {
		if (pattern.indexOf((char) 167) == -1) {
			pattern = pattern.replace('&', (char) 167);
		}
		this.entryName = name;
		this.regexString = pattern;
		this.regexPattern = Pattern.compile(pattern);
	}
	
	public PlexCoreChatRegexEntry addGroup(int group, String name) {
		this.patternNames.put(name, group);
		return this;
	}
	
	public boolean matches(String string) {
		return string.matches(this.regexString);
	}
	
	public String getValue(String input, String groupName) {
		if (input.indexOf((char) 167) == -1) {
			input = input.replace('&', (char) 167);
		}
		Matcher matcher = this.regexPattern.matcher(input);
		matcher.find();
		if (this.patternNames.containsKey(groupName)) {
			try {
				return matcher.group(this.patternNames.get(groupName));
			}
			catch (Throwable e) {}
		}
		return null;
	}
}
