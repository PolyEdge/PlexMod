package pw.ipex.plex.core;

import java.awt.Desktop;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.*;
//import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import akka.actor.SupervisorStrategy;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import pw.ipex.plex.Plex;

public class PlexCoreUtils {
	public static ConcurrentHashMap<String, String> nameToUuid = new ConcurrentHashMap<String, String>();
	public static ConcurrentHashMap<String, String> uuidToName = new ConcurrentHashMap<String, String>();
	public static ConcurrentHashMap<String, Long> uuidLookupTimes = new ConcurrentHashMap<String, Long>();
	public static ConcurrentHashMap<String, Long> nameLookupTimes = new ConcurrentHashMap<String, Long>();
	public static ConcurrentHashMap<String, Long> skinLookupTimes = new ConcurrentHashMap<String, Long>();
	public static ConcurrentHashMap<String, ResourceLocation> uuidToTexture = new ConcurrentHashMap<String, ResourceLocation>();
	public static ConcurrentHashMap<String, ResourceLocation> nameDefaultSkins = new ConcurrentHashMap<String, ResourceLocation>();
	public static ConcurrentHashMap<String, ResourceLocation> uuidDefaultSkins = new ConcurrentHashMap<String, ResourceLocation>();
	public static Map<String, Integer> colourCode = new HashMap<>();
	
	public static String FORMAT_SYMBOL = Character.toString ((char) 167);

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


	public static void chatAddMessage(String message, ICommandSender sender) {
		try {
			sender.addChatMessage(new ChatComponentText(message));
		}
		catch (NullPointerException e) {
		}
	}
	
	public static void chatAddMessage(String message) {
		try {
			Plex.minecraft.thePlayer.addChatComponentMessage(new ChatComponentText(message));
		}
		catch (NullPointerException e) {
		}
	}
	
	public static String chatStyleText(String ...args) {
		String valueText = args[(args.length - 1)];
		List<String> styles = Arrays.asList(args).subList(0, (args.length - 1));
		String outputText = EnumChatFormatting.RESET + "";
		String[] words = valueText.split("\\s", -1);
		String output = "";
		for (String word : words) {
			String outputWord = "";
			for (String style : styles) {
				outputWord += EnumChatFormatting.valueOf(style.toUpperCase());
			}
			outputWord += word;
			if (!output.equals("")) {
				output += " " + outputWord;
			}
			else {
				output += outputWord;
			}
		}
		outputText = output + EnumChatFormatting.RESET;
		return outputText;
	}
	
	public static String chatPlexPrefix() {
		return chatStyleText("GOLD", "Plex") + chatStyleText("BLACK", ">") + " ";
	}
	
	public static String getUiChatMessage(String message) {
		if (message.equalsIgnoreCase("plex.modInfo")) {
			return chatPlexPrefix() + chatStyleText("GRAY", "Plex v" + Plex.VERSION + (Plex.PATCHID == null ? "" : "-" + Plex.PATCHID) + " ") + chatStyleText("GRAY", ">> ") + chatStyleText("GOLD", "@PolyEdge/cysk ")  + "\n" + 
					chatPlexPrefix() + chatStyleText("GRAY", "Use ") + chatStyleText("AQUA", "/plex help") + chatStyleText("GRAY", " to open mod help menu");			
		}
		if (message.equalsIgnoreCase("plex.unsupportedServer")) {
			return chatPlexPrefix() + chatStyleText("DARK_RED", "Log on to Mineplex and try again!");
		}
		if (message.equalsIgnoreCase("plex.unsupportedServerStrangeAddress")) {
			return chatPlexPrefix() + chatStyleText("DARK_RED", "Log on to Mineplex and try again!") +
					chatPlexPrefix() + chatStyleText("DARK_RED", "Please note that the server address you're connecting to must contain mineplex.com for the mod to work");
		}
		if (message.equalsIgnoreCase("plex.nullModCommand")) {
			return chatPlexPrefix() + chatStyleText("DARK_RED", "Unknown command. Try using /plex help");
		}
		return chatPlexPrefix() + chatStyleText("GRAY", message);
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
	public String chatFromAmpersand(String input) {
		return chatFromAmpersand(input, false);
	}

	/**
	 * Converts non-escaped ampersands with a subsequent valid formatting character in a string to formatting codes
	 * [Shortcut method to chatFromAmpersand(text, escape) with escape set to false]
	 *
	 * @param input The chat message
	 * @param escape Determines whether backslashes used to escape ampersands will be removed.
	 * @return the chat message, valid ampersands converted to formatting codes
	 */
	public String chatFromAmpersand(String input, boolean escape) {
		input = input.replaceAll("(?<!\\\\)(?:((?:\\\\\\\\)*))&([0-9a-fA-FkKlLmMnNoOrR])", "$1" + FORMAT_SYMBOL + "$2");
		if (escape) {
			input = input.replaceAll("\\\\(&[0-9a-fA-FkKlLmMnNoOrR])", "$1");
		}
		return input;
	}

	/*    old poorly written chat management
	
	public static String condenseChatAmpersandFilter(String text) {
		return text.replace(FORMAT_SYMBOL, "&").replaceAll("\\&f|\\&r", "");
	}
	
	public static String condenseChatFilter(String text) {
		return text.replace(FORMAT_SYMBOL + "f", "").replace(FORMAT_SYMBOL + "r", "");
	}
	
	public static String ampersandToFormatCharacter(String input) {
		return input.replace("&", FORMAT_SYMBOL);
	}
	
	public static String removeFormatting(String text) {
		return text.replaceAll("&[0-9a-zA-Z]", "");
	}
	
	public static String minimalize(String text) {
		return removeFormatting(condenseChatAmpersandFilter(text)).trim().toLowerCase();
	}
	
	public static String minimalizeKeepCase(String text) {
		return removeFormatting(condenseChatAmpersandFilter(text)).trim();
	} */
	
	public static Boolean isChatMessage(byte messageType) {
		return (messageType == (byte) 0) || (messageType == (byte) 1);
	}
	
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
	
	public static Integer intRange(Integer num, Integer min, Integer max) {
		return ((min == null && max == null) ? num : (min == null ? (num <= max ? num : max) : (max == null ? (num >= min ? num : min) : (num >= min ? (num <= max ? num : max) : min))));
	}
	
	public static Float floatRange(Float num, Float min, Float max) {
		return ((min == null && max == null) ? num : (min == null ? (num <= max ? num : max) : (max == null ? (num >= min ? num : min) : (num >= min ? (num <= max ? num : max) : min))));
	}
	
	public static Long longRange(Long num, Long min, Long max) {
		return ((min == null && max == null) ? num : (min == null ? (num <= max ? num : max) : (max == null ? (num >= min ? num : min) : (num >= min ? (num <= max ? num : max) : min))));
	}
	
	public static Integer colourCodeFrom(Integer r, Integer g, Integer b, Integer a) {
		return (r << 16) + (g << 8) + (b) + (a << 24);
	}
	
	public static Integer[] rgbCodeFrom(Integer colourCode) {
		Integer[] values = {((colourCode >> 16) & 255), ((colourCode >> 8) & 255), (colourCode & 255), ((colourCode >> 24) & 255)};
		return values;
	}
	
	public static Integer betweenColours(Integer colour1, Integer colour2, Float between) {
		Integer[] rgbsColour1 = PlexCoreUtils.rgbCodeFrom(colour1);
		Integer[] rgbsColour2 = PlexCoreUtils.rgbCodeFrom(colour2);
		Integer colourR = rgbsColour1[0] + ((int) (between * (rgbsColour2[0] - rgbsColour1[0])));
		Integer colourG = rgbsColour1[1] + ((int) (between * (rgbsColour2[1] - rgbsColour1[1])));
		Integer colourB = rgbsColour1[2] + ((int) (between * (rgbsColour2[2] - rgbsColour1[2])));
		Integer colourA = rgbsColour1[3] + ((int) (between * (rgbsColour2[3] - rgbsColour1[3])));
		return colourCodeFrom(colourR, colourG, colourB, colourA);
	}
	
	public static Integer betweenColours(Integer colour1, Integer colour2, Double between) {
		if (between < 0) {
			between = 0.0D;
		}
		if (between > 1) {
			between = 1.0D;
		}
		Integer[] rgbsColour1 = PlexCoreUtils.rgbCodeFrom(colour1);
		Integer[] rgbsColour2 = PlexCoreUtils.rgbCodeFrom(colour2);
		Integer colourR = rgbsColour1[0] + ((int) (between * (rgbsColour2[0] - rgbsColour1[0])));
		Integer colourG = rgbsColour1[1] + ((int) (between * (rgbsColour2[1] - rgbsColour1[1])));
		Integer colourB = rgbsColour1[2] + ((int) (between * (rgbsColour2[2] - rgbsColour1[2])));
		Integer colourA = rgbsColour1[3] + ((int) (between * (rgbsColour2[3] - rgbsColour1[3])));
		return colourCodeFrom(colourR, colourG, colourB, colourA);
	}
	
	public static Integer replaceColour(Integer colour, Integer r, Integer g, Integer b, Integer a) {
		Integer[] rgba = rgbCodeFrom(colour);
		return colourCodeFrom(r == null ? rgba[0] : r, g == null ? rgba[1] : g, b == null ? rgba[2] : b, a == null ? rgba[3] : a);
	}
	
	public static Integer[] getChromaRGB(Double i) {
		Integer i2 = (int) (i % 1531);
		return getChromaRGB(i2);
	}
	
	public static Integer[] getChromaRGB(int i) {
		i = i % 1531;
		Integer red = 0;
		Integer green = 0;
		Integer blue = 0;
		if (i <= 510) {
			red = i <= 255 ? 255 : (510 - i);
			green = i <= 255 ? i : 255;			
		}
		if ((i > 510) && (i <= 1020)) {
			green = i <= 765 ? 255 : (1020 - i);
			blue = i <= 765 ? i - 510 : 255;			
		}
		if ((i > 1020) && (i <= 1530)) {
			blue = i <= 1275 ? 255 : (1530 - i);
			red = i <= 1275 ? i - 1020 : 255;
		}
		return new Integer[] {red, green, blue};
	}
	
	public static Integer globalChromaCycle() {
		Integer[] chromaRGB = getChromaRGB((Minecraft.getSystemTime() / 100.0D * 20.0D));
		return PlexCoreUtils.colourCodeFrom(chromaRGB[0], chromaRGB[1], chromaRGB[2], 255);
	}
	
	public static Integer multiplyColour(int colour, float mul) {
		return multiplyColour(colour, mul, false);
	}
	
	public static Integer multiplyColour(int colour, float mul, boolean alpha) {
		Integer[] newColour = PlexCoreUtils.rgbCodeFrom(colour);
		return PlexCoreUtils.colourCodeFrom(PlexCoreUtils.intRange((int)(newColour[0] * mul), 0, 255), PlexCoreUtils.intRange((int)(newColour[1] * mul), 0, 255), PlexCoreUtils.intRange((int)(newColour[2] * mul), 0, 255), alpha ? PlexCoreUtils.intRange((int)(newColour[3] * mul), 0, 255) : newColour[3]);
	}
	
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
	
	public static <T> Integer listPageCount(Integer pageSize, List<T> list) {
		if (list.size() == 0) {
			return 1;
		}
		return 1 + (int) Math.floor((Double.valueOf(list.size()) - 1.0D) / Double.valueOf(pageSize));
	}
	
	public static Integer listSizePage(Integer pageSize, Integer itemCount) {
		if (itemCount == 0) {
			return 1;
		}
		return 1 + (int) Math.floor((Double.valueOf(itemCount) - 1.0D) / Double.valueOf(pageSize));
	}
	
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
	
	public static ResourceLocation getDefaultSkin() {
		return DefaultPlayerSkin.getDefaultSkinLegacy();
	}
	
	public static ResourceLocation getDefaultSkin(String input) {
		return DefaultPlayerSkin.getDefaultSkin(UUID.nameUUIDFromBytes(input.getBytes()));
	}

	public static ResourceLocation getSkin(String player) {
		return getSkin(new GameProfile(null, player));
	}
	
	public static ResourceLocation profileDefaultSkin(GameProfile profile) {
		Boolean uuidNull = (profile.getId() == null);
		Boolean nameNull = (profile.getName() == null);
		if (nameNull && uuidNull) {
			return getDefaultSkin();
		}
		if (!uuidNull) {
			if (uuidDefaultSkins.containsKey(profile.getId().toString())) {
				return uuidDefaultSkins.get(profile.getId().toString());
			}
		}
		if (!nameNull) {
			if (nameDefaultSkins.containsKey(profile.getName().toLowerCase())) {
				return nameDefaultSkins.get(profile.getName().toLowerCase());
			}
		}
		if (!nameNull) {
			nameDefaultSkins.put(profile.getName().toLowerCase(), getDefaultSkin(profile.getName().toLowerCase()));
			return nameDefaultSkins.get(profile.getName().toLowerCase());
		}
		uuidDefaultSkins.put(profile.getId().toString(), getDefaultSkin(profile.getId().toString()));
		return uuidDefaultSkins.get(profile.getId().toString());
	}

	public static String getUUID(String ign) {
		return getUUID(ign, true);
	}
	
	public static String getUUID(String ign, boolean requestIfNeeded) {
		String friendlyIgn = ign.toLowerCase();
		if (nameToUuid.get(friendlyIgn) == null) {
			NetworkPlayerInfo playerInfo = Plex.minecraft.getNetHandler().getPlayerInfo(ign);
			if (playerInfo != null) {
				if (playerInfo.getGameProfile().getId() != null) {
					nameToUuid.put(friendlyIgn, playerInfo.getGameProfile().getId().toString());
					uuidToName.put(playerInfo.getGameProfile().getId().toString(), friendlyIgn);
				}
			}
		}
		if (nameToUuid.get(friendlyIgn) != null) {
			return nameToUuid.get(friendlyIgn);
		}
		if (uuidLookupTimes.get(friendlyIgn) == null) {
			uuidLookupTimes.put(friendlyIgn, 0L);
		}
		if (Minecraft.getSystemTime() > uuidLookupTimes.get(friendlyIgn) + 30000L && requestIfNeeded) {
			uuidLookupTimes.put(friendlyIgn, Minecraft.getSystemTime());
			new Thread(new Runnable() {
				public void run() {
					PlexCoreUtils.fetchUUID(ign);
				}
			}).start();
		}
		if (nameToUuid.containsKey(friendlyIgn)) {
			return nameToUuid.get(friendlyIgn);
		}
		return null;
	}

	public static String getName(String playerUuid) {
		return getName(playerUuid, true);
	}

	public static String getName(String playerUuid, boolean requestIfNeeded) {
		final String uuid = toFormattedUUID(playerUuid);
		if (uuidToName.get(uuid) == null) {
			NetworkPlayerInfo playerInfo = Plex.minecraft.getNetHandler().getPlayerInfo(UUID.fromString(uuid));
			if (playerInfo != null) {
				if (playerInfo.getGameProfile().getName() != null) {
					nameToUuid.put(playerInfo.getGameProfile().getName(), uuid);
					uuidToName.put(uuid, playerInfo.getGameProfile().getName());
				}
			}
		}
		if (uuidToName.get(uuid) != null) {
			return nameToUuid.get(uuid);
		}
		if (nameLookupTimes.get(uuid) == null) {
			nameLookupTimes.put(uuid, 0L);
		}
		if (Minecraft.getSystemTime() > nameLookupTimes.get(uuid) + 30000L && requestIfNeeded) {
			nameLookupTimes.put(uuid, Minecraft.getSystemTime());
			new Thread(new Runnable() {
				public void run() {
					PlexCoreUtils.fetchIGN(uuid);
				}
			}).start();
		}
		if (uuidToName.containsKey(uuid)) {
			return uuidToName.get(uuid);
		}
		return null;
	}
	
	public static ResourceLocation getSkin(final GameProfile player) {	
		return getSkin(player, true);
	}
	
	public static ResourceLocation getSkin(final GameProfile player, boolean requestIfNeeded) {
		if (player == null) {
			return getDefaultSkin();
		}
		if (player.getId() == null && player.getName() == null) {
			return getDefaultSkin();
		}
		String playerUUID = null;
		if (player.getId() == null) {
			playerUUID = getUUID(player.getName().toLowerCase(), true);
		}
		else {
			playerUUID = player.getId().toString();
		}
		if (playerUUID == null) {
			return profileDefaultSkin(player);
		}
		if (uuidToTexture.containsKey(playerUUID)) {
			return uuidToTexture.get(playerUUID);
		}
		if (skinLookupTimes.get(playerUUID) == null) {
			skinLookupTimes.put(playerUUID, 0L);
		}
		if (Minecraft.getSystemTime() > skinLookupTimes.get(playerUUID) + 30000L && requestIfNeeded) {
			skinLookupTimes.put(playerUUID, Minecraft.getSystemTime());
			final GameProfile playerGameProfile = new GameProfile(UUID.fromString(playerUUID), player.getName());
			new Thread(new Runnable() {
				public void run() {
					PlexCoreUtils.fetchSkin(playerGameProfile);
				}
			}).start();
		}
		if (uuidToTexture.containsKey(playerUUID)) {
			return uuidToTexture.get(playerUUID);
		}
		return profileDefaultSkin(player);
	}
	
	public static String fetchUUID(String ign) {
		if (nameToUuid.containsKey(ign)) {
			return nameToUuid.get(ign);
		}
		//PlexCoreUtils.chatAddMessage("fetching uuid for " + ign);
		try {
			URL uuidAPI = new URL("https://api.mojang.com/users/profiles/minecraft/" + ign);
			InputStreamReader uuidInputReader = new InputStreamReader(uuidAPI.openStream());
			JsonReader jsonReader = new JsonReader(uuidInputReader);
			JsonParser jsonParser = new com.google.gson.JsonParser();
			JsonElement apiResponse = jsonParser.parse(jsonReader);
			uuidInputReader.close();
			final String playerUUID = apiResponse.getAsJsonObject().get("id").getAsString();
			final String formattedPlayerUUID = toFormattedUUID(playerUUID);
			nameToUuid.put(ign.toLowerCase(), formattedPlayerUUID);
			uuidToName.put(formattedPlayerUUID, ign);
			return playerUUID;
		}
		catch (Throwable e) {
			Plex.logger.info("[PlexCoreUtils] exception getting player uuid for " + ign);
			Plex.logger.info(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public static String fetchIGN(String uuid) {
		uuid = toFormattedUUID(uuid);
		if (uuidToName.containsKey(uuid)) {
			return uuidToName.get(uuid);
		}
		//PlexCoreUtils.chatAddMessage("fetching uuid for " + ign);
		try {
			URL nameAPI = new URL("https://api.mojang.com/user/profiles/" + toUnformattedUUID(uuid) + "/names");
			InputStreamReader uuidInputReader = new InputStreamReader(nameAPI.openStream());
			JsonReader jsonReader = new JsonReader(uuidInputReader);
			JsonParser jsonParser = new com.google.gson.JsonParser();
			JsonElement apiResponse = jsonParser.parse(jsonReader);
			uuidInputReader.close();
			JsonArray names = apiResponse.getAsJsonArray();
			final String playerName = names.get(names.size() - 1).getAsJsonObject().get("name").getAsString();
			nameToUuid.put(playerName, uuid);
			uuidToName.put(uuid, playerName);
			return playerName;
		}
		catch (Throwable e) {
			Plex.logger.info("[PlexCoreUtils] exception getting player name for " + uuid);
			Plex.logger.info(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
		}
		return null;
	}
	
	public static void fetchSkin(final GameProfile player) {
		if (player.getId() == null) {
			return;
		}
		final String uuid = player.getId().toString();
		if (uuidToTexture.containsKey(uuid)) {
			return;
		}
		//PlexCoreUtils.chatAddMessage("loading skin " + player.getId() + (player.getName() == null ? "" : " // " + player.getName()));
		Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> skinCache = Plex.minecraft.getSkinManager().loadSkinFromCache(player);
		if (skinCache.containsKey(MinecraftProfileTexture.Type.SKIN)) {
			PlexCoreUtils.uuidToTexture.put(uuid, Plex.minecraft.getSkinManager().loadSkin((MinecraftProfileTexture) skinCache.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN));
			return;
		}
		
		GameProfile profile = Plex.minecraft.getSessionService().fillProfileProperties(player, true);
		
		if (profile.getName() != null) {
			if (!PlexCoreUtils.nameToUuid.containsKey(profile.getName().toLowerCase())) {
				PlexCoreUtils.nameToUuid.put(profile.getName().toLowerCase(), uuid);
			}
		}
		
		Plex.minecraft.getSkinManager().loadProfileTextures(profile, new SkinManager.SkinAvailableCallback() {
			public void skinAvailable(MinecraftProfileTexture.Type type, ResourceLocation resourceLocation, MinecraftProfileTexture profileTexture) {
				if (type == MinecraftProfileTexture.Type.SKIN) {
					uuidToTexture.put(player.getId().toString(), resourceLocation);
				}
			}
		}, true);		
	}
	
	public static String toFormattedUUID(String uuid) {
		if (uuid.length() == 32) {
			return uuid.substring(0, 8) + '-' + uuid.substring(8, 12) + '-' + uuid.substring(12, 16) + '-' + uuid.substring(16, 20) + '-' + uuid.substring(20, 32);
				   // 8 chars                   // 4 chars                    // 4 chars                     // 4 chars               // 12 chars
		}
		if (uuid.length() == 36) {
			return uuid;
		}
		
// thanks java ;(
//		   UUID                   = <time_low> "-" <time_mid> "-" <time_high_and_version> "-" <variant_and_sequence> "-" <node>
//         time_low               = 4*<hexOctet>
//         time_mid               = 2*<hexOctet>
//         time_high_and_version  = 2*<hexOctet>
//         variant_and_sequence   = 2*<hexOctet>
//         node                   = 6*<hexOctet>
//         hexOctet               = <hexDigit><hexDigit>
		return null;
	}

	public static String toUnformattedUUID(String uuid) {
		return uuid.replace("-", "");
	}
}
