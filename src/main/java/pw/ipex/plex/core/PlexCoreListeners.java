package pw.ipex.plex.core;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.time.OffsetDateTime;
import java.util.*;
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
import pw.ipex.plex.commandqueue.PlexCommandQueue;
import pw.ipex.plex.commandqueue.PlexCommandQueueCommand;
import pw.ipex.plex.mods.messaging.ui.PlexMessagingUIScreen;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIModMenuScreen;

public class PlexCoreListeners {
	public String MATCH_SERVER_MESSAGE = "^Portal> You are currently on server: (.*)$";
	public String MATCH_GAME_NAME = "^&aGame - &e&l(.*)$";
	public String MATCH_EMOTE = "^&e:(.+):&7 -> &e(.+)$";
	
	public Pattern PATTERN_SERVER_MESSAGE = Pattern.compile(MATCH_SERVER_MESSAGE);
	public Pattern PATTERN_GAME_NAME = Pattern.compile(MATCH_GAME_NAME);
	public Pattern PATTERN_EMOTE = Pattern.compile(MATCH_EMOTE);
	
	public PlexUIBase targetUI = null;
	public Boolean resetUI = false;
	public Boolean lobbyUpdateRequired = false;
	public Boolean awaitingLoad = false;
	public Map<Integer, Integer> lobbyLoadCounter = new HashMap<Integer, Integer>();
	public Map<Integer, List<Long>> lobbyLoadTimes = new HashMap<Integer, List<Long>>();
	public List<Integer> dispatchedLobbyChanges = new ArrayList<>();

	public Integer lobbyDeterminationAttempts = 0;
	public Integer maxLobbyDeterminationRetries = 2;
	public PlexCommandQueue serverCommandQueue = new PlexCommandQueue("plexCore", Plex.plexCommandQueue);
	public PlexCommandQueue otherCommandsQueue = new PlexCommandQueue("plexCore", Plex.plexCommandQueue);
	
	public Long lobbyDeterminationTimeout = 8000L;

	public List<String> mineplexIPs = new ArrayList<>();
	public List<String> hostnameBlacklist = new ArrayList<>();


	public PlexCoreListeners() {
		mineplexIPs.add("173.236.67.11");
		mineplexIPs.add("173.236.67.16");
		mineplexIPs.add("96.45.82.193");
		mineplexIPs.add("96.45.82.216");
		mineplexIPs.add("107.6.176.138");

		hostnameBlacklist.add("build.mineplex.com");

		this.serverCommandQueue.setPriority(0);
		this.serverCommandQueue.delaySet.chatOpenDelay = -1000L;
		this.serverCommandQueue.delaySet.lobbySwitchDelay = -1000L;
		this.serverCommandQueue.delaySet.joinServerDelay = -1000L;
		this.serverCommandQueue.delaySet.commandDelay = 600L;

		this.otherCommandsQueue.setPriority(1);
		this.otherCommandsQueue.delaySet.chatOpenDelay = 0L;
		this.otherCommandsQueue.delaySet.lobbySwitchDelay = 0L;
		this.otherCommandsQueue.delaySet.joinServerDelay = 500L;
		this.otherCommandsQueue.delaySet.commandDelay = 900L;
	}
	
	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent e) {
		if (!PlexCoreUtils.isChatMessage(e.type)) {
			return;
		}
		String message = PlexCoreUtils.condenseChatAmpersandFilter(e.message.getFormattedText());
		String min = PlexCoreUtils.minimalizeKeepCase(e.message.getFormattedText());
		if (Plex.serverState.onMineplex) {
			if (message.matches(this.MATCH_GAME_NAME)) {
				Matcher gameName = this.PATTERN_GAME_NAME.matcher(message);
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
		if (min.matches(this.MATCH_SERVER_MESSAGE)) {
			//Plex.logger.info("test");
			if (this.serverCommandQueue.hasItems()) {
				if (this.serverCommandQueue.getItem(0).isSent()) {
					this.serverCommandQueue.getItem(0).markComplete();
					e.setCanceled(true);
				}
			}

			Matcher lobbyName = this.PATTERN_SERVER_MESSAGE.matcher(min);
			lobbyName.find();
			String lobbyServerName = lobbyName.group(1);
			PlexCore.updateServerName(lobbyServerName);
			Plex.serverState.updatedLobbyName = lobbyServerName;
		}
		if (min.matches("Chat> Emotes List:")) {
			if (otherCommandsQueue.hasItems()) {
				if (this.otherCommandsQueue.getItem(0).isSent() && this.otherCommandsQueue.getItem(0).command.equals("/emotes")) {
					e.setCanceled(true);
				}
			}
		}
		if (message.matches(this.MATCH_EMOTE)) {
			Matcher emoteDetails = this.PATTERN_EMOTE.matcher(message);
			emoteDetails.find();
			Plex.serverState.emotesList.put(emoteDetails.group(1), emoteDetails.group(2));
			if (otherCommandsQueue.hasItems()) {
				if (this.otherCommandsQueue.getItem(0).isSent() && this.otherCommandsQueue.getItem(0).command.equals("/emotes")) {
					e.setCanceled(true);
				}
			}
		}
		if (min.toLowerCase().startsWith("permissions> you do not have permission to do that")) {
			if (otherCommandsQueue.hasItems()) {
				if (this.otherCommandsQueue.getItem(0).isSent() && this.otherCommandsQueue.getItem(0).command.equals("/emotes")) {
					if (this.otherCommandsQueue.getItem(0).getTimeSinceSent() < 2000L) {
						e.setCanceled(true);
						Plex.serverState.canUseEmotes = false;
						this.otherCommandsQueue.getItem(0).markComplete();
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void playerLoggedIn(ClientConnectedToServerEvent event) {
		Plex.serverState.onMineplex = false;
		this.serverCommandQueue.cancelAll();
		if (Plex.minecraft.isSingleplayer()) {
			return;
		}
		InetSocketAddress address = (InetSocketAddress) event.manager.getRemoteAddress();
		String hostname = address.getHostString().toLowerCase();
		if (hostname.endsWith(".")) {
			hostname = hostname.substring(0, hostname.length() - 1); // y
		}
		Plex.serverState.serverHostname = hostname;
		Plex.serverState.serverIP = address.getAddress().getHostAddress();
		for (String blacklistItem : this.hostnameBlacklist) {
			if (Plex.serverState.serverHostname.contains(blacklistItem)) {
				return;
			}
		}

		if (Plex.serverState.serverHostname.endsWith("mineplex.com") || Plex.serverState.serverHostname.equals("mineplex.com") || mineplexIPs.contains(Plex.serverState.serverIP)) {
			Plex.serverState.onMineplex = true;
		}

		if (Plex.serverState.onMineplex) {
			Plex.serverState.setToOnline();
			Plex.serverState.lastControlInput = Minecraft.getSystemTime();
			//Plex.logger.info("[plex mod] registering mod to event bus");
			PlexCore.joinedMineplex();
			this.sendServerCommand();

			try {
				Plex.serverState.serverJoinTime = Minecraft.getSystemTime();
				Plex.serverState.serverJoinDateTime = OffsetDateTime.now();
			}
			catch (Throwable eee) {
			}
			this.awaitingLoad = true;
			if (Plex.serverState.emotesList.size() == 0) {
				PlexCommandQueueCommand emoteCommand = new PlexCommandQueueCommand("plexCore", "/emotes", 4000L);
				emoteCommand.completeAfter = 3000L;
				this.otherCommandsQueue.addCommand(emoteCommand);
			}
		}
	}

	@SubscribeEvent
	public void playerLoggedOut(ClientDisconnectionFromServerEvent event) {
		if (Plex.serverState.onMineplex) {
			Plex.serverState.onMineplex = false;
			Plex.serverState.lastControlInput = Minecraft.getSystemTime();
			PlexCore.leftMineplex();
			//Plex.logger.info("[plex mod] removing mod from event bus");
		}
		Plex.serverState.serverIP = null;
		this.serverCommandQueue.cancelAll();
		this.awaitingLoad = true;
		Plex.serverState.resetToOffline();
	}
	
	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload e) {
		if (!Plex.serverState.onMineplex) {
			return;
		}
		if (!this.lobbyLoadTimes.containsKey(e.world.hashCode())) {
			this.lobbyLoadTimes.put(e.world.hashCode(), new ArrayList<>());
		}
		if (this.lobbyLoadTimes.get(e.world.hashCode()).size() == 0) {
			return;
		}
		List<Long> times = this.lobbyLoadTimes.get(e.world.hashCode());
		//if (Minecraft.getSystemTime() > times.get(times.size() - 1) + 200L) {
			//this.lobbySwitched();
		//}
		this.lobbyLoadTimes.get(e.world.hashCode()).remove(times.size() - 1);
	}
	
	@SubscribeEvent
	public void worldLoad(WorldEvent.Load e) {
		if (!Plex.serverState.onMineplex) {
			return;
		}
		if (!this.lobbyLoadTimes.containsKey(e.world.hashCode())) {
			this.lobbyLoadTimes.put(e.world.hashCode(), new ArrayList<>());
		}
		this.lobbyLoadTimes.get(e.world.hashCode()).add(Minecraft.getSystemTime());
		if (!this.awaitingLoad) {
			return;
		}
		this.awaitingLoad = false;
		//this.lobbySwitched();
	}
	
	@SubscribeEvent
	public void onInput(InputEvent e) {
		Plex.serverState.lastControlInput = Minecraft.getSystemTime();
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (Plex.minecraft.ingameGUI.getChatGUI().getChatOpen() || PlexMessagingUIScreen.isChatOpen()) {
			Plex.serverState.lastChatOpen = Minecraft.getSystemTime();
		}
		if (Plex.serverState.onMineplex) {
			for (Integer worldHash : this.lobbyLoadTimes.keySet()) {
				if (this.lobbyLoadTimes.get(worldHash).size() != 0) {
					boolean found = false;
					for (Long loadTime : this.lobbyLoadTimes.get(worldHash)) {
						if (Minecraft.getSystemTime() > loadTime + 200L) {
							found = true;
						}
						if (Minecraft.getSystemTime() > loadTime + 200L && !this.dispatchedLobbyChanges.contains(worldHash)) {
							this.lobbySwitched();
							this.dispatchedLobbyChanges.add(worldHash);
						}
					}
					if (!found) {
						this.dispatchedLobbyChanges.remove(worldHash);
					}
				}
				else {
					this.dispatchedLobbyChanges.remove(worldHash);
				}
			}
			this.updateLobbyType(getScoreboardTitle());
			if (this.serverCommandQueue.hasItems()) {
				if (this.serverCommandQueue.getItem(0).isSent()) {
					if (Minecraft.getSystemTime() > this.serverCommandQueue.getItem(0).getSendTime() + this.lobbyDeterminationTimeout) {
						this.serverCommandQueue.getItem(0).cancel();
						if (lobbyDeterminationAttempts - 1 <= this.maxLobbyDeterminationRetries) {
							this.sendServerCommand();
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
		this.lobbyUpdateRequired = true;
		this.lobbyDeterminationAttempts = 0;
		Plex.serverState.isGameSpectator = false;
		this.serverCommandQueue.cancelAllUnsent();
		this.sendServerCommand();
		PlexCore.setLobbyType(PlexCoreLobbyType.SERVER_UNDETERMINED);
		PlexCore.dispatchLobbyChanged(PlexCoreLobbyType.SWITCHED_SERVERS);
		//PlexCore.updateLobby(PlexCoreLobbyType.SERVER_UNDETERMINED, true);
		PlexCore.updateGameName(null);
		Plex.serverState.lastLobbySwitch = Minecraft.getSystemTime();
		Plex.serverState.gameStartEpoch = null;
		Plex.serverState.gameStartDateTime = null;
		Plex.serverState.updatedLobbyName = null;
	}
	
	public void sendServerCommand() {
		this.serverCommandQueue.addCommand("/server");
	}
	
	public void updateLobbyType(String scoreboardText) {
		boolean forcedUpdate = false;
		boolean updateIfPossible = false;
		if (this.lobbyUpdateRequired) {
			forcedUpdate = true;
			this.lobbyUpdateRequired = false;
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
				IChatComponent tabHeaderText = null;
				try {
					Field tabHeader = Plex.minecraft.ingameGUI.getTabList().getClass().getDeclaredField("header");
					tabHeader.setAccessible(true);
					tabHeaderText = (IChatComponent) tabHeader.get(Plex.minecraft.ingameGUI.getTabList());
				}
				catch (Throwable e) {}
				if (tabHeaderText != null) {
					String headerText = PlexCoreUtils.condenseChatAmpersandFilter(tabHeaderText.getFormattedText());
					if (!headerText.toLowerCase().contains("mineplex network")) {
						PlexCore.updateGameName(PlexCoreUtils.removeFormatting(headerText));
					}
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
