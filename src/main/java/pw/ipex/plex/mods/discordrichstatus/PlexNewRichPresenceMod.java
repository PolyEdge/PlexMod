package pw.ipex.plex.mods.discordrichstatus;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.mineplex.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mod.PlexModBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
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
	
	public AtomicBoolean richPresenceOpening = new AtomicBoolean();
	public AtomicBoolean richPresenceOpened = new AtomicBoolean();

	public Thread currentConnectionThread;
	public AtomicLong lastConnectionAttempt = new AtomicLong();
	
	public AtomicBoolean richPresenceErrored = new AtomicBoolean();

	public boolean isAfk = false;
	public boolean modEnabled = false;
	public boolean displayLobbyName = true;
	public boolean displayIGN = true;
	public int timerMode = 1;
	public boolean showAfk = true;
	public boolean showIP = true;
	
	public static Integer MAX_TIMER_MODE = 3;
	
	public Long lastRPupdate = 0L;
	
	
	@Override
	public String getModName() {
		return "Rich Presence";
	}
	
	@Override
	public void modInit() {
		richPresenceOpening.set(false);
		richPresenceOpened.set(false);
		richPresenceErrored.set(false);

		lastConnectionAttempt.set(0L);

		ipcClient = new IPCClient(463568324458971147L);
		ipcClient.setListener(new IPCListener() {
			@Override
			public void onReady(IPCClient client) {
				richPresenceOpening.set(false);
				richPresenceOpened.set(true);
			}
		});
		
		this.modEnabled = this.modSetting("rich_presence_enabled", false).getBoolean(false);
		this.displayLobbyName = this.modSetting("richPresence_showLobbies", true).getBoolean(true);
		this.displayIGN = this.modSetting("richPresence_showIGN", true).getBoolean(true);
		this.timerMode = this.modSetting("richPresence_timerMode", 1).getInt();
		this.showAfk = this.modSetting("richPresence_showAFK", true).getBoolean(true);
		this.showIP = this.modSetting("richPresence_showIP", true).getBoolean(true);
		
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
		gameIcons.put("minestrike", "tnt");
		gameIcons.put("speed builders", "quartz_block");
		gameIcons.put("super smash mobs", "creeper_head");
		gameIcons.put("super smash mobs teams", "creeper_head");
		gameIcons.put("the bridges", "iron_pickaxe");
		gameIcons.put("skywars", "feather");
		gameIcons.put("skywars teams", "feather");
		gameIcons.put("cake wars standard", "cake");
		gameIcons.put("cake wars duos", "cake");
		gameIcons.put("survival games", "diamond_sword");
		gameIcons.put("survival games teams", "diamond_sword");
		gameIcons.put("clans", "iron_sword");

		lobbyNames.put("lobby", "$Main Hub");

		lobbyNames.put("bld", "Master Builders");
		lobbyNames.put("dmt", "Draw My Thing");
		lobbyNames.put("bb", "Bacon Brawl");
		lobbyNames.put("bl", "Bomb Lobbers");
		lobbyNames.put("dt", "Death Tag");
		lobbyNames.put("de", "Dragon Escape");
		lobbyNames.put("dr", "Dragons");
		lobbyNames.put("evo", "Evolution");
		lobbyNames.put("gld", "Gladiators");
		lobbyNames.put("mb", "Micro Battles");
		lobbyNames.put("mm", "Monster Maze");
		lobbyNames.put("oitq", "OITQ");
		lobbyNames.put("run", "Runner");
		lobbyNames.put("sq", "Sheep Quest");
		lobbyNames.put("sn", "Snake");
		lobbyNames.put("sa", "Sneaky Assassins");
		lobbyNames.put("pb", "Paintball");
		lobbyNames.put("ss", "Spleef");
		lobbyNames.put("tf", "Turf Wars");
		lobbyNames.put("wa", "Wither Assault");
		lobbyNames.put("min", "Mixed Arcade");
		lobbyNames.put("sb", "Speed Builders");
		lobbyNames.put("bh", "Block Hunt");
		lobbyNames.put("cw4", "Cake Wars Standard");
		lobbyNames.put("cw2", "Cake Wars Duos");
		lobbyNames.put("hg", "Survival Games");
		lobbyNames.put("sg", "Survival Games"); // just in case
		lobbyNames.put("hg2", "SG Teams"); // just in case
		lobbyNames.put("sg2", "SG Teams");
		lobbyNames.put("sky", "Skywars");
		lobbyNames.put("sky2", "Skywars Teams");
		lobbyNames.put("br", "Bridges");
		lobbyNames.put("ms", "MineStrike");
		lobbyNames.put("ssm", "Super Smash Mobs");
		lobbyNames.put("ssm2", "SSM Teams");
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
		if (!this.richPresenceOpened.get()) {
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
		if (!richPresenceOpened.get()) {
			return;
		}
		lastRPupdate = Minecraft.getSystemTime();
		if (this.modEnabled && Plex.serverState.onMineplex) {
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
		if (Plex.serverState.lastControlInput != -1L && (Minecraft.getSystemTime() > Plex.serverState.lastControlInput + AFK_IDLE_TIME) && this.showAfk) {
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
					if (this.timerMode == 1 && (Plex.serverState.gameStartDateTime != null)) {
						presence.setStartTimestamp(Plex.serverState.gameStartDateTime);
					}
				}
			}
			if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.CLANS_SERVER)) {
				presence.setSmallImage(gameIcons.get("clans"), "Playing Clans");
			}	
		}
		if (this.timerMode == 2 && Plex.serverState.serverJoinDateTime != null) {
			presence.setStartTimestamp(Plex.serverState.serverJoinDateTime);
		}
		return presence.build();
	}
	

	public String getServerIP() {
		if (Plex.serverState.serverHostname.startsWith("us")) {
			return this.showIP ? "us.mineplex.com" : "Mineplex (US)";
		}
		if (Plex.serverState.serverHostname.startsWith("eu")) {
			return this.showIP ? "eu.mineplex.com" : "Mineplex (EU)";
		}
		if (Plex.serverState.serverHostname.startsWith("clans")) {
			return this.showIP ? "clans.mineplex.com" : "Mineplex (Clans)";
		}
		return this.showIP ? "mineplex.com" : "Mineplex";
	}

	public String getLobbyRepresentation() {
		if (Plex.serverState.currentLobbyName == null) {
			return null;
		}
		if (!Plex.serverState.currentLobbyName.matches(MATCH_SERVER_NAME)) {
			return null;
		}
		Matcher serverIDmatcher = PATTERN_SERVER_NAME.matcher(Plex.serverState.currentLobbyName);
		serverIDmatcher.find();
		String serverID = serverIDmatcher.group(1);
		if (lobbyNames.containsKey(serverID.toLowerCase())) {
			return lobbyNames.get(serverID.toLowerCase());
		}
		return null;
	}

	public String wrappedLobbyRepresentation(String def, String prefix, String suffix) {
		String rep = this.getLobbyRepresentation();
		if (rep == null) {
			return def;
		}
		if (rep.startsWith("$")) {
			return rep.substring(1);
		}
		return prefix + rep + suffix;
	}
	
	public String serverIGN(boolean addIP) {
		String output = "";
		if (this.displayLobbyName && Plex.serverState.currentLobbyName != null) {
			output += Plex.serverState.currentLobbyName;
		}
		if (this.displayIGN) {
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
		String state = serverIGN(true);
		if (Plex.serverState.currentLobbyType == null) {
			return new String[] {"Playing on " + getServerIP(), serverIGN(false)};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.SERVER_UNDETERMINED) || Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.SERVER_UNKNOWN)) {
			return new String[] {"Playing on " + getServerIP(), serverIGN(false)};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.CLANS_HUB)) {
			return new String[] {"Clans Hub", state};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.CLANS_SERVER)) {
			if (Plex.serverState.clansSeason == null) {
				return new String[] {"Playing Clans", state};
			}
			else {
				return new String[] {"Clans (Season " + Plex.serverState.clansSeason + ")", state};

			}
		}
		String lobbyRep = this.getLobbyRepresentation();
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.SERVER_HUB)) {
			return new String[] {this.wrappedLobbyRepresentation("Main Hub", "In ", ""), state};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.GAME_LOBBY)) {
			return new String[] {this.wrappedLobbyRepresentation("Game Lobby", "Game Lobby: ", ""), state};
		}
		if (Plex.serverState.currentLobbyType.equals(PlexCoreLobbyType.GAME_INGAME)) {
			if (Plex.serverState.currentGameName == null) {
				return new String[] {(Plex.serverState.isGameSpectator ? "Spectating" : "Playing") + " a Game", state};
			}
			return new String[] {(Plex.serverState.isGameSpectator ? "Spectating " : "Playing ") + Plex.serverState.currentGameName, state};
		}
		return new String[] {"Playing on " + getServerIP(), serverIGN(false)};
	}

	@Override
	public void saveModConfig() {
		this.modSetting("rich_presence_enabled", false).set(this.modEnabled);
		this.modSetting("richPresence_showLobbies", false).set(this.displayLobbyName);
		this.modSetting("richPresence_showIGN", false).set(this.displayIGN);
		this.modSetting("richPresence_timerMode", 1).set(this.timerMode);
		this.modSetting("richPresence_showAFK", true).set(this.showAfk);
		this.modSetting("richPresence_showIP", true).set(this.showIP);


		new Timer().schedule(new TimerTask() {
			public void run() {
				if (Plex.serverState.onMineplex) {
					updateRichPresence();
				}
			}
		}, 2000L);
	}

	public void connectIPCThread() {
		lastConnectionAttempt.set(Minecraft.getSystemTime());
		try {
			ipcClient.connect(DiscordBuild.ANY);
			while (true) {
				if (ipcClient.getStatus().equals(PipeStatus.CONNECTED)) {
					break;
				}
			}
			if (richPresenceErrored.get()) {
				try {
					PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GREEN", "Discord reconnected!"));
				}
				catch (Throwable ee) {
				}
			}
			richPresenceErrored.set(false);
			richPresenceOpened.set(true);
			richPresenceOpening.set(false);
		}
		catch (Throwable e) {
			if (!richPresenceErrored.get()) {
				String error = String.valueOf(e.getMessage());
				Plex.logger.error("The rich presence failed to connect due to " + error);
				Plex.logger.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
				try {
					PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("DARK_RED", "Discord disconnected! Check the log for details."));
				}
				catch (Throwable ee) {}
			}
			richPresenceOpened.set(false);
			richPresenceOpening.set(false);
			richPresenceErrored.set(true);
		}
	}

	public void attemptIPCConnection() {
		richPresenceOpening.set(true);
		lastConnectionAttempt.set(Minecraft.getSystemTime());
		currentConnectionThread = new Thread(new Runnable() {
			public void run() {
				connectIPCThread();
			}
		});
		currentConnectionThread.start();
	}

	public void handleIPCClientState() {
		if (!Plex.serverState.onMineplex || !this.modEnabled) {
			lastConnectionAttempt.set(0);
			try {
				ipcClient.close();
			}
			catch (Throwable e) {}
			richPresenceOpened.set(false);
			richPresenceOpening.set(false);
			richPresenceErrored.set(false);
			return;
		}
		if ((ipcClient.getStatus().equals(PipeStatus.CLOSED) || ipcClient.getStatus().equals(PipeStatus.DISCONNECTED)) && !richPresenceOpening.get() && richPresenceOpened.get()) {
			this.richPresenceOpened.set(false);
		}
		if (richPresenceOpening.get() && Minecraft.getSystemTime() > lastConnectionAttempt.get() + 6000L) {
			try {
				currentConnectionThread.interrupt();
			}
			catch (Throwable ee) {}
			if (!richPresenceErrored.get()) {
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("DARK_RED", "Connection to Discord timed out!"));
			}
			currentConnectionThread = null;
			richPresenceOpened.set(false);
			richPresenceOpening.set(false);
			richPresenceErrored.set(true);
			//lastConnectionAttempt.set(Minecraft.getSystemTime());
		}
		if (richPresenceOpening.get() || richPresenceOpened.get()) {
			return;
		}
		if (Minecraft.getSystemTime() > lastConnectionAttempt.get() + 15000L) {
			attemptIPCConnection();
		}
	}

	@Override
	public void joinedMineplex() {
		lastRPupdate = Minecraft.getSystemTime();
	}

	@Override
	public void leftMineplex() {
	}

	public void modLoop(boolean online) {
		handleIPCClientState();
		if (Plex.serverState.onMineplex && (Minecraft.getSystemTime() > lastRPupdate + 15000L)) {
			updateRichPresence();
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
	public void lobbyUpdated(PlexCoreLobbyType type) {
		if (type.equals(PlexCoreLobbyType.E_GAME_UPDATED)) {
			new Timer().schedule(new TimerTask() {
				public void run() {
					updateRichPresence();
				}
			}, 250L);
		}
		if (type.equals(PlexCoreLobbyType.E_SWITCHED_SERVERS)) {
			new Timer().schedule(new TimerTask() {
				public void run() {
					updateRichPresence();
				}
			}, 3000L);
		}
	}

	public long getLoopDelay() {
		return 120;
	}
}
