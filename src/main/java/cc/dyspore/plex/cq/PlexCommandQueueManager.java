package cc.dyspore.plex.cq;

import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.client.Minecraft;
import cc.dyspore.plex.Plex;

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
		this.queuedCommands.add(command);
		return command;
	}


	public void showDebug(PlexCommandQueueCommand command) {
		if (!this.debug) {
			return;
		}
		StringBuilder debug = new StringBuilder(); //PlexUtil.chatStyleText("DARK_RED", "BOLD", "== Queued Commands ==");
		for (PlexCommandQueueCommand com : this.queuedCommands) {
			debug.append(PlexUtilChat.chatStyleText(com.isSent() ? "GREEN" : "BLUE", com.command));// + " " + PlexUtil.chatStyleText("BLUE", com.group) + ": " + PlexUtil.chatStyleText("DARK_GRAY", com.toString()) + " " + PlexUtil.chatStyleText("GOLD", "" + com.priority);
			debug.append(" ");
		}
		//PlexUtil.chatAddMessage(PlexUtil.chatStyleText("GREEN", " >") + " " + PlexUtil.chatStyleText("BLUE", command.group) + ": " + PlexUtil.chatStyleText("DARK_GRAY", command.command) + " " + PlexUtil.chatStyleText("GOLD", "" + command.getPriority()));
		PlexUtilChat.chatAddMessage(debug.toString());
	}
	
	public void clearQueue() {
		cancelAll(this.queuedCommands);
		this.queuedCommands.clear();
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
			if (!command.isSent() && command.isSendable()) {
				context.sendError = sendCommand(command);
				break;
			}
		}
	}

	public void processQueue() {
		synchronized (this.queuedCommands) {
			this.removeCompleted(this.queuedCommands);
			PlexCommandQueueProcessContext context = new PlexCommandQueueProcessContext();
			List<Integer> priorities = getPrioritySet(this.queuedCommands);
			for (int priority : priorities) {
				List<PlexCommandQueueCommand> commands = getCommandsWithPriority(this.queuedCommands, priority);
				this.processQueueList(commands, context);
			}
		}
	}
}
