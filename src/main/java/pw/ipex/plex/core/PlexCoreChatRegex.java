package pw.ipex.plex.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlexCoreChatRegex {
	
	// group format: (1:level) (2:rank) (3:ign) (4:message)
	
	public static String MATCH_PLAYER_CHAT = "^(?:&7Dead )?(?:(?:&[0-9a-fA-Fklmnor])*([0-9]{1,3}) ) *(?:(?:&[0-9a-fA-Fklmnor])*&l(ULTRA|HERO|LEGEND|TITAN|ETERNAL|YT|YOUTUBE|ARTIST|TRAINEE|SUPPORT|MOD|SR\\.MOD|MAPPER|BUILDER|MAPLEAD|JR\\.DEV|DEV|ADMIN|LEADER|OWNER))? *(?:&[0-9a-fA-Fklmnor])* *([a-zA-Z0-9_-]+) *(?:&[0-9a-fA-Fklmnor])* *(.*)$";
	public static String MATCH_PLAYER_MPS_CHAT = "^(?:&7Dead )?(?:(?:&[0-9a-fA-Fklmnor])*([0-9]{1,3}) )? *(?:(?:&[0-9a-fA-Fklmnor])*&l(ULTRA|HERO|LEGEND|TITAN|ETERNAL|YT|YOUTUBE|ARTIST|TRAINEE|SUPPORT|MOD|SR\\.MOD|MAPPER|BUILDER|MAPLEAD|JR\\.DEV|DEV|ADMIN|LEADER|OWNER))? *(?:&[0-9a-fA-Fkmnor])* *([a-zA-Z0-9_-]+) *(?:&[0-9a-fA-Fklmnor])* *(.*)$"; // there are no levels in mps, so a weaker regex is given (player's name cannot be bold)
	public static String MATCH_PARTY_CHAT_OLD = "^(?:(?:&[0-9a-fA-Fklmnor]){1,3}([0-9]{1,3}) )? ?(?:&5&lParty) ()?(?:&[0-9a-fA-Fklmnor]){0,4}([a-zA-Z0-9_-]{1,16}) ?(?:&[0-9a-fA-Fklmnor]){0,4} ?(.*)$";
	public static String MATCH_PARTY_CHAT = "^(?:(?:&[0-9a-fA-Fklmnor]){1,3}([0-9]{1,3}) )? ?(?:&5&lParty) ()?(?:&[0-9a-fA-Fklmnor]){0,4}([a-zA-Z0-9_-]{1,16}) ?&d(.*)$";
	public static String MATCH_TEAM_CHAT = "^&lTeam (?:&7Dead )?(?:(?:&[0-9a-fA-Fklmnor]){1,3}([0-9]{1,3}) )?(?:&[0-9a-fA-Fklmnor]){0,4}(ULTRA|HERO|LEGEND|TITAN|ETERNAL|YT|YOUTUBE|ARTIST|TRAINEE|SUPPORT|MOD|SR\\.MOD|MAPPER|BUILDER|MAPLEAD|JR\\.DEV|DEV|ADMIN|LEADER|OWNER)? ?(?:&[0-9a-fA-Fklmnor]){0,4}([a-zA-Z0-9_-]{1,16}) ?(?:&[0-9a-fA-Fklmnor]){0,4} ?(.*)$";
	public static String MATCH_DIRECT_MESSAGE = "^&6&l([a-zA-Z0-9 _]+) > ([a-zA-Z0-9 _]+)&e &e&l(.*)$";
	// COMS - &[0-9a-f]&l([a-zA-Z0-9])&[0-9a-f]&l([a-zA-Z0-9])
	
	
	public static Pattern PATTERN_PLAYER_CHAT = Pattern.compile(MATCH_PLAYER_CHAT);
	public static Pattern PATTERN_PLAYER_MPS_CHAT = Pattern.compile(MATCH_PLAYER_MPS_CHAT);
	public static Pattern PATTERN_PARTY_CHAT = Pattern.compile(MATCH_PARTY_CHAT);
	public static Pattern PATTERN_TEAM_CHAT = Pattern.compile(MATCH_TEAM_CHAT);
	public static Pattern PATTERN_DIRECT_MESSAGE = Pattern.compile(MATCH_DIRECT_MESSAGE);
	
	public static String determinePotentialChatType(String message) { //already filtered
		if (message.matches(MATCH_PARTY_CHAT)) {
			return "party";
		}
		if (message.matches(MATCH_TEAM_CHAT)) {
			return "team";
		}
		if (message.matches(MATCH_PLAYER_CHAT)) {
			return "player";
		}
		if (message.matches(MATCH_DIRECT_MESSAGE)) {
			return "direct_message";
		}
		if (message.matches(MATCH_PLAYER_MPS_CHAT)) {
			if (possibleMpsChat(message)) {
				return "player_mps";
			}
		}
		return "unknown";
	}
	
	public static Boolean possibleMpsChat(String message) {
		if (!message.matches(MATCH_PLAYER_MPS_CHAT)) {
			return false;
		}
		Matcher matcher = PATTERN_PLAYER_MPS_CHAT.matcher(message);
		matcher.find();
		if (matcher.group(2) == null) {
			return false;
		}
		return PlexCore.getPlayerIGNList(true).contains(matcher.group(2).toLowerCase());
	}
	
	public static String getMessageField(List<String> data, String field) {
		if (data == null) {
			return null;
		}
		if (!Integer.valueOf(data.size()).equals(4)) {
			return null;
		}
		if (field.equals("level")) {
			return data.get(0);
		}
		if (field.equals("rank")) {
			return data.get(1);
		}
		if (field.equals("ign")) {
			return data.get(2);
		}
		if (field.equals("message")) {
			return data.get(3);
		}
		return null;
	}
	
	public static Boolean compare(List<String> data, String field, String value) {
		String fieldData = getMessageField(data, field);
		if (fieldData == null) {
			return false;
		}
		if (fieldData.equals("")) {
			return false;
		}
		return fieldData.equalsIgnoreCase(value);
	}
	
	public static Matcher getChatMatcher(String message) {
		String messageType = determinePotentialChatType(message);
		Matcher matcher = null;
		if (messageType.equals("party")) {
			matcher = PATTERN_PARTY_CHAT.matcher(message);
		}
		else if (messageType.equals("team")) {
			matcher = PATTERN_TEAM_CHAT.matcher(message);
		}
		else if (messageType.equals("player")) {
			matcher = PATTERN_PLAYER_CHAT.matcher(message);
		}
		else if (messageType.equals("player_mps")) {
			matcher = PATTERN_PLAYER_MPS_CHAT.matcher(message);
		}
		else if (messageType.equals("direct_message")) {
			matcher = PATTERN_DIRECT_MESSAGE.matcher(message);
		}
		return matcher;
	}
	
	public static List<String> determineRegularMessageData(String message) {
		String messageType = determinePotentialChatType(message);
		if (!(messageType.equals("team") || messageType.equals("party") || messageType.equals("player") || messageType.equals("player_mps"))) {
			return null;
		}
		Matcher matcher = getChatMatcher(message);
		if (matcher == null) {
			return null;
		}
		try {
			matcher.find();
			return new ArrayList<String>(Arrays.asList(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4)));			
		}
		catch (Exception e) {
			return null;
		}
	}
}
