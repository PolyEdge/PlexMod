package pw.ipex.plex.commandqueue;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreUtils;

// this queue promises 2 things

// 1 - higher priority (0 = highest) commands are executed first if possible (which is almost always)
// 2 - queue groups MUST be dispatched in order


public class PlexCommandQueueManager {
	public List<PlexCommandQueue> knownQueues = new ArrayList<>();
	public List<PlexCommandQueueCommand> queuedCommands = new ArrayList<>();

	public long lastCommandSent = 0L;

	public void sendCommand(PlexCommandQueueCommand command) {
		if (command.sendCommand()) {
			lastCommandSent = Minecraft.getSystemTime();
			showDebug(command);
		}
	}

	public PlexCommandQueueCommand addCommandToQueue(PlexCommandQueueCommand command) {
		queuedCommands.add(command);
		if (command.parentQueue != null) {
			if (!knownQueues.contains(command.parentQueue)) {
				knownQueues.add(command.parentQueue);
			}
		}
		return command;
	}

	public void registerQueue(PlexCommandQueue queue) {
		if (!knownQueues.contains(queue)) {
			knownQueues.add(queue);
		}
	}

	public void showDebug(PlexCommandQueueCommand command) {
		String debug = PlexCoreUtils.chatStyleText("DARK_RED", "BOLD", "== Queued Commands ==");
		for (PlexCommandQueueCommand com : queuedCommands) {
			debug += " " + PlexCoreUtils.chatStyleText("BLUE", com.group) + ": " + PlexCoreUtils.chatStyleText("DARK_GRAY", com.toString()) + " " + PlexCoreUtils.chatStyleText("GOLD", "" + com.priority);
		}
		//PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GREEN", " >") + " " + PlexCoreUtils.chatStyleText("BLUE", command.group) + ": " + PlexCoreUtils.chatStyleText("DARK_GRAY", command.command) + " " + PlexCoreUtils.chatStyleText("GOLD", "" + command.priority));
	}


	public Boolean canSendCommandWithDelays(PlexCommandQueueCommand command) {
		return this.canSendCommandWithDelays(command.delaySet.commandDelay, command.delaySet.lobbySwitchDelay, command.delaySet.joinServerDelay, command.delaySet.chatOpenDelay);
	}

	public Boolean canSendCommandWithDelays(Long commandDelay, Long lobbySwitchDelay, Long joinServerDelay, Long chatOpenDelay) {
		if (!Plex.serverState.onMineplex) {
			return false;
		}
		if (!(Minecraft.getSystemTime() > lastCommandSent + commandDelay)) {
			return false;
		}
		if (!(Minecraft.getSystemTime() > Plex.serverState.lastLobbySwitch + lobbySwitchDelay)) {
			return false;
		}
		if (!(Minecraft.getSystemTime() > Plex.serverState.lastServerJoin + joinServerDelay)) {
			return false;
		}
		if (!(Minecraft.getSystemTime() > Plex.serverState.lastChatOpen + chatOpenDelay)) {
			return false;
		}
		return true;
	}
	
	public void clearQueue() {
		cancelAll(queuedCommands);
		queuedCommands.clear();
	}
	
	public void cancelAll(List<PlexCommandQueueCommand> commandList) {
		for (PlexCommandQueueCommand command : commandList) {
			command.cancel();
		}		
	}
	
	public void cancelAllMatchingGroup(String group, List<PlexCommandQueueCommand> commandList) {
		for (PlexCommandQueueCommand command : commandList) {
			if (command.group.equals(group)) {
				command.cancel();
			}
		}		
	}
	
	public void cancelCommandsPastAge(Long sentAgo, List<PlexCommandQueueCommand> commandList) {
		for (PlexCommandQueueCommand command : commandList) {
			if (command.isSent()) {
				if (Minecraft.getSystemTime() > command.latestCommandSentTimestamp + sentAgo) {
					command.cancel();
				}
			}
		}				
	}

	public void cancelTimedOutCommands(List<PlexCommandQueueCommand> commandList) {
		for (PlexCommandQueueCommand command : commandList) {
			if (command.hasPassedTimeout()) {
				command.cancel();
			}
		}
	}
	
	public void removeCompleted(List<PlexCommandQueueCommand> commandList) {
		List<PlexCommandQueueCommand> completed = new ArrayList<PlexCommandQueueCommand>();
		for (PlexCommandQueueCommand command : commandList) {
			if (command.isComplete()) {
				completed.add(command);
				//PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GREEN", "[completed]") + " " + command.getDebug());
			}
			else if (command.isCanceled()) {
				completed.add(command);
				//PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("RED", "[canceled]") + " " + command.getDebug());
			}
		}
		commandList.removeAll(completed);
	}

	public List<Integer> getPrioritySet(List<PlexCommandQueueCommand> commandList) {
		List<Integer> priorities = new ArrayList<>();
		for (PlexCommandQueueCommand command : commandList) {
			if (!priorities.contains(command.priority)) {
				priorities.add(command.priority);
			}
		}
		return priorities;
	}

	public List<PlexCommandQueueCommand> getCommandsWithPriority(List<PlexCommandQueueCommand> commandList, int priority) {
		List<PlexCommandQueueCommand> commands = new ArrayList<>();
		for (PlexCommandQueueCommand command : commandList) {
			if (command.priority.equals(priority)) {
				commands.add(command);
			}
		}
		return commands;
	}
	
	public List<PlexCommandQueueCommand> checkForAvailableResends(List<PlexCommandQueueCommand> commandList) {
		List<PlexCommandQueueCommand> resends = new ArrayList<PlexCommandQueueCommand>();
		for (PlexCommandQueueCommand command : commandList) {
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
	
	public List<String> listSentUncompletedGroups(List<PlexCommandQueueCommand> commandList) {
		List<String> sentGroups = new ArrayList<String>();
		for (PlexCommandQueueCommand command : commandList) {
			if (command.isCommandSent() && (!sentGroups.contains(command.group))) {
				sentGroups.add(command.group);
			}
		}
		return sentGroups;
	}
	
	public List<PlexCommandQueueCommand> getCommandListExcludingGroups(List<PlexCommandQueueCommand> commandList, List<String> groups) {
		List<PlexCommandQueueCommand> commands = new ArrayList<PlexCommandQueueCommand>();
		for (PlexCommandQueueCommand command : commandList) {
			if (!(groups.contains(command.group))) {
				commands.add(command);
			}
		}
		return commands;
	}
	
	public List<PlexCommandQueueCommand> getCommandListMatchingGroup(List<PlexCommandQueueCommand> commandList, String group) {
		List<PlexCommandQueueCommand> commands = new ArrayList<PlexCommandQueueCommand>();
		for (PlexCommandQueueCommand command : commandList) {
			if (group.equals(command.group)) {
				commands.add(command);
			}
		}
		return commands;
	}
	
	public List<PlexCommandQueueCommand> getCommandListAvailableTimes(List<PlexCommandQueueCommand> commandList) {
		List<PlexCommandQueueCommand> commands = new ArrayList<PlexCommandQueueCommand>();
		for (PlexCommandQueueCommand command : commandList) {
			if (command.sendCommandAt == null) {
				commands.add(command);
			}
			else if (Minecraft.getSystemTime() > command.sendCommandAt) {
				commands.add(command);
			}
		}
		return commands;		
	}

	public List<String> processQueueList(List<PlexCommandQueueCommand> queueList) {
		return processQueueList(queueList, new ArrayList<>());
	}
	
	public List<String> processQueueList(List<PlexCommandQueueCommand> queueList, List<String> ignore) {  // returns ignore groups for other priorities
		removeCompleted(queueList);

		List<String> groupsAwaitingCompletion = listSentUncompletedGroups(queueList);
		List<String> ignoreGroups = new ArrayList<>();
		ignoreGroups.addAll(ignore);

		List<PlexCommandQueueCommand> potentialCommands = new ArrayList<>();

		for (PlexCommandQueueCommand command : queueList) {
			if (command.isAwaitingResend()) {
				if (ignoreGroups.contains(command.group)) {
					continue;
				}
				potentialCommands.add(command);
				ignoreGroups.add(command.group);
			}
		}

		for (PlexCommandQueueCommand command : queueList) {
			if (ignoreGroups.contains(command.group)) {
				continue;
			}
			if (command.isSent()) {
				continue;
			}
			if (command.waitForPrevious && groupsAwaitingCompletion.contains(command.group)) {
				continue;
			}
			potentialCommands.add(command);
			ignoreGroups.add(command.group);
		}

		for (PlexCommandQueueCommand command : potentialCommands) {
			if (this.canSendCommandWithDelays(command) && !command.isSent() && command.isSendable()) {
				sendCommand(command);
				break;
			}
		}

		return ignoreGroups;
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		removeCompleted(queuedCommands);
		List<String> ignore = new ArrayList<>();
		List<Integer> priorities = getPrioritySet(queuedCommands);
		for (int priority : priorities) {
			List<PlexCommandQueueCommand> commands = getCommandsWithPriority(queuedCommands, priority);
			ignore = processQueueList(commands, ignore);
		}
	}
}
