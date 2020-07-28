package cc.dyspore.plex.commands.queue;

import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.PlexMP;
import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlexCommandQueue {
    public PlexCommandQueueManager manager;

    public String group;
    public int priority = 50;
    public boolean respectQueueOrder = true;

    public CommandConditions conditions = new CommandConditions();
    private final List<Command> queueItems = Collections.synchronizedList(new ArrayList<>());

    public PlexCommandQueue(String group, PlexCommandQueueManager manager) {
        this.group = group;
        this.manager = manager;
    }

    public PlexCommandQueue(String group, PlexCommandQueueManager manager, int priority) {
        this.group = group;
        this.manager = manager;
        this.setPriority(priority);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Command add(String textCommand) {
        Command command = new Command(textCommand);
        this.add(command);
        return command;
    }

    public Command add(String textCommand, long delay) {
        Command command = new Command(textCommand, delay);
        this.add(command);
        return command;
    }

    public Command add(Command command) {
        this.queueItems.add(command);
        this.manager.addCommandToQueue(command);
        return command;
    }

    public Command newCommand(String textCommand) {
        return new Command(textCommand);
    }

    public void removeCompleted() {
        List<Command> completed = new ArrayList<>();
        synchronized (queueItems) {
            for (Command command : queueItems) {
                if (command.isDone()) {
                    completed.add(command);
                }
            }
        }
        queueItems.removeAll(completed);
    }

    public List<Command> getItems() {
        this.removeCompleted();
        return this.queueItems;
    }

    public Command getItem(int item) {
        this.removeCompleted();
        if (item >= this.queueItems.size()) {
            return null;
        }
        return this.queueItems.get(item);
    }

    public Command getFirstItem() {
        return this.getItem(0);
    }

    public boolean hasItems() {
        this.removeCompleted();
        return this.queueItems.size() > 0;
    }

    public boolean firstItemSent() {
        return this.hasItems() && this.getItem(0).isSent();
    }

    public void cancelAll() {
        synchronized (this.queueItems) {
            for (Command command : this.queueItems) {
                command.cancel();
            }
            this.removeCompleted();
        }
    }

    public void cancelAllUnsent() {
        synchronized (this.queueItems) {
            for (Command command : this.queueItems) {
                if (!command.isSent()) {
                    command.cancel();
                }
            }
        }
        this.removeCompleted();
    }

    @SuppressWarnings("RedundantIfStatement")
    public class Command {
        public String command;
        public CommandCompletion completionStatus = CommandCompletion.WAITING; // -1=none 0=complete 1=canceled
        public CommandStatus sendStatus = CommandStatus.UNSENT; // -1=unsent 0=sent 1=awaitingResend 2=resent

        protected boolean conditionsOverride;
        protected boolean priorityOverride;
        protected boolean respectOrderOverride;
        protected boolean blocksQueueOverride;

        protected CommandConditions conditions;
        protected int priority;
        protected boolean respectOrder;
        protected boolean blocksQueue;

        private boolean hasSendAfter = false;
        private boolean hasCompleteAfterSent = false;
        private boolean hasCompleteAfterNow = false;
        private boolean hasTimeout = false;

        private long sendAfter; // when set, flags the manager to wait until after the set time in milliseconds before sending the command
        private long completeAfterSent; // when set, flags the manager to mark the command as complete after this number of milliseconds when sent
        private long completeAfterNow;
        private long timeout;

        public List<Long> sendTimes = new ArrayList<>();

        public Command(String command) {
            this.command = command;
        }

        public Command(String command, long delay) {
            this(command);
            this.setSendAfter(delay);
        }



        public CommandConditions getOverrideConditions() {
            this.conditions = this.conditions == null ? new CommandConditions() : this.conditions;
            return this.conditions;
        }

        public Command setOverrideConditions(CommandConditions conditions) {
            this.conditions = conditions;
            return this;
        }

        public Command setConditionsOverride() {
            this.conditionsOverride = true;
            return this;
        }

        public Command clearConditionsOverride() {
            this.conditionsOverride = false;
            return this;
        }

        public Command setPriority(int priority) {
            this.priority = priority;
            this.priorityOverride = true;
            return this;
        }

        public Command clearPriority() {
            this.priority = 0;
            this.priorityOverride = false;
            return this;
        }

        public Command setRespectsOrder(boolean respectOrder) {
            this.respectOrder = respectOrder;
            this.respectOrderOverride = true;
            return this;
        }

        public Command clearRespectsOrder() {
            this.respectOrderOverride = false;
            return this;
        }

        public Command setBlocksQueue(boolean blocksQueue) {
            this.blocksQueue = blocksQueue;
            this.blocksQueueOverride = true;
            return this;
        }

        public Command clearBlocksQueue() {
            this.blocksQueue = false;
            return this;
        }

        //

        public String getGroup() {
            return PlexCommandQueue.this.group;
        }

        public CommandConditions getConditions() {
            this.conditions = this.conditionsOverride && this.conditions == null ? new CommandConditions() : this.conditions;
            return this.conditionsOverride ? this.conditions : PlexCommandQueue.this.conditions;
        }

        public int getPriority() {
            return this.priorityOverride ? this.priority : PlexCommandQueue.this.priority;
        }

        public boolean getRespectQueueOrder() {
            return this.respectOrderOverride ? this.respectOrder : PlexCommandQueue.this.respectQueueOrder;
        }

        public boolean getBlocksQueue() {
            return this.blocksQueueOverride ? this.blocksQueue : this.getRespectQueueOrder();
        }

        //

        public Command setSendAfter(long time) {
            this.sendAfter = Minecraft.getSystemTime() + time;
            this.hasSendAfter = true;
            return this;
        }

        public Command clearSendAfter() {
            this.sendAfter = 0;
            this.hasSendAfter = false;
            return this;
        }

        public Command setCompleteAfterSentFor(long time) {
            this.completeAfterSent = time;
            this.hasCompleteAfterSent = true;
            return this;
        }

        public Command clearCompleteAfterSentFor() {
            this.completeAfterSent = 0;
            this.hasCompleteAfterSent = false;
            return this;
        }


        public Command setCompleteOnSend() {
            this.completeAfterSent = 0;
            this.hasCompleteAfterSent = true;
            return this;
        }

        public Command clearCompleteOnSend() {
            this.completeAfterSent = 0;
            this.hasCompleteAfterSent = false;
            return this;
        }

        public Command setCompleteAfterNow(long time) {
            this.completeAfterNow = Minecraft.getSystemTime() + time;
            this.hasCompleteAfterNow = true;
            return this;
        }

        public Command clearCompleteAfterNow() {
            this.completeAfterNow = 0;
            this.hasCompleteAfterNow = false;
            return this;
        }

        public Command setTimeout(long time) {
            this.completeAfterNow = Minecraft.getSystemTime() + time;
            this.hasCompleteAfterNow = true;
            return this;
        }

        public Command clearTimeout() {
            this.completeAfterNow = 0;
            this.hasCompleteAfterNow = false;
            return this;
        }

        //

        public boolean isSendable() {
            if (this.isDone()) {
                return false;
            }
            if (this.hasSendAfter && Minecraft.getSystemTime() < this.sendAfter) {
                return false;
            }
            return this.getConditions().areMet(PlexCommandQueue.this);
        }

        public boolean isSent() {
            return this.sendStatus == CommandStatus.SENT;
        }

        public boolean hasSent() {
            return this.sendTimes.size() > 0;
        }

        public boolean isAwaitingResend() {
            return this.sendStatus == CommandStatus.AWAITING_RESEND;
        }

        public boolean isDone() {
            if (this.completionStatus == CommandCompletion.COMPLETE || this.completionStatus == CommandCompletion.CANCELED) {
                return true;
            }
            if (this.hasCompleteAfterSent && this.isSent() && this.completeAfterSent == 0L) {
                return true;
            }
            if (this.hasCompleteAfterSent && this.isSent() && this.hasSent() && Minecraft.getSystemTime() > this.getSendTime() + this.completeAfterSent) {
                return true;
            }
            if (this.hasCompleteAfterNow && Minecraft.getSystemTime() > this.completeAfterNow) {
                return true;
            }
            if (this.hasTimeout && this.hasSent() && Minecraft.getSystemTime() > this.sendTimes.get(0) + this.timeout) {
                return true;
            }
            return false;
        }

        public long getSendTime() {
            return this.sendTimes.size() > 0 ? this.sendTimes.get(this.sendTimes.size() - 1) : 0;
        }

        public long getSentElapsed() {
            if (!this.isSent()) {
                return 0;
            }
            return Minecraft.getSystemTime() - this.getSendTime();
        }

        public boolean hasBeenSentFor(long time) {
            return this.isSent() && this.getSendTime() > Minecraft.getSystemTime() + time;
        }

        //

        protected boolean sendCommand() {
            boolean sent = PlexUtilChat.chatSendMessageToServer(this.command);
            if (!sent) {
                return false;
            }
            this.sendTimes.add(Minecraft.getSystemTime());
            this.sendStatus = CommandStatus.SENT;
            return true;
        }

        public void send() {
            this.completionStatus = CommandCompletion.WAITING;
            this.sendStatus = this.hasSent() ? CommandStatus.AWAITING_RESEND : CommandStatus.UNSENT;
            this.sendAfter = 0;
            this.hasSendAfter = false;
        }

        public void sendIn(long time) {
            this.completionStatus = CommandCompletion.WAITING;
            this.sendStatus = this.hasSent() ? CommandStatus.AWAITING_RESEND : CommandStatus.UNSENT;
            this.sendAfter = Minecraft.getSystemTime() + time;
            this.hasSendAfter = true;
        }

        public void markComplete() {
            this.completionStatus = CommandCompletion.COMPLETE;
        }

        public void cancel() {
            this.completionStatus = CommandCompletion.CANCELED;
        }

        public String getDebug() {
            return PlexUtilChat.chatStyleText("DARK_BLUE", "BOLD", this.getGroup()) + " " +
                    PlexUtilChat.chatStyleText("GRAY", this.command) + " "  +
                    PlexUtilChat.chatStyleText(this.hasSent() ? "GREEN" : "RED", "sent");
        }

    }

    public static class CommandConditions implements Cloneable {
        private boolean hasCommandDelay = true;
        private boolean hasLobbySwitchDelay = true;
        private boolean hasJoinServerDelay = true;
        private boolean hasChatOpenDelay = true;

        private long commandDelay = 1600L;
        private long lobbySwitchDelay = 4000L;
        private long joinServerDelay = 4000L;
        private long chatOpenDelay = 1000L;

        private boolean sendableOffMineplex = false;

        public CommandConditions clone() {
            try {
                return (CommandConditions) super.clone();
            }
            catch (CloneNotSupportedException e) {
                return new CommandConditions();
            }
        }

        public CommandConditions afterCommand(long commandDelay) {
            this.commandDelay = commandDelay;
            this.hasCommandDelay = commandDelay > 0;
            return this;
        }

        public CommandConditions afterLobbyChange(long lobbySwitchDelay) {
            this.lobbySwitchDelay = lobbySwitchDelay;
            this.hasLobbySwitchDelay = lobbySwitchDelay > 0;
            return this;
        }

        public CommandConditions afterLogon(long joinServerDelay) {
            this.joinServerDelay = joinServerDelay;
            this.hasJoinServerDelay = joinServerDelay > 0;
            return this;
        }

        public CommandConditions afterChatOpen(long chatOpenDelay) {
            this.chatOpenDelay = chatOpenDelay;
            this.hasChatOpenDelay = chatOpenDelay > 0;
            return this;
        }

        public CommandConditions canSendOffMineplex(boolean sendableOffMineplex) {
            this.sendableOffMineplex = sendableOffMineplex;
            return this;
        }

        public boolean areMet(PlexCommandQueue queue) {
            long time = Minecraft.getSystemTime();
            if (!PlexMP.session.isMineplex && !this.sendableOffMineplex) {
                return false;
            }
            if (this.hasCommandDelay && time < queue.manager.lastCommandSent + this.commandDelay) {
                return false;
            }
            if (this.hasLobbySwitchDelay && time < PlexMP.lobby.joinTime.milliseconds + this.lobbySwitchDelay) {
                return false;
            }
            if (this.hasJoinServerDelay && time < PlexMP.session.joinTime.milliseconds + this.joinServerDelay) {
                return false;
            }
            if (this.hasChatOpenDelay && time < Plex.listeners.getChatOpenTime() + this.chatOpenDelay) {
                return false;
            }
            return true;
        }
    }

    public enum CommandCompletion {
        WAITING,
        CANCELED,
        COMPLETE
    }

    public enum CommandStatus {
        UNSENT,
        AWAITING_RESEND,
        SENT,
    }
}
