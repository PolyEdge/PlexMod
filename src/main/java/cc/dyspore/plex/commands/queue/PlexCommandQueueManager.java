package cc.dyspore.plex.commands.queue;

import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.client.Minecraft;

import java.util.*;


/**
 * this queue promises 2 things:<br/>
 * 1 - queue groups MUST be dispatched in order excluding commands explicitly marked as not requiring to be. these are
 * also ignored when determining ordering
 * 2 - higher priority (0 = highest) commands are executed first if possible (which is almost always)<br/><br/>
*/
public class PlexCommandQueueManager {
	private final List<PlexCommandQueue.Command> queuedCommands = Collections.synchronizedList(new ArrayList<>());
	private ProcessContext processContext = new ProcessContext();

	public long lastCommandSent = 0L;
	public boolean debug = false;

	private boolean sendCommand(PlexCommandQueue.Command command) {
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

	public PlexCommandQueue.Command addCommandToQueue(PlexCommandQueue.Command command) {
		this.queuedCommands.add(command);
		return command;
	}


	public void showDebug(PlexCommandQueue.Command command) {
		if (!this.debug) {
			return;
		}
		StringBuilder debug = new StringBuilder(); //PlexUtil.chatStyleText("DARK_RED", "BOLD", "== Queued Commands ==");
		for (PlexCommandQueue.Command com : this.queuedCommands) {
			debug.append(PlexUtilChat.chatStyleText(com.isSent() ? "GREEN" : "BLUE", com.command));// + " " + PlexUtil.chatStyleText("BLUE", com.tag) + ": " + PlexUtil.chatStyleText("DARK_GRAY", com.toString()) + " " + PlexUtil.chatStyleText("GOLD", "" + com.priority);
			debug.append(" ");
		}
		//PlexUtil.chatAddMessage(PlexUtil.chatStyleText("GREEN", " >") + " " + PlexUtil.chatStyleText("BLUE", command.tag) + ": " + PlexUtil.chatStyleText("DARK_GRAY", command.command) + " " + PlexUtil.chatStyleText("GOLD", "" + command.getPriority()));
		PlexUtilChat.chatAddMessage(debug.toString());
	}
	
	public void clearQueue() {
		cancelAll(this.queuedCommands);
		this.queuedCommands.clear();
	}
	
	public void cancelAll(List<PlexCommandQueue.Command> commandList) {
		for (PlexCommandQueue.Command command : commandList) {
			command.cancel();
		}
	}
	
	public void removeCompleted(List<PlexCommandQueue.Command> commandList) {
		List<PlexCommandQueue.Command> completed = new ArrayList<>();
		for (PlexCommandQueue.Command command : commandList) {
			if (command.isDone()) {
				completed.add(command);
			}
		}
		commandList.removeAll(completed);
	}

	public List<Integer> getPrioritySet(List<PlexCommandQueue.Command> commandList) {
		List<Integer> priorities = new ArrayList<>();
		for (PlexCommandQueue.Command command : commandList) {
			int priority = command.getPriority();
			if (!priorities.contains(priority)) {
				priorities.add(priority);
			}
		}
		Collections.sort(priorities);
		return priorities;
	}

	public List<PlexCommandQueue.Command> getCommandsWithPriority(List<PlexCommandQueue.Command> commandList, int priority) {
		List<PlexCommandQueue.Command> commands = new ArrayList<>();
		for (PlexCommandQueue.Command command : commandList) {
			if (command.getPriority() == priority) {
				commands.add(command);
			}
		}
		return commands;
	}

	public void processQueueList(List<PlexCommandQueue.Command> queueList) {
		processQueueList(queueList, new ProcessContext());
	}

	public void processQueueList(List<PlexCommandQueue.Command> queueList, ProcessContext context) {  // returns ignore groups for other priorities
		this.removeCompleted(queueList);
		List<PlexCommandQueue.Command> potentialCommands = new ArrayList<>();

		for (PlexCommandQueue.Command command : queueList) {
			if (command.isSent() && !command.isDone() && command.getBlocksQueue()) {
				context.blockingGroups.add(command.getGroup());
			}
		}

		for (PlexCommandQueue.Command command : queueList) {
			if (context.blockingGroups.contains(command.getGroup())) {
				continue;
			}
			if (command.isAwaitingResend()) {
				potentialCommands.add(command);
				if (command.getBlocksQueue()) {
					context.blockingGroups.add(command.getGroup());
				}
			}
		}

		for (PlexCommandQueue.Command command : queueList) {
			if (command.isSent() || command.isAwaitingResend()) {
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

		for (PlexCommandQueue.Command command : potentialCommands) {
			if (!command.isSent() && command.isSendable()) {
				context.sendError = sendCommand(command);
				break;
			}
		}
	}

	public void processQueue() {
		synchronized (this.queuedCommands) {
			this.removeCompleted(this.queuedCommands);
			this.processContext.reset();
			List<Integer> priorities = this.getPrioritySet(this.queuedCommands);
			for (int priority : priorities) {
				List<PlexCommandQueue.Command> commands = this.getCommandsWithPriority(this.queuedCommands, priority);
				this.processQueueList(commands, this.processContext);
			}
		}
	}

	public static class ProcessContext {
		public Set<String> blockingGroups = new HashSet<>();
		public boolean sendError = false;

		public void reset() {
			this.blockingGroups.clear();
			this.sendError = false;
		}
	}
}
