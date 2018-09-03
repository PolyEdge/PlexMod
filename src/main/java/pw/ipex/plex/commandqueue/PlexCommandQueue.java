package pw.ipex.plex.commandqueue;

import java.util.ArrayList;
import java.util.List;

public class PlexCommandQueue {
    public String group;
    public PlexCommandQueueManager manager;

    public int priority = 1;
    public boolean requiresPreviousCompletion = true;

    public PlexCommandQueueDelaySet delaySet = new PlexCommandQueueDelaySet();

    public List<PlexCommandQueueCommand> queueItems = new ArrayList<>();

    public PlexCommandQueue(String group, PlexCommandQueueManager manager) {
        this.group = group;
        this.manager = manager;
        this.manager.registerQueue(this);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public PlexCommandQueueCommand addCommand(String textCommand) {
        PlexCommandQueueCommand command = new PlexCommandQueueCommand(this.group, textCommand);
        this.addCommand(command);
        return command;
    }

    public PlexCommandQueueCommand addCommand(String textCommand, Long delay) {
        PlexCommandQueueCommand command = new PlexCommandQueueCommand(this.group, textCommand, delay);
        this.addCommand(command);
        return command;
    }

    public PlexCommandQueueCommand addCommand(PlexCommandQueueCommand command) {
        command.parentQueue = this;
        command.priority = this.priority;
        command.waitForPrevious = this.requiresPreviousCompletion;
        command.delaySet = this.delaySet;
        command.group = this.group;
        this.queueItems.add(command);
        this.manager.addCommandToQueue(command);
        return command;
    }

    public void removeCompleted() {
        this.manager.removeCompleted(this.queueItems);
    }

    public List<PlexCommandQueueCommand> getItems() {
        this.removeCompleted();
        return this.queueItems;
    }

    public PlexCommandQueueCommand getItem(int item) {
        if (item >= this.queueItems.size()) {
            return null;
        }
        return this.queueItems.get(item);
    }

    public boolean hasItems() {
        this.removeCompleted();
        return this.queueItems.size() > 0;
    }

    public void cancelAll() {
        for (PlexCommandQueueCommand command : this.queueItems) {
            command.cancel();
        }
        this.removeCompleted();
    }

    public void cancelAllUnsent() {
        for (PlexCommandQueueCommand command : this.queueItems) {
            if (!command.isSent()) {
                command.cancel();
            }
        }
        this.removeCompleted();
    }
}
