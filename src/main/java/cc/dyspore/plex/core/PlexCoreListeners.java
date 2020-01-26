package cc.dyspore.plex.core;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.dyspore.plex.core.mineplex.PlexGame;
import cc.dyspore.plex.core.mineplex.PlexLobby;
import cc.dyspore.plex.core.mineplex.PlexLobbyType;
import cc.dyspore.plex.core.util.PlexUtil;
import cc.dyspore.plex.core.util.PlexUtilChat;
import cc.dyspore.plex.cq.PlexCommandQueue;
import cc.dyspore.plex.mods.messagingscreen.PlexMessagingUIScreen;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.cq.PlexCommandQueueCommand;

@SuppressWarnings("FieldCanBeLocal")
public class PlexCoreListeners {
	private String MATCH_SERVER_MESSAGE = "^Portal> You are currently on server: (.*)$";
	private String MATCH_GAME_NAME = "^&aGame - &e&l(.*)$";
	private String MATCH_EMOTE = "^&e:(.+):&7 -> (.+)$";

	private Pattern PATTERN_SERVER_MESSAGE = Pattern.compile(MATCH_SERVER_MESSAGE);
	private Pattern PATTERN_GAME_NAME = Pattern.compile(MATCH_GAME_NAME);
	private Pattern PATTERN_EMOTE = Pattern.compile(MATCH_EMOTE);

	private boolean packetListenerActive;
	private boolean resetGameStateNextTick;

	private boolean guiScreenUpdate = false;
	private GuiScreen guiScreenTarget = null;

	private long lastTimeChatOpened;
	private long lastTimeControlInput;
	private long lastTimeLobbySwitchFired;

	private boolean lobbyTypeDeterminationEventRequired = false;
	private boolean lobbyNameDeterminationEventRequired = false;
	private int lobbyNameDeterminationCurrentAttempts = 0;
	private int lobbyNameDeterminationMaxAttempts = 2;
	private long lobbyNameDeterminationCommandTimeout = 4000L;

	public PlexCommandQueue serverCommandQueue = new PlexCommandQueue("plexCore", Plex.queue, 0);
	public PlexCommandQueue otherCommandsQueue = new PlexCommandQueue("plexCore", Plex.queue, 1);

	public List<String> mineplexIPs = new ArrayList<>();
	public List<String> hostnameBlacklist = new ArrayList<>();

	public PlexCoreListeners() {
		// us.mineplex.com
		mineplexIPs.add("173.236.67.11");
		mineplexIPs.add("173.236.67.12");
		mineplexIPs.add("173.236.67.14");
		mineplexIPs.add("173.236.67.15");
		mineplexIPs.add("173.236.67.16");
		mineplexIPs.add("173.236.67.17");
		mineplexIPs.add("173.236.67.23");
		mineplexIPs.add("173.236.67.24");
		mineplexIPs.add("173.236.67.26");
		mineplexIPs.add("173.236.67.29");
		mineplexIPs.add("173.236.67.31");
		mineplexIPs.add("173.236.67.32");
		mineplexIPs.add("173.236.67.34");
		mineplexIPs.add("173.236.67.38");

		// mineplex.com
		mineplexIPs.add("96.45.82.193");
		mineplexIPs.add("96.45.82.3");
		mineplexIPs.add("96.45.83.216");
		mineplexIPs.add("96.45.83.38");

		// clans.mineplex.com
		mineplexIPs.add("173.236.67.101");
		mineplexIPs.add("173.236.67.102");
		mineplexIPs.add("173.236.67.103");

		// eu.mineplex.com
		mineplexIPs.add("107.6.151.174");
		mineplexIPs.add("107.6.151.190");
		mineplexIPs.add("107.6.151.206");
		mineplexIPs.add("107.6.151.210");
		mineplexIPs.add("107.6.151.22");
		mineplexIPs.add("107.6.176.114");
		mineplexIPs.add("107.6.176.122");
		mineplexIPs.add("107.6.176.138");
		mineplexIPs.add("107.6.176.14");
		mineplexIPs.add("107.6.176.166");
		mineplexIPs.add("107.6.176.194");

		hostnameBlacklist.add("build.mineplex.com");

		this.serverCommandQueue.conditions
				.afterChatOpen(0)
				.afterLobbyChange(0)
				.afterLogon(0)
				.afterCommand(600);

		this.otherCommandsQueue.conditions
				.afterChatOpen(0)
				.afterLobbyChange(0)
				.afterLogon(500)
				.afterCommand(900);
	}

	@SubscribeEvent
	public void onCommand(CommandEvent e) {
		if (e.sender.getCommandSenderEntity() == null) {
			return;
		}
		if (!e.sender.getCommandSenderEntity().equals(Plex.minecraft.thePlayer)) {
			return;
		}
		if (Plex.minecraft.ingameGUI.getChatGUI().getSentMessages().size() == 0) {
			return;
		}
		String message = Plex.minecraft.ingameGUI.getChatGUI().getSentMessages().get(Plex.minecraft.ingameGUI.getChatGUI().getSentMessages().size() - 1);
		if (!message.startsWith("/")) {
			e.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onLogin(ClientConnectedToServerEvent event) {
		this.serverCommandQueue.cancelAll();
		this.otherCommandsQueue.cancelAll();

		if (Plex.minecraft.isSingleplayer()) {
			return;
		}

		Plex.gameState.isMultiplayer = true;
		Plex.gameState.joinTimeMS = Minecraft.getSystemTime();
		Plex.gameState.joinTimeDT = PlexUtil.getCurrentTime();

		InetSocketAddress address = (InetSocketAddress) event.manager.getRemoteAddress();
		String hostname = address.getHostString().toLowerCase();
		if (hostname.endsWith(".")) {
			hostname = hostname.substring(0, hostname.length() - 1); // y
		}

		Plex.gameState.multiplayerServerHostname = hostname;
		Plex.gameState.multiplayerServerIP = address.getAddress().getHostAddress();

		for (String blacklistItem : this.hostnameBlacklist) {
			if (Plex.gameState.multiplayerServerHostname.contains(blacklistItem)) {
				return;
			}
		}

		if (Plex.gameState.multiplayerServerHostname.endsWith("mineplex.com") || Plex.gameState.multiplayerServerHostname.equals("mineplex.com") || mineplexIPs.contains(Plex.gameState.multiplayerServerIP)) {
			Plex.gameState.currentLobby = new PlexLobby(); // avoid hairpin npes
			Plex.gameState.isMineplex = true;

			this.onLobbySwitch();
			PlexCore.joinedMineplex();

			if (Plex.gameState.emotesList.size() == 0) {
				PlexCommandQueueCommand emoteCommand = new PlexCommandQueueCommand("plexCore", "/emotes", 4000L);
				emoteCommand.completeAfter = 3000L;
				this.otherCommandsQueue.addCommand(emoteCommand);
			}
		}
	}

	@SubscribeEvent
	public void onLogoff(ClientDisconnectionFromServerEvent event) {
		if (Plex.gameState.isMineplex) {
			Plex.gameState.isMineplex = false;
			this.lastTimeControlInput = Minecraft.getSystemTime();
			PlexCore.leftMineplex();
		}
		this.serverCommandQueue.cancelAll();
		this.resetGameStateNextTick = true;
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load e) {
		if (!Plex.gameState.isMineplex) {
			return;
		}
		if (Minecraft.getSystemTime() < this.lastTimeLobbySwitchFired + 200L) {
			return;
		}
		this.lastTimeLobbySwitchFired = Minecraft.getSystemTime();
		this.onLobbySwitch();
	}

	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent e) {
		if (!PlexUtilChat.chatIsMessage(e.type) || !Plex.gameState.isMineplex) {
			return;
		}
		String message = PlexUtilChat.chatCondenseAndAmpersand(e.message.getFormattedText());
		String min = PlexUtilChat.chatMinimalize(e.message.getFormattedText());

		if (message.matches(this.MATCH_GAME_NAME)) {
			Matcher gameMatcher = this.PATTERN_GAME_NAME.matcher(message);
			gameMatcher.find();
			this.putGame(gameMatcher.group(1), false);
			e.setCanceled(false);
			return;
		}

		if (min.matches(this.MATCH_SERVER_MESSAGE)) {
			if (this.serverCommandQueue.hasItems()) {
				if (this.serverCommandQueue.getItem(0).isSent()) {
					this.serverCommandQueue.getItem(0).markComplete();
					e.setCanceled(true);
				}
			}

			Matcher lobbyName = this.PATTERN_SERVER_MESSAGE.matcher(min);
			lobbyName.find();
			String lobbyServerName = lobbyName.group(1);
			this.onLobbyNameDetermine(lobbyServerName);
		}

		if (min.matches("Chat> Emotes List:")) {
			Plex.gameState.emotesDisallowed = false;
			if (otherCommandsQueue.hasItems()) {
				if (this.otherCommandsQueue.getItem(0).isSent() && this.otherCommandsQueue.getItem(0).command.equals("/emotes")) {
					e.setCanceled(true);
				}
			}
		}

		if (message.matches(this.MATCH_EMOTE)) {
			Matcher emoteDetails = this.PATTERN_EMOTE.matcher(message);
			emoteDetails.find();
			Plex.gameState.emotesList.put(emoteDetails.group(1), emoteDetails.group(2));
			if (otherCommandsQueue.hasItems()) {
				if (this.otherCommandsQueue.getItem(0).isSent() && this.otherCommandsQueue.getItem(0).command.equals("/emotes")) {
					e.setCanceled(true);
				}
			}
		}

		if (min.toLowerCase().startsWith("permissions> you do not have permission to do that")) {
			if (otherCommandsQueue.hasItems()) {
				if (this.otherCommandsQueue.getItem(0).isSent() && this.otherCommandsQueue.getItem(0).command.equals("/emotes")) {
					if (this.otherCommandsQueue.getItem(0).getSentElapsed() < 2000L) {
						e.setCanceled(true);
						Plex.gameState.emotesDisallowed = true;
						this.otherCommandsQueue.getItem(0).markComplete();
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onInput(InputEvent e) {
		this.lastTimeControlInput = Minecraft.getSystemTime();
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (Plex.minecraft.ingameGUI.getChatGUI().getChatOpen() || PlexMessagingUIScreen.isChatOpen()) {
			this.lastTimeChatOpened = Minecraft.getSystemTime();
		}
		if (this.guiScreenUpdate) {
			Plex.minecraft.displayGuiScreen(this.guiScreenTarget);
			this.guiScreenUpdate = false;
			this.guiScreenTarget = null;
		}
		if (this.resetGameStateNextTick) {
			Plex.gameState.reset();
			this.resetGameStateNextTick = false;
		}
	}

	public void onPacket(ChannelHandlerContext context, Packet packet) {
		this.packetListenerActive = true;
		if ((packet instanceof S14PacketEntity) || (packet instanceof S3BPacketScoreboardObjective) || (packet instanceof S3CPacketUpdateScore) || (packet instanceof S2APacketParticles)) {
			return;
		}
		//Plex.logger.info(packet.getClass().getName());

		if (packet instanceof S47PacketPlayerListHeaderFooter) {
			S47PacketPlayerListHeaderFooter item = (S47PacketPlayerListHeaderFooter)packet;
			if (item.getHeader() != null) {
				String header = PlexUtilChat.chatMinimalize(item.getHeader().getFormattedText());
				if (header.toLowerCase().startsWith("mineplex network")) {
					this.onLobbyNameDetermine(header.split(" {2,}", 2)[1]);
				}
			}
		}
	}

	public void onLobbySwitch() {
		Plex.gameState.currentLobby = new PlexLobby();
		PlexCore.dispatchLobbyChanged(PlexLobbyType.E_LOBBY_SWITCH);

		this.lobbyTypeDeterminationEventRequired = true;
		this.lobbyNameDeterminationEventRequired = true;
		this.lobbyNameDeterminationCurrentAttempts = 0;

		this.serverCommandQueue.cancelAllUnsent();

		this.sendServerCommand();
	}

	public void onLobbyNameDetermine(String name) {
		this.serverCommandQueue.cancelAllUnsent();
		if (Plex.gameState.currentLobby.name.equalsIgnoreCase(name) && this.lobbyNameDeterminationEventRequired) {
			Plex.gameState.currentLobby.name = name;
			PlexCore.dispatchLobbyChanged(PlexLobbyType.E_LOBBY_NAME_UPDATED);
		}
		Plex.gameState.currentLobby.name = name;
	}

	public void onLobbyTypeDetermine(PlexLobbyType type, boolean force) {
		Plex.gameState.currentLobby.type = type;
		if (this.lobbyTypeDeterminationEventRequired || force) {
			PlexCore.dispatchLobbyChanged(type);
			this.lobbyTypeDeterminationEventRequired = false;
		}
	}

	// tools

	public void setTargetGuiScreen(GuiScreen guiScreen) {
		this.guiScreenUpdate = true;
		this.guiScreenTarget = guiScreen;
	}

	public long getChatOpenTime() {
		if (Plex.minecraft.ingameGUI.getChatGUI().getChatOpen() || PlexMessagingUIScreen.isChatOpen()) {
			this.lastTimeChatOpened = Minecraft.getSystemTime();
		}
		return this.lastTimeChatOpened;
	}

	public long getLastInputTime() {
		return this.lastTimeControlInput;
	}

	// lobbies

	public void sendServerCommand() {
		this.serverCommandQueue.addCommand("/server");
	}

	public void handleLobbySwitching() {
		if (!Plex.gameState.isMineplex) {
			return;
		}

		if (Minecraft.getSystemTime() > this.lastTimeLobbySwitchFired + 150L) {
			this.updateLobbyType();
		}

		if (this.serverCommandQueue.hasItems()) {
			if (this.serverCommandQueue.getItem(0).isSent()) {
				if (Minecraft.getSystemTime() > this.serverCommandQueue.getItem(0).getSendTime() + this.lobbyNameDeterminationCommandTimeout) {
					this.serverCommandQueue.getItem(0).cancel();
					if (lobbyNameDeterminationCurrentAttempts - 1 <= this.lobbyNameDeterminationMaxAttempts) {
						this.sendServerCommand();
					}
				}
			}
		}
	}

	public void putGame(String newGameName, boolean allowSpectator) {
		if (newGameName == null) {
			return;
		}
		String currentGame = Plex.gameState.currentLobby.currentGame != null ? Plex.gameState.currentLobby.currentGame.name : null;
		if (!newGameName.equalsIgnoreCase(currentGame)) {
			Plex.gameState.currentLobby.currentGame = new PlexGame(newGameName, (Plex.minecraft.thePlayer.capabilities.allowFlying || Plex.minecraft.thePlayer.capabilities.isFlying) && allowSpectator);
		}
		if (!Plex.gameState.currentLobby.type.equals(PlexLobbyType.GAME_INGAME)) {
			this.onLobbyTypeDetermine(PlexLobbyType.GAME_INGAME, true);
		}
		if (!newGameName.equalsIgnoreCase(currentGame)) {
			PlexCore.dispatchLobbyChanged(PlexLobbyType.E_GAME_UPDATED);
		}
	}

	public void updateLobbyType() {
		if (!Plex.gameState.isMineplex) {
			return;
		}

		PlexLobbyType determinedLobbyType = this.determineLobbyType();

		if (determinedLobbyType.equals(PlexLobbyType.GAME_INGAME) && (Minecraft.getSystemTime() > Plex.gameState.currentLobby.joinTimeMs + 1000L) && (Plex.gameState.currentLobby.currentGame == null)) {
			String header = PlexUtil.readTablistHeader();
			if (header.contains(" - ")) {
				header = header.split(" - ", 2)[1];
			}
			this.putGame(header, true);
		}

		PlexLobbyType currentType = Plex.gameState.currentLobby.type;

		if (!currentType.equals(PlexLobbyType.UNDETERMINED) && !currentType.equals(PlexLobbyType.UNKNOWN) && determinedLobbyType.equals(PlexLobbyType.UNKNOWN)) {
			return;
		}
		
		Plex.gameState.currentLobby.type = currentType;
		
		if (this.lobbyTypeDeterminationEventRequired && !currentType.equals(PlexLobbyType.UNDETERMINED) && !currentType.equals(PlexLobbyType.UNKNOWN)) {
			this.onLobbyTypeDetermine(currentType, false);
		}
	}

	public PlexLobbyType determineLobbyType() {
		String scoreboardTitle = PlexUtil.readScoreboardTitle();
		if (scoreboardTitle == null) {
			return PlexLobbyType.UNKNOWN;
		}
		String compareText = PlexUtilChat.chatMinimalizeLowercase(scoreboardTitle);
		String ign = PlexCore.getPlayerIGN();

		if (compareText.equals("mineplex")) {
			return PlexLobbyType.GAME_INGAME;
		}
		if (ign != null && ("welcome " + ign.toLowerCase() + ", to the mineplex network!").contains(compareText)) {
			return PlexLobbyType.MAIN_HUB;
		}
		if (compareText.contains("mineplex clans")) {
			return PlexLobbyType.CLANS_HUB;
		}
		if (compareText.contains("clans season")) {
			Plex.gameState.currentClansSeason = compareText.replace("clans season", "").trim();
			return PlexLobbyType.CLANS_SERVER;
		}
		if (compareText.contains("waiting for players") || compareText.contains("waiting for game") || compareText.contains("starting in") || compareText.contains("vote") || compareText.contains("voting") || compareText.contains("game over")) {
			return PlexLobbyType.GAME_LOBBY;
		}
		return PlexLobbyType.UNKNOWN;
	}
}
