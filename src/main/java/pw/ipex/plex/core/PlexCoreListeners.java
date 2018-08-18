package pw.ipex.plex.core;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.commandqueue.PlexQueueCommand;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIMenuScreen;

public class PlexCoreListeners {
	public static String MATCH_SERVER_MESSAGE = "^Portal> You are currently on server: (.*)$";
	public static String MATCH_GAME_NAME = "^&aGame - &e&l(.*)$";
	
	public static Pattern PATTERN_SERVER_MESSAGE = Pattern.compile(MATCH_SERVER_MESSAGE);
	public static Pattern PATTERN_GAME_NAME = Pattern.compile(MATCH_GAME_NAME);
	
	public PlexUIBase targetUI = null;
	public Boolean resetUI = false;
	public static Boolean lobbyUpdateRequired = false;
	public static Boolean awaitingLoad = false;
	public static Boolean isGameSpectator = false;
	public static Long gameStartEpoch = null;
	public static Long serverJoinEpoch = null;
	public static Long lastControlInput = null;
	public static OffsetDateTime gameStartDT = null;
	public static OffsetDateTime serverJoinDT = null;
	public static Long lastLobbySwitch = 0L;
	public static Long lastServerJoin = 0L;
	public static Long lastChatOpen = 0L;
	public static String serverIP = "";

	public static Integer lobbyDeterminationAttempts = 0;
	public static List<PlexQueueCommand> lobbyDeterminationQueue = new ArrayList<PlexQueueCommand>();
	public static Boolean awaitingLobbyName = false;
	
	public static Long lobbyDeterminationTimeout = 8000L;
	public static Integer maxLobbyDeterminationRetries = 1;
	
	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent e) {
		if (!PlexCoreUtils.isChatMessage(e.type)) {
			return;
		}
		String message = PlexCoreUtils.condenseChatAmpersandFilter(e.message.getFormattedText());
		String min = PlexCoreUtils.minimalizeKeepCase(e.message.getFormattedText());
		Plex.plexCommandQueue.removeCompleted(lobbyDeterminationQueue);
		if (Plex.onMineplex) {
			if (message.matches(MATCH_GAME_NAME)) {
				Matcher gameName = PATTERN_GAME_NAME.matcher(message);
				gameName.find();
				PlexCore.updateGameName(gameName.group(1));
				try {
					gameStartEpoch = Minecraft.getSystemTime();
					gameStartDT = OffsetDateTime.now();
				}
				catch (Throwable ee) {}
				e.setCanceled(false);
				return;
			}
		}
		if (lobbyDeterminationQueue.size() > 0) {
			if (lobbyDeterminationQueue.get(0).isSent()) {
				if (min.matches(MATCH_SERVER_MESSAGE)) {
					lobbyDeterminationQueue.get(0).markComplete();
					Matcher lobbyName = PATTERN_SERVER_MESSAGE.matcher(min);
					lobbyName.find();
					PlexCore.updateServerName(lobbyName.group(1));
					e.setCanceled(true);
				}				
			}
		}
	}
	
	@SubscribeEvent
	public void playerLoggedIn(ClientConnectedToServerEvent event) {
		Plex.currentLobbyType = null;
		Plex.currentLobbyName = null;
		PlexCoreListeners.serverIP = event.manager.getRemoteAddress().toString().toLowerCase();
		if (!Plex.minecraft.isSingleplayer()) {
			Plex.onMineplex = (serverIP.contains("mineplex.com") || serverIP.equals("173.236.67.30") || serverIP.equals("96.45.82.193") || serverIP.equals("107.6.151.210"));
			Plex.plexCommandQueue.cancelAllFromHighPriorityQueueMatchingGroup("plexCore");
			if (Plex.onMineplex) {
				lastControlInput = Minecraft.getSystemTime();
				Plex.logger.info("[plex mod] registering mod to event bus");
				PlexCore.joinedMineplex();
				try {
					serverJoinEpoch = Minecraft.getSystemTime();
					serverJoinDT = OffsetDateTime.now();
				}
				catch (Throwable eee) {}
				PlexCoreListeners.isGameSpectator = false;
				PlexCoreListeners.awaitingLoad = true;
				PlexCoreListeners.lastServerJoin = Minecraft.getSystemTime();
				lastLobbySwitch = Minecraft.getSystemTime();
			}
		}
	}

	@SubscribeEvent
	public void playerLoggedOut(ClientDisconnectionFromServerEvent event) {
		if (Plex.onMineplex) {
			lastControlInput = Minecraft.getSystemTime();
			PlexCore.leftMineplex();
			Plex.logger.info("[plex mod] removing mod from event bus");
		}
		PlexCoreListeners.serverIP = null;
		Plex.plexCommandQueue.cancelAllFromHighPriorityQueueMatchingGroup("plexCore");
		PlexCoreListeners.awaitingLoad = true;
		PlexCoreListeners.isGameSpectator = false;
		Plex.currentLobbyType = null;
		Plex.currentLobbyName = null;
		Plex.onMineplex = false;
	}
	
	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload e) {
		if (Plex.onMineplex) {
			PlexCoreListeners.awaitingLoad = true;
		}
	}
	
	@SubscribeEvent
	public void worldLoad(WorldEvent.Load e) {
		if (!Plex.onMineplex) {
			return;
		}
		if (!PlexCoreListeners.awaitingLoad) {
			return;
		}
		PlexCoreListeners.awaitingLoad = false;
		lobbySwitched();
	}
	
	@SubscribeEvent
	public void onInput(InputEvent e) {
		lastControlInput = Minecraft.getSystemTime();
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		Plex.plexCommandQueue.removeCompleted(lobbyDeterminationQueue);
		if (Plex.minecraft.ingameGUI.getChatGUI().getChatOpen()) {
			PlexCoreListeners.lastChatOpen = Minecraft.getSystemTime();
		}
		if (Plex.onMineplex) {
			String scoreboardTitle = getScoreboardTitle();
			if (scoreboardTitle != null) {
				updateLobbyType(scoreboardTitle);
			}
			else if (Minecraft.getSystemTime() > lastLobbySwitch + 3000L) {
				updateLobbyType(null);
			}
			if (lobbyDeterminationQueue.size() > 0) {
				if (lobbyDeterminationQueue.get(0).isSent()) {
					if (Minecraft.getSystemTime() > lobbyDeterminationQueue.get(0).getSendTime() + PlexCoreListeners.lobbyDeterminationTimeout) {
						lobbyDeterminationQueue.get(0).cancel();
						if (lobbyDeterminationAttempts - 1 <= maxLobbyDeterminationRetries) {
							sendServerCommand();
						}
					}	
				}
			}
		}
		if (this.targetUI != null) {
			Plex.minecraft.displayGuiScreen(new PlexUIMenuScreen(targetUI));
		}
		if (resetUI) {
			Plex.minecraft.displayGuiScreen((GuiScreen) null);
			resetUI = false;
		}
		this.targetUI = null;	
	}
	
	public String getScoreboardTitle() {
		try {
			return Plex.minecraft.theWorld.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public void lobbySwitched() {
		PlexCoreListeners.lobbyUpdateRequired = true;
		PlexCoreListeners.lobbyDeterminationAttempts = 0;
		PlexCoreListeners.isGameSpectator = false;
		PlexCore.updateLobby(null, true);
		PlexCore.updateGameName(null);
		lastLobbySwitch = Minecraft.getSystemTime();
		gameStartEpoch = null;
		gameStartDT = null;
		for (PlexQueueCommand command : lobbyDeterminationQueue) {
			if (!command.isSent()) {
				command.cancel();
			}
		}
		sendServerCommand();
	}
	
	public void sendServerCommand() {
		PlexCoreListeners.lobbyDeterminationQueue.add(Plex.plexCommandQueue.addHighPriorityCommand("plexCore", "/server", 800L));
	}
	
	public void updateLobbyType(String scoreboardText) {
		Boolean update = false;
		if (PlexCoreListeners.lobbyUpdateRequired) {
			update = true;
			PlexCoreListeners.lobbyUpdateRequired = false;
		}
		if (PlexCore.getCurrentLobbyType() != null) {
			if ((PlexCore.getCurrentLobbyType().equals("serverUnknown")) && (scoreboardText != null)) {
				update = true;
			}			
		}
		if (scoreboardText == null) {
			PlexCore.updateLobby("serverUnknown", update);
			return;
		}
		String compareText = PlexCoreUtils.removeFormatting(PlexCoreUtils.condenseChatAmpersandFilter(scoreboardText)).trim().toLowerCase();
		if (compareText.equals("mineplex")) {
			PlexCore.updateLobby("gameIngame", update);
			if ((Minecraft.getSystemTime() > lastLobbySwitch + 3000L) && (Plex.currentGameName == null)) {
				if ((Plex.minecraft.thePlayer.capabilities.allowFlying || Plex.minecraft.thePlayer.capabilities.isFlying)) {
					PlexCoreListeners.isGameSpectator = true;
				}
				IChatComponent chatHeaderText = null;
				try {
					Field tabHeader = Plex.minecraft.ingameGUI.getTabList().getClass().getDeclaredField("header");
					tabHeader.setAccessible(true);
					chatHeaderText = (IChatComponent) tabHeader.get(Plex.minecraft.ingameGUI.getTabList());
				}
				catch (Throwable e) {}
				if (chatHeaderText != null) {
					PlexCore.updateGameName(PlexCoreUtils.removeFormatting(PlexCoreUtils.condenseChatAmpersandFilter(chatHeaderText.getFormattedText())));
				}
			}
		}
		else if (("welcome " + PlexCore.getPlayerIGN().toLowerCase() + ", to the mineplex network!").contains(compareText)) {
			PlexCore.updateLobby("mineplexHub", update);
		}
		else if (compareText.contains("mineplex clans")) {
			PlexCore.updateLobby("clansHub", update);
		}
		else if (compareText.contains("clans season")) {
			PlexCore.updateLobby("clansServer", update);
		}
		else if (compareText.contains("waiting for players")) {
			PlexCore.updateLobby("gameLobby", update);
		}
		else if (compareText.contains("waiting for game")) {
			PlexCore.updateLobby("gameLobby", update);
		}
		else if (compareText.contains("starting in")) {
			PlexCore.updateLobby("gameLobby", update);
		}
		else if (compareText.contains("vote ends in")) {
			PlexCore.updateLobby("gameLobby", update);
		}
	}
}
