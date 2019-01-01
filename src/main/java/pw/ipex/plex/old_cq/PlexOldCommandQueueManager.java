package pw.ipex.plex.old_cq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreUtils;

// this queue promises 2 things

// 1 - higher priority (0 = highest) commands are executed first if possible (which is almost always)
// 2 - queue groups MUST be dispatched in order


public class PlexOldCommandQueueManager {
	private List<PlexOldCommandQueue> knownQueues = new CopyOnWriteArrayList<>();
	private List<PlexOldCommandQueueCommand> queuedCommands = new CopyOnWriteArrayList<>();
	private Thread thread;
	private boolean errored = false;

	public long lastCommandSent = 0L;

	public boolean sendCommand(PlexOldCommandQueueCommand command) {
		if (command.sendCommand()) {
			lastCommandSent = Minecraft.getSystemTime();
			showDebug(command);
			//Plex.logger.info("sent " + command.command);
			return true;
		}
		else {
			//Plex.logger.error("failed to send " + command.command);
			return false;
		}
	}

	public PlexOldCommandQueueCommand addCommandToQueue(PlexOldCommandQueueCommand command) {
		queuedCommands.add(command);
		if (command.parentQueue != null) {
			if (!knownQueues.contains(command.parentQueue)) {
				knownQueues.add(command.parentQueue);
			}
		}
		return command;
	}

	public void registerQueue(PlexOldCommandQueue queue) {
		if (!knownQueues.contains(queue)) {
			knownQueues.add(queue);
		}
	}

	public void showDebug(PlexOldCommandQueueCommand command) {
		String debug = ""; //PlexCoreUtils.chatStyleText("DARK_RED", "BOLD", "== Queued Commands ==");
		for (PlexOldCommandQueueCommand com : queuedCommands) {
			debug += PlexCoreUtils.chatStyleText(com.isSent() ? "GREEN" : "BLUE", com.command) + " "; //PlexCoreUtils.chatStyleText("BLUE", com.group) + ": " + PlexCoreUtils.chatStyleText("DARK_GRAY", com.toString()) + " " + PlexCoreUtils.chatStyleText("GOLD", "" + com.priority);
		}
		//PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GREEN", " >") + " " + PlexCoreUtils.chatStyleText("BLUE", command.group) + ": " + PlexCoreUtils.chatStyleText("DARK_GRAY", command.command) + " " + PlexCoreUtils.chatStyleText("GOLD", "" + command.priority));
		//PlexCoreUtils.chatAddMessage(debug);
	}


	public Boolean canSendCommandWithDelays(PlexOldCommandQueueCommand command) {
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
	
	public void cancelAll(List<PlexOldCommandQueueCommand> commandList) {
		for (PlexOldCommandQueueCommand command : commandList) {
			command.cancel();
		}
	}
	
	public void cancelAllMatchingGroup(String group, List<PlexOldCommandQueueCommand> commandList) {
		for (PlexOldCommandQueueCommand command : commandList) {
			if (command.group.equals(group)) {
				command.cancel();
			}
		}
	}
	
	public void cancelCommandsPastAge(Long sentAgo, List<PlexOldCommandQueueCommand> commandList) {
		for (PlexOldCommandQueueCommand command : commandList) {
			if (command.isSent()) {
				if (Minecraft.getSystemTime() > command.latestCommandSentTimestamp + sentAgo) {
					command.cancel();
				}
			}
		}
	}

	public void cancelTimedOutCommands(List<PlexOldCommandQueueCommand> commandList) {
		for (PlexOldCommandQueueCommand command : commandList) {
			if (command.hasPassedTimeout()) {
				command.cancel();
			}
		}
	}
	
	public void removeCompleted(List<PlexOldCommandQueueCommand> commandList) {
		List<PlexOldCommandQueueCommand> completed = new ArrayList<PlexOldCommandQueueCommand>();
		for (PlexOldCommandQueueCommand command : commandList) {
			if (command.isComplete()) {
				completed.add(command);
				//PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GREEN", "[completed]") + " " + command.getDebug());
			} else if (command.isCanceled()) {
				completed.add(command);
				//PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("RED", "[canceled]") + " " + command.getDebug());
			}
		}
		commandList.removeAll(completed);
	}

	public List<Integer> getPrioritySet(List<PlexOldCommandQueueCommand> commandList) {
		List<Integer> priorities = new ArrayList<>();
		for (PlexOldCommandQueueCommand command : commandList) {
			if (!priorities.contains(command.priority)) {
				priorities.add(command.priority);
			}
		}
		return priorities;
	}

	public List<PlexOldCommandQueueCommand> getCommandsWithPriority(List<PlexOldCommandQueueCommand> commandList, int priority) {
		List<PlexOldCommandQueueCommand> commands = new ArrayList<>();
		for (PlexOldCommandQueueCommand command : commandList) {
			if (command.priority.equals(priority)) {
				commands.add(command);
			}
		}
		return new CopyOnWriteArrayList<>(commands);
	}
	
	public List<PlexOldCommandQueueCommand> checkForAvailableResends(List<PlexOldCommandQueueCommand> commandList) {
		List<PlexOldCommandQueueCommand> resends = new ArrayList<PlexOldCommandQueueCommand>();
		for (PlexOldCommandQueueCommand command : commandList) {
			if (command.awaitingResend) {
				if (command.sendCommandAt == null) {
					resends.add(command);
				} else if (Minecraft.getSystemTime() > command.sendCommandAt) {
					resends.add(command);
				}
			}
		}
		return new CopyOnWriteArrayList<>(resends);
	}
	
	public List<String> listSentUncompletedGroups(List<PlexOldCommandQueueCommand> commandList) {
		List<String> sentGroups = new ArrayList<String>();
		for (PlexOldCommandQueueCommand command : commandList) {
			if (command.isCommandSent() && (!sentGroups.contains(command.group))) {
				sentGroups.add(command.group);
			}
		}
		return new CopyOnWriteArrayList<>(sentGroups);
	}
	
	public List<PlexOldCommandQueueCommand> getCommandListExcludingGroups(List<PlexOldCommandQueueCommand> commandList, List<String> groups) {
		List<PlexOldCommandQueueCommand> commands = new ArrayList<PlexOldCommandQueueCommand>();
		for (PlexOldCommandQueueCommand command : commandList) {
			if (!(groups.contains(command.group))) {
				commands.add(command);
			}
		}
		return new CopyOnWriteArrayList<>(commands);
	}
	
	public List<PlexOldCommandQueueCommand> getCommandListMatchingGroup(List<PlexOldCommandQueueCommand> commandList, String group) {
		List<PlexOldCommandQueueCommand> commands = new ArrayList<PlexOldCommandQueueCommand>();
		for (PlexOldCommandQueueCommand command : commandList) {
			if (group.equals(command.group)) {
				commands.add(command);
			}
		}
		return new CopyOnWriteArrayList<>(commands);
	}
	
	public List<PlexOldCommandQueueCommand> getCommandListAvailableTimes(List<PlexOldCommandQueueCommand> commandList) {
		List<PlexOldCommandQueueCommand> commands = new ArrayList<PlexOldCommandQueueCommand>();
		for (PlexOldCommandQueueCommand command : commandList) {
			if (command.sendCommandAt == null) {
				commands.add(command);
			} else if (Minecraft.getSystemTime() > command.sendCommandAt) {
				commands.add(command);
			}
		}
		return new CopyOnWriteArrayList<>(commands);
	}

	public List<String> processQueueList(List<PlexOldCommandQueueCommand> queueList) {
		return processQueueList(queueList, new ArrayList<>());
	}

	public List<String> processQueueList(List<PlexOldCommandQueueCommand> queueList, List<String> ignore) {  // returns ignore groups for other priorities
		removeCompleted(queueList);

		List<String> groupsAwaitingCompletion = listSentUncompletedGroups(queueList);
		List<String> ignoreGroups = new ArrayList<>();
		ignoreGroups.addAll(ignore);

		List<PlexOldCommandQueueCommand> potentialCommands = new ArrayList<>();

		for (PlexOldCommandQueueCommand command : queueList) {
			if (command.isAwaitingResend()) {
				if (ignoreGroups.contains(command.group)) {
					continue;
				}
				potentialCommands.add(command);
				ignoreGroups.add(command.group);
			}
		}

		for (PlexOldCommandQueueCommand command : queueList) {
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

		for (PlexOldCommandQueueCommand command : potentialCommands) {
			if (this.canSendCommandWithDelays(command) && !command.isSent() && command.isSendable()) {
				this.errored = sendCommand(command);
				break;
			}
		}

		return ignoreGroups;
	}

	public void processQueue() {
		removeCompleted(queuedCommands);
		this.errored = false;
		List<String> ignore = new ArrayList<>();
		List<Integer> priorities = getPrioritySet(queuedCommands);
		for (int priority : priorities) {
			List<PlexOldCommandQueueCommand> commands = getCommandsWithPriority(queuedCommands, priority);
			ignore = processQueueList(commands, ignore);
			if (this.errored) {
				break;
			}
		}
	}

	public void processQueueForever() {
		while (true) {
			this.processQueue();
		}
	}


	public void start() {
		assert this.thread == null;
		final PlexOldCommandQueueManager instance = this;
		this.thread = new Thread(new Runnable() {
			@Override
			public void run() {
				instance.processQueueForever();
			}
		});
		this.thread.start();
	}
}
