package cc.dyspore.plex.core.util;

import cc.dyspore.plex.Plex;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.awt.Desktop;
import java.net.URI;
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
	
	public static Integer clamp(Integer num, Integer min, Integer max) {
		return ((min == null && max == null) ? num : (min == null ? (num <= max ? num : max) : (max == null ? (num >= min ? num : min) : (num >= min ? (num <= max ? num : max) : min))));
	}
	
	public static Float clamp(Float num, Float min, Float max) {
		return ((min == null && max == null) ? num : (min == null ? (num <= max ? num : max) : (max == null ? (num >= min ? num : min) : (num >= min ? (num <= max ? num : max) : min))));
	}
	
	public static Long clamp(Long num, Long min, Long max) {
		return ((min == null && max == null) ? num : (min == null ? (num <= max ? num : max) : (max == null ? (num >= min ? num : min) : (num >= min ? (num <= max ? num : max) : min))));
	}

	// colour operations

	// misc

	public static List<String> matchStringToList(String input, List<String> list) {
		ArrayList<String> matches = new ArrayList<String>();
		for (String item : list) {
			if (item.toLowerCase().contains(input.toLowerCase())) {
				matches.add(item);
			}
			if (item.equalsIgnoreCase(input)) {
				List<String> output = new ArrayList<String>();
				output.add(item);
				return output;
			}
		}
		if (matches.size() < 2) {
			return matches;
		}
		ArrayList<String> prefixMatches = new ArrayList<String>();
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
	
	public static <T> List<T> listPage(Integer page, Integer pageSize, List<T> list) {
		List<T> output = new ArrayList<T>();
		Integer index = (page - 1) * pageSize;
		for (Integer indexAdd = 0; indexAdd < pageSize; indexAdd++) {
			if (index + indexAdd >= list.size()) {
				break;
			}
			output.add(list.get(index + indexAdd));
		}
		return output;
	}
	
	public static <T> int listPageCount(Integer pageSize, List<T> list) {
		if (list.size() == 0) {
			return 1;
		}
		return 1 + (int) Math.floor(((double) list.size() - 1.0D) / Double.valueOf(pageSize));
	}
	
	public static int listSizePage(Integer pageSize, Integer itemCount) {
		if (itemCount == 0) {
			return 1;
		}
		return 1 + (int) Math.floor((Double.valueOf(itemCount) - 1.0D) / Double.valueOf(pageSize));
	}

	// network
	
	public static void openWebsite(String url) {
		try {
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
			URI uri = new URL(url).toURI();
			desktop.browse(uri);
		}
		catch (Exception e) {}
	}

	// data

	public static String readScoreboardTitle() {
		Scoreboard scoreboard = Plex.minecraft.theWorld != null ? Plex.minecraft.theWorld.getScoreboard() : null;
		ScoreObjective objective = scoreboard != null ? scoreboard.getObjectiveInDisplaySlot(1) : null;
		return objective != null ? objective.getDisplayName() : null;
	}

	public static String readTablistHeader() {
		return (String)ObfuscationReflectionHelper.getPrivateValue(GuiPlayerTabOverlay.class, Plex.minecraft.ingameGUI.getTabList(), "header");
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
