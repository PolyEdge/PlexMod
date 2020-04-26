package cc.dyspore.plex.core.regex;

import cc.dyspore.plex.core.util.PlexUtilCache;
import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.util.IChatComponent;

import java.util.*;
import java.util.regex.Pattern;

public class PlexRegexPattern {
    public static char FORMAT_SYMBOL_CHAR = (char) 167;
    public static String FORMAT_SYMBOL = String.valueOf(FORMAT_SYMBOL_CHAR);

    public PlexUtilCache<String, Matcher> cache = new PlexUtilCache<>();

    public String expression;
    public Pattern pattern;

    public Set<PlexRegex.ChatGroup> tags;
    public Set<InputFlag> flags;
    public List<String> groups;

    public PlexRegexPattern(String expression) {
        this.expression = PlexUtilChat.chatFromAnyAmpersand(expression, true);
        this.tags = new HashSet<>();
        this.flags = new HashSet<>();
        this.groups = new ArrayList<>();
        this.build();
    }

    private void build() {
        this.pattern = Pattern.compile(this.expression, this.flags.contains(InputFlag.CASE_IGNORE) ? Pattern.CASE_INSENSITIVE : 0);
    }

    public PlexRegexPattern tag(PlexRegex.ChatGroup group) {
        this.tags.add(group);
        return this;
    }

    public PlexRegexPattern flag(InputFlag flag) {
        this.flags.add(flag);
        if (flag == InputFlag.CASE_IGNORE) {
            this.build();
        }
        return this;
    }

    public PlexRegexPattern group(int index, String name) {
        while (index <= this.groups.size()) {
            this.groups.add(null);
        }
        this.groups.set(index, name);
        return this;
    }

    public boolean hasTag(PlexRegex.ChatGroup group) {
        return this.tags.contains(group);
    }

    public boolean hasFlag(InputFlag flag) {
        return this.flags.contains(flag);
    }

    public boolean hasGroup(String group) {
        return this.groups.contains(group);
    }

    private String prepare(String input) {
        if (this.hasFlag(InputFlag.STRIPPED)) {
            input = PlexUtilChat.chatMinimalize(input);
        }
        else {
            if (!this.hasFlag(InputFlag.INCLUDE_R)) {
                input = input.replaceAll(FORMAT_SYMBOL + "[rR]","");
            }
            if (!this.hasFlag(InputFlag.INCLUDE_F)) {
                input = input.replaceAll(FORMAT_SYMBOL + "[fF]","");
            }
        }
        if (this.hasFlag(InputFlag.LOWERCASE)) {
            input = input.toLowerCase();
        }
        return input;
    }

    public Matcher getMatcher(IChatComponent ichatcomponent) {
        return this.getMatcher(PlexUtilChat.getFormattedChatText(ichatcomponent));
    }

    public Matcher getMatcher(String string) {
        Matcher matcher = this.cache.get(string);
        if (matcher != null) {
            return matcher;
        }
        return this.cache.put(string, new Matcher(string));
    }

    public void clearCache() {
        this.cache.clear();
    }

    public boolean equals(PlexRegex.Chat entry) {
        return this.equals(entry.pattern);
    }

    public enum InputFlag {
        LOWERCASE,
        STRIPPED,
        CASE_IGNORE,
        INCLUDE_R,
        INCLUDE_F
    }

    public class Matcher {
        public String input;
        public String prepared;
        public java.util.regex.Matcher matcher;

        private boolean found = false;
        private boolean matches = false;

        private String match = null;
        private Map<String, String> groups = new HashMap<>();

        public Matcher(String input) {
            this.input = input;
            this.prepared = PlexRegexPattern.this.prepare(input);
            this.matcher = PlexRegexPattern.this.pattern.matcher(this.prepared);
        }

        public boolean find() {
            if (this.found) {
                return this.matches;
            }
            this.found = true;
            this.matches = this.matcher.find();
            if (!this.matches) {
                return false;
            }
            this.match = this.matcher.group(0);
            int count = this.matcher.groupCount();
            for (int i = 0; i < count; i++) {
                this.groups.put(PlexRegexPattern.this.groups.get(i), this.matcher.group(i + 1));
            }
            return true;
        }

        public boolean hasMatch() {
            this.find();
            return this.matches;
        }

        public String fullMatch() {
            this.find();
            return this.match;
        }

        public boolean hasGroup(String group) {
            this.find();
            return this.groups.containsKey(group);
        }

        public String group(String group) {
            this.find();
            return this.groups.getOrDefault(group, null);
        }
    }
}
