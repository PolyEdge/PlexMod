package cc.dyspore.plex.mods.discordrichstatus;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import cc.dyspore.plex.commands.client.PlexCommandHandler;
import cc.dyspore.plex.core.PlexCore;

public class PlexRichPresenceCommand extends PlexCommandHandler {

	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return new ArrayList<String>();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		if (args.length == 0) {
			PlexCore.displayMenu(new PlexRichPresenceUI());
		}
	}

}
