package pw.ipex.plex.mods.developmentmod;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import pw.ipex.plex.ci.PlexCommandHandler;
import pw.ipex.plex.core.PlexCore;

public class PlexDevelopmentCommand extends PlexCommandHandler {

	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		if (args.length == 0) {
			return;
		}
		if (args[0].equalsIgnoreCase("sounds")) {
			PlexCore.getSharedValue("_plexDev_soundStream").set(!PlexCore.getSharedValue("_plexDev_soundStream").booleanValue);
		}
		if (args[0].equalsIgnoreCase("chat")) {
			PlexCore.getSharedValue("_plexDev_chatStream").set(!PlexCore.getSharedValue("_plexDev_chatStream").booleanValue);
		}
		if (args[0].equalsIgnoreCase("server") || args[0].equalsIgnoreCase("lobby")) {
			PlexCore.getSharedValue("_plexDev_lobbyStream").set(!PlexCore.getSharedValue("_plexDev_lobbyStream").booleanValue);
		}
	}

}
