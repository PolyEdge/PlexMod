package pw.ipex.plex.commandqueue;

import net.minecraft.client.Minecraft;
import pw.ipex.plex.Plex;

public class PlexQueueCommand {
	public String command;
	public String group;
	
	public Boolean markedComplete = false;
	public Boolean commandSent = false;
	public Boolean commandCanceled = false;
	public Long latestCommandSentTimestamp = null;
	
	public Boolean awaitingResend = false;
	public Boolean commandResent = false;
	public Integer resendCount = 0;
	
	public Long sendCommandAt = null;
	
	public PlexQueueCommand(String group, String command) {
		this.group = group;
		this.command = command;
	}
	
	public PlexQueueCommand(String group, String command, Long delay) {
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
		this.commandSent = true;
		this.latestCommandSentTimestamp = Minecraft.getSystemTime();
		if (this.awaitingResend) {
			this.commandResent = true;
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
	
	public void cancel() {
		this.commandCanceled = true;
	}
	
	public void resendCommandIn(Long time) {
		this.resend();
		this.sendCommandAt = Minecraft.getSystemTime() + time;
	}
}
