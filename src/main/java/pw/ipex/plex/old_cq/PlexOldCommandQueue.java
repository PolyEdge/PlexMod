package pw.ipex.plex.old_cq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlexOldCommandQueue {
    public String group;
    public PlexOldCommandQueueManager manager;

    public int priority = 1;
    public boolean requiresPreviousCompletion = true;

    public PlexOldCommandQueueDelaySet delaySet = new PlexOldCommandQueueDelaySet();

    public final List<PlexOldCommandQueueCommand> queueItems = Collections.synchronizedList(new CopyOnWriteArrayList<>());

    public PlexOldCommandQueue(String group, PlexOldCommandQueueManager manager) {
        this.group = group;
        this.manager = manager;
        this.manager.registerQueue(this);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public PlexOldCommandQueueCommand addCommand(String textCommand) {
        PlexOldCommandQueueCommand command = new PlexOldCommandQueueCommand(this.group, textCommand);
        this.addCommand(command);
        return command;
    }

    public PlexOldCommandQueueCommand addCommand(String textCommand, Long delay) {
        PlexOldCommandQueueCommand command = new PlexOldCommandQueueCommand(this.group, textCommand, delay);
        this.addCommand(command);
        return command;
    }

    public PlexOldCommandQueueCommand addCommand(PlexOldCommandQueueCommand command) {
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

    public List<PlexOldCommandQueueCommand> getItems() {
        this.removeCompleted();
        return this.queueItems;
    }

    public PlexOldCommandQueueCommand getItem(int item) {
        this.removeCompleted();
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
        synchronized (this.queueItems) {
            for (PlexOldCommandQueueCommand command : this.queueItems) {
                command.cancel();
            }
            this.removeCompleted();
        }
    }

    public void cancelAllUnsent() {
        synchronized (this.queueItems) {
            for (PlexOldCommandQueueCommand command : this.queueItems) {
                if (!command.isSent()) {
                    command.cancel();
                }
            }
        }
        this.removeCompleted();
    }
}
