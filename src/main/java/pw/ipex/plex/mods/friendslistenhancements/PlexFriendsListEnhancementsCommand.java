package pw.ipex.plex.mods.friendslistenhancements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ci.PlexCommandHandler;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexFriendsListEnhancementsCommand extends PlexCommandHandler {
	
	public ArrayList<String> displayValues = new ArrayList<String>(Arrays.asList("outgoing", "incoming", "offline", "online"));

	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		if (namespace.equals("plex.friends")) {
			PlexCore.displayUIScreen(new PlexFriendsListEnhancementsUI());
		}
		else if (namespace.equals("ff")) {
			if (args.length == 0) {
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() +  
						PlexCoreUtils.chatStyleText("DARK_RED", "Usage: ") + PlexCoreUtils.chatStyleText("GRAY", "/" + namespace + " ") + 
						PlexCoreUtils.chatStyleText("GOLD", "incoming") + PlexCoreUtils.chatStyleText("GRAY", ", ") + 
						PlexCoreUtils.chatStyleText("DARK_GRAY", "outgoing") + PlexCoreUtils.chatStyleText("GRAY", ", ") +
						PlexCoreUtils.chatStyleText("GOLD", "offline") + PlexCoreUtils.chatStyleText("GRAY", ", ") +
						PlexCoreUtils.chatStyleText("DARK_GRAY", "search"));
				return;
			}
			if (displayValues.contains(args[0])) {
				PlexCore.getSharedValue("friendsListEnhancements_itemTarget").set(args[0]);
				Plex.minecraft.thePlayer.sendChatMessage("/f");
			}
			if (args[0].equals("search")) {
				if (args.length == 1) {
					PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + 
							PlexCoreUtils.chatStyleText("DARK_RED", "Usage: ") + PlexCoreUtils.chatStyleText("GRAY", "/" + namespace + " ") + 
							PlexCoreUtils.chatStyleText("GRAY", "search <query>"));
					return;
				}
				PlexCore.getSharedValue("friendsListEnhancements_searchTerm").set(args[1]);
				Plex.minecraft.thePlayer.sendChatMessage("/f");
			}
		}
		else if (namespace.equals("fs")) {
			if (args.length == 0) {
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + 
						PlexCoreUtils.chatStyleText("DARK_RED", "Usage: ") + PlexCoreUtils.chatStyleText("GRAY", "/" + namespace + " ") + 
						PlexCoreUtils.chatStyleText("GRAY", "<query>"));
				return;
			}
			PlexCore.getSharedValue("friendsListEnhancements_searchTerm").set(args[0]);
			Plex.minecraft.thePlayer.sendChatMessage("/f");			
		}
	}
}
