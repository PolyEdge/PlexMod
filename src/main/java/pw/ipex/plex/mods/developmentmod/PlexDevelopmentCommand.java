package pw.ipex.plex.mods.developmentmod;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import pw.ipex.plex.ci.PlexCommandHandler;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.regex.PlexCoreRegex;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexDevelopmentCommand extends PlexCommandHandler {

	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		if (args.length == 0) {
			PlexCoreUtils.chatAddMessage("plexdev -> ?");
			return;
		}
		PlexDevelopmentMod devInstance = PlexCore.modInstance(PlexDevelopmentMod.class);
		if (args[0].equalsIgnoreCase("sounds")) {
			devInstance.soundStream = (!devInstance.soundStream);
			PlexCoreUtils.chatAddMessage("plexdev -> " + (devInstance.soundStream ? "now showing" : "no longer showing") + " sound information.");
		}
		else if (args[0].equalsIgnoreCase("chat")) {
			devInstance.chatStream = !devInstance.chatStream;
			PlexCoreUtils.chatAddMessage("plexdev -> " + (devInstance.chatStream ? "now showing" : "no longer showing") + " chat formatting.");
			PlexCoreUtils.chatAddMessage("plexdev -> currently displaying " + (PlexCore.getSharedValue("_plexDev_minifyMessages").booleanValue ? "minified" : "non minified") + " messages. use the \"chatmode\" subcommand to toggle display behaviour.");

		}
		else if (args[0].equalsIgnoreCase("chatmode")) {
			PlexCore.getSharedValue("_plexDev_chatStreamMinify").set(!PlexCore.getSharedValue("_plexDev_chatStream").booleanValue);
			PlexCoreUtils.chatAddMessage("plexdev -> " + (PlexCore.getSharedValue("_plexDev_soundStream").booleanValue ? "now showing" : "no longer showing") + " chat formatting");
		}
		else if (args[0].equalsIgnoreCase("server") || args[0].equalsIgnoreCase("lobby")) {
			PlexCore.getSharedValue("_plexDev_lobbyStream").set(!PlexCore.getSharedValue("_plexDev_lobbyStream").booleanValue);
		}
		else if (args[0].equalsIgnoreCase("regex") && args.length > 1) {
			String input = "";
			for (int x = 1; x < args.length; x++) {
				input = input + args[x] + " ";
			}
			input = input.trim();
			PlexCoreUtils.chatAddMessage("split of \"" + input + "\"");
			for (String x : PlexCoreRegex.splitFormatRegionString(input)) {
				PlexCoreUtils.chatAddMessage(x);
			}
		}
		else {
			PlexCoreUtils.chatAddMessage("plexdev -> ?");
		}
	}

}
