package cc.dyspore.plex.core.regex.parsed;

import cc.dyspore.plex.core.regex.parser.PlexCoreChatFormattingParser;
import cc.dyspore.plex.core.regex.parser.PlexCoreChatFormattingParserToken;
import cc.dyspore.plex.core.regex.PlexCoreRegexParsedEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlexCommunityChatParsedEntry extends PlexCoreRegexParsedEntry {
    public Map<String, String> parse(String input) {
        List<PlexCoreChatFormattingParserToken> items = PlexCoreChatFormattingParser.parse(input);
        for (int word = 0; word < items.size(); word++) {

        }
        return new HashMap<>();
    }
}
