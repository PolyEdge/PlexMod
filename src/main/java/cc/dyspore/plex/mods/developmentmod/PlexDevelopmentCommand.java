package cc.dyspore.plex.mods.developmentmod;

import java.util.Collections;
import java.util.List;

import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.ci.PlexCommandHandler;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.regex.PlexCoreRegex;

public class PlexDevelopmentCommand extends PlexCommandHandler {

	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		if (args.length == 0) {
			PlexUtilChat.chatAddMessage("plexdev -> ?");
			return;
		}
		PlexDevelopmentMod devInstance = PlexCore.modInstance(PlexDevelopmentMod.class);
		if (args[0].equalsIgnoreCase("sounds")) {
			devInstance.soundStream = (!devInstance.soundStream);
			PlexUtilChat.chatAddMessage("plexdev -> " + (devInstance.soundStream ? "now showing" : "no longer showing") + " sound information.");
		}
		else if (args[0].equalsIgnoreCase("chat")) {
			devInstance.chatStream = !devInstance.chatStream;
			PlexUtilChat.chatAddMessage("plexdev -> " + (devInstance.chatStream ? "now showing" : "no longer showing") + " chat formatting.");
			if (devInstance.chatStream) {
				PlexUtilChat.chatAddMessage("plexdev -> currently displaying " + (devInstance.chatMinify ? "condensed" : "non condensed") + " messages. use the \"chatmode\" subcommand to toggle display behaviour.");
				PlexUtilChat.chatAddMessage("plexdev -> currently displaying " + (devInstance.chatCharacterCodes ? "no " : "") + "character codes. use the \"chatcharacters\" subcommand to toggle character codes.");
			}
		}
		else if (args[0].equalsIgnoreCase("chatregex")) {
			devInstance.regexEntries = !devInstance.regexEntries;
			PlexUtilChat.chatAddMessage("plexdev -> " + (devInstance.regexEntries ? "now showing" : "no longer showing") + " chat regex matches.");
		}
		else if (args[0].equalsIgnoreCase("chatmode")) {
			devInstance.chatMinify = !devInstance.chatMinify;
			PlexUtilChat.chatAddMessage("plexdev -> " + (devInstance.chatMinify ? "now showing" : "no longer showing") + " messages in condensed format");
		}
		else if (args[0].equalsIgnoreCase("chatcharacters")) {
			devInstance.chatCharacterCodes = !devInstance.chatCharacterCodes;
			PlexUtilChat.chatAddMessage("plexdev -> " + (devInstance.chatCharacterCodes ? "now showing" : "no longer showing") + " chat character codes");
		}
		else if (args[0].equalsIgnoreCase("server") || args[0].equalsIgnoreCase("lobby")) {
			devInstance.lobbySwitchStream = !devInstance.lobbySwitchStream;
			PlexUtilChat.chatAddMessage("plexdev -> " + (devInstance.lobbySwitchStream ? "now showing" : "no longer showing") + " lobby switch events.");
		}
		else if (args[0].equalsIgnoreCase("cq") || args[0].equalsIgnoreCase("queue")) {
			Plex.queue.debug = !Plex.queue.debug;
			PlexUtilChat.chatAddMessage("plexdev -> " + (Plex.queue.debug ? "now showing" : "no longer showing") + " command queue debug.");
		}
		else if (args[0].equalsIgnoreCase("fmtregion") && args.length > 1) {
			String input = "";
			for (int x = 1; x < args.length; x++) {
				input = input + args[x] + " ";
			}
			input = input.trim();
			PlexUtilChat.chatAddMessage("split of \"" + input + "\"");
			for (String x : PlexCoreRegex.splitFormatRegionString(input)) {
				PlexUtilChat.chatAddMessage(x);
			}
		}
		else {
			PlexUtilChat.chatAddMessage("plexdev -> ?");
		}
	}

}
