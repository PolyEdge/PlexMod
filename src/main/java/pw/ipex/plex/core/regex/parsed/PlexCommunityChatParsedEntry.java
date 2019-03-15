package pw.ipex.plex.core.regex.parsed;

import pw.ipex.plex.core.regex.PlexCoreRegexParsedEntry;
import pw.ipex.plex.core.parser.PlexCoreChatFormattingParser;
import pw.ipex.plex.core.parser.PlexCoreChatFormattingParserToken;

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
