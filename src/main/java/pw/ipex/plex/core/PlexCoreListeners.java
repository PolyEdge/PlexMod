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
import pw.ipex.plex.ui.PlexUIModMenuScreen;

public class PlexCoreListeners {
	public static String MATCH_SERVER_MESSAGE = "^Portal> You are currently on server: (.*)$";
	public static String MATCH_GAME_NAME = "^&aGame - &e&l(.*)$";
	
	public static Pattern PATTERN_SERVER_MESSAGE = Pattern.compile(MATCH_SERVER_MESSAGE);
	public static Pattern PATTERN_GAME_NAME = Pattern.compile(MATCH_GAME_NAME);
	
	public PlexUIBase targetUI = null;
	public Boolean resetUI = false;
	public static Boolean lobbyUpdateRequired = false;
	public static Boolean awaitingLoad = false;

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
		if (Plex.serverState.onMineplex) {
			if (message.matches(MATCH_GAME_NAME)) {
				Matcher gameName = PATTERN_GAME_NAME.matcher(message);
				gameName.find();
				PlexCore.updateGameName(gameName.group(1));
				try {
					Plex.serverState.gameStartEpoch = Minecraft.getSystemTime();
					Plex.serverState.gameStartDateTime = OffsetDateTime.now();
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
		String serverIP = Plex.serverState.serverIP = event.manager.getRemoteAddress().toString().toLowerCase();
		if (!Plex.minecraft.isSingleplayer()) {
			Plex.serverState.onMineplex = (serverIP.contains("mineplex.com") || serverIP.equals("173.236.67.30") || serverIP.equals("96.45.82.193") || serverIP.equals("107.6.151.210"));
			Plex.plexCommandQueue.cancelAllFromHighPriorityQueueMatchingGroup("plexCore");
			if (Plex.serverState.onMineplex) {
				Plex.serverState.setToOnline();
				Plex.serverState.lastControlInput = Minecraft.getSystemTime();
				Plex.logger.info("[plex mod] registering mod to event bus");
				PlexCore.joinedMineplex();
				try {
					Plex.serverState.serverJoinTime = Minecraft.getSystemTime();
					Plex.serverState.serverJoinDateTime = OffsetDateTime.now();
				}
				catch (Throwable eee) {}
				PlexCoreListeners.awaitingLoad = true;
			}
		}
	}

	@SubscribeEvent
	public void playerLoggedOut(ClientDisconnectionFromServerEvent event) {
		if (Plex.serverState.onMineplex) {
			Plex.serverState.lastControlInput = Minecraft.getSystemTime();
			PlexCore.leftMineplex();
			Plex.logger.info("[plex mod] removing mod from event bus");
		}
		Plex.serverState.serverIP = null;
		Plex.plexCommandQueue.cancelAllFromHighPriorityQueueMatchingGroup("plexCore");
		PlexCoreListeners.awaitingLoad = true;
		Plex.serverState.resetToOffline();
	}
	
	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload e) {
		if (Plex.serverState.onMineplex) {
			PlexCoreListeners.awaitingLoad = true;
		}
	}
	
	@SubscribeEvent
	public void worldLoad(WorldEvent.Load e) {
		if (!Plex.serverState.onMineplex) {
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
		Plex.serverState.lastControlInput = Minecraft.getSystemTime();
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		Plex.plexCommandQueue.removeCompleted(lobbyDeterminationQueue);
		if (Plex.minecraft.ingameGUI.getChatGUI().getChatOpen()) {
			Plex.serverState.lastChatOpen = Minecraft.getSystemTime();
		}
		if (Plex.serverState.onMineplex) {
			updateLobbyType(getScoreboardTitle());
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
			Plex.minecraft.displayGuiScreen(new PlexUIModMenuScreen(targetUI));
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
		Plex.serverState.isGameSpectator = false;
		PlexCore.setLobbyType(PlexCoreLobbyType.SERVER_UNDETERMINED);
		PlexCore.dispatchLobbyChanged(PlexCoreLobbyType.SWITCHED_SERVERS);
		//PlexCore.updateLobby(PlexCoreLobbyType.SERVER_UNDETERMINED, true);
		PlexCore.updateGameName(null);
		Plex.serverState.lastLobbySwitch = Minecraft.getSystemTime();
		Plex.serverState.gameStartEpoch = null;
		Plex.serverState.gameStartDateTime = null;
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
		boolean forcedUpdate = false;
		boolean updateIfPossible = false;
		if (PlexCoreListeners.lobbyUpdateRequired) {
			forcedUpdate = true;
			PlexCoreListeners.lobbyUpdateRequired = false;
		}
		if (PlexCore.getCurrentLobbyType().equals(PlexCoreLobbyType.SERVER_UNDETERMINED)) {
			forcedUpdate = true;
		}
		if (PlexCore.getCurrentLobbyType().equals(PlexCoreLobbyType.SERVER_UNKNOWN)) {
			updateIfPossible = true;
		}			
		if (scoreboardText == null) {
			PlexCore.setLobbyType(PlexCoreLobbyType.SERVER_UNKNOWN);
			if (forcedUpdate) {
				PlexCore.dispatchLobbyChanged(PlexCoreLobbyType.SERVER_UNKNOWN);
			}
			return;
		}
		String compareText = PlexCoreUtils.removeFormatting(PlexCoreUtils.condenseChatAmpersandFilter(scoreboardText)).trim().toLowerCase();
		PlexCoreLobbyType lobbyType = PlexCoreLobbyType.SERVER_UNKNOWN;
		
		if (compareText.equals("mineplex")) {
			lobbyType = PlexCoreLobbyType.GAME_INGAME;
			if ((Minecraft.getSystemTime() > Plex.serverState.lastLobbySwitch + 3000L) && (Plex.serverState.currentGameName == null)) {
				if ((Plex.minecraft.thePlayer.capabilities.allowFlying || Plex.minecraft.thePlayer.capabilities.isFlying)) {
					Plex.serverState.isGameSpectator = true;
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
			lobbyType = PlexCoreLobbyType.SERVER_HUB;
		}
		else if (compareText.contains("mineplex clans")) {
			lobbyType = PlexCoreLobbyType.CLANS_HUB;
		}
		else if (compareText.contains("clans season")) {
			lobbyType = PlexCoreLobbyType.CLANS_SERVER;
		}
		else if (compareText.contains("waiting for players") || compareText.contains("waiting for game") || compareText.contains("starting in") || compareText.contains("vote ends in")) {
			lobbyType = PlexCoreLobbyType.GAME_LOBBY;
		}
		
		PlexCoreLobbyType finalStatus = PlexCoreLobbyType.SERVER_UNKNOWN;
		
		if (PlexCore.getCurrentLobbyType().equals(PlexCoreLobbyType.SERVER_UNDETERMINED) && lobbyType.equals(PlexCoreLobbyType.SERVER_UNKNOWN)) {
			finalStatus = PlexCoreLobbyType.SERVER_UNKNOWN;
		}
		else if (!PlexCore.getCurrentLobbyType().equals(PlexCoreLobbyType.SERVER_UNKNOWN) && lobbyType.equals(PlexCoreLobbyType.SERVER_UNKNOWN)) {
			finalStatus = PlexCore.getCurrentLobbyType();
		}
		else {
			finalStatus = lobbyType;
		}
		
		PlexCore.setLobbyType(finalStatus);
		
		if (forcedUpdate) {
			PlexCore.dispatchLobbyChanged(finalStatus);
		}
		else if (updateIfPossible && !finalStatus.equals(PlexCoreLobbyType.SERVER_UNDETERMINED) && !finalStatus.equals(PlexCoreLobbyType.SERVER_UNKNOWN)) {
			PlexCore.dispatchLobbyChanged(finalStatus);
		}
	}
}
