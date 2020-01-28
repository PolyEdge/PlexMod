package cc.dyspore.plex.core.util;

import cc.dyspore.plex.Plex;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlexUtilChat {
    public static final String PLEX = chatStyleText("GOLD", "Plex") + chatStyleText("BLACK", ">") + chatStyleText("RESET", " ");
    public static String FORMAT_SYMBOL = Character.toString ((char) 167);
    public static Map<String, Integer> colourCode = new HashMap<>();

    static {
        colourCode.put(null, 0xffffff);
        colourCode.put("0", 0x000000);
        colourCode.put("1", 0x0000aa);
        colourCode.put("2", 0x00aa00);
        colourCode.put("3", 0x00aaaa);
        colourCode.put("4", 0xaa0000);
        colourCode.put("5", 0xaa00aa);
        colourCode.put("6", 0xffaa00);
        colourCode.put("7", 0xaaaaaa);
        colourCode.put("8", 0x555555);
        colourCode.put("9", 0x5555ff);
        colourCode.put("a", 0x55ff55);
        colourCode.put("b", 0x55ffff);
        colourCode.put("c", 0xff5555);
        colourCode.put("d", 0xff55ff);
        colourCode.put("e", 0xffff55);
        colourCode.put("f", 0xffffff);
    }

    public static void chatAddMessage(String message) {
        chatAddMessage(new ChatComponentText(message));
    }

    public static void chatAddMessage(IChatComponent message) {
        try {
            Plex.minecraft.ingameGUI.getChatGUI().printChatMessage(message);
        }
        catch (NullPointerException ignored) {
        }
    }

    public static boolean chatIsMessage(byte messageType) {
        return (messageType == (byte) 0) || (messageType == (byte) 1);
    }

    public static String chatStyleText(String ...args) {
        String valueText = args[(args.length - 1)];
        List<String> styles = Arrays.asList(args).subList(0, (args.length - 1));
        StringBuilder output = new StringBuilder();
        String[] words = valueText.split("\\s", -1);
        for (String word : words) {
            StringBuilder outputWord = new StringBuilder();
            for (String style : styles) {
                outputWord.append(EnumChatFormatting.valueOf(style.toUpperCase()));
            }
            outputWord.append(word);
            if (output.length() == 0) {
                output.append(outputWord);
            }
            else {
                output.append(" ");
                output.append(outputWord);
            }
        }
        output.insert(0, EnumChatFormatting.RESET);
        output.append(EnumChatFormatting.RESET);
        return output.toString();
    }

    public static String getUiChatMessage(String message) {
        if (message.equalsIgnoreCase("plex.modInfo")) {
            return PLEX + chatStyleText("GRAY", "Plex v" + Plex.VERSION + (Plex.PATCHID == null ? "" : "-" + Plex.PATCHID) + " ") + chatStyleText("GRAY", ">> ") + chatStyleText("GOLD", "@PolyEdge/cysk ")  + "\n" +
                    PLEX + chatStyleText("GRAY", "Use ") + chatStyleText("AQUA", "/plex help") + chatStyleText("GRAY", " to open mod help menu");
        }
        if (message.equalsIgnoreCase("plex.unsupportedServer")) {
            return PLEX + chatStyleText("DARK_RED", "Log on to Mineplex and try again!");
        }
        if (message.equalsIgnoreCase("plex.unsupportedServerStrangeAddress")) {
            return PLEX + chatStyleText("DARK_RED", "Log on to Mineplex and try again!") +
                    PLEX + chatStyleText("DARK_RED", "Please note that the server address you're connecting to must contain mineplex.com for the mod to work");
        }
        if (message.equalsIgnoreCase("plex.nullModCommand")) {
            return PLEX + chatStyleText("DARK_RED", "Unknown command. Try using /plex help");
        }
        return PLEX + chatStyleText("GRAY", message);
    }

    /**
     * Removes all formatting from a chat message
     *
     * @param text The chat message
     * @return the chat message, formatting removed
    */
    public static String chatRemoveFormatting(String text) {
        return text.replaceAll(FORMAT_SYMBOL + "[0-9a-zA-Z]", "");
    }

    /**
     * Removes WHITE (f) and RESET (r) chat formatting codes from a chat message
     *
     * @param text The chat message
     * @return the chat message, WHITE and RESET codes removed
     */
    public static String chatCondense(String text) {
        return text.replace(FORMAT_SYMBOL + "f", "").replace(FORMAT_SYMBOL + "r", "");
    }

    /**
     * Replaces formatting symbols in a chat message with ampersands (&)s
     * [Shortcut method to chatAmpersandFilter(text, escape) with escape set to false]
     *
     * @param text The chat message
     * @return the chat message, formatting symbols replaced with ampersands.
     */
    public static String chatAmpersandFilter(String text) {
        return chatAmpersandFilter(text, false);
    }

    /**
     * Replaces formatting symbols in a chat message with ampersands (&)s
     *
     * @param text The chat message
     * @param escape Determines whether existing ampersands in the message will be escaped with a backslash.
     * @return the chat message, formatting symbols replaced with ampersands.
     */
    public static String chatAmpersandFilter(String text, boolean escape) {
        if (escape) {
            text = text.replace("&", "\\&");
        }
        return text.replace(FORMAT_SYMBOL, "&");
    }

    /**
     * Shortcut function that puts the input string through both chatCondense() and chatAmpersandFilter() in respective order.
     * [Shortcut method to chatCondenseAndAmpersand(text, escape) with escape set to false]
     *
     * @param text The chat message
     * @return the chat message, put through chatCondense() and chatAmpersandFilter().
     */
    public static String chatCondenseAndAmpersand(String text) {
        return chatCondenseAndAmpersand(text, false);
    }

    /**
     * Shortcut function that puts the input string through both chatCondense() and chatAmpersandFilter() in respective order.
     * [Shortcut method to chatCondenseAndAmpersand(text, escape) with escape set to false]
     *
     * @param text The chat message
     * @param escape Determines whether existing ampersands in the message will be escaped with a backslash
     * @return the chat message, put through chatCondense() and chatAmpersandFilter().
     */
    public static String chatCondenseAndAmpersand(String text, boolean escape) {
        return chatAmpersandFilter(chatCondense(text), escape);
    }

    /**
     * Removes all formatting from a message and trims it.
     *
     * @param text The chat message
     * @return the chat message, formatting removed and trimmed
     */
    public static String chatMinimalize(String text) {
        return chatRemoveFormatting(text).trim();
    }

    /**
     * Returns the output of chatMinimalize() converted to lowercase
     *
     * @param text The chat message
     * @return the chat message, formatting removed, lowercase and trimmed
     */
    public static String chatMinimalizeLowercase(String text) {
        return chatMinimalize(text).toLowerCase();
    }

    /**
     * Converts non-escaped ampersands with a subsequent valid formatting character in a string to formatting codes
     * [Shortcut method to chatFromAmpersand(text, escape) with escape set to false]
     *
     * @param input The chat message
     * @return the chat message, valid ampersands converted to formatting codes
     */
    public static String chatFromAmpersand(String input) {
        return chatFromAmpersand(input, false);
    }

    /**
     * Converts non-escaped ampersands with a subsequent valid formatting character in a string to formatting codes
     * [Shortcut method to chatFromAmpersand(text, escape) with escape set to false]
     *
     * @param input The chat message
     * @param unescape Determines whether backslashes used to escape ampersands will be removed.
     * @return the chat message, valid ampersands converted to formatting codes
     */
    public static String chatFromAmpersand(String input, boolean unescape) {
        input = input.replaceAll("(?<!\\\\)(?:((?:\\\\\\\\)*))&([0-9a-fA-FkKlLmMnNoOrR])", "$1" + FORMAT_SYMBOL + "$2");
        if (unescape) {
            input = input.replaceAll("\\\\(&[0-9a-fA-FkKlLmMnNoOrR])", "$1");
        }
        return input;
    }
}
