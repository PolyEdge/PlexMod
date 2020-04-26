package cc.dyspore.plex.commands.client;

import java.util.*;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import cc.dyspore.plex.Plex;

public class PlexClientCommandListener implements ICommand {
	public String commandName;
	public PlexCommandListener parent;
	
	public PlexClientCommandListener(String commandName, PlexCommandListener parent) {
		this.commandName = commandName;
		this.parent = parent;
	}

	public String[] getArgs(String[] args) {
		List<String> allArgs = new ArrayList<String>(Arrays.asList(args));
		allArgs.add(0, this.commandName);
		return allArgs.toArray(new String[0]);
	}

	public void sendToServer(String[] args) {
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

	@Override
	public List<String> getCommandAliases() {
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		this.parent.processCommand(this, sender, this.getArgs(args));
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return this.parent.processTabCompletion(this, sender, this.getArgs(args), pos);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
	
	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public int compareTo(ICommand command) {
		return this.getCommandName().compareTo(command.getCommandName());
	}
}
