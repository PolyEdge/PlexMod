package pw.ipex.plex.core.regex.parsed;

import pw.ipex.plex.core.regex.PlexCoreRegexParsedEntry;
import pw.ipex.plex.core.regex.parser.PlexCoreChatFormattingParser;
import pw.ipex.plex.core.regex.parser.PlexCoreChatFormattingParserToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlexCommunityChatParsedEntry extends PlexCoreRegexParsedEntry {
    public Map<String, String> parse(String input) {
        List<PlexCoreChatFormattingParserToken> items = PlexCoreChatFormattingParser.parse(input);
        int word = 0;

        for (PlexCoreChatFormattingParserToken item : items) {

        }
        return new HashMap<>();
    }
}
