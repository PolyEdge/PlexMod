package pw.ipex.plex.core.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlexCoreRegex {
	public static String SPLIT_FORMAT_REGION = "((?:([^ \n]*?)(?<!\\\\)\\{\\{([^|]+?)\\|(.+?)(?<!\\\\)\\}\\}|[^ \n]+|\n)|( +))";
	public static Pattern PATTERN_FORMAT_REGION = Pattern.compile(SPLIT_FORMAT_REGION); // why cant i keep the delimiter in java
	
	public static String MATCH_PLAYER_CHAT = "^(?:&7Dead )?(?:(?:&[0-9a-fA-Fklmnor])*([0-9]{1,3}) ) *(?:(?:&[0-9a-fA-Fklmnor])*&l(ULTRA|HERO|LEGEND|TITAN|ETERNAL|YT|YOUTUBE|ARTIST|TRAINEE|SUPPORT|MOD|SR\\.MOD|MAPPER|BUILDER|MAPLEAD|JR\\.DEV|DEV|ADMIN|LEADER|OWNER))? *(?:&[0-9a-fA-Fklmnor])* *([a-zA-Z0-9_-]+) *(?:&[0-9a-fA-Fklmnor])* *(.*)$";
	public static String MATCH_PLAYER_MPS_CHAT = "^(?:&7Dead )?(?:(?:&[0-9a-fA-Fklmnor])*([0-9]{1,3}) )? *(?:(?:&[0-9a-fA-Fklmnor])*&l(ULTRA|HERO|LEGEND|TITAN|ETERNAL|YT|YOUTUBE|ARTIST|TRAINEE|SUPPORT|MOD|SR\\.MOD|MAPPER|BUILDER|MAPLEAD|JR\\.DEV|DEV|ADMIN|LEADER|OWNER))? *(?:&[0-9a-fA-Fkmnor])* *([a-zA-Z0-9_-]+) *(?:&[0-9a-fA-Fklmnor])* *(.*)$"; // there are no levels in mps, so a weaker regex is given (player's name cannot be bold)
	public static String MATCH_PARTY_CHAT_OLDER = "^(?:(?:&[0-9a-fA-Fklmnor]){1,3}([0-9]{1,3}) )? ?(?:&5&lParty) ()?(?:&[0-9a-fA-Fklmnor]){0,4}([a-zA-Z0-9_-]{1,16}) ?(?:&[0-9a-fA-Fklmnor]){0,4} ?(.*)$";
	public static String MATCH_PARTY_CHAT_OLD = "^(?:(?:&[0-9a-fA-Fklmnor]){1,3}([0-9]{1,3}) )? ?(?:&5&l(?:Party|PARTY)) ()?(?:&[0-9a-fA-Fklmnor]){0,4}([a-zA-Z0-9_-]{1,16}) ?&d(.*)$";
	public static String MATCH_PARTY_CHAT = "^(?:(?:&[0-9a-fA-Fklmnor]){1,3}([0-9]{1,3}) )? ?(?:&5&l(?:Party|PARTY)) (?:(?:&[0-9a-fA-Fklmnor]){1,3}([0-9]{1,3}) )? ?(?:&[0-9a-fA-Fklmnor]){0,4}([a-zA-Z0-9_-]{1,16}) ?&d(.*)$";
	public static String MATCH_TEAM_CHAT = "^&l(?:Team|TEAM) (?:&7Dead )?(?:(?:&[0-9a-fA-Fklmnor]){1,3}([0-9]{1,3}) )?(?:&[0-9a-fA-Fklmnor]){0,4}(ULTRA|HERO|LEGEND|TITAN|ETERNAL|YT|YOUTUBE|ARTIST|TRAINEE|SUPPORT|MOD|SR\\.MOD|MAPPER|BUILDER|MAPLEAD|JR\\.DEV|DEV|ADMIN|LEADER|OWNER)? ?(?:&[0-9a-fA-Fklmnor]){0,4}([a-zA-Z0-9_-]{1,16}) ?(?:&[0-9a-fA-Fklmnor]){0,4} ?(.*)$";
	public static String MATCH_DIRECT_MESSAGE = "^&6&l([a-zA-Z0-9 _]+) > ([a-zA-Z0-9 _]+)&e &e&l(.*)$";
	public static String MATCH_COMMUNITY_CHAT_OLD = "^(?:&([0-9a-f]))?&l([a-zA-Z0-9_]+) (?:&([0-9a-f]))?&l([a-zA-Z0-9_]+) (?:&([0-9a-f]?))?(.+)$";
	public static String MATCH_COMMUNITY_CHAT = "^(?:&([0-9a-f]))?&l([a-zA-Z0-9_]+) ?(?:(?:&([0-9a-f]))?&l(Trainee|Support|Mod|Sr\\.Mod|Mapper|Builder|Maplead|Jr\\.Dev|Dev|Admin|Leader|Owner) |(?:&([0-9a-f]))?&l)([a-zA-Z0-9_]+) (?:&([0-9a-f]))?(.+)$";

	public static String MATCH_PARTY_CREATE = "^&9Party> &7You don't seem to have a party, so I've created a new one for you!$";
	public static String MATCH_PARTY_DISBAND = "^&9Party> &7The party has been disbanded\\.?$";
	public static String MATCH_PARTY_INVITE = "^&9Party> &e([a-zA-Z0-9_]+)&7 has invited you to join their party on &e([a-zA-Z0-9_-]+)&7\\.?$";
	public static String MATCH_PARTY_INVITE_LOCAL = "^&9Party> &e([a-zA-Z0-9_]+)&7 has invited you to join their party\\.?$";
	public static String MATCH_PARTY_SEARCH = "^&9Party> Searching for &e([a-zA-Z0-9_]+)&7 across the network\\.\\.\\.$";
	public static String MATCH_PARTY_SEARCH_SENT = "^&9Party> You invited &e([a-zA-Z0-9_]+)&7 to the party\\.?$";
	public static String MATCH_PARTY_REPLY = "^&9Party> &7Click to &a&lACCEPT&7 or &c&lDENY&7 in the next 2 minutes\\.?$";
	public static String MATCH_PARTY_JOIN  = "^&9Party> &e([a-zA-Z0-9_]+)&7 has joined the party\\.?$";
	public static String MATCH_PARTY_REMOVE = "^&9Party> &e([a-zA-Z0-9_]+)&7 has been removed from the party\\.?$";
	public static String MATCH_PARTY_LEFT = "^&9Party> &e([a-zA-Z0-9_]+)&7 has left the party\\.?$";
	public static String MATCH_PARTY_LEAVE = "^&9Party> &7You have left the party\\.?$";
	public static String MATCH_PARTY_DECLINED = "^&9Party> &e([a-zA-Z0-9_]+)&7 declined your party invite\\.?$";
	public static String MATCH_PARTY_DECLINE = "^&9Party> &7You declined the party invite\\.?$";
	public static String MATCH_PARTY_OFFLINE = "^&9Party> &7Could not locate &e([a-zA-Z0-9_]+)&?7?\\.?$";
	
	public static String MATCH_DM_PLAYER_OFFLINE = "^&9Online Player Search> &e0&7 matches for \\[&e([A-Za-z0-9_]+)&7]\\.?$";

	public static Pattern PATTERN_PLAYER_CHAT = Pattern.compile(MATCH_PLAYER_CHAT);
	public static Pattern PATTERN_PLAYER_MPS_CHAT = Pattern.compile(MATCH_PLAYER_MPS_CHAT);
	public static Pattern PATTERN_PARTY_CHAT = Pattern.compile(MATCH_PARTY_CHAT);
	public static Pattern PATTERN_TEAM_CHAT = Pattern.compile(MATCH_TEAM_CHAT);
	public static Pattern PATTERN_DIRECT_MESSAGE = Pattern.compile(MATCH_DIRECT_MESSAGE);
	
	public static List<PlexCoreRegexEntry> regexEntries = new ArrayList<PlexCoreRegexEntry>();
	
	static {
		addEntry("player_chat", MATCH_PLAYER_CHAT).addField(1, "level").addField(2, "rank").addField(3, "author").addField(4, "message").tag("chatMessage");
		addEntry("player_chat_mps", MATCH_PLAYER_MPS_CHAT).addField(1, "level").addField(2, "rank").addField(3, "author").addField(4, "message");
		addEntry("party_chat", MATCH_PARTY_CHAT, "party").addField(1, "level").addField(2, "level2").addField(3, "author").addField(4, "message").addField(5, "rank").tag("chatMessage");
		addEntry("team_chat", MATCH_TEAM_CHAT).addField(1, "level").addField(2, "rank").addField(3, "author").addField(4, "message").tag("chatMessage");
		addEntry("direct_message", MATCH_DIRECT_MESSAGE).addField(1, "author").addField(2, "destination").addField(3, "message").tag("chatMessage");
		addEntry("community_chat_staff", MATCH_COMMUNITY_CHAT).addField(1, "com_name_colour").addField(2, "community").addField(3, "rank_colour").addField(4, "rank").addField(5, "author_colour").addField(6, "author").addField(7, "message_colour").addField(8, "message").tag("chatMessage");

		addEntry("party_create", MATCH_PARTY_CREATE, "party");
		addEntry("party_disband", MATCH_PARTY_DISBAND, "party");
		addEntry("party_invite", MATCH_PARTY_INVITE, "party").addField(1, "sender").addField(2, "extra");
		addEntry("party_invite_local", MATCH_PARTY_INVITE_LOCAL, "party").addField(1, "sender").addField(2, "server");
		addEntry("party_invite_search", MATCH_PARTY_SEARCH, "party").addField(1, "invited_player");
		addEntry("party_invite_sent", MATCH_PARTY_SEARCH_SENT, "party").addField(1, "invited_player");
		addEntry("party_invite_reply", MATCH_PARTY_REPLY, "party");
		addEntry("party_join", MATCH_PARTY_JOIN, "party").addField(1, "ign");
		addEntry("party_remove", MATCH_PARTY_REMOVE, "party").addField(1, "ign");
		addEntry("party_left", MATCH_PARTY_LEFT, "party").addField(1, "ign");
		addEntry("party_leave", MATCH_PARTY_LEAVE, "party");
		addEntry("party_declined", MATCH_PARTY_DECLINED, "party").addField(1, "ign");
		addEntry("party_decline", MATCH_PARTY_DECLINE, "party");
		addEntry("party_offline", MATCH_PARTY_OFFLINE, "party").addField(1, "ign");

		addEntry("direct_message_player_offline", MATCH_DM_PLAYER_OFFLINE, "direct_message").addField(1, "destination");
		
	}

	public static List<String> splitFormatRegionString(String input) {
		Matcher matcher = PATTERN_FORMAT_REGION.matcher(input);
		List<String> output = new ArrayList<>();
		while (matcher.find()) {
			if (matcher.group(3) != null && matcher.group(4) != null) {
				if (matcher.group(2) != null) {
					if (!matcher.group(2).equals("")) {
						output.add("$" + matcher.group(2));
					}
				}
				output.add("!" + matcher.group(3) + "|" + matcher.group(4));
			}
			else if (matcher.group(5) != null) {
				output.add("$" + matcher.group(5));
			}
			else {
				output.add("$" + matcher.group(1));
			}
		}
		return output;
	}

	
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
		return "unknown";
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
	
	public static PlexCoreRegexEntry addEntry(String entryName, String entryRegex) {
		return addEntry(new PlexCoreRegexEntry(entryName, entryRegex));
	}
	
	public static PlexCoreRegexEntry addEntry(String entryName, String entryRegex, String tag) {
		return addEntry(new PlexCoreRegexEntry(entryName, entryRegex, tag));
	}
	
	public static PlexCoreRegexEntry addEntry(PlexCoreRegexEntry entry) {
		regexEntries.add(entry);
		return entry;
	}
	
	public static PlexCoreRegexEntry getEntryNamed(String name) {
		for (PlexCoreRegexEntry entry : regexEntries) {
			if (entry.entryName.equals(name)) {
				return entry;
			}
		}
		return null;
	}
	
	public static PlexCoreRegexEntry getEntryMatchingText(String text) {
		for (PlexCoreRegexEntry entry : regexEntries) {
			if (entry.matches(text)) {
				return entry;
			}
		}
		return null;
	}
}
