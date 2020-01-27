package cc.dyspore.plex.cq;

import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.client.Minecraft;
import cc.dyspore.plex.Plex;

import java.util.ArrayList;
import java.util.List;

public class PlexCommandQueueCommand {
    public PlexCommandQueue parent;

    public String command;
    public int completionStatus = -1; // -1=none 0=complete 1=canceled
    public int sendStatus = -1; // -1=unsent 0=sent 1=awaitingResend 2=resent

    public boolean overridesParent = false;
    public String group;
    public int priority;
    public boolean respectQueueOrder;
    public boolean blocksQueue;
    public PlexCommandQueueConditions conditions;

    private Long sendAfter = null; // when set, flags the manager to wait until after the set time in milliseconds before sending the command
    private Long completeAfter = null; // when set, flags the manager to mark the command as complete after this number of milliseconds when sent
    private Long completeAfterTime = null;

    public List<Long> sendTimes = new ArrayList<>();

    public Long timeout = null;

    public PlexCommandQueueCommand(PlexCommandQueue parent, String command) {
        this.parent = parent;
        this.command = command;
    }

    public PlexCommandQueueCommand(PlexCommandQueue parent, String command, long delay) {
        this.parent = parent;
        this.command = command;
        this.sendAfter = Minecraft.getSystemTime() + delay;
    }

    public PlexCommandQueueCommand(String group, String command) {
        this.group = group;
        this.command = command;
        this.priority = 100;
        this.conditions = new PlexCommandQueueConditions();
    }

    public PlexCommandQueueCommand(String group, String command, long delay) {
        this.group = group;
        this.command = command;
        this.priority = 100;
        this.sendAfter = Minecraft.getSystemTime() + delay;
        this.conditions = new PlexCommandQueueConditions();
    }

    public PlexCommandQueueCommand sendAfter(long time) {
        this.sendAfter = Minecraft.getSystemTime() + time;
        return this;
    }

    public PlexCommandQueueCommand setCompleteAfterSentFor(Long time) {
        this.completeAfter = time;
        return this;
    }

    public PlexCommandQueueCommand setCompleteAfterNow(long time) {
        this.completeAfterTime = Minecraft.getSystemTime() + time;
        return this;
    }


    public String getGroup() {
        return this.parent == null ? this.group : (this.overridesParent ? this.group : parent.group);
    }

    public int getPriority() {
        return this.parent == null ? this.priority : (this.overridesParent ? this.priority : parent.priority);
    }

    public PlexCommandQueueConditions getConditions() {
        return this.parent == null ? this.conditions : (this.overridesParent ? this.conditions : parent.conditions);
    }

    public boolean getRespectQueueOrder() {
        return this.parent == null ? this.respectQueueOrder : (this.overridesParent ? this.respectQueueOrder : parent.respectQueueOrder);
    }

    public boolean getBlocksQueue() {
        return this.parent == null ? this.blocksQueue : (this.overridesParent ? this.blocksQueue : parent.respectQueueOrder);
    }

    public Boolean sendCommand() {
        try {
            Plex.minecraft.thePlayer.sendChatMessage(this.command);
        }
        catch (Exception e) {
            return false;
        }
        this.sendTimes.add(Minecraft.getSystemTime());
        this.sendStatus = this.sendStatus == 1 ? 2 : 0;
        return true;
    }

    public boolean isSendable() {
        if (this.completionStatus == 0 || this.completionStatus == 1) {
            return false;
        }
        if (this.sendAfter != null && Minecraft.getSystemTime() < this.sendAfter) {
            return false;
        }
        return this.getConditions().sendable();
    }

    public boolean isSent() {
        return this.sendStatus == 0 || this.sendStatus == 2;
    }

    public boolean hasSent() {
        return this.sendTimes.size() > 0;
    }

    public boolean isTerminate() {
        if (this.completionStatus == 0 || this.completionStatus == 1) {
            return true;
        }
        if (this.completeAfter != null && this.isSent() && this.completeAfter == 0L) {
            return true;
        }
        if (this.completeAfter != null && this.isSent() && this.hasSent() && Minecraft.getSystemTime() > this.getSendTime() + completeAfter) {
            return true;
        }
        if (this.completeAfterTime != null && Minecraft.getSystemTime() > this.completeAfterTime) {
            return true;
        }
        if (this.timeout != null && this.sendTimes.size() > 0 && Minecraft.getSystemTime() > this.sendTimes.get(0) + this.timeout) {
            return true;
        }
        return false;
    }

    public boolean isAwaitResend() {
        return this.sendStatus == 1;
    }

    public Long getSendTime() {
        return this.sendTimes.size() > 0 ? this.sendTimes.get(this.sendTimes.size() - 1) : null;
    }

    public Long getSentElapsed() {
        if (this.getSendTime() == null) {
            return null;
        }
        return Minecraft.getSystemTime() - this.getSendTime();
    }

    public void sendIn(long time) {
        this.completionStatus = -1;
        this.sendAfter = Minecraft.getSystemTime() + time;
    }

    public void resend() {
        this.sendStatus = 1;
        this.completionStatus = -1;
        this.sendAfter = null;
    }

    public void resendIn(long time) {
        this.sendStatus = 1;
        this.completionStatus = -1;
        this.sendAfter = Minecraft.getSystemTime() + time;
    }

    public void markComplete() {
        this.completionStatus = 0;
    }

    public void cancel() {
        this.completionStatus = 1;
    }

    public void setCompleteOnSend(boolean completeOnSend) {
        this.completeAfter = completeOnSend ? 0L : null;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDebug() {
        return PlexUtilChat.chatStyleText("DARK_BLUE", "BOLD", this.group) + " " +
                PlexUtilChat.chatStyleText("GRAY", this.command) + " "  +
                PlexUtilChat.chatStyleText(this.hasSent() ? "GREEN" : "RED", "sent");
    }
}
