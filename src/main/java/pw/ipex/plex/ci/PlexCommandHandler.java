package pw.ipex.plex.ci;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

public abstract class PlexCommandHandler {
	public abstract List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos);
	
	public abstract void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException;
	
	public Boolean allowUnsupportedServers() {
		return false;
	}
	
	public static String getCommandNamespace(String[] args) {
		if (args.length == 0) {
			return "invalid";
		}
		return args[0];
	}
	
	public static String[] getCommandArgs(String[] args) {
		if (args.length < 2) {
			return new String[] {};
		}
		List<String> finalArgs = Arrays.asList(args).subList(1, (args.length));
		return finalArgs.toArray(new String[0]);
	}
}
