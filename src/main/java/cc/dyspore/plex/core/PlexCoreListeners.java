package cc.dyspore.plex.core;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.dyspore.plex.core.util.PlexUtil;
import cc.dyspore.plex.core.util.PlexUtilChat;
import cc.dyspore.plex.commands.queue.PlexCommandQueue;
import cc.dyspore.plex.mods.messagingscreen.PlexMessagingUIScreen;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cc.dyspore.plex.Plex;

@SuppressWarnings("FieldCanBeLocal")
public class PlexCoreListeners {
	private String MATCH_SERVER_MESSAGE = "^Portal> You are currently on server: (.*)$";
	private String MATCH_GAME_NAME = "^&aGame - &e&l(.*)$";
	private String MATCH_EMOTE = "^&e:(.+):&7 -> (.+)$";

	private Pattern PATTERN_SERVER_MESSAGE = Pattern.compile(MATCH_SERVER_MESSAGE);
	private Pattern PATTERN_GAME_NAME = Pattern.compile(MATCH_GAME_NAME);
	private Pattern PATTERN_EMOTE = Pattern.compile(MATCH_EMOTE);

	private IChatComponent tablistHeader;

	private boolean resetGameStateNextTick = false;
	private boolean packetListenerActive = false;
	private boolean lastPacketWasRespawn = false;

	private boolean guiScreenUpdate = false;
	private GuiScreen guiScreenTarget = null;

	private long lastTimeChatOpened;
	private long lastTimeControlInput;
	private long lastWorldLoadEvent;
	private long lastTimeWorldSwitchFired;
	private long lastTimeServerNamePacket;
	private long lastTimePacketPlayerRespawn;

	private boolean lobbyTypeDeterminationEventRequired = false;
	private boolean lobbyNameDeterminationEventRequired = false;
	private int lobbyNameDeterminationCurrentAttempts = 0;
	private int lobbyNameDeterminationMaxAttempts = 2;
	private long lobbyNameDeterminationCommandTimeout = 4000L;

	private String lastLobbyName;
	private String lastLobbyNamePacket;
	private boolean lobbyNameCheckRequired = false;
	private boolean lobbyNameDifferenceTriggersEvent = false;

	private PlexCommandQueue serverCommandQueue = new PlexCommandQueue("plexCore", Plex.queue, 0);
	private PlexCommandQueue otherCommandsQueue = new PlexCommandQueue("plexCore", Plex.queue, 1);

	private List<String> mineplexIPs = new ArrayList<>();
	private List<String> hostnameBlacklist = new ArrayList<>();

	public PlexCoreListeners() {
		// us.mineplex.com
		this.mineplexIPs.add("173.236.67.11");
		this.mineplexIPs.add("173.236.67.12");
		this.mineplexIPs.add("173.236.67.13");
		this.mineplexIPs.add("173.236.67.14");
		this.mineplexIPs.add("173.236.67.15");
		this.mineplexIPs.add("173.236.67.16");
		this.mineplexIPs.add("173.236.67.17");
		this.mineplexIPs.add("173.236.67.18");
		this.mineplexIPs.add("173.236.67.19");
		this.mineplexIPs.add("173.236.67.20");
		this.mineplexIPs.add("173.236.67.21");
		this.mineplexIPs.add("173.236.67.22");
		this.mineplexIPs.add("173.236.67.23");
		this.mineplexIPs.add("173.236.67.24");
		this.mineplexIPs.add("173.236.67.25");
		this.mineplexIPs.add("173.236.67.26");
		this.mineplexIPs.add("173.236.67.27");
		this.mineplexIPs.add("173.236.67.28");
		this.mineplexIPs.add("173.236.67.29");
		this.mineplexIPs.add("173.236.67.30");
		this.mineplexIPs.add("173.236.67.31");
		this.mineplexIPs.add("173.236.67.32");
		this.mineplexIPs.add("173.236.67.33");
		this.mineplexIPs.add("173.236.67.34");
		this.mineplexIPs.add("173.236.67.35");
		this.mineplexIPs.add("173.236.67.36");
		this.mineplexIPs.add("173.236.67.37");
		this.mineplexIPs.add("173.236.67.38");


		// mineplex.com
		this.mineplexIPs.add("96.45.82.193");
		this.mineplexIPs.add("96.45.82.3");
		this.mineplexIPs.add("96.45.83.216");
		this.mineplexIPs.add("96.45.83.38");

		// clans.mineplex.com
		this.mineplexIPs.add("173.236.67.101");
		this.mineplexIPs.add("173.236.67.102");
		this.mineplexIPs.add("173.236.67.103");

		this.hostnameBlacklist.add("build.mineplex.com");

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
	public void onCommand(CommandEvent event) {
		if (event.sender.getCommandSenderEntity() == null) {
			return;
		}
		if (!event.sender.getCommandSenderEntity().equals(Plex.minecraft.thePlayer)) {
			return;
		}
		if (Plex.minecraft.ingameGUI.getChatGUI().getSentMessages().size() == 0) {
			return;
		}
		String message = Plex.minecraft.ingameGUI.getChatGUI().getSentMessages().get(Plex.minecraft.ingameGUI.getChatGUI().getSentMessages().size() - 1);
		if (!message.startsWith("/")) {
			event.setCanceled(true); // this patches a bug in forge that makes commands execute without a slash at the beginning
		}
	}

	@SubscribeEvent
	public void onLogin(ClientConnectedToServerEvent event) {
		this.serverCommandQueue.cancelAll();
		this.otherCommandsQueue.cancelAll();

		if (Plex.minecraft.isSingleplayer()) {
			return;
		}

		PlexMP.session.join();

		InetSocketAddress address = (InetSocketAddress) event.manager.getRemoteAddress();
		String hostname = address.getHostString().toLowerCase();
		if (hostname.endsWith(".")) {
			hostname = hostname.substring(0, hostname.length() - 1); // ends with dot bec fully qualified names and w/e gfdjkgsdj
		}

		PlexMP.session.multiplayerServerHostname = hostname;
		PlexMP.session.multiplayerServerIP = address.getAddress().getHostAddress();

		for (String blacklistItem : this.hostnameBlacklist) {
			if (PlexMP.session.multiplayerServerHostname.contains(blacklistItem)) {
				return;
			}
		}

		if (!(PlexMP.session.multiplayerServerHostname.endsWith("mineplex.com") ||
				PlexMP.session.multiplayerServerHostname.equals("mineplex.com") ||
				mineplexIPs.contains(PlexMP.session.multiplayerServerIP))) {
			return;
		}

		PlexMP.lobby = new PlexMP.Lobby(); // avoid npes in case lobby detector is in fallback mode
		PlexMP.session.isMineplex = true;

		PlexCore.dispatchJoin();

		if (this.packetListenerActive) {
			this.onWorldSwitch();
		}

		if (PlexMP.session.emoteList.size() == 0) {
			PlexCommandQueue.Command emoteCommand = new PlexCommandQueue.Command("plexCore", "/emotes", 4000L).setCompleteAfterSentFor(8000L);
			this.otherCommandsQueue.add(emoteCommand);
		}
	}

	@SubscribeEvent
	public void onLogoff(ClientDisconnectionFromServerEvent event) {
		if (PlexMP.session.isMineplex) {
			PlexMP.session.isMineplex = false;
			this.lastTimeControlInput = Minecraft.getSystemTime();
			PlexCore.dispatchLeave();
		}
		this.serverCommandQueue.cancelAll();
		this.resetGameStateNextTick = true;
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!PlexMP.session.isMineplex) {
			return;
		}
		if (Minecraft.getSystemTime() < this.lastWorldLoadEvent + 200L) {
			return;
		}
		this.lastWorldLoadEvent = Minecraft.getSystemTime();
		if (!this.packetListenerActive) {
			this.onWorldSwitch();
		}
	}

	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent event) {
		if (!PlexUtilChat.chatIsMessage(event.type) || !PlexMP.session.isMineplex) {
			return;
		}
		String message = PlexUtilChat.chatCondenseAndAmpersand(event.message.getFormattedText());
		String min = PlexUtilChat.chatMinimalize(event.message.getFormattedText());
		String minLower = min.toLowerCase();

		if (message.matches(this.MATCH_GAME_NAME)) {
			Matcher gameMatcher = this.PATTERN_GAME_NAME.matcher(message);
			gameMatcher.find();
			this.putGame(gameMatcher.group(1), true);
			event.setCanceled(false);
			return;
		}

		//if (minLower.startsWith("portal> you have been sent")) {
			//this.lobbyNextSwitchGameEnd = false;
		//}

		if (minLower.matches("^1st place -? ?(.*)$")) {
			this.putGameEnded();
		}
		else if (minLower.matches("^([a-z]+) (.* )?won the game!$")) {
			this.putGameEnded();
		}

		if (min.matches(this.MATCH_SERVER_MESSAGE)) {
			if (this.serverCommandQueue.hasItems()) {
				if (this.serverCommandQueue.getItem(0).isSent()) {
					this.serverCommandQueue.getItem(0).markComplete();
					event.setCanceled(true);
				}
			}

			Matcher lobbyName = this.PATTERN_SERVER_MESSAGE.matcher(min);
			lobbyName.find();
			String lobbyServerName = lobbyName.group(1);
			this.onLobbyNameDetermine(lobbyServerName);
		}

		if (min.matches("Chat> Emotes List:")) {
			PlexMP.session.emotesDisallowed = false;
			if (otherCommandsQueue.hasItems()) {
				if (this.otherCommandsQueue.getItem(0).isSent() && this.otherCommandsQueue.getItem(0).command.equals("/emotes")) {
					event.setCanceled(true);
					this.otherCommandsQueue.getItem(0).setCompleteAfterNow(100L);
				}
			}
		}

		if (message.matches(this.MATCH_EMOTE)) {
			Matcher emoteDetails = this.PATTERN_EMOTE.matcher(message);
			emoteDetails.find();
			PlexMP.session.emoteList.put(emoteDetails.group(1), emoteDetails.group(2));
			if (otherCommandsQueue.hasItems()) {
				if (this.otherCommandsQueue.getItem(0).isSent() && this.otherCommandsQueue.getItem(0).command.equals("/emotes")) {
					event.setCanceled(true);
				}
			}
		}

		if (min.toLowerCase().startsWith("permissions> you do not have permission to do that")) {
			if (otherCommandsQueue.hasItems()) {
				if (this.otherCommandsQueue.getItem(0).isSent() && this.otherCommandsQueue.getItem(0).command.equals("/emotes")) {
					if (this.otherCommandsQueue.getItem(0).getSentElapsed() < 2000L) {
						event.setCanceled(true);
						PlexMP.session.emotesDisallowed = true;
						this.otherCommandsQueue.getItem(0).markComplete();
					}
				}
			}
		}
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
			PlexMP.reset();
			this.resetGameStateNextTick = false;
		}
	}

	@SubscribeEvent
	public void onInput(InputEvent e) {
		this.lastTimeControlInput = Minecraft.getSystemTime();
	}

	public void onPacket(ChannelHandlerContext context, Packet packet) {
		this.packetListenerActive = true;

		if (packet instanceof S07PacketRespawn) {
			if (Minecraft.getSystemTime() > this.lastTimePacketPlayerRespawn + 50L && !this.lastPacketWasRespawn) {
				this.onWorldSwitch();
			}
			this.lastPacketWasRespawn = true;
			this.lastTimePacketPlayerRespawn = Minecraft.getSystemTime();
		}
		else {
			this.lastPacketWasRespawn = false;
		}

		if (packet instanceof S47PacketPlayerListHeaderFooter) {
			S47PacketPlayerListHeaderFooter item = (S47PacketPlayerListHeaderFooter)packet;
			if (item.getHeader() != null) {
				this.tablistHeader = item.getHeader();
				String header = PlexUtilChat.chatMinimalize(this.tablistHeader.getFormattedText());
				if (header.toLowerCase().startsWith("mineplex network")) {
					this.lastTimeServerNamePacket = Minecraft.getSystemTime();
					this.lastLobbyNamePacket = header.split(" {2,}", 2)[1];
					this.onLobbyNameDetermine(this.lastLobbyNamePacket);
				}
			}
		}
	}

	// worlds

	public void onWorldSwitch() {
		this.lobbyNameDifferenceTriggersEvent = false;
		this.serverCommandQueue.cancelAllUnsent();
		this.lastTimeWorldSwitchFired = Minecraft.getSystemTime();

		PlexMP.lobby.type = PlexMP.LobbyType.UNDETERMINED;
		PlexMP.game = null;
		PlexMP.session.currentClansSeason = null;

		PlexCore.dispatchLobbyChanged(PlexMP.LobbyType.E_WORLD_CHANGE);
		this.lobbyTypeDeterminationEventRequired = true;

		if (!this.lobbyNameCheckRequired || !this.packetListenerActive) {
			this.sendServerCommand(this.packetListenerActive ? 1500L : 0L);
		}

		this.lobbyNameDeterminationEventRequired = true;
		this.lobbyNameDeterminationCurrentAttempts = 0;

		if (this.lobbyNameCheckRequired) {
			this.lobbyNameCheckRequired = false;
			this.lobbyNameDifferenceTriggersEvent = true;
			return;
		}

		this.onLobbySwitch();
	}

	public void onLobbySwitch() {
		this.putGameEnded();
		PlexMP.session.currentLobby = new PlexMP.Lobby();
		PlexCore.dispatchLobbyChanged(PlexMP.LobbyType.E_LOBBY_SWITCH);

		this.lobbyNameCheckRequired = false;
	}

	public void onLobbyNameDetermine(String name) {
		this.serverCommandQueue.cancelAllUnsent();
		boolean lobbyDifferent = !name.equalsIgnoreCase(PlexMP.session.currentLobby.server);

		if (this.lobbyNameDifferenceTriggersEvent && lobbyDifferent) {
			this.lobbyNameDifferenceTriggersEvent = false;
			this.onLobbySwitch();
		}

		if (this.lobbyNameDeterminationEventRequired && lobbyDifferent) {
			PlexMP.session.currentLobby.server = name;
			PlexCore.dispatchLobbyChanged(PlexMP.LobbyType.E_LOBBY_NAME_UPDATED);
		}
		PlexMP.session.currentLobby.server = name;
	}

	public void onLobbyTypeDetermine(PlexMP.LobbyType type, boolean force) {
		PlexMP.session.currentLobby.type = type;
		if (this.lobbyTypeDeterminationEventRequired || force) {
			PlexCore.dispatchLobbyChanged(type);
			this.lobbyTypeDeterminationEventRequired = false;
		}
		if (type.equals(PlexMP.LobbyType.GAME_LOBBY) && PlexMP.session.currentLobby.currentGame != null) {
			PlexMP.session.currentLobby.currentGame = null;
			PlexCore.dispatchLobbyChanged(PlexMP.LobbyType.E_GAME_ENDED);
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

	public IChatComponent getTablistHeader() {
		if (this.tablistHeader != null) {
			return this.tablistHeader;
		}
		return PlexUtil.readTablistHeader();
	}


	// lobbies

	public void sendServerCommand(long delay) {
		this.serverCommandQueue.add("/server", delay);
	}

	public void tickLobby() {
		if (!PlexMP.session.isMineplex) {
			return;
		}

		if (Minecraft.getSystemTime() > this.lastTimeWorldSwitchFired + (this.packetListenerActive ? 500L : 100L)) {
			this.updateLobbyType();
		}

		if (this.serverCommandQueue.firstItemSent() && this.serverCommandQueue.getFirstItem().hasBeenSentFor(this.lobbyNameDeterminationCommandTimeout)) {
			this.serverCommandQueue.getItem(0).cancel();
			if (this.lobbyNameDeterminationCurrentAttempts++ < this.lobbyNameDeterminationMaxAttempts) {
				this.sendServerCommand(0L);
			}
		}
	}

	public void putGame(String newGameName, boolean fromChat) {
		if (newGameName == null) {
			return;
		}
		newGameName = newGameName.trim();
		boolean spectating = (Plex.minecraft.thePlayer.capabilities.allowFlying || Plex.minecraft.thePlayer.capabilities.isFlying) && !fromChat;
		String currentGame = PlexMP.session.currentLobby.currentGame != null ? PlexMP.session.currentLobby.currentGame.name.trim() : null;
		if (!newGameName.equalsIgnoreCase(currentGame)) {
			PlexMP.session.currentLobby.currentGame = new PlexMP.Game(newGameName, spectating);
		}
		else if (fromChat) {
			PlexMP.session.currentLobby.currentGame.spectating = false;
		}
		if (!PlexMP.session.currentLobby.type.equals(PlexMP.LobbyType.GAME_INGAME)) {
			this.onLobbyTypeDetermine(PlexMP.LobbyType.GAME_INGAME, true);
		}
		if (!newGameName.equalsIgnoreCase(currentGame)) {
			PlexCore.dispatchLobbyChanged(PlexMP.LobbyType.E_GAME_UPDATED);
		}
	}

	public void putGameEnded() {
		if (PlexMP.session.currentLobby.currentGame != null) {
			if (!PlexMP.session.currentLobby.currentGame.ended) {
				PlexMP.session.currentLobby.currentGame.ended = true;
				PlexCore.dispatchLobbyChanged(PlexMP.LobbyType.E_GAME_ENDED);
			}
		}
		//this.lobbyNextSwitchGameEnd = true;
	}

	public void updateLobbyType() {
		if (!PlexMP.session.isMineplex) {
			return;
		}

		PlexMP.LobbyType determinedLobbyType = this.determineLobbyType();

		if (determinedLobbyType.equals(PlexMP.LobbyType.GAME_INGAME) && (Minecraft.getSystemTime() > PlexMP.session.currentLobby.joinTimeMs + 1000L) && (PlexMP.session.currentLobby.currentGame == null)) {
			String header = PlexUtilChat.chatMinimalize(this.getTablistHeader().getFormattedText());
			if (!header.toLowerCase().contains("mineplex network")) {
				if (header.contains(" - ")) {
					header = header.split(" - ", 2)[1];
				}
				this.putGame(header, false);
			}
		}

		if (determinedLobbyType.equals(PlexMP.LobbyType.GAME_LOBBY) || determinedLobbyType.equals(PlexMP.LobbyType.GAME_INGAME)) {
			this.lobbyNameCheckRequired = true;
		}

		PlexMP.LobbyType currentType = PlexMP.session.currentLobby.type;

		if (!currentType.equals(PlexMP.LobbyType.UNDETERMINED) && !currentType.equals(PlexMP.LobbyType.UNKNOWN) && determinedLobbyType.equals(PlexMP.LobbyType.UNKNOWN)) {
			return;
		}

		//if (currentType.equals(LobbyType.GAME_INGAME) && determinedLobbyType.equals(LobbyType.GAME_LOBBY) && !this.lobbyTypeDeterminationEventRequired) {
		//	this.onLobbyTypeDetermine();
		//}
		
		//PlexMP.session.currentLobby.type = determinedLobbyType;

		if (!determinedLobbyType.equals(PlexMP.LobbyType.UNDETERMINED) && !determinedLobbyType.equals(PlexMP.LobbyType.UNKNOWN)) {
			this.onLobbyTypeDetermine(determinedLobbyType, currentType.equals(PlexMP.LobbyType.GAME_INGAME) && determinedLobbyType.equals(PlexMP.LobbyType.CLANS_SERVER));
		}
	}

	public PlexMP.LobbyType determineLobbyType() {
		String scoreboardTitle = PlexUtil.readScoreboardTitle();
		if (scoreboardTitle == null) {
			return PlexMP.LobbyType.UNKNOWN;
		}
		String compareText = PlexUtilChat.chatMinimalizeLowercase(scoreboardTitle);
		String ign = PlexCore.getPlayerName();

		if (compareText.contains("mineplex clans")) {
			return PlexMP.LobbyType.CLANS_HUB;
		}
		if (compareText.contains("clans s")) {
			if (PlexMP.session.currentClansSeason == null) {
				PlexMP.session.currentClansSeason = this.getClansSeason(compareText);
			}
			return PlexMP.LobbyType.CLANS_SERVER;
		}
		if (compareText.equals("mineplex")) {
			return PlexMP.LobbyType.GAME_INGAME;
		}
		if (ign != null && ("welcome " + ign.toLowerCase() + ", to the mineplex network!").contains(compareText)) {
			return PlexMP.LobbyType.MAIN_HUB;
		}
		if (compareText.contains("waiting for players") || compareText.contains("waiting for game") || compareText.contains("starting in") || compareText.contains("vote") || compareText.contains("voting") || compareText.contains("game over")) {
			return PlexMP.LobbyType.GAME_LOBBY;
		}
		return PlexMP.LobbyType.UNKNOWN;
	}

	public String getClansSeason(String compareText) {
		String[] words = compareText.split(" ");
		for (String string : words) {
			if (string.startsWith("s")) {
				string = string.substring(1);
			}
			try {
				Integer.parseInt(string);
			}
			catch (NumberFormatException e) {
				try {
					Float.parseFloat(string);
				}
				catch (NumberFormatException e1) {
					continue;
				}
			}
			return string.trim();
		}
		return null;
	}
}
