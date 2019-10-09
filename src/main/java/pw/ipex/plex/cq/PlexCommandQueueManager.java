package pw.ipex.plex.cq;

import net.minecraft.client.Minecraft;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// this queue promises 2 things

// 1 - higher priority (0 = highest) commands are executed first if possible (which is almost always)
// 2 - queue groups MUST be dispatched in order


public class PlexCommandQueueManager {
	private final List<PlexCommandQueueCommand> queuedCommands = Collections.synchronizedList(new ArrayList<>());

	private Thread thread;

	public long lastCommandSent = 0L;
	public boolean debug = false;

	private boolean sendCommand(PlexCommandQueueCommand command) {
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

	public PlexCommandQueueCommand addCommandToQueue(PlexCommandQueueCommand command) {
		queuedCommands.add(command);
		return command;
	}


	public void showDebug(PlexCommandQueueCommand command) {
		if (!this.debug) {
			return;
		}
		String debug = ""; //PlexCoreUtils.chatStyleText("DARK_RED", "BOLD", "== Queued Commands ==");
		for (PlexCommandQueueCommand com : queuedCommands) {
			debug += PlexCoreUtils.chatStyleText(com.isSent() ? "GREEN" : "BLUE", com.command);// + " " + PlexCoreUtils.chatStyleText("BLUE", com.group) + ": " + PlexCoreUtils.chatStyleText("DARK_GRAY", com.toString()) + " " + PlexCoreUtils.chatStyleText("GOLD", "" + com.priority);
		}
		//PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GREEN", " >") + " " + PlexCoreUtils.chatStyleText("BLUE", command.group) + ": " + PlexCoreUtils.chatStyleText("DARK_GRAY", command.command) + " " + PlexCoreUtils.chatStyleText("GOLD", "" + command.getPriority()));
		PlexCoreUtils.chatAddMessage(debug);
	}


	public Boolean canSendCommandWithDelays(PlexCommandQueueCommand command) {
		return this.canSendCommandWithDelays(command.getDelaySet().commandDelay, command.getDelaySet().lobbySwitchDelay, command.getDelaySet().joinServerDelay, command.getDelaySet().chatOpenDelay);
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
	
	public void removeCompleted(List<PlexCommandQueueCommand> commandList) {
		List<PlexCommandQueueCommand> completed = new ArrayList<PlexCommandQueueCommand>();
		for (PlexCommandQueueCommand command : commandList) {
			if (command.isTerminate()) {
				completed.add(command);
			}
		}
		commandList.removeAll(completed);
	}

	public List<Integer> getPrioritySet(List<PlexCommandQueueCommand> commandList) {
		List<Integer> priorities = new ArrayList<>();
		for (PlexCommandQueueCommand command : commandList) {
			if (!priorities.contains(command.getPriority())) {
				priorities.add(command.getPriority());
			}
		}
		Collections.sort(priorities);
		return priorities;
	}

	public List<PlexCommandQueueCommand> getCommandsWithPriority(List<PlexCommandQueueCommand> commandList, int priority) {
		List<PlexCommandQueueCommand> commands = new ArrayList<>();
		for (PlexCommandQueueCommand command : commandList) {
			if (command.getPriority() == priority) {
				commands.add(command);
			}
		}
		return commands;
	}

	public void processQueueList(List<PlexCommandQueueCommand> queueList) {
		processQueueList(queueList, new PlexCommandQueueProcessContext());
	}

	public void processQueueList(List<PlexCommandQueueCommand> queueList, PlexCommandQueueProcessContext context) {  // returns ignore groups for other priorities
		removeCompleted(queueList);

		List<PlexCommandQueueCommand> potentialCommands = new ArrayList<>();

		for (PlexCommandQueueCommand command : queueList) {
			if (command.isSent() && !command.isTerminate() && command.getBlocksQueue()) {
				context.blockingGroups.add(command.getGroup());
			}
		}

		for (PlexCommandQueueCommand command : queueList) {
			if (context.blockingGroups.contains(command.getGroup())) {
				continue;
			}
			if (command.isAwaitResend()) {
				potentialCommands.add(command);
				if (command.getBlocksQueue()) {
					context.blockingGroups.add(command.getGroup());
				}
			}
		}

		for (PlexCommandQueueCommand command : queueList) {
			if (command.isSent() || command.isAwaitResend()) {
				continue;
			}
			if (command.getRespectQueueOrder() && context.blockingGroups.contains(command.getGroup())) {
				continue;
			}
			potentialCommands.add(command);
			if (command.getBlocksQueue()) {
				context.blockingGroups.add(command.getGroup());
			}
		}

		for (PlexCommandQueueCommand command : potentialCommands) {
			if (this.canSendCommandWithDelays(command) && !command.isSent() && command.isSendable()) {
				context.sendError = sendCommand(command);
				break;
			}
			else {
			}
		}
	}

	public void processQueue() {
		synchronized (queuedCommands) {
			removeCompleted(queuedCommands);
			PlexCommandQueueProcessContext context = new PlexCommandQueueProcessContext();
			List<Integer> priorities = getPrioritySet(queuedCommands);
			for (int priority : priorities) {
				List<PlexCommandQueueCommand> commands = getCommandsWithPriority(queuedCommands, priority);
				this.processQueueList(commands, context);
			}
		}
	}

	public void processQueueForever() {
		long lastProcessTime = 0L;
		while (true) {
			if (Minecraft.getSystemTime() > lastProcessTime + 20L) {
				this.processQueue();
			}
			lastProcessTime = Minecraft.getSystemTime();
		}
	}


	public void start() {
		assert this.thread == null;
		final PlexCommandQueueManager instance = this;
		this.thread = new Thread(new Runnable() {
			@Override
			public void run() {
				instance.processQueueForever();
			}
		});
		this.thread.start();
	}
}
