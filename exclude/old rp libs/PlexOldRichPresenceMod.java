package pw.ipex.plex.mods.richpresence;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreListeners;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.PlexCoreValue;
import pw.ipex.plex.mod.PlexModBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;

public class PlexOldRichPresenceMod extends PlexModBase {
	public String MATCH_SERVER_NAME = "([a-zA-Z0-9]+)-([0-9]+)";
	
	public Pattern PATTERN_SERVER_NAME = Pattern.compile(MATCH_SERVER_NAME);
	
	public Long AFK_IDLE_TIME = 30000L;
	
	public Map<String, String> gameIcons = new HashMap<String, String>();
	public Map<String, String> lobbyNames = new HashMap<String, String>();
	
	public Boolean richPresenceOpened = false;
	public Boolean richPresenceLoaded = false;
	public Boolean richPresenceErrorShown = false;
	public Boolean showRichPresenceError = false;
	public Boolean isAfk = false;
	public PlexCoreValue modEnabled = new PlexCoreValue("richPresence_enabled", false);
	public PlexCoreValue displayLobbyName = new PlexCoreValue("richPresence_showLobbies", false);
	public PlexCoreValue displayIGN = new PlexCoreValue("richPresence_showIGN", false);
	public PlexCoreValue timerMode = new PlexCoreValue("richPresence_timerMode", 1);
	
	public static Integer MAX_TIMER_MODE = 3;
	
	public Long lastRPupdate = 0L;

	
	@Override
	public String getModName() {
		return "Rich Presence";
	}
	
	@Override
	public void modInit() {
		this.modEnabled.set(this.modSetting("rich_presence_enabled", false).getBoolean(false));
		this.displayLobbyName.set(this.modSetting("richPresence_showLobbies", false).getBoolean(false));
		this.displayIGN.set(this.modSetting("richPresence_showIGN", false).getBoolean(false));
		this.timerMode.set(this.modSetting("richPresence_timerMode", 1).getInt());
		
		gameIcons.put("bacon brawl", "raw_porkchop");
		gameIcons.put("bawk bawk battles", "egg");
		gameIcons.put("bomb lobbers", "fire_charge");
		gameIcons.put("death tag", "skeleton_skull");
		gameIcons.put("dragon escape", "dragon_egg");
		gameIcons.put("dragons", "end_stone");
		gameIcons.put("evolution", "null_spawn_egg");
		gameIcons.put("gladiators", "iron_sword");
		gameIcons.put("micro battle", "lava_bucket");
		gameIcons.put("monster maze", "rotten_flesh");
		gameIcons.put("one in the quiver", "bow_drawn");
		gameIcons.put("runner", "leather_boots");
		gameIcons.put("sheep quest", "white_wool");
		gameIcons.put("snake", "yellow_wool");
		gameIcons.put("sneaky assassins", "ink_sac");
		gameIcons.put("super paintball", "ender_pearl");
		gameIcons.put("super spleef", "iron_shovel");
		gameIcons.put("turf wars", "stained_hardened_clay");
		gameIcons.put("wither assault", "wither_skeleton_skull");
		gameIcons.put("champions ctf", "banner_white");
		gameIcons.put("champions dominate", "beacon");
		gameIcons.put("champions domination", "beacon");
		gameIcons.put("block hunt", "grass_block");
		gameIcons.put("draw my thing", "book_and_quill");
		gameIcons.put("master builders", "oak_wood_planks");
		gameIcons.put("mine-strike", "tnt");
		gameIcons.put("speed builders", "quartz_block");
		gameIcons.put("super smash mobs", "creeper_head");
		gameIcons.put("the bridges", "iron_pickaxe");
		gameIcons.put("skywars", "feather");
		gameIcons.put("skywars teams", "feather");
		gameIcons.put("cake wars standard", "cake");
		gameIcons.put("cake wars duos", "cake");
		gameIcons.put("survival games", "diamond_sword");
		gameIcons.put("survival games teams", "diamond_sword");
		gameIcons.put("clans", "iron_sword");
		
		lobbyNames.put("bld", "Master Builders");
		lobbyNames.put("dmt", "Draw My Thing");
		lobbyNames.put("mb", "Micro Battles");
		lobbyNames.put("min", "Mixed Arcade");
		lobbyNames.put("tf", "Turf Wars");
		lobbyNames.put("sb", "Speed Builders");
		lobbyNames.put("bh", "Block Hunt");
		lobbyNames.put("cw4", "Cake Wars Standard");
		lobbyNames.put("cw2", "Cake Wars Duos");
		lobbyNames.put("hg", "Survival Games");
		lobbyNames.put("sg", "Survival Games"); // just in case
		lobbyNames.put("hg2", "Survival Games Teams"); // just in case
		lobbyNames.put("sg2", "Survival Games Teams");
		lobbyNames.put("sky", "Skywars");
		lobbyNames.put("sky2", "Skywars Teams");
		lobbyNames.put("br", "Bridges");
		lobbyNames.put("ms", "MineStrike");
		lobbyNames.put("ssm", "Super Smash Mobs");
		lobbyNames.put("ssm2", "Super Smash Mobs Teams");
		lobbyNames.put("dom", "Dominate");
		lobbyNames.put("ctf", "CTF");
		lobbyNames.put("retro", "Retro");
		
		Plex.plexCommand.registerPlexCommand("discord", new PlexRichPresenceCommand());
		
		Plex.plexCommand.addPlexHelpCommand("discord", "Displays discord integration options");
		
		PlexCore.registerUiTab("Discord", PlexRichPresenceUI.class);
	}
	
	public void putRichPresence() {
		DiscordRPC.discordUpdatePresence(getRichPresence());
		//DiscordRPC.INSTANCE.Discord_UpdatePresence(getRichPresence());
	}
	
	public void updateRichPresence() {
		lastRPupdate = Minecraft.getSystemTime();
		if (!richPresenceLoaded) {
			return;
		}
		if (this.modEnabled.booleanValue && Plex.onMineplex) {
			putRichPresence();
		}
		else {
			DiscordRPC.discordClearPresence();
			//DiscordRPC.INSTANCE.Discord_ClearPresence();
		}
	}
	
	public DiscordRichPresence getRichPresence() {
		String serverIP = getServerIP();
		String[] gameState = getPresenceStrings(this.displayLobbyName.booleanValue);
		DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder(gameState[1]);
		//DiscordRichPresence presence = new DiscordRichPresence();
		presence.setDetails(gameState[0]);
		presence.setBigImage("mineplex_logo", serverIP);
		//presence.details = gameState[0];
		//presence.largeImageKey = "mineplex_logo";
		//presence.largeImageText = serverIP;
		//PlexCoreUtils.chatAddMessage("" + ((PlexCoreListeners.lastControlInput + AFK_IDLE_TIME) - Minecraft.getSystemTime()));
		if ((Minecraft.getSystemTime() > (PlexCoreListeners.lastControlInput == null ? 0 : PlexCoreListeners.lastControlInput) + AFK_IDLE_TIME) && PlexCoreListeners.lastControlInput != null) {
			isAfk = true;
			presence.setDetails("AFK | " + gameState[0]);
			presence.setSmallImage("afk", "AFK | " + gameState[0]);
			//presence.setTimestamps(Minecraft.getSystemTime() - (PlexCoreListeners.lastControlInput + AFK_IDLE_TIME), 0L);
			return presence.build();
		}
		if (Plex.currentLobbyType != null) {
			if (Plex.currentLobbyType.equals("gameIngame")) {
				if (Plex.currentGameName != null) {
					if (gameIcons.containsKey(Plex.currentGameName.toLowerCase())) {
						presence.setSmallImage(gameIcons.get(Plex.currentGameName.toLowerCase()), (PlexCoreListeners.isGameSpectator ? "Spectating " : "Playing ") + Plex.currentGameName);
						//presence.smallImageKey = gameIcons.get(Plex.currentGameName.toLowerCase());
						//presence.smallImageText = (PlexCoreListeners.isGameSpectator ? "Spectating " : "Playing ") + Plex.currentGameName;
					}
					if (PlexCore.getSharedValue("richPresence_timerMode").integerValue.equals(1) && (PlexCoreListeners.gameStartEpoch != null)) {
						presence.setTimestamps(PlexCoreListeners.gameStartEpoch, 0L);
						//presence.startTimestamp = PlexCoreListeners.gameStartEpoch;
					}
				}
			}
			if (Plex.currentLobbyType.equals("clansServer")) {
				presence.setSmallImage(gameIcons.get("iron_sword"), "Playing Clans");
				//presence.smallImageKey = "iron_sword";
				//presence.smallImageText = "Playing Clans";
			}	
		}
		if (PlexCore.getSharedValue("richPresence_timerMode").integerValue.equals(2)) {
			presence.setTimestamps(PlexCoreListeners.serverJoinEpoch, 0L);
			//presence.startTimestamp = PlexCoreListeners.serverJoinEpoch;
		}
		return presence.build();
		//return presence;
	}
	

	public String getServerIP() {
		return PlexCoreListeners.serverIP.startsWith("us") ? "us.mineplex.com" : (PlexCoreListeners.serverIP.startsWith("eu") ? "eu.mineplex.com" : "mineplex.com");
	}
	
	public String serverIgn() {
		Boolean showLobby = this.displayLobbyName.booleanValue && (Plex.currentLobbyName != null);
		Boolean showIGN = this.displayIGN.booleanValue;
		String output = "";
		if (showLobby) {
			output += Plex.currentLobbyName;
		}
		if (showIGN) {
			if (showLobby) {
				output += " | ";
			}
			output += "IGN: " + PlexCore.getPlayerIGN();
		}
		return output;
	}
	
	public String[] getPresenceStrings(Boolean showLobby) {
		String state = serverIgn();
		if (Plex.currentLobbyType == null) {
			return new String[] {"Playing on " + getServerIP(), "No information available"};
		}
		if (Plex.currentLobbyType.equals("mineplexHub")) {
			return new String[] {"In a Main Lobby", state};
		}
		if (Plex.currentLobbyType.equals("clansHub")) {
			return new String[] {"In a Clans Hub", state};
		}
		if (Plex.currentLobbyType.equals("clansServer")) {
			return new String[] {"Playing Clans", state};
		}
		if (Plex.currentLobbyType.equals("gameLobby")) {
			if (Plex.currentLobbyName != null) {
				if (Plex.currentLobbyName.matches(MATCH_SERVER_NAME)) {
					Matcher serverIDmatcher = PATTERN_SERVER_NAME.matcher(Plex.currentLobbyName);
					serverIDmatcher.find();
					String serverID = serverIDmatcher.group(1);
					if (lobbyNames.containsKey(serverID.toLowerCase())) {
						return new String[] {"In a " + lobbyNames.get(serverID.toLowerCase()) + " lobby", state};
					}
				}
			}
			return new String[] {"In a Game Lobby", state};
		}
		if (Plex.currentLobbyType.equals("gameIngame")) {
			if (Plex.currentGameName == null) {
				return new String[] {(PlexCoreListeners.isGameSpectator ? "Spectating" : "Playing") + " a Game", state};
			}
			return new String[] {(PlexCoreListeners.isGameSpectator ? "Spectating " : "Playing ") + Plex.currentGameName, state};
		}
		return new String[] {"Playing on " + getServerIP(), "No information available"};
	}

	@Override
	public void saveModConfig() {
		this.modSetting("rich_presence_enabled", false).set(this.modEnabled.booleanValue);
		this.modSetting("richPresence_showLobbies", false).set(this.displayLobbyName.booleanValue);
		this.modSetting("richPresence_showIGN", false).set(this.displayLobbyName.booleanValue);
		this.modSetting("richPresence_timerMode", 1).set(this.timerMode.integerValue);
		
		if (this.modEnabled.booleanValue) {
			openRP();
		}
		else {
			closeRP();
		}
		new Timer().schedule(new TimerTask() {
			public void run() {
				if (Plex.onMineplex) {
					updateRichPresence();
				}
			}
		}, 2000L);
	}
	
	public void showLibError() {
		for (Integer x = 0; x < 6; x++) {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("DARK_RED", "Failed to load the rich presence libraries! Check the log for details."));
		}		
	}
	
	public void openRP() {
		if (!richPresenceOpened) {
			try {
				DiscordRPC.discordInitialize("463568324458971147", new DiscordEventHandlers(), true);
				//DiscordRPC.INSTANCE.Discord_Initialize("463568324458971147", new DiscordEventHandlers(), true, null);
			}
			catch (Throwable e) {
				Plex.logger.error("Failed to load the rich presence libraries");
				Plex.logger.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
				richPresenceLoaded = false;
				showRichPresenceError = true;
				return;
			}
			richPresenceLoaded = true;
			richPresenceOpened = true;
			return;
		}
		richPresenceLoaded = true;
	}
	
	public void closeRP() {
		if (!richPresenceLoaded) {
			return;
		}
		if (richPresenceOpened) {
			DiscordRPC.discordShutdown();
			//DiscordRPC.INSTANCE.Discord_Shutdown();
		}
		richPresenceOpened = false;
	}

	public void joinedMineplex() {
		if (this.modEnabled.booleanValue) {
			openRP();
		}
		MinecraftForge.EVENT_BUS.register(this);
		lastRPupdate = Minecraft.getSystemTime();
	}
	
	public void leftMineplex() {
		closeRP();
//		if (richPresenceOpened && richPresenceLoaded) {
//			DiscordRPC.discordClearPresence();
//		}
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (Plex.onMineplex && (Minecraft.getSystemTime() > lastRPupdate + 30000L)) {
			updateRichPresence();
		}
		if (Plex.onMineplex && showRichPresenceError && !richPresenceErrorShown) {
			showRichPresenceError = false;
			richPresenceErrorShown = true;
			new Timer().schedule(new TimerTask() {
				public void run() {
					showLibError();
				}
			}, 1000L);
		}
	}
	
	@SubscribeEvent
	public void onInput(InputEvent e) {
		if (isAfk) {
			isAfk = false;
			new Timer().schedule(new TimerTask() {
				public void run() {
					updateRichPresence();
				}
			}, 2000L);			
		}
	}

	@Override
	public void switchedLobby(String name) {
		new Timer().schedule(new TimerTask() {
			public void run() {
				updateRichPresence();
			}
		}, 4000L);
	}
}
