package pw.ipex.plex.commandqueue;

import net.minecraft.client.Minecraft;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexCommandQueueCommand {
	public PlexCommandQueue parentQueue;

	public String command;
	public String group;
	public Integer priority = 1;
	public Boolean waitForPrevious = true;

	public PlexCommandQueueDelaySet delaySet;
	
	public Boolean markedComplete = false;
	public Boolean commandSent = false;
	public Boolean commandCanceled = false;
	public Long latestCommandSentTimestamp = null;
	
	public Boolean awaitingResend = false;
	public Boolean commandResent = false;
	public Integer sendCount = 0;
	public Long firstSendTime = null;
	public Long timeout = 15000L;
	
	public Long sendCommandAt = null;
	
	public PlexCommandQueueCommand(String group, String command) {
		this.group = group;
		this.command = command;
	}
	
	public PlexCommandQueueCommand(String group, String command, Long delay) {
		this.group = group;
		this.command = command;
		this.sendCommandAt = delay;
	}
	
	public Boolean sendCommand() {
		try {
			Plex.minecraft.thePlayer.sendChatMessage(this.command);
		}
		catch (Exception e) {
			return false;
		}
		this.sendCount += 1;
		this.commandSent = true;
		this.latestCommandSentTimestamp = Minecraft.getSystemTime();
		if (this.awaitingResend) {
			this.commandResent = true;
		}
		if (this.firstSendTime == null) {
			this.firstSendTime = Minecraft.getSystemTime();
		}
		return true;
	}
	
	public Boolean isCommandSent() {
		return this.commandSent;
	}
	
	public Boolean isMarkedComplete() {
		return this.markedComplete;
	}
	
	public Boolean isResent() {
		return this.commandResent;
	}
	
	public Boolean isCanceled() {
		return this.commandCanceled;
	}
	
	public Boolean isSent() {
		return this.commandSent;
	}

	public Boolean isAwaitingResend() {
		return this.awaitingResend;
	}

	public Boolean hasPassedTimeout() {
		return this.firstSendTime == null ? false : (Minecraft.getSystemTime() > this.firstSendTime + this.timeout);
	}

	public Boolean hasBeenSent() {
		return this.firstSendTime != null;
	}
	
	public void markComplete() {
		this.markedComplete = true;
	}
	
	public Long getSendTime() {
		return this.latestCommandSentTimestamp;
	}
	
	public void resend() {
		this.commandSent = false;
		this.awaitingResend = true;
		this.commandResent = false;
		this.sendCommandAt = null;
	}

	public Boolean isSendable() {
		if (this.sendCommandAt == null) {
			return true;
		}
		return Minecraft.getSystemTime() > this.sendCommandAt;
	}

	public void cancel() {
		this.commandCanceled = true;
	}
	
	public void resendCommandIn(Long time) {
		this.resend();
		this.sendCommandAt = Minecraft.getSystemTime() + time;
	}

	public String getDebug() {
		return PlexCoreUtils.chatStyleText("DARK_BLUE", "BOLD", this.group) + " " +
				PlexCoreUtils.chatStyleText("GRAY", this.command) + " "  +
				PlexCoreUtils.chatStyleText(this.hasBeenSent() ? "GREEN" : "RED", "sent");
	}
}
