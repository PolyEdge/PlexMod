package pw.ipex.plex.mods.richpresence;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.PlexCoreValue;
import pw.ipex.plex.mod.PlexModBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jagrosh.discordipc.*;
import com.jagrosh.discordipc.entities.Callback;
import com.jagrosh.discordipc.entities.DiscordBuild;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;

import net.minecraft.client.Minecraft;

public class PlexNewRichPresenceMod extends PlexModBase {
	public String MATCH_SERVER_NAME = "([a-zA-Z0-9]+)-([0-9]+)";
	
	public Pattern PATTERN_SERVER_NAME = Pattern.compile(MATCH_SERVER_NAME);
	
	public Map<String, String> gameIcons = new HashMap<String, String>();
	public Map<String, String> lobbyNames = new HashMap<String, String>();
	
	public static IPCClient ipcClient;
	
	public Long AFK_IDLE_TIME = 30000L;
	public RichPresence currentStatus = null;
	
	public Boolean richPresenceOpening = false;
	public Boolean richPresenceOpened = false;
	
	public Boolean richPresenceErrorShown = false;
	public Boolean showRichPresenceError = false;
	public Boolean timeoutShown = false;
	
	public Boolean isAfk = false;
	public PlexCoreValue modEnabled = new PlexCoreValue("richPresence_enabled", false);
	public PlexCoreValue displayLobbyName = new PlexCoreValue("richPresence_showLobbies", false);
	public PlexCoreValue displayIGN = new PlexCoreValue("richPresence_showIGN", false);
	public PlexCoreValue timerMode = new PlexCoreValue("richPresence_timerMode", 1);
	
	public static Integer MAX_TIMER_MODE = 3;
	
	public Long lastRPupdate = 0L;
	public Long lastIPCupdate = 0L;
	
	
	@Override
	public String getModName() {
		return "Rich Presence";
	}
	
	@Override
	public void modInit() {
		ipcClient = new IPCClient(463568324458971147L);
		ipcClient.setListener(new IPCListener() {
			@Override
			public void onReady(IPCClient client) {
				richPresenceOpening = false;
				richPresenceOpened = true;
			}
		});
		
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
		lobbyNames.put("nano", "Nano Games");
		lobbyNames.put("event", "$In an Event");
		lobbyNames.put("staff", "$In a Staff Server");
		
		Plex.plexCommand.registerPlexCommand("discord", new PlexRichPresenceCommand());
		
		Plex.plexCommand.addPlexHelpCommand("discord", "Displays discord integration options");
		
		PlexCore.registerUiTab("Discord", PlexRichPresenceUI.class);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void putRichPresence() {
		if (!this.richPresenceOpened) {
			return;
		}
		final RichPresence status = getRichPresence();
		final PlexNewRichPresenceMod me = this;
		try {
			ipcClient.sendRichPresence(status, new Callback() {
				@Override
				public void succeed(Packet packet) {
					me.currentStatus = status;
				}
				@Override
				public void fail(String message) {

				}
			});			
		}
		catch (Throwable e) {}
	}
	
	public void updateRichPresence() {
		lastRPupdate = Minecraft.getSystemTime();
		if (!richPresenceOpened) {
			return;
		}
		handleIPCClientState();
		if (this.modEnabled.booleanValue && Plex.serverState.onMineplex) {
			putRichPresence();
		}
	}
	
	public RichPresence getRichPresence() {
		String serverIP = getServerIP();
		String[] gameState = getPresenceStrings();
		RichPresence.Builder presence = new RichPresence.Builder();
		presence.setDetails(gameState[0]);
		presence.setState(gameState[1]);
		presence.setLargeImage("mineplex_logo", serverIP);
		if ((Minecraft.getSystemTime() > (Plex.serverState.lastControlInput == null ? 0 : Plex.serverState.lastControlInput) + AFK_IDLE_TIME) && Plex.serverState.lastControlInput != null) {
			isAfk = true;
			presence.setDetails("AFK | " + gameState[0]);
			presence.setSmallImage("afk", "AFK | " + gameState[0]);
			return presence.build();
		}
		if (Plex.serverState.currentLobbyType != null) {
			if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.GAME_INGAME)) {
				if (Plex.serverState.currentGameName != null) {
					if (gameIcons.containsKey(Plex.serverState.currentGameName.toLowerCase())) {
						presence.setSmallImage(gameIcons.get(Plex.serverState.currentGameName.toLowerCase()), gameState[0]);
					}
					if (PlexCore.getSharedValue("richPresence_timerMode").integerValue.equals(1) && (Plex.serverState.gameStartDateTime != null)) {
						presence.setStartTimestamp(Plex.serverState.gameStartDateTime);
					}
				}
			}
			if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.CLANS_SERVER)) {
				presence.setSmallImage(gameIcons.get("clans"), "Playing Clans");
			}	
		}
		if (PlexCore.getSharedValue("richPresence_timerMode").integerValue.equals(2) && Plex.serverState.serverJoinDateTime != null) {
			presence.setStartTimestamp(Plex.serverState.serverJoinDateTime);
		}
		return presence.build();
	}
	

	public String getServerIP() {
		return Plex.serverState.serverHostname.startsWith("us") ? "us.mineplex.com" : (Plex.serverState.serverHostname.startsWith("eu") ? "eu.mineplex.com" : "mineplex.com");
	}
	
	public String serverIgn(boolean addIP) {
		Boolean showLobby = this.displayLobbyName.booleanValue && (Plex.serverState.currentLobbyName != null);
		Boolean showIGN = this.displayIGN.booleanValue;
		String output = "";
		if (showLobby && Plex.serverState.currentLobbyName != null) {
			output += Plex.serverState.currentLobbyName;
		}
		if (showIGN) {
			if (!output.equals("")) {
				output += " | ";
			}
			output += "IGN: " + PlexCore.getPlayerIGN();
		}
		if (output.equals("") && addIP) {
			output = getServerIP();
		}
		else if (output.equals("")) {
			output = "No information available";
		}
		return output;
	}
	
	public String[] getPresenceStrings() {
		String state = serverIgn(true);
		if (Plex.serverState.currentLobbyType == null) {
			return new String[] {"Playing on " + getServerIP(), serverIgn(false)};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.SERVER_UNDETERMINED) || Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.SERVER_UNKNOWN)) {
			return new String[] {"Playing on " + getServerIP(), serverIgn(false)};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.SERVER_HUB)) {
			return new String[] {"In a Main Lobby", state};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.CLANS_HUB)) {
			return new String[] {"In a Clans Hub", state};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.CLANS_SERVER)) {
			return new String[] {"Playing Clans", state};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.GAME_LOBBY)) {
			if (Plex.serverState.currentLobbyName != null) {
				if (Plex.serverState.currentLobbyName.matches(MATCH_SERVER_NAME)) {
					Matcher serverIDmatcher = PATTERN_SERVER_NAME.matcher(Plex.serverState.currentLobbyName);
					serverIDmatcher.find();
					String serverID = serverIDmatcher.group(1);
					if (lobbyNames.containsKey(serverID.toLowerCase())) {
						if (lobbyNames.get(serverID.toLowerCase()).startsWith("$")) {
							return new String[] {lobbyNames.get(serverID.toLowerCase()).substring(1), state};
						}
						return new String[] {"In a " + lobbyNames.get(serverID.toLowerCase()) + " lobby", state};
					}
				}
			}
			return new String[] {"In a Game Lobby", state};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.GAME_INGAME)) {
			if (Plex.serverState.currentGameName == null) {
				return new String[] {(Plex.serverState.isGameSpectator ? "Spectating" : "Playing") + " a Game", state};
			}
			return new String[] {(Plex.serverState.isGameSpectator ? "Spectating " : "Playing ") + Plex.serverState.currentGameName, state};
		}
		return new String[] {"Playing on " + getServerIP(), serverIgn(false)};
	}

	@Override
	public void saveModConfig() {
		this.modSetting("rich_presence_enabled", false).set(this.modEnabled.booleanValue);
		this.modSetting("richPresence_showLobbies", false).set(this.displayLobbyName.booleanValue);
		this.modSetting("richPresence_showIGN", false).set(this.displayIGN.booleanValue);
		this.modSetting("richPresence_timerMode", 1).set(this.timerMode.integerValue);
		
		handleIPCClientState();
		new Timer().schedule(new TimerTask() {
			public void run() {
				if (Plex.serverState.onMineplex) {
					updateRichPresence();
				}
			}
		}, 2000L);
	}
	
	public void showLibError() {
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("DARK_RED", "The Discord Rich Presence libraries failed to connect to discord. This error is normal when Discord closes unexpectedly while the mod is already connected. Check the log for details"));
	}
	
	public void handleIPCClientState() {
		if (!Plex.serverState.onMineplex || !this.modEnabled.booleanValue) {
			try {
				ipcClient.close();
			}
			catch (Throwable e) {}
			richPresenceOpened = false;
			return;
		}
		if ((ipcClient.getStatus().equals(PipeStatus.CLOSED) || ipcClient.getStatus().equals(PipeStatus.DISCONNECTED)) && !richPresenceOpening && richPresenceOpened) {
			this.richPresenceOpened = false;
			try {
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("DARK_RED", "Discord disconnected!"));
			}
			catch (Throwable ee) {}
		}
		if (richPresenceOpening) {
			return;
		}
		if (!richPresenceOpened) {
			richPresenceOpening = true;
			final Thread openClientThread = new Thread(new Runnable() {
				public void run() {
					try {
						ipcClient.connect(DiscordBuild.ANY);
						if (richPresenceErrorShown || timeoutShown) {
							new Timer().schedule(new TimerTask() {
								public void run() {
									try {
										PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GREEN", "Discord reconnected!"));
									}
									catch (Throwable ee) {}
								}
							}, 1000L);
						}
						richPresenceErrorShown = false;
						timeoutShown = false;
					}
					catch (Throwable e) {
						String error = e.getMessage();
						if (error == null) {
							error = "null";
						}
						Plex.logger.error("The rich presence failed to connect due to " + error);
						Plex.logger.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
						richPresenceOpened = false;
						richPresenceOpening = false;
						showRichPresenceError = true;
						return;
					}
					richPresenceOpened = true;
					richPresenceOpening = false;
				}
			});
			openClientThread.start();
			new Timer().schedule(new TimerTask() {
				public void run() {
					if (openClientThread.isAlive()) {
						try {
							openClientThread.interrupt();
							richPresenceOpening = false;
							if (!timeoutShown) {
								PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("DARK_RED", "Connection to Discord timed out!"));
								timeoutShown = true;
							}
						}
						catch (Throwable ee) {}
					}
				}
			}, 6000L);
		}
	}

	@Override
	public void joinedMineplex() {
		handleIPCClientState();
		lastRPupdate = Minecraft.getSystemTime();
	}

	@Override
	public void leftMineplex() {
		handleIPCClientState();
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (Plex.serverState.onMineplex && (Minecraft.getSystemTime() > lastRPupdate + 15000L)) {
			updateRichPresence();
		}
		if (Minecraft.getSystemTime() > lastIPCupdate + 15000L) {
			handleIPCClientState();
			lastIPCupdate = Minecraft.getSystemTime();
		}
		if (Plex.serverState.onMineplex && showRichPresenceError && !richPresenceErrorShown) {
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
	public void switchedLobby(PlexCoreLobbyType type) {
		new Timer().schedule(new TimerTask() {
			public void run() {
				handleIPCClientState();
				updateRichPresence();
			}
		}, 3000L);
	}
}
