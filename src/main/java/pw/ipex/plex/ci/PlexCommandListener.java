package pw.ipex.plex.ci;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;

public class PlexCommandListener extends CommandBase {
	public String commandName;
	public Boolean listenerEnabled = true;
	public Boolean enableOnUnsupportedServers = false;
	
	public PlexCommandListener(String commandName) {
		this.commandName = commandName;
	}
	
	public PlexCommandListener setDisabled(Boolean disabled) {
		this.listenerEnabled = !disabled;
		return this;
	}
	
	public PlexCommandListener setGlobal(Boolean allowAllServers) {
		this.enableOnUnsupportedServers = allowAllServers;
		return this;
	}
	
	public Boolean isListenerActive() {
		return (this.listenerEnabled && (this.enableOnUnsupportedServers || Plex.serverState.onMineplex));
	}
	
	@Override
	public String getCommandName() {
		return this.commandName;
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return null;
	}
	
	public boolean canSenderUseCommand(ICommandSender sender) {
		return true;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		List<String> allArgs = new ArrayList<String>(Arrays.asList(args));
		allArgs.add(0, this.commandName);
		String[] finalArgs = allArgs.toArray(new String[0]);
		if (!this.isListenerActive()) {
		    StringJoiner joiner = new StringJoiner(" ", "/", "");
		    for (String arg : finalArgs) {
		    	joiner.add(arg);
		    }
		    Plex.minecraft.thePlayer.sendChatMessage(joiner.toString());
		    return;
		}
		PlexCore.processModCommand(sender, finalArgs);
	}	
	
	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		List<String> allArgs = new ArrayList<String>(Arrays.asList(args));
		allArgs.add(0, this.commandName);
		String[] finalArgs = allArgs.toArray(new String[0]);
		if (!this.isListenerActive()) {
			return Collections.emptyList();
		}
		return PlexCore.commandTabCompletion(sender, finalArgs, pos);
	}
}
