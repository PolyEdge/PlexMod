package pw.ipex.plex.core.regex;

import pw.ipex.plex.Plex;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public abstract class PlexCoreRegexParsedEntry extends PlexCoreRegexEntry {
    public PlexCoreRegexParsedEntry() {
    }

    public PlexCoreRegexParsedEntry(String idTag) {
       this.tag(idTag);
    }

    public String getMinified(String input) {
        return this.clear(input, this.hidesWhite(), this.hidesReset());
    }

    @Deprecated
    public PlexCoreRegexEntry addField(int group, String name) {
        return this;
    }

    public boolean hasField(String field) {
        return this.patternNames.keySet().contains(field);
    }

    public boolean matches(String string) {
        Map<String, String> result = this.parse(string);
        return result != null;
    }

    public String getField(String input, String field) {
        try {
            return this.getAllFields(input).get(field);
        }
        catch (Throwable e) {
            return null;
        }
    }

    public Map<String, String> getAllFields(String input) {
        Map<String, String> output = this._parse(input);
        if (output == null) {
            output = new HashMap<>();
        }
        return output;
    }

    public String formatStringWithGroups(String messageInput, String formattingString) {
        formattingString = this.fromAmpersand(formattingString);
        Map<String, String> groups = this.getAllFields(messageInput);
        for (String groupName : groups.keySet()) {
            formattingString = formattingString.replace("{" + groupName + "}", groups.get(groupName) != null ? groups.get(groupName) : "");
            formattingString = formattingString.replace("{$" + groupName + "}", groups.get(groupName) != null ? groups.get(groupName).replaceAll(FORMAT_SYMBOL + "[0-9a-fA-Fklmor]", "") : "");
        }
        return formattingString;
    }

    public Map<String, String> _parse(String input) {
        try {
            return this.parse(this.prepareInputString(input));
        }
        catch (Throwable e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Plex.logger.error("ERROR! attempted to parse message via " + this.getClass().getCanonicalName() + " which unexpectedly failed with exception:\n" + sw.toString());
            return null;
        }
    }

    public boolean hidesReset() {
        return false;
    }

    public boolean hidesWhite() {
        return false;
    }

    public abstract Map<String, String> parse(String input);
}
