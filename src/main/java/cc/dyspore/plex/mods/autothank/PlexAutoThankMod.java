package cc.dyspore.plex.mods.autothank;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.dyspore.plex.core.PlexMP;
import cc.dyspore.plex.core.util.PlexUtilChat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.commands.queue.PlexCommandQueue;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.PlexModBase;

public class PlexAutoThankMod extends PlexModBase {
	public static String MATCH_AMPLIFIER_GLOBAL = "^[a-zA-Z0-9_]{1,20} has activated a game amplifier on ([a-zA-Z ]+)!$";
	public static String MATCH_AMPLIFIER_LOCAL = "^[a-zA-Z0-9_]{1,20} has activated a game amplifier for .*$";
	public static String MATCH_SUCCESSFUL_TIP = "^you thanked ([a-zA-Z0-9_]{1,20}).*$";
	
	public static Pattern PATTERN_SUCCESSFUL_TIP = Pattern.compile(MATCH_SUCCESSFUL_TIP, Pattern.CASE_INSENSITIVE);
	
	public boolean modEnabled = false;
	public boolean compactMessagesEnabled = false;

	public Map<String, String> gameNames = new HashMap<String, String>();
	public PlexCommandQueue thankQueue = new PlexCommandQueue("autoThank", Plex.queue);
	public long lastThankWave = 0L;

	public long thankWaveInterval = 900000L; // 15 minutes
	public long cooldownBackoffDelay = 5000L; // 5 seconds
	public long thankResponseTimeout = 10000L; // 10 seconds

	@Override
	public void modInit() {
		this.modEnabled = this.configValue("autoThank_enabled", false).getBoolean(false);
		this.compactMessagesEnabled = this.configValue("autoThank_compactMessages", false).getBoolean(false);

		this.thankQueue.setPriority(40);
		
		gameNames.put("master builders", "Master_Builders");
		gameNames.put("draw my thing", "Draw_My_Thing");
		gameNames.put("arcade", "Arcade");
		gameNames.put("speed builders", "Speed_Builders");
		gameNames.put("block hunt", "Block_Hunt");
		gameNames.put("cake wars", "Cake_Wars");
		gameNames.put("survival games", "Survival_Games");
		gameNames.put("skywars", "Skywars");
		gameNames.put("bridges", "Bridges");
		gameNames.put("minestrike", "MineStrike");
		gameNames.put("smash mobs", "Smash_Mobs");
		gameNames.put("dominate", "Dominate");
		gameNames.put("ctf", "CTF");
		gameNames.put("event", "Event");
		gameNames.put("nano", "Nano_Games");
		

		Plex.plexCommand.registerPlexCommand("thank", new PlexAutoThankCommand());
		
		PlexCore.registerMenuTab("AutoThank", PlexAutoThankUI.class);
	}

	@Override
	public String getModName() {
		return "AutoThank";
	}

	public String getMessageIfTip(String message) {
		String minified = PlexUtilChat.chatMinimalize(message);
		String[] items = minified.split(">", 2);
		String name = items[0].toLowerCase();
		if (items.length > 1 && name.equals("tip") || name.equals("amplifier") || name.equals("thanks")) {
			return items[1].trim();
		}
		return null;
	}

	public void onlineModLoop() {
		if (!this.modEnabled) {
			this.lastThankWave = 0L; // just kidding
			this.thankQueue.cancelAll();
			return;
		}
		if (Plex.gameState.currentLobby.type.equals(PlexMP.LobbyType.CLANS_SERVER)) {
			this.lastThankWave = 0L;
			this.thankQueue.cancelAll();
			return;
		}
		if (this.thankQueue.hasItems()) {
			if (this.thankQueue.getItem(0).isSent()) {
				if (this.thankQueue.getItem(0).getSentElapsed() > this.thankResponseTimeout) {
					this.thankQueue.getItem(0).markComplete();
				}				
			}
		}
		if (Plex.gameState.isMineplex && this.modEnabled) {
			if (Minecraft.getSystemTime() > (this.lastThankWave + this.thankWaveInterval)) {
				for (String game : this.gameNames.values()) {
					this.thankQueue.add("/amplifier thank " + game);
				}
				this.lastThankWave = Minecraft.getSystemTime();
			}			
		}
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent e) {
		if (!this.modEnabled) {
			return;
		}

		String message = this.getMessageIfTip(e.message.getFormattedText());
		if (message == null) {
			return;
		}
		String messageLowercase = message.toLowerCase();


		if (this.thankQueue.hasItems()) {
			if ((messageLowercase.matches(MATCH_AMPLIFIER_GLOBAL) || messageLowercase.matches(MATCH_AMPLIFIER_LOCAL)) && this.compactMessagesEnabled) {
				e.setCanceled(true);
			}
			if (messageLowercase.matches(MATCH_SUCCESSFUL_TIP) && this.compactMessagesEnabled) {
				Matcher nameExtract = PATTERN_SUCCESSFUL_TIP.matcher(message);
				nameExtract.find();
				PlexUtilChat.chatAddMessage(PlexUtilChat.chatStyleText("BLUE", thankQueue.getItem(0).isSent() ? "AutoThank> " : "Thank> ") + PlexUtilChat.chatStyleText("GRAY", "Thanked ") + PlexUtilChat.chatStyleText("YELLOW", nameExtract.group(1)) + PlexUtilChat.chatStyleText("GRAY", "."));
				e.setCanceled(true);
			}
			if (thankQueue.getItem(0).isSent()) {
				if (messageLowercase.matches(MATCH_SUCCESSFUL_TIP)) {
					this.thankQueue.getItem(0).markComplete();
				}
				else if (messageLowercase.startsWith("you have already thanked this amplifier") ||
						messageLowercase.startsWith("there was an error handling your request") ||
						messageLowercase.startsWith("you can't thank yourself") ||
						messageLowercase.startsWith("an error")) {
					e.setCanceled(true);
					thankQueue.getItem(0).markComplete();
				}
				else if (messageLowercase.startsWith("please wait before trying that again")) {
					e.setCanceled(true);
					thankQueue.getItem(0).resendIn(cooldownBackoffDelay);
				}
			}
		}
		if (messageLowercase.startsWith("click here to thank")) {
			JsonParser messageParser = new JsonParser();
			JsonObject messageJson = messageParser.parse(IChatComponent.Serializer.componentToJson(e.message)).getAsJsonObject();
			JsonElement globalClickEvent = messageJson.get("clickEvent");
			JsonElement extra = messageJson.get("extra");
			String command = null;
			if (globalClickEvent != null) {
				command = globalClickEvent.getAsJsonObject().get("value").getAsString();
			}
			else if (extra != null) {
				JsonArray extraTag = extra.getAsJsonArray();
				for (JsonElement item : extraTag) {
					JsonObject clickEvent = (JsonObject) item.getAsJsonObject().get("clickEvent");
					if (clickEvent != null) {
						command = clickEvent.get("value").getAsString();
						break;
					}
				}
			}
			if (command == null) {
				PlexUtilChat.chatAddMessage(PlexUtilChat.chatStyleText("BLUE", "AutoThank> ") + PlexUtilChat.chatStyleText("RED", "Unexpectedly failed to detect which game this is. Is it a thank message?"));
			}
			else if (this.compactMessagesEnabled) {
				e.setCanceled(true);
			}
			PlexCommandQueue.Command queueCommand = this.thankQueue.newCommand(command);
			queueCommand.setPriority(39);
			this.thankQueue.add(queueCommand);
		}
	}

	@Override
	public void saveConfig() {
		this.configValue("autoThank_enabled", false).set(this.modEnabled);
		this.configValue("autoThank_compactMessages", false).set(this.compactMessagesEnabled);
	}

	@Override
	public void onJoin() {
		MinecraftForge.EVENT_BUS.register(this);
		thankQueue.cancelAll();
	}

	@Override
	public void onLeave() {
		MinecraftForge.EVENT_BUS.unregister(this);
		thankQueue.cancelAll();
	}

	@Override
	public void onLobbyUpdate(PlexMP.LobbyType type) {
		if (type.equals(PlexMP.LobbyType.CLANS_SERVER)) {
			new Timer().schedule(new TimerTask() {
				public void run() {
					PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX + PlexUtilChat.chatStyleText("DARK_GRAY", "Note: AutoThank does not work on clans servers and is inactive until you leave this clans game."));
				}
			}, 8000L);
		}
	}
}
