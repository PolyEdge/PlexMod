package cc.dyspore.plex.mods.discordrichstatus;

import cc.dyspore.plex.core.mineplex.PlexLobbyType;
import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.PlexModBase;

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
	public long AFK_IDLE_TIME = 30000L;

	public String MATCH_SERVER_NAME = "([a-zA-Z0-9]+)-([0-9]+)";
	
	public Pattern PATTERN_SERVER_NAME = Pattern.compile(MATCH_SERVER_NAME);
	
	public Map<String, String> gameIcons = new HashMap<String, String>();
	public Map<String, String> lobbyNames = new HashMap<String, String>();
	
	public static IPCClient ipcClient;

	public RichPresence currentStatus = null;

	public AtomicBoolean richPresenceReady = new AtomicBoolean();
	public AtomicBoolean richPresenceClose = new AtomicBoolean();
	public AtomicBoolean richPresenceConnectThread = new AtomicBoolean();
	public AtomicBoolean richPresenceShowErrors = new AtomicBoolean();
	public AtomicBoolean richPresenceShowReconnect = new AtomicBoolean();

	public Thread currentConnectionThread;
	public AtomicLong lastConnectionAttempt = new AtomicLong();

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
		richPresenceReady.set(false);
		richPresenceClose.set(false);
		richPresenceConnectThread.set(false);
		richPresenceShowErrors.set(false);
		richPresenceShowReconnect.set(false);

		lastConnectionAttempt.set(0L);

		ipcClient = new IPCClient(463568324458971147L);
		ipcClient.setListener(new IPCListener() {
			@Override
			public void onReady(IPCClient client) {
				if (richPresenceClose.get()) {
					closeIPCConnection();
					return;
				}
				lastConnectionAttempt.set(0L);
				richPresenceReady.set(true);
				if (richPresenceShowReconnect.get()) {
					PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX + PlexUtilChat.chatStyleText("GREEN", "Discord reconnected!"));
				}
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
		lobbyNames.put("cw4", "Cake Wars");
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
		if (!this.richPresenceReady.get()) {
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
		if (!isRichPresenceReady()) {
			return;
		}
		if (this.modEnabled && Plex.gameState.isMineplex) {
			this.lastRPupdate = Minecraft.getSystemTime();
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
		if ((Minecraft.getSystemTime() > Plex.listeners.getLastInputTime() + AFK_IDLE_TIME) && this.showAfk) {
			this.isAfk = true;
			presence.setDetails("AFK | " + gameState[0]);
			presence.setSmallImage("afk", "AFK | " + gameState[0]);
			return presence.build();
		}
		if (Plex.gameState.currentLobby.type.equals(PlexLobbyType.GAME_INGAME)) {
			if (Plex.gameState.currentLobby.currentGame != null) {
				String game = Plex.gameState.currentLobby.currentGame.name.toLowerCase();
				if (this.gameIcons.containsKey(game)) {
					presence.setSmallImage(game, gameState[0]);
				}
				if (this.timerMode == 1 && (Plex.gameState.currentLobby.currentGame.startTimeDT != null)) {
					presence.setStartTimestamp(Plex.gameState.currentLobby.currentGame.startTimeDT);
				}
			}
		}
		if (Plex.gameState.currentLobby.type.equals(PlexLobbyType.CLANS_SERVER)) {
			presence.setSmallImage(gameIcons.get("clans"), "Playing Clans");
		}
		if (this.timerMode == 2 && Plex.gameState.joinTimeDT != null) {
			presence.setStartTimestamp(Plex.gameState.joinTimeDT);
		}
		return presence.build();
	}
	

	public String getServerIP() {
		if (Plex.gameState.multiplayerServerHostname.startsWith("us")) {
			return this.showIP ? "us.mineplex.com" : "Mineplex (US)";
		}
		if (Plex.gameState.multiplayerServerHostname.startsWith("eu")) {
			return this.showIP ? "eu.mineplex.com" : "Mineplex (EU)";
		}
		if (Plex.gameState.multiplayerServerHostname.startsWith("clans")) {
			return this.showIP ? "clans.mineplex.com" : "Mineplex (Clans)";
		}
		return this.showIP ? "mineplex.com" : "Mineplex";
	}

	public String getLobbyRepresentation() {
		if (Plex.gameState.currentLobby.name == null) {
			return null;
		}
		if (!Plex.gameState.currentLobby.name.matches(MATCH_SERVER_NAME)) {
			return null;
		}
		Matcher serverIDMatcher = PATTERN_SERVER_NAME.matcher(Plex.gameState.currentLobby.name);
		serverIDMatcher.find();
		String serverID = serverIDMatcher.group(1);
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
		if (this.displayLobbyName && Plex.gameState.currentLobby.name != null) {
			output += Plex.gameState.currentLobby.name;
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
		PlexLobbyType lobbyType = Plex.gameState.currentLobby.type;
		if (lobbyType == null) {
			return new String[] {"Playing on " + getServerIP(), serverIGN(false)};
		}
		if (lobbyType.equals(PlexLobbyType.UNDETERMINED) || lobbyType.equals(PlexLobbyType.UNKNOWN)) {
			return new String[] {"Playing on " + getServerIP(), serverIGN(false)};
		}
		if (lobbyType.equals(PlexLobbyType.CLANS_HUB)) {
			return new String[] {"Clans Hub", state};
		}
		if (lobbyType.equals(PlexLobbyType.CLANS_SERVER)) {
			if (Plex.gameState.currentClansSeason == null) {
				return new String[] {"Playing Clans", state};
			}
			else {
				return new String[] {"Clans (Season " + Plex.gameState.currentClansSeason + ")", state};

			}
		}

		if (lobbyType.equals(PlexLobbyType.MAIN_HUB)) {
			return new String[] {this.wrappedLobbyRepresentation("Main Hub", "In ", ""), state};
		}
		if (lobbyType.equals(PlexLobbyType.GAME_LOBBY)) {
			return new String[] {this.wrappedLobbyRepresentation("Game Lobby", "Game Lobby: ", ""), state};
		}
		if (lobbyType.equals(PlexLobbyType.GAME_INGAME)) {
			if (Plex.gameState.currentLobby.currentGame == null) {
				return new String[] {"In a Game", state};
			}
			return new String[] {(Plex.gameState.currentLobby.currentGame.spectating ? "Spectating " : "Playing ") + Plex.gameState.currentLobby.currentGame.name, state};
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
				if (Plex.gameState.isMineplex) {
					updateRichPresence();
				}
			}
		}, 2000L);
	}

	public boolean isRichPresenceReady() {
		return ipcClient.getStatus().equals(PipeStatus.CONNECTED);
	}

	public boolean isRichPresenceClosed() {
		return !ipcClient.getStatus().equals(PipeStatus.CONNECTED) && !ipcClient.getStatus().equals(PipeStatus.CONNECTING);
	}

	public void attemptIPCConnection() {
		if (ipcClient.getStatus().equals(PipeStatus.CONNECTING)) {
			return;
		}
		this.closeIPCConnection();
		lastConnectionAttempt.set(Minecraft.getSystemTime());
		final PlexNewRichPresenceMod me = this;
		richPresenceConnectThread.set(true);
		currentConnectionThread = new Thread(new Runnable() {
			public void run() {
				me.ipcConnectionHandler();
			}
		});
		currentConnectionThread.start();
	}

	public void closeIPCConnection() {
		if (ipcClient.getStatus().equals(PipeStatus.CONNECTED)) {
			try {
				ipcClient.close();
			}
			catch (Throwable e) {}
		}
		lastConnectionAttempt.set(0);
		richPresenceReady.set(false);
	}

	public void ipcConnectionHandler() {
		try {
			ipcClient.connect(DiscordBuild.ANY);
		}
		catch (Throwable e) {
			if (richPresenceShowErrors.get()) {
				String error = String.valueOf(e.getMessage());
				Plex.logger.error("The rich presence failed to connect due to " + error);
				Plex.logger.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
				PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX + PlexUtilChat.chatStyleText("DARK_RED", "Discord disconnected! Check the log for details."));
			}
			richPresenceShowErrors.set(false);
			richPresenceShowReconnect.set(true);
		}
		richPresenceConnectThread.set(false);
		currentConnectionThread = null;
	}

	public void handleIPCClientState() {
		if (!Plex.gameState.isMineplex || !this.modEnabled) {
			this.closeIPCConnection();
			richPresenceReady.set(false);
			richPresenceShowErrors.set(true);
			richPresenceShowReconnect.set(false);
			return;
		}
		if (isRichPresenceClosed()) {
			this.richPresenceReady.set(false);
		}
		else {
			currentConnectionThread = null;
		}
		if (richPresenceConnectThread.get() && Minecraft.getSystemTime() > lastConnectionAttempt.get() + 6000L) {
			try {
				currentConnectionThread.interrupt();
			}
			catch (Throwable ee) {}
			if (richPresenceShowErrors.get()) {
				PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX + PlexUtilChat.chatStyleText("DARK_RED", "Connection to Discord timed out!"));
				this.richPresenceShowErrors.set(false);
			}
			currentConnectionThread = null;
		}
		if (!isRichPresenceClosed()) {
			return;
		}
		if (Minecraft.getSystemTime() > lastConnectionAttempt.get() + 8000L && currentConnectionThread == null) {
			attemptIPCConnection();
		}
	}

	@Override
	public void joinedMineplex() {
		lastRPupdate = 0L;
	}

	@Override
	public void leftMineplex() {
		new Timer().schedule(new TimerTask() {
			public void run() {
				lastConnectionAttempt.set(0L);
			}
		}, 5000L);

	}

	public void modLoop(boolean online) {
		handleIPCClientState();
		if (Plex.gameState.isMineplex && (Minecraft.getSystemTime() > lastRPupdate + 15000L)) {
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
	public void lobbyUpdated(PlexLobbyType type) {
		if (type.equals(PlexLobbyType.E_GAME_UPDATED)) {
			this.lastRPupdate = Minecraft.getSystemTime();
			new Timer().schedule(new TimerTask() {
				public void run() {
					updateRichPresence();
				}
			}, 250L);
		}
		if (type.equals(PlexLobbyType.E_LOBBY_SWITCH)) {
			this.lastRPupdate = Minecraft.getSystemTime();
			new Timer().schedule(new TimerTask() {
				public void run() {
					updateRichPresence();
				}
			}, 2000L);
		}
	}

	public long getLoopDelay() {
		return 120;
	}
}
