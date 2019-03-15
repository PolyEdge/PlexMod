package pw.ipex.plex.core.parser;

import java.util.ArrayList;
import java.util.List;

public class PlexCoreChatFormattingParser {
    public static char FORMAT_SYMBOL_CHAR = (char) 167;
    public static String FORMAT_SYMBOL = String.valueOf(FORMAT_SYMBOL_CHAR);

    public static List<PlexCoreChatFormattingParserToken> parse(String input) {
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;
        boolean obfuscated = false;
        String colour = "f";
        List<PlexCoreChatFormattingParserToken> output = new ArrayList<>();
        for (String item : input.split("(?<=" + FORMAT_SYMBOL + "[0-9a-fA-FkKlLmMnNoOrR])|(?=" + FORMAT_SYMBOL + "[0-9a-fA-FkKlLmMnNoOrR])|(?<= )|(?= )")) {
            PlexCoreChatFormattingParserToken token = new PlexCoreChatFormattingParserToken();
            if (item.length() == 2 && item.charAt(0) == FORMAT_SYMBOL_CHAR) {
                String i = item.substring(1).toLowerCase();
                token.SYMBOL = i;
                token.IS_SYMBOL = true;
                if (i.equals("l")) {
                    bold = true;
                }
                else if (i.equals("o")) {
                    italic = true;
                }
                else if (i.equals("n")) {
                    underline = true;
                }
                else if (i.equals("m")) {
                    strikethrough = true;
                }
                else if (i.equals("k")) {
                    obfuscated = true;
                }
                else if (i.matches("^[0-9a-fA-FkKlLmMnNoOrR]$")) {
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;
                    obfuscated = false;
                    colour = i;
                }
                else if (i.equals("r")) {
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;
                    obfuscated = false;
                    colour = "f";
                }
            }
            else {
                token.IS_SYMBOL = false;
                token.text = item;
            }
            token.bold = bold;
            token.italic = italic;
            token.underline = underline;
            token.strikethrough = strikethrough;
            token.obfuscated = obfuscated;
            token.colour = colour;
            output.add(token);
        }
        return output;
    }

    public List<PlexCoreChatFormattingParserToken> parseText(String input) {
        List<PlexCoreChatFormattingParserToken> output = new ArrayList<>();
        for (PlexCoreChatFormattingParserToken token : parse(input)) {
            if (!token.IS_SYMBOL) {
                output.add(token);
            }
        }
        return output;
    }
}
