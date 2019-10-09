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

public class PlexCommandListenerClientCommandListener extends CommandBase {
	public String commandName;
	public PlexCommandListener parent;
	
	public PlexCommandListenerClientCommandListener(String commandName, PlexCommandListener parent) {
		this.commandName = commandName;
		this.parent = parent;
	}

	public String[] getArgs(String[] args) {
		List<String> allArgs = new ArrayList<String>(Arrays.asList(args));
		allArgs.add(0, this.commandName);
		return allArgs.toArray(new String[0]);
	}

	public void sendAsPlayerCommand(String[] args) {
		if (args.length == 0 || !args[0].toLowerCase().equals(this.commandName.toLowerCase())) {
			args = this.getArgs(args);
		}
		StringJoiner joiner = new StringJoiner(" ", "/", "");
		for (String arg : args) {
			joiner.add(arg);
		}
		Plex.minecraft.thePlayer.sendChatMessage(joiner.toString());
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
		this.parent.processCommand(this, sender, this.getArgs(args));
	}	
	
	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return this.parent.processTabCompletion(this, sender, this.getArgs(args), pos);
	}
}
