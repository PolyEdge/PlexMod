package pw.ipex.plex.commandqueue;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.core.PlexCoreListeners;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexCommandQueue {
	public static Long MAX_WAIT = 30000L;
	public static Long lastLobbySwitch;
	public static Long lastCommandSent = 0L;
	public static Long lastChatOpen = 0L;
	public static Long lastServerJoin;
	public static List<PlexQueueCommand> lowPriorityQueue = new ArrayList<PlexQueueCommand>();
	public static List<PlexQueueCommand> highPriorityQueue = new ArrayList<PlexQueueCommand>();
	
	public static Long lowPriorityCommandDelay = 1600L; // 1.6 seconds
	public static Long lowPriorityLobbySwitchDelay = 4000L; // 4 seconds
	public static Long lowPriorityJoinServerDelay = 4000L; // 4 seconds
	public static Long lowPriorityChatOpenDelay = 1000L; // 1.0 second
	
	public static Long highPriorityCommandDelay = 900L;
	
	public static Long sendingTooFastDelay = 2000L; // 2 seconds
	
	public void sendCommand(PlexQueueCommand command) {
		if (command.sendCommand()) {
			lastCommandSent = Minecraft.getSystemTime();
		}
		showDebug();
	}
	
	public Boolean canSendCommand(Long commandDelay, Long lobbySwitchDelay, Long joinServerDelay, Long chatOpenDelay) {
		if (!(Minecraft.getSystemTime() > lastCommandSent + commandDelay)) {
			return false;
		}
		if (!(Minecraft.getSystemTime() > PlexCoreListeners.lastLobbySwitch + lobbySwitchDelay)) {
			return false;
		}
		if (!(Minecraft.getSystemTime() > PlexCoreListeners.lastServerJoin + joinServerDelay)) {
			return false;
		}
		if (!(Minecraft.getSystemTime() > PlexCoreListeners.lastChatOpen + chatOpenDelay)) {
			return false;
		}
		return true;
	}
	
	public PlexQueueCommand addLowPriorityCommand(String group, String textCommand) {
		PlexQueueCommand command = new PlexQueueCommand(group, textCommand);
		lowPriorityQueue.add(command);
		return command;
	}
	
	public PlexQueueCommand addHighPriorityCommand(String group, String textCommand) {
		PlexQueueCommand command = new PlexQueueCommand(group, textCommand);
		highPriorityQueue.add(command);
		return command;
	}
	
	public PlexQueueCommand addLowPriorityCommand(String group, String textCommand, Long delay) {
		PlexQueueCommand command = new PlexQueueCommand(group, textCommand, delay);
		lowPriorityQueue.add(command);
		return command;
	}
	
	public PlexQueueCommand addHighPriorityCommand(String group, String textCommand, Long delay) {
		PlexQueueCommand command = new PlexQueueCommand(group, textCommand, delay);
		highPriorityQueue.add(command);
		return command;
	}
	
	public List<PlexQueueCommand> getLowPriorityQueueForGroup(String group) {
		return getCommandListMatchingGroup(lowPriorityQueue, group);
	}
	
	public List<PlexQueueCommand> getHighPriorityQueueForGroup(String group) {
		return getCommandListMatchingGroup(highPriorityQueue, group);
	}
	

	public void showDebug() {
		String debug = PlexCoreUtils.chatStyleText("DARK_RED", "BOLD", "HP");
		for (PlexQueueCommand com : lowPriorityQueue) {
			debug += " " + PlexCoreUtils.chatStyleText("BLUE", com.group) + ":" + PlexCoreUtils.chatStyleText("DARK_GRAY", com.group);
		}
		debug = debug + " " + PlexCoreUtils.chatStyleText("DARK_RED", "BOLD", "LP");
		for (PlexQueueCommand com : lowPriorityQueue) {
			debug += " " + PlexCoreUtils.chatStyleText("DARK_GREEN", com.group) + ":" + PlexCoreUtils.chatStyleText("DARK_GRAY", com.group);
		}
	}
	
	public void clearQueues() {
		cancelAll(lowPriorityQueue);
		cancelAll(highPriorityQueue);
		lowPriorityQueue.clear();
		highPriorityQueue.clear();
	}
	
	public void cancelAll(List<PlexQueueCommand> commandList) {
		for (PlexQueueCommand command : commandList) {
			command.cancel();
		}		
	}
	
	public void cancelAllMatchingGroup(String group, List<PlexQueueCommand> commandList) {
		for (PlexQueueCommand command : commandList) {
			if (command.group.equals(group)) {
				command.cancel();
			}
		}		
	}
	
	public void cancelCommandsPastAge(Long sentAgo, List<PlexQueueCommand> commandList) {
		for (PlexQueueCommand command : commandList) {
			if (command.isSent()) {
				if (Minecraft.getSystemTime() > command.latestCommandSentTimestamp + sentAgo) {
					command.cancel();
				}
			}
		}				
	}
	
	public void cancelAllFromHighPriorityQueueMatchingGroup(String group) {
		cancelAllMatchingGroup(group, highPriorityQueue);	
	}
	
	public void cancelAllFromLowPriorityQueueMatchingGroup(String group) {
		cancelAllMatchingGroup(group, lowPriorityQueue);		
	}
	
	public void removeCompleted(List<PlexQueueCommand> commandList) {
		List<PlexQueueCommand> completed = new ArrayList<PlexQueueCommand>();
		for (PlexQueueCommand command : commandList) {
			if (command.isMarkedComplete()) {
				completed.add(command);
			}
			else if (command.isCanceled()) {
				completed.add(command);
			}
		}
		commandList.removeAll(completed);
	}
	
	public List<PlexQueueCommand> checkForAvailableResends(List<PlexQueueCommand> commandList) {
		List<PlexQueueCommand> resends = new ArrayList<PlexQueueCommand>();
		for (PlexQueueCommand command : commandList) {
			if (command.awaitingResend) {
				if (command.sendCommandAt == null) {
					resends.add(command);
				}
				else if (Minecraft.getSystemTime() > command.sendCommandAt) {
					resends.add(command);
				}
			}
		}
		return resends;
	}
	
	public List<String> listSentUncompletedGroups(List<PlexQueueCommand> commandList) {
		List<String> sentGroups = new ArrayList<String>();
		for (PlexQueueCommand command : commandList) {
			if (command.isCommandSent() && (!sentGroups.contains(command.group))) {
				sentGroups.add(command.group);
			}
		}
		return sentGroups;
	}
	
	public List<PlexQueueCommand> getCommandListExcludingGroups(List<PlexQueueCommand> commandList, List<String> groups) {
		List<PlexQueueCommand> commands = new ArrayList<PlexQueueCommand>();
		for (PlexQueueCommand command : commandList) {
			if (!(groups.contains(command.group))) {
				commands.add(command);
			}
		}
		return commands;
	}
	
	public List<PlexQueueCommand> getCommandListMatchingGroup(List<PlexQueueCommand> commandList, String group) {
		List<PlexQueueCommand> commands = new ArrayList<PlexQueueCommand>();
		for (PlexQueueCommand command : commandList) {
			if (group.equals(command.group)) {
				commands.add(command);
			}
		}
		return commands;
	}
	
	public List<PlexQueueCommand> getCommandListAvailableTimes(List<PlexQueueCommand> commandList) {
		List<PlexQueueCommand> commands = new ArrayList<PlexQueueCommand>();
		for (PlexQueueCommand command : commandList) {
			if (command.sendCommandAt == null) {
				commands.add(command);
			}
			else if (Minecraft.getSystemTime() > command.sendCommandAt) {
				commands.add(command);
			}
		}
		return commands;		
	}
	
	public void processQueueList(List<PlexQueueCommand> queueList, Long commandDelay, Long lobbySwitchDelay, Long joinServerDelay, Long chatOpenDelay) {
		removeCompleted(queueList);
		if (!canSendCommand(commandDelay, lobbySwitchDelay, joinServerDelay, chatOpenDelay)) {
			return;
		}
		List<PlexQueueCommand> resendQueue = checkForAvailableResends(queueList);
		if (resendQueue.size() > 0) {
			sendCommand(resendQueue.get(0));
			return;
		}
		List<String> groupsAwaitingCompletion = listSentUncompletedGroups(queueList);
		List<PlexQueueCommand> potentialQueueList = getCommandListExcludingGroups(queueList, groupsAwaitingCompletion);
		potentialQueueList = getCommandListAvailableTimes(potentialQueueList);
		if (potentialQueueList.size() > 0) {
			sendCommand(potentialQueueList.get(0));
			return;
		}
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		processQueueList(highPriorityQueue, highPriorityCommandDelay, -1L, -1L, -1L);
		processQueueList(lowPriorityQueue, lowPriorityCommandDelay, lowPriorityLobbySwitchDelay, lowPriorityJoinServerDelay, lowPriorityChatOpenDelay);		
	}
}
