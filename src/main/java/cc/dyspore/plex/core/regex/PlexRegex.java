package cc.dyspore.plex.core.regex;

import cc.dyspore.plex.core.regex.PlexRegexPattern.*;
import cc.dyspore.plex.core.util.PlexUtilCache;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PlexRegex {
    public enum ChatGroup {
        CHAT,
        PUBLIC,
        PARTY,
        PRIVATE,
        COMMUNITY
    }

    public static String SPLIT_FORMAT_REGION = "((?:([^ \n]*?)(?<!\\\\)\\{\\{([^|]+?)\\|(.+?)(?<!\\\\)\\}\\}|[^ \n]+|\n)|( +))";
    public static Pattern PATTERN_FORMAT_REGION = Pattern.compile(SPLIT_FORMAT_REGION); // why cant i keep the delimiter in java

    public static String MATCH_PLAYER_CHAT = "^(?:&7Dead )?(?:(?:&[0-9a-fA-Fklmnor])*([0-9]{1,3}) ) *(?:&[0-9a-fA-Fklmnor]. )? *(?:&2&lMPS (?:Host|Co-Host))? *(?:(?:&[0-9a-fA-Fklmnor])*&l(ULTRA|HERO|LEGEND|TITAN|ETERNAL|IMMORTAL|YT|YOUTUBE|ARTIST|TRAINEE|SUPPORT|MOD|SR\\.MOD|MAPPER|BUILDER|MAPLEAD|JR\\.DEV|DEV|ADMIN|LEADER|OWNER))? *(?:&[0-9a-fA-Fklmnor])* *([a-zA-Z0-9_-]+) *(?:&[0-9a-fA-Fklmnor])* *(.*)$";
    public static String MATCH_TEAM_CHAT = "^&l(?:Team|TEAM) (?:&7Dead )?(?:(?:&[0-9a-fA-Fklmnor])*([0-9]{1,3}) ) *(?:&[0-9a-fA-Fklmnor]. )? *(?:&2&lMPS (?:Host|Co-Host))? *(?:&[0-9a-fA-Fklmnor]){0,4}(ULTRA|HERO|LEGEND|TITAN|ETERNAL|IMMORTAL|YT|YOUTUBE|ARTIST|TRAINEE|SUPPORT|MOD|SR\\.MOD|MAPPER|BUILDER|MAPLEAD|JR\\.DEV|DEV|ADMIN|LEADER|OWNER)? ?(?:&[0-9a-fA-Fklmnor]){0,4}([a-zA-Z0-9_-]{1,16}) ?(?:&[0-9a-fA-Fklmnor]){0,4} ?(.*)$";

    public static String MATCH_PARTY_CHAT = "^(?:(?:&[0-9a-fA-Fklmnor])*([0-9]{1,3}) )? ?(?:&5&l(?:Party|PARTY)) (?:(?:&[0-9a-fA-Fklmnor])*([0-9]{1,3}) )? ?(?:&[0-9a-fA-Fklmnor]){0,4}([a-zA-Z0-9_-]{1,16}) ?&d(.*)$";
    public static String MATCH_PARTY_CREATE = "^&9Party> &7You don't seem to have a party, so I've created a new one for you!$";
    public static String MATCH_PARTY_DISBAND = "^&9Party> &7The party has been disbanded\\.?$";
    public static String MATCH_PARTY_INVITE = "^&9Party> &e([a-zA-Z0-9_]+)&7 has invited you to join their party on &e([a-zA-Z0-9_-]+)&7\\.?$";
    public static String MATCH_PARTY_INVITE_LOCAL = "^&9Party> &e([a-zA-Z0-9_]+)&7 has invited you to join their party\\.?$";
    public static String MATCH_PARTY_SEARCH = "^&9Party> &7Searching for &e([a-zA-Z0-9_]+)&7 across the network\\.\\.\\.$";
    public static String MATCH_PARTY_SEARCH_SENT = "^&9Party> &7You invited &e([a-zA-Z0-9_]+)&7 to the party\\.?$";
    public static String MATCH_PARTY_REPLY = "^&9Party> &7Click to &a&lACCEPT&7 or &c&lDENY&7 in the next 2 minutes\\.?$";
    public static String MATCH_PARTY_JOIN  = "^&9Party> &e([a-zA-Z0-9_]+)&7 has joined the party\\.?$";
    public static String MATCH_PARTY_REMOVE = "^&9Party> &e([a-zA-Z0-9_]+)&7 has been removed from the party\\.?$";
    public static String MATCH_PARTY_LEFT = "^&9Party> &e([a-zA-Z0-9_]+)&7 has left the party\\.?$";
    public static String MATCH_PARTY_LEAVE = "^&9Party> &7You have left the party\\.?$";
    public static String MATCH_PARTY_DECLINED = "^&9Party> &e([a-zA-Z0-9_]+)&7 declined your party invite\\.?$";
    public static String MATCH_PARTY_DECLINE = "^&9Party> &7You declined the party invite\\.?$";
    public static String MATCH_PARTY_OFFLINE = "^&9Party> &7Could not locate &e([a-zA-Z0-9_]+)&?7?\\.?$";

    public static String MATCH_DIRECT_MESSAGE = "^&6&l([a-zA-Z0-9 _]+) > ([a-zA-Z0-9 _]+)&e &e&l(.*)$";

    public static String MATCH_COMMUNITY_CHAT = "^(?:&([0-9a-f]))&l([a-zA-Z0-9_]+) ?(?:(?:&([0-9a-f])) ?&l ?(Trainee|Support|Mod|Sr\\.Mod|Mapper|Builder|Maplead|Jr\\.Dev|Dev|Admin|Leader|Owner) ?(?:&[0-9a-f]&l)? ?|(?:&([0-9a-f]))&l ?)([a-zA-Z0-9_]+) (?:&([0-9a-f]))(.+)$";

    public static String MATCH_PLAYER_OFFLINE = "^&9Online Player Search> &e0&7 matches for \\[&e([A-Za-z0-9_]+)&7]\\.?$";
    public static String MATCH_IMMORTAL_JOIN_LOBBY = "^&9Join> (:?&7)?&e&lIMMORTAL ([A-Za-z0-9_])+&7 has joined the lobby.$";

    public enum Chat {
        // public

        PUBLIC_CHAT(entry(MATCH_PLAYER_CHAT, ChatGroup.CHAT)
                .tag(ChatGroup.PUBLIC)
                .group(1, "level")
                .group(2, "rank")
                .group(3, "author")
                .group(4, "message")),

        // party

        PARTY_CHAT(entry(MATCH_PARTY_CHAT, ChatGroup.CHAT)
                .tag(ChatGroup.PARTY)
                .group(1, "level")
                .group(2, "level2")
                .group(3, "author")
                .group(4, "message")
                .group(5, "rank")),
        
        PARTY_CREATE(entry(MATCH_PARTY_CREATE, ChatGroup.PARTY)),

        PARTY_DISBAND(entry(MATCH_PARTY_DISBAND, ChatGroup.PARTY)),

        PARTY_INVITE(entry(MATCH_PARTY_INVITE, ChatGroup.PARTY)
                .group(1, "sender")
                .group(2, "server")),

        PARTY_INVITE_LOCAL(entry(MATCH_PARTY_INVITE_LOCAL, ChatGroup.PARTY)
                .group(1, "sender")),

        PARTY_SEARCH(entry(MATCH_PARTY_SEARCH, ChatGroup.PARTY)
                .group(1, "invited_player")),

        PARTY_SEARCH_SENT(entry(MATCH_PARTY_SEARCH_SENT, ChatGroup.PARTY)
                .group(1, "invited_player")),

        PARTY_REPLY(entry(MATCH_PARTY_REPLY, ChatGroup.PARTY)),

        PARTY_JOIN(entry(MATCH_PARTY_JOIN, ChatGroup.PARTY)
                .group(1, "ign")),

        PARTY_REMOVE(entry(MATCH_PARTY_REMOVE, ChatGroup.PARTY)
                .group(1, "ign")),

        PARTY_LEFT(entry(MATCH_PARTY_LEFT, ChatGroup.PARTY)
                .group(1, "ign")),

        PARTY_LEAVE(entry(MATCH_PARTY_LEAVE, ChatGroup.PARTY)),

        PARTY_DECLINED(entry(MATCH_PARTY_DECLINED, ChatGroup.PARTY)
                .group(1, "ign")),

        PARTY_DECLINE(entry(MATCH_PARTY_DECLINE, ChatGroup.PARTY)),

        PARTY_OFFLINE(entry(MATCH_PARTY_OFFLINE, ChatGroup.PARTY)
                .group(1, "ign")),

        // team

        TEAM_CHAT(entry(MATCH_TEAM_CHAT, ChatGroup.CHAT)
                .tag(ChatGroup.PUBLIC)
                .group(1, "level")
                .group(2, "rank")
                .group(3, "author")
                .group(4, "message")),

        // private

        PRIVATE_MESSAGE(entry(MATCH_DIRECT_MESSAGE, ChatGroup.CHAT)
                .tag(ChatGroup.PRIVATE)
                .group(1, "author")
                .group(2, "destination")
                .group(3, "message")),
        
        COMMUNITY_CHAT(entry(MATCH_COMMUNITY_CHAT, ChatGroup.CHAT, InputFlag.INCLUDE_F)
                .tag(ChatGroup.COMMUNITY)
                .group(1, "com_name_colour")
                .group(2, "community")
                .group(3, "rank_colour")
                .group(4, "rank")
                .group(5, "author_colour")
                .group(6, "author")
                .group(7, "message_colour")
                .group(8, "message")),
        
        // misc

        TARGET_OFFLINE(entry(MATCH_PLAYER_OFFLINE)
                .group(1, "player")),

        IMMORTAL_JOIN(entry(MATCH_IMMORTAL_JOIN_LOBBY)
                .group(1, "player_name"))

        ;

        PlexRegexPattern pattern;

        Chat(PlexRegexPattern matcher) {
            this.pattern = matcher;
        }

        public boolean equals(Chat entry) {
            return this.pattern.equals(entry.pattern);
        }

        public boolean equals(PlexRegexPattern entry) {
            return this.pattern.equals(entry);
        }
    }

    public static PlexUtilCache<IChatComponent, ChatMessage> chatCache = new PlexUtilCache<>(true);

    //

    public static PlexRegexPattern entry(String pattern, ChatGroup[] groups, PlexRegexPattern.InputFlag[] inputFlags) {
        PlexRegexPattern matcher = new PlexRegexPattern(pattern);
        for (ChatGroup group : groups) {
            matcher.tag(group);
        }
        for (InputFlag group : inputFlags) {
            matcher.flag(group);
        }
        return matcher;
    }

    public static PlexRegexPattern entry(String pattern, ChatGroup group, PlexRegexPattern.InputFlag... inputFlags) {
        return entry(pattern, new ChatGroup[] {group}, inputFlags);
    }

    public static PlexRegexPattern entry(String pattern) {
        return entry(pattern, new ChatGroup[] {}, new PlexRegexPattern.InputFlag[] {});
    }

    public static List<String> splitFormatRegion(String input) {
        java.util.regex.Matcher matcher = PATTERN_FORMAT_REGION.matcher(input);
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

    public static ChatMessage getChatMessage(IChatComponent ichatcomponent) {
        ChatMessage chatMessage = chatCache.get(ichatcomponent);
        if (chatMessage != null) {
            return chatMessage;
        }
        ChatMessage chatMessage1 = new ChatMessage(ichatcomponent);
        chatMessage1.build();
        return chatCache.put(ichatcomponent, chatMessage1);
    }


    public static class ChatMessage {
        public IChatComponent chatMessage;
        public Map<Chat, Matcher> chatMatches = new HashMap<>();

        private ChatMessage(IChatComponent ichatcomponent) {
            this.chatMessage = ichatcomponent;
        }

        private void build() {
            for (Chat entry : Chat.values()) {
                Matcher matcher = entry.pattern.getMatcher(this.chatMessage);
                if (matcher.find()) {
                    this.chatMatches.put(entry, matcher);
                }
            }
        }

        public boolean matches(Chat entry) {
            return this.chatMatches.containsKey(entry);
        }

        public Matcher getMatch(Chat entry) {
            return this.chatMatches.get(entry);
        }

        public String getGroup(Chat entry, String group) {
            Matcher matcher = this.chatMatches.get(entry);
            if (matcher != null) {
                return matcher.group(group);
            }
            return null;
        }
    }
}
