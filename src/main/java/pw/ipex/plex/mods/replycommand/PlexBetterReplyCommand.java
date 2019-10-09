package pw.ipex.plex.mods.replycommand;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ci.PlexCommandHandler;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexBetterReplyCommand extends PlexCommandHandler {

	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		PlexBetterReplyMod instance = PlexCore.modInstance(PlexBetterReplyMod.class);
		if (namespace.equals("plex.messages")) {
			PlexCore.displayUIScreen(new PlexBetterReplyUI());
		}
		else if (namespace.equals("r")) {
			if (instance.currentConversation == null) {
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "You haven't messaged anybody yet!"));
				return;
			}
			Plex.minecraft.thePlayer.sendChatMessage("/msg " + instance.currentConversation + " " + PlexCoreUtils.buildCommand(args));
			instance.lastConversationTime = Minecraft.getSystemTime();
		}
		else if (namespace.equals("rr")) {
			if (args.length == 0) {
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "Please specify somebody to message."));
				return;
			}
			List<String> matchUsers = PlexCoreUtils.matchStringToList(args[0], new ArrayList<>(instance.contacts));
			if (matchUsers.size() == 0) {
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "You haven't messaged anybody matching the ign ") + PlexCoreUtils.chatStyleText("BLUE", args[0]) + PlexCoreUtils.chatStyleText("GRAY", "."));
				return;
			}
			if (matchUsers.size() > 1) {
				String outputString = PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "You have messaged with multiple people matching the ign ") + PlexCoreUtils.chatStyleText("BLUE", args[0]) + PlexCoreUtils.chatStyleText("GRAY", ": ");
				StringJoiner joiner = new StringJoiner(PlexCoreUtils.chatStyleText("GRAY", ", "), "", "");
				for (String ign : matchUsers) {
					joiner.add(PlexCoreUtils.chatStyleText("GOLD", ign));
				}
				PlexCoreUtils.chatAddMessage(outputString + joiner.toString() + PlexCoreUtils.chatStyleText("GRAY", "."));
				return;
			}
			Plex.minecraft.thePlayer.sendChatMessage("/msg " + matchUsers.get(0) + " " + PlexCoreUtils.buildCommand(Arrays.copyOfRange(args, 1, args.length)));
			instance.currentConversation = matchUsers.get(0);
			instance.lastConversationTime = Minecraft.getSystemTime();
		}
		else {
			if (args.length == 0) {
				Plex.minecraft.thePlayer.sendChatMessage("/msg");
				return;
			}
			Plex.minecraft.thePlayer.sendChatMessage("/msg " + PlexCoreUtils.buildCommand(args));
			instance.currentConversation = args[0];
			instance.lastConversationTime = Minecraft.getSystemTime();
		}
	}
}
