package pw.ipex.plex.mods.richpresence;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import pw.ipex.plex.ci.PlexCommandHandler;
import pw.ipex.plex.core.PlexCore;

public class PlexRichPresenceCommand extends PlexCommandHandler {

	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return new ArrayList<String>();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		if (args.length == 0) {
			PlexCore.displayUIScreen(new PlexRichPresenceUI());
		}
	}

}
