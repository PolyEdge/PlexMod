package pw.ipex.plex.core.regex.parser;

import net.minecraft.util.EnumChatFormatting;

public class PlexCoreChatFormattingParserToken {
    public String SYM_RESET = "r";
    public String SYM_BOLD = "l";
    public String SYM_ITALIC = "o";
    public String SYM_UNDERLINE = "n";
    public String SYM_STRIKETHROUGH = "m";
    public String SYM_OBFUSCATED = "k";

    public boolean IS_SYMBOL = false;
    public String SYMBOL;

    public boolean bold = false;
    public boolean italic = false;
    public boolean underline = false;
    public boolean strikethrough = false;
    public boolean obfuscated = false;
    public String colour = "f";
    public String text = "";
}
