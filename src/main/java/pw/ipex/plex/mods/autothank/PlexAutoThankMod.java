package pw.ipex.plex.mods.autothank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ci.PlexCommandListener;
import pw.ipex.plex.commandqueue.PlexQueueCommand;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.PlexCoreValue;
import pw.ipex.plex.mod.PlexModBase;

public class PlexAutoThankMod extends PlexModBase {

	public static String MATCH_AMPLIFIER_MESSAGE = "^amplifier> [a-zA-Z0-9_]{1,20} has activated a game amplifier on ([a-zA-Z ]+)!$";
	public static String MATCH_SUCCESSFUL_TIP = "^tip> you thanked [a-zA-Z0-9_]{1,20}\\. they earned 5 treasure shards and you got 5 treasure shards in return!$";
	
	public static Pattern PATTERN_AMPLIFIER_MESSAGE = Pattern.compile(MATCH_AMPLIFIER_MESSAGE);
	
	public PlexCoreValue modEnabled = new PlexCoreValue("autoThank_enabled", false);
	public Map<String, String> gameNames = new HashMap<String, String>();
	public List<PlexQueueCommand> thankQueue = new ArrayList<PlexQueueCommand>();
	public Long lastThankWave = 0L;
	public Boolean thankingInProgress = false;
	public Boolean cooldownWait = false;

	public Long thankWaveInterval = 900000L; // 15 minutes
	public Long cooldownBackoffDelay = 5000L; // 5 seconds
	public Long thankResponseTimeout = 15000L; // 15 seconds

	@Override
	public void modInit() {
		this.modEnabled.set(this.modSetting("autoThank_enabled", false).getBoolean(false));
		
		gameNames.put("master builders", "Master_Builders");
		gameNames.put("draw my thing", "Draw_My_Thing");
		gameNames.put("arcade", "Arcade");
		gameNames.put("speed builders", "Speed_Builders");
		gameNames.put("block hunt", "Block_Hunt");
		gameNames.put("cake wars", "Cake_Wars");
		gameNames.put("survival games", "Survival_Games");
		gameNames.put("skywars", "Skywars");
		gameNames.put("the bridges", "Bridges");
		gameNames.put("minestrike", "MineStrike");
		gameNames.put("smash mobs", "Smash_Mobs");
		gameNames.put("dominate", "Dominate");
		gameNames.put("ctf", "CTF");
		
		
		PlexCore.registerCommandListener(new PlexCommandListener("ath"));
		PlexCore.registerCommandHandler("ath", new PlexAutoThankCommand());
		Plex.plexCommand.registerPlexCommand("thank", new PlexAutoThankCommand());
		
		Plex.plexCommand.addPlexHelpCommand("thank", "ath", "Displays AutoThank options");
		
		PlexCore.registerUiTab("AutoThank", PlexAutoThankUI.class);
	}
	
	@Override
	public String getModName() {
		return "AutoThank";
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (!this.modEnabled.booleanValue) {
			lastThankWave = 0L; // remove this in release
			Plex.plexCommandQueue.cancelAll(thankQueue);
			Plex.plexCommandQueue.cancelAllFromLowPriorityQueueMatchingGroup("autothank");
			return;
		}
		if (Plex.currentLobbyType != null) {
			if (Plex.currentLobbyType.equals("clansServer")) {
				lastThankWave = 0L;
				Plex.plexCommandQueue.cancelAll(thankQueue);
				Plex.plexCommandQueue.cancelAllFromLowPriorityQueueMatchingGroup("autothank");
				return;
			}
		}
		Plex.plexCommandQueue.removeCompleted(thankQueue);
		if (thankQueue.size() > 0) {
			if (thankQueue.get(0).isCommandSent()) {
				if (Minecraft.getSystemTime() > thankQueue.get(0).latestCommandSentTimestamp + thankResponseTimeout) {
					thankQueue.get(0).markComplete();
				}				
			}
		}
		if (Plex.onMineplex && this.modEnabled.booleanValue) {
			if (Minecraft.getSystemTime() > (lastThankWave + thankWaveInterval)) {
				for (String game : gameNames.values()) {
					thankQueue.add(Plex.plexCommandQueue.addLowPriorityCommand("autothank", "/amplifier thank " + game));
				}
				lastThankWave = Minecraft.getSystemTime();
			}			
		}
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent e) {
		if (!this.modEnabled.booleanValue) {
			return;
		}
		String minified = PlexCoreUtils.minimalize(e.message.getFormattedText());
		if (thankQueue.size() > 0) {
			if (thankQueue.get(0).isCommandSent()) {
				if (minified.matches(MATCH_SUCCESSFUL_TIP)) {
					thankQueue.remove(0).markComplete();
					cooldownWait = false;
				}
				if (minified.toLowerCase().startsWith("amplifier> you have already thanked this amplifier")) {
					e.setCanceled(true);
					cooldownWait = false;
					thankQueue.remove(0).markComplete();		
				}
				if (minified.toLowerCase().startsWith("thanks> you have already thanked this amplifier")) {
					e.setCanceled(true);
					cooldownWait = false;
					thankQueue.remove(0).markComplete();
				}
				if (minified.toLowerCase().startsWith("amplifier> there was an error handling your request")) {
					e.setCanceled(true);
					cooldownWait = false;
					thankQueue.remove(0).markComplete();
				}
				if (minified.toLowerCase().startsWith("amplifier> please wait before trying that again")) {
					e.setCanceled(true);
					cooldownWait = true;
					thankQueue.get(0).resendCommandIn(cooldownBackoffDelay);
				}
			}
		}
		if (minified.matches(MATCH_AMPLIFIER_MESSAGE)) {
			Matcher gameMatcher = PATTERN_AMPLIFIER_MESSAGE.matcher(minified);
			gameMatcher.find();
			String gameName = gameMatcher.group(1);
			if (!(gameNames.containsKey(gameName))) {
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "The game \"" + gameName + "\" isn't recognized by AutoThank, try thanking it manually."));
			}
			else {
				thankQueue.add(Plex.plexCommandQueue.addLowPriorityCommand("autothank", "/amplifier thank " + gameNames.get(gameName)));
			}
		}
	}

	@Override
	public void saveModConfig() {
		this.modSetting("autoThank_enabled", false).set(this.modEnabled.booleanValue);
	}



	@Override
	public void leftMineplex() {
		MinecraftForge.EVENT_BUS.unregister(this);
		thankQueue.clear();
	}

	@Override
	public void switchedLobby(String name) {
		if (name != null) {
			if (name.equals("clansServer")) {
				new Timer().schedule(new TimerTask() {
					public void run() {
						PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("DARK_GRAY", "Note: AutoThank does not work on clans servers and is inactive until you leave this clans game."));	
					}
				}, 2000L);
			}
		}
	}
}
