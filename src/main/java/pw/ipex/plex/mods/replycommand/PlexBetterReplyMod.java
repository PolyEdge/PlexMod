package pw.ipex.plex.mods.replycommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ci.PlexCommandListener;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.mineplex.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.PlexCoreValue;
import pw.ipex.plex.mod.PlexModBase;

public class PlexBetterReplyMod extends PlexModBase {
	public String MATCH_DIRECT_MESSAGE = "&6&l([a-zA-Z0-9 _]+) > ([a-zA-Z0-9 _]+)&e &e&l(.*)";
	
	public Pattern PATTERN_DIRECT_MESSAGE = Pattern.compile(MATCH_DIRECT_MESSAGE);
	
	public static Integer DEFAULT_REPLY_TIMEOUT = 300;
	public static Integer MAX_REPLY_TIMEOUT = 600;
	
	public PlexCoreValue currentConversation = new PlexCoreValue("betterReply_currentConversation", "");
	public PlexCoreValue lastConversationTime = new PlexCoreValue("betterReply_lastConversationReply", 0L);
	public PlexCoreValue lastReceived = new PlexCoreValue("betterReply_lastMessageReceived", "");	
	
	public PlexCoreValue betterReplyEnabled = new PlexCoreValue("betterReply_enabled", false);
	public PlexCoreValue replyTimeoutSeconds = new PlexCoreValue("betterReply_replyTimeout", 300);
	public List<String> sentMessagesQueue = new ArrayList<String>();
	public static List<java.util.Map.Entry<String, java.util.Map.Entry<Boolean, Long>>> contacts = new ArrayList<java.util.Map.Entry<String, java.util.Map.Entry<Boolean, Long>>>();
	public static Map<String, ArrayList<java.util.Map.Entry<String, Long>>> dmsList = new HashMap<String, ArrayList<java.util.Map.Entry<String, Long>>>();
	
	public String getModName() {
		return "Better Reply";
	}
	
	@Override
	public void modInit() {
		this.betterReplyEnabled.set(this.modSetting("better_reply_enabled", false).getBoolean());
		this.replyTimeoutSeconds.set(this.modSetting("reply_timeout_seconds", DEFAULT_REPLY_TIMEOUT).getInt());
		
		PlexCore.registerCommandListener(new PlexCommandListener("message"));
		PlexCore.registerCommandListener(new PlexCommandListener("whisper"));
		PlexCore.registerCommandListener(new PlexCommandListener("tell"));
		PlexCore.registerCommandListener(new PlexCommandListener("msg"));
		PlexCore.registerCommandListener(new PlexCommandListener("m"));
		PlexCore.registerCommandListener(new PlexCommandListener("t"));
		PlexCore.registerCommandListener(new PlexCommandListener("w"));
		
		PlexCore.registerCommandListener(new PlexCommandListener("r"));
		PlexCore.registerCommandListener(new PlexCommandListener("rr"));
		PlexCore.registerCommandListener(new PlexCommandListener("dms"));
		
		PlexBetterReplyCommand betterReplyCommand = new PlexBetterReplyCommand();
		PlexCore.registerCommandHandler("message", betterReplyCommand);
		PlexCore.registerCommandHandler("whisper", betterReplyCommand);
		PlexCore.registerCommandHandler("tell", betterReplyCommand);
		PlexCore.registerCommandHandler("msg", betterReplyCommand);
		PlexCore.registerCommandHandler("m", betterReplyCommand);
		PlexCore.registerCommandHandler("t", betterReplyCommand);
		PlexCore.registerCommandHandler("w", betterReplyCommand);
		
		PlexCore.registerCommandHandler("r", betterReplyCommand);
		PlexCore.registerCommandHandler("rr", betterReplyCommand);
		PlexCore.registerCommandHandler("dms", betterReplyCommand);
		
		Plex.plexCommand.registerPlexCommand("reply", new PlexBetterReplyCommand());
		
		PlexCore.registerUiTab("Reply", PlexBetterReplyUI.class);
		
		Plex.plexCommand.addPlexHelpCommand("reply", "Displays messagingscreen enhancements options");
		Plex.plexCommand.addHelpCommand("rr", "$partial username", "Messages somebody who you have messaged with previously (without typing full ign)");
		Plex.plexCommand.addHelpCommand("dms", "%page", "Shows DM history");
		Plex.plexCommand.addHelpCommand("dms", "$user", "%page", "Shows previous DMs between you and a user.");

		PlexCore.getCommandListener("r").setDisabled(!this.betterReplyEnabled.booleanValue);
	}
	
	public void communicate(Object ...args) {
		String arg1 = String.valueOf(args[0]);
		if (arg1.equals("addSentUser")) {
			String arg2 = String.valueOf(args[1]);
			bumpDmHistory(arg2, true);
		}
		if (arg1.equals("addToHistory")) {
			String arg2 = String.valueOf(args[1]);
			String arg3 = String.valueOf(args[2]);
			addDmToHistory(arg2, arg3, true);
		}
		if (arg1.equals("dmHistory")) {
			String arg2 = String.valueOf(args[1]);
			Integer page = null;
			try {
				page = Integer.parseInt(arg2);
			}
			catch (NumberFormatException e) {
				displayUserDmHistory(arg2, 1);
			}
			if (page != null) {
				displayGenericDmHistory(page);
			}
		}
		if (arg1.equals("dmHistoryUser")) {
			String arg2 = String.valueOf(args[1]);
			String arg3 = String.valueOf(args[2]);
			Integer page = 1;
			try {
				page = Integer.parseInt(arg3);
			}
			catch (NumberFormatException e) {}
			displayUserDmHistory(arg2, page);
		}
	}
	
	public Object receive(Object ...args) {
		String arg1 = String.valueOf(args[0]);
		if (arg1.equals("getAutoCompleteUser")) {
			String arg2 = String.valueOf(args[1]);
			ArrayList<String> users = new ArrayList<String>();
			for (java.util.Map.Entry<String, java.util.Map.Entry<Boolean, Long>> item : PlexBetterReplyMod.contacts) {
				users.add(item.getKey());
			}
			List<String> matchUsers = PlexCoreUtils.matchStringToList(arg2, users);
			if (matchUsers.size() == 0) {
				return ("$" + PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "You haven't messaged anybody matching the ign ") + PlexCoreUtils.chatStyleText("BLUE", arg2) + PlexCoreUtils.chatStyleText("GRAY", "."));
			}
			if (matchUsers.size() == 1) {
				return "%" + matchUsers.get(0);
			}
			if (matchUsers.size() > 1) {
				String outputString = PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "You have messaged with multiple people matching the ign ") + PlexCoreUtils.chatStyleText("BLUE", arg2) + PlexCoreUtils.chatStyleText("GRAY", ": ");
			    StringJoiner joiner = new StringJoiner(PlexCoreUtils.chatStyleText("GRAY", ", "), "", "");
			    for (String ign : matchUsers) {
			    	joiner.add(PlexCoreUtils.chatStyleText("GOLD", ign));
			    }
			    return "$" + outputString + joiner.toString() + PlexCoreUtils.chatStyleText("GRAY", ".");
			}
			return "$" + PlexCoreUtils.chatStyleText("GRAY", "Unknown error.");
		}
		return null;
	}
	
	public void bumpDmHistory(String user, Boolean outgoing) {
		ArrayList<java.util.Map.Entry<String, java.util.Map.Entry<Boolean, Long>>> found = new ArrayList<java.util.Map.Entry<String, java.util.Map.Entry<Boolean, Long>>>();
		for (java.util.Map.Entry<String, java.util.Map.Entry<Boolean, Long>> item : PlexBetterReplyMod.contacts) {
		    if (item.getKey().toLowerCase().equals(user.toLowerCase())) {
		        found.add(item);
		    }
		}
		PlexBetterReplyMod.contacts.removeAll(found);
		PlexBetterReplyMod.contacts.add(0, new java.util.AbstractMap.SimpleEntry<String, java.util.Map.Entry<Boolean, Long>>(user, new java.util.AbstractMap.SimpleEntry<Boolean, Long>(outgoing, Minecraft.getSystemTime())));
	}
	
	public void addDmToHistory(String user, String message, Boolean outgoing) {
		if (!dmsList.containsKey(user)) {
			dmsList.put(user, new ArrayList<java.util.Map.Entry<String, Long>>());
		}
		dmsList.get(user).add(0, new java.util.AbstractMap.SimpleEntry<String, Long>((outgoing ? "O" : "I") + message, Minecraft.getSystemTime()));
	}
	
	public void displayGenericDmHistory(Integer page) {
		List<java.util.Map.Entry<String, java.util.Map.Entry<Boolean, Long>>> dms = PlexCoreUtils.listPage(page, 8, PlexBetterReplyMod.contacts);
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========") + PlexCoreUtils.chatStyleText("GOLD", " DM History ") + PlexCoreUtils.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========"));
		if (dms.size() == 0) {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GRAY", "(No previous DMs on this page)"));
		}
		for (java.util.Map.Entry<String, java.util.Map.Entry<Boolean, Long>> item : dms) {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("DARK_GRAY", "[") + (item.getValue().getKey() ? PlexCoreUtils.chatStyleText("GRAY", "BOLD", "<-") : PlexCoreUtils.chatStyleText("LIGHT_PURPLE", "BOLD", "->")) + 
					PlexCoreUtils.chatStyleText("DARK_GRAY", "] ") + PlexCoreUtils.chatStyleText("GOLD", item.getKey()) + " " +
					PlexCoreUtils.chatStyleText("BLUE", PlexCoreUtils.shortHandTimeMs(Minecraft.getSystemTime() - item.getValue().getValue())));
		}
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GRAY", "(Page " + page + "/" + PlexCoreUtils.listSizePage(8, PlexBetterReplyMod.contacts.size()) + ")"));
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========================="));
	}
	
	public void displayUserDmHistory(String user, Integer page) {
		ArrayList<String> users = new ArrayList<String>();
		for (java.util.Map.Entry<String, java.util.Map.Entry<Boolean, Long>> item : PlexBetterReplyMod.contacts) {
			users.add(item.getKey());
		}
		List<String> matchUsers = PlexCoreUtils.matchStringToList(user, users);
		if (matchUsers.size() == 0) {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "You haven't messaged anybody matching the ign ") + PlexCoreUtils.chatStyleText("BLUE", user) + PlexCoreUtils.chatStyleText("GRAY", "."));
			return;
		}
		if (matchUsers.size() > 1) {
			String outputString = PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "You have messaged with multiple people matching the ign ") + PlexCoreUtils.chatStyleText("BLUE", user) + PlexCoreUtils.chatStyleText("GRAY", ": ");
			StringJoiner joiner = new StringJoiner(PlexCoreUtils.chatStyleText("GRAY", ", "), "", "");
			for (String ign : matchUsers) {
				joiner.add(PlexCoreUtils.chatStyleText("GOLD", ign));
			}
			PlexCoreUtils.chatAddMessage(outputString + joiner.toString() + PlexCoreUtils.chatStyleText("GRAY", "."));
		}
		String finalUser = matchUsers.get(0);
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========") + PlexCoreUtils.chatStyleText("GOLD", " DM History ") + PlexCoreUtils.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========"));
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GRAY", "Showing results for: ") + PlexCoreUtils.chatStyleText("LIGHT_PURPLE", finalUser));
		
		if (!dmsList.containsKey(finalUser)) {
			dmsList.put(finalUser, new ArrayList<java.util.Map.Entry<String, Long>>());
		}
		List<java.util.Map.Entry<String, Long>> dmsPage = PlexCoreUtils.listPage(page, 8, dmsList.get(finalUser));
		if (dmsPage.size() == 0) {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GRAY", "(No DMs found on this page)"));
		}
		for (java.util.Map.Entry<String, Long> dmItem : dmsPage) {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("DARK_GRAY", "[") + (dmItem.getKey().substring(0, 1).equals("O") ? PlexCoreUtils.chatStyleText("GRAY", "BOLD", "<-") : PlexCoreUtils.chatStyleText("LIGHT_PURPLE", "BOLD", "->")) + 
					PlexCoreUtils.chatStyleText("DARK_GRAY", "] ") + PlexCoreUtils.chatStyleText("BLUE", PlexCoreUtils.shortHandTimeMs(Minecraft.getSystemTime() - dmItem.getValue())) + " " +
					PlexCoreUtils.chatStyleText("GOLD", dmItem.getKey().substring(1)));
		}
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GRAY", "(Page " + page + "/" + PlexCoreUtils.listSizePage(8, dmsList.get(finalUser).size()) + ")"));
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========================="));

	}
	
	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent e) {
		if (!PlexCoreUtils.isChatMessage(e.type)) {
			return;
		}
		String filtered = PlexCoreUtils.condenseChatAmpersandFilter(e.message.getFormattedText());
		if (filtered.matches(this.MATCH_DIRECT_MESSAGE)) {
			Matcher matcher = PATTERN_DIRECT_MESSAGE.matcher(filtered);
			matcher.find();
			if (matcher.group(1).equalsIgnoreCase(PlexCore.getPlayerIGN())) {
				this.lastConversationTime.set(Minecraft.getSystemTime());
				this.currentConversation.set(matcher.group(2));
				//if (!this.betterReplyEnabled.booleanValue) {
				bumpDmHistory(matcher.group(2), true);		
				//}
				addDmToHistory(matcher.group(2), matcher.group(3), true);
			}
			else if (matcher.group(2).equalsIgnoreCase(PlexCore.getPlayerIGN())) {
				bumpDmHistory(matcher.group(1), false);
				addDmToHistory(matcher.group(1), matcher.group(3), false);
				if (matcher.group(1).equalsIgnoreCase(this.currentConversation.stringValue)) {
					this.lastConversationTime.set(Minecraft.getSystemTime());
				}
				if (this.replyTimeoutSeconds.integerValue >= MAX_REPLY_TIMEOUT) {
					if (this.currentConversation.stringValue.equals("")) {
						this.currentConversation.set(matcher.group(1));	
					}
					return;
				}
				if (Minecraft.getSystemTime() > this.lastConversationTime.longValue + Long.valueOf(this.replyTimeoutSeconds.integerValue)) {
					this.currentConversation.set(matcher.group(1));		
				}
			}
		}
	}
	
//	@SubscribeEvent 
//	public void onSound(final PlaySoundEvent e) {
//		if (e.name.contains("step") || e.name.contains("rain")) {
//			return;
//		}
//		PlexCoreUtils.chatAddMessage("Sound " + e.name + ": " + e.sound.getPitch());
//	}

	@Override
	public void saveModConfig() {
		this.modSetting("better_reply_enabled", false).set(this.betterReplyEnabled.booleanValue);
		this.modSetting("reply_timeout_seconds", 300).set(this.replyTimeoutSeconds.integerValue);
	}

	@Override
	public void lobbyUpdated(PlexCoreLobbyType type) {
	}
}
