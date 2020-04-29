package cc.dyspore.plex.core.util;

import cc.dyspore.plex.Plex;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.*;
//import java.util.HashMap;


public class PlexUtil {
	// command builder

	public static String buildCommand(List<String> args) {
	    StringJoiner joiner = new StringJoiner(" ", "", "");
	    for (String arg : args) {
	    	joiner.add(arg);
	    }
	    return joiner.toString();
	}
	
	public static String buildCommand(String[] args) {
	    StringJoiner joiner = new StringJoiner(" ", "", "");
	    for (String arg : args) {
	    	joiner.add(arg);
	    }
	    return joiner.toString();
	}

	// clamping
	
	public static int clamp(int num, int min, int max) {
		return Math.max(min, Math.min(max, num));
	}
	
	public static float clamp(float num, float min, float max) {
		return Math.max(min, Math.min(max, num));
	}

	public static long clamp(long num, long min, long max) {
		return Math.max(min, Math.min(max, num));
	}

	// colour operations

	// misc

	public static List<String> matchStringToList(String input, List<String> list) {
		ArrayList<String> matches = new ArrayList<>();
		for (String item : list) {
			if (item.toLowerCase().contains(input.toLowerCase())) {
				matches.add(item);
			}
			if (item.equalsIgnoreCase(input)) {
				List<String> output = new ArrayList<>();
				output.add(item);
				return output;
			}
		}
		if (matches.size() < 2) {
			return matches;
		}
		ArrayList<String> prefixMatches = new ArrayList<>();
		for (String item : matches) {
			if (item.toLowerCase().startsWith(input.toLowerCase())) {
				prefixMatches.add(item);
			}
		}
		if (prefixMatches.size() == 0) {
			return matches;
		}
		return prefixMatches;
	}
	
	public static String shortHandTimeMs(Long time) {
		if (time < 60000L) {
			return (time / 1000L) + "s";
		}
		if (time < 3600000L) {
			return (time / 60000L) + "m";
		}
		if (time < 86400000L) {
			return (time / 3600000L) + "h";
		}
		if (time < 31536000000L) {
			return (time / 86400000L) + "d";
		}
		return (time / 31536000000L) + "y";
	}

	// pages
	
	public static <T> List<T> listPage(int page, int pageSize, List<T> list) {
		List<T> output = new ArrayList<>();
		int index = (page - 1) * pageSize;
		for (int indexAdd = 0; indexAdd < pageSize; indexAdd++) {
			if (index + indexAdd >= list.size()) {
				break;
			}
			output.add(list.get(index + indexAdd));
		}
		return output;
	}
	
	public static <T> int listPageCount(int pageSize, List<T> list) {
		if (list.size() == 0) {
			return 1;
		}
		return 1 + (int) Math.floor(((double) list.size() - 1.0D) / (double) pageSize);
	}
	
	public static int listSizePage(int pageSize, int itemCount) {
		if (itemCount == 0) {
			return 1;
		}
		return 1 + (int) Math.floor(((double) itemCount - 1.0D) / (double) pageSize);
	}

	// network
	
	public static void openURL(String url) {
		if (!Desktop.isDesktopSupported()) {
			return;
		}
		Desktop desktop = Desktop.getDesktop();
		if (!desktop.isSupported(Desktop.Action.BROWSE)) {
			return;
		}
		if (!(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"))) {
			url = "http://" + url;
		}
		try {
			URI uri = new URL(url).toURI();
			desktop.browse(uri);
		}
		catch (IOException | URISyntaxException ignored) {}
	}

	// data

	public static String readScoreboardTitle() {
		Scoreboard scoreboard = Plex.minecraft.theWorld != null ? Plex.minecraft.theWorld.getScoreboard() : null;
		ScoreObjective objective = scoreboard != null ? scoreboard.getObjectiveInDisplaySlot(1) : null;
		return objective != null ? objective.getDisplayName() : null;
	}

	public static IChatComponent readTablistHeader() {
		try {
			Field tabHeaderField = Plex.minecraft.ingameGUI.getTabList().getClass().getDeclaredField("header");
			tabHeaderField.setAccessible(true);
			return (IChatComponent)tabHeaderField.get(Plex.minecraft.ingameGUI.getTabList());
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			return null;
		}

	}

	// time

	public static OffsetDateTime getCurrentTime() {
		try {
			return OffsetDateTime.now();
		}
		catch (Throwable e) {
			return null;
		}
	}
}
