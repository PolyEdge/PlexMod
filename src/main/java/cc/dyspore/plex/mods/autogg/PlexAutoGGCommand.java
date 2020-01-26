package cc.dyspore.plex.mods.autogg;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import cc.dyspore.plex.ci.PlexCommandHandler;
import cc.dyspore.plex.core.PlexCore;

public class PlexAutoGGCommand extends PlexCommandHandler {

	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		if (args.length == 0) {
			PlexCore.displayUIScreen(new PlexAutoGGUI());
		}
	}

}
