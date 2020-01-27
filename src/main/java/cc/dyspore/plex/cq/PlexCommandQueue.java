package cc.dyspore.plex.cq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlexCommandQueue {
    public PlexCommandQueueManager manager;

    public String group;
    public int priority = 50;
    public boolean respectQueueOrder = true;

    public PlexCommandQueueConditions conditions = new PlexCommandQueueConditions();

    private final List<PlexCommandQueueCommand> queueItems = Collections.synchronizedList(new ArrayList<>());

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

    public PlexCommandQueueCommand addCommand(String textCommand) {
        PlexCommandQueueCommand command = new PlexCommandQueueCommand(this.group, textCommand);
        this.addCommand(command);
        return command;
    }

    public PlexCommandQueueCommand addCommand(String textCommand, long delay) {
        PlexCommandQueueCommand command = new PlexCommandQueueCommand(this.group, textCommand, delay);
        this.addCommand(command);
        return command;
    }

    public PlexCommandQueueCommand addCommand(PlexCommandQueueCommand command) {
        command.parent = this;
        this.queueItems.add(command);
        this.manager.addCommandToQueue(command);
        return command;
    }

    public PlexCommandQueueCommand newCommand(String textCommand) {
        return new PlexCommandQueueCommand(this.group, textCommand);
    }

    public void removeCompleted() {
        List<PlexCommandQueueCommand> completed = new ArrayList<PlexCommandQueueCommand>();
        synchronized (queueItems) {
            for (PlexCommandQueueCommand command : queueItems) {
                if (command.isTerminate()) {
                    completed.add(command);
                }
            }
        }
        queueItems.removeAll(completed);
    }

    public List<PlexCommandQueueCommand> getItems() {
        this.removeCompleted();
        return this.queueItems;
    }

    public PlexCommandQueueCommand getItem(int item) {
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
            for (PlexCommandQueueCommand command : this.queueItems) {
                command.cancel();
            }
            this.removeCompleted();
        }
    }

    public void cancelAllUnsent() {
        synchronized (this.queueItems) {
            for (PlexCommandQueueCommand command : this.queueItems) {
                if (!command.isSent()) {
                    command.cancel();
                }
            }
        }
        this.removeCompleted();
    }
}
