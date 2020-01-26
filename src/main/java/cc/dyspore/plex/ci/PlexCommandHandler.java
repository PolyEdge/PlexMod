package cc.dyspore.plex.ci;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

public abstract class PlexCommandHandler {
	public abstract List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos);
	
	public abstract void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException;
}
