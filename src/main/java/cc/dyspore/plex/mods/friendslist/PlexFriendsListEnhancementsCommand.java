package cc.dyspore.plex.mods.friendslist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.commands.client.PlexCommandHandler;
import cc.dyspore.plex.core.PlexCore;

public class PlexFriendsListEnhancementsCommand extends PlexCommandHandler {
	
	public ArrayList<String> displayValues = new ArrayList<String>(Arrays.asList("outgoing", "incoming", "offline", "online"));

	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		PlexFriendsListEnhancementsMod instance = PlexCore.modInstance(PlexFriendsListEnhancementsMod.class);
		if (namespace.equals("plex.friends")) {
			PlexCore.displayUIScreen(new PlexFriendsListEnhancementsUI());
		}
		else if (namespace.equals("ff")) {
			if (args.length == 0) {
				PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX +
						PlexUtilChat.chatStyleText("DARK_RED", "Usage: ") + PlexUtilChat.chatStyleText("GRAY", "/" + namespace + " ") +
						PlexUtilChat.chatStyleText("GOLD", "incoming") + PlexUtilChat.chatStyleText("GRAY", ", ") +
						PlexUtilChat.chatStyleText("DARK_GRAY", "outgoing") + PlexUtilChat.chatStyleText("GRAY", ", ") +
						PlexUtilChat.chatStyleText("GOLD", "offline") + PlexUtilChat.chatStyleText("GRAY", ", ") +
						PlexUtilChat.chatStyleText("DARK_GRAY", "search"));
				return;
			}
			if (displayValues.contains(args[0])) {
				instance.itemTarget = args[0];
				Plex.minecraft.thePlayer.sendChatMessage("/f");
			}
			if (args[0].equals("search")) {
				if (args.length == 1) {
					PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX +
							PlexUtilChat.chatStyleText("DARK_RED", "Usage: ") + PlexUtilChat.chatStyleText("GRAY", "/" + namespace + " ") +
							PlexUtilChat.chatStyleText("GRAY", "search <query>"));
					return;
				}
				instance.searchTerm = args[1];
				Plex.minecraft.thePlayer.sendChatMessage("/f");
			}
		}
		else if (namespace.equals("fs")) {
			if (args.length == 0) {
				PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX +
						PlexUtilChat.chatStyleText("DARK_RED", "Usage: ") + PlexUtilChat.chatStyleText("GRAY", "/" + namespace + " ") +
						PlexUtilChat.chatStyleText("GRAY", "<query>"));
				return;
			}
			instance.searchTerm = args[0];
			Plex.minecraft.thePlayer.sendChatMessage("/f");			
		}
	}
}
