package cc.dyspore.plex.mods.replycommand;

import java.util.*;

import cc.dyspore.plex.core.util.PlexUtil;
import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.commands.client.PlexCommandHandler;
import cc.dyspore.plex.core.PlexCore;

public class PlexBetterReplyCommand extends PlexCommandHandler {

	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		PlexBetterReplyMod instance = PlexCore.modInstance(PlexBetterReplyMod.class);
		if (namespace.equals("plex.reply")) {
			PlexCore.displayMenu(new PlexBetterReplyUI());
		}
		else if (namespace.equals("r")) {
			if (instance.currentConversation == null) {
				PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX + PlexUtilChat.chatStyleText("GRAY", "You haven't messaged anybody yet!"));
				return;
			}
			Plex.minecraft.thePlayer.sendChatMessage("/msg " + instance.currentConversation + " " + PlexUtil.buildCommand(args));
			instance.lastConversationTime = Minecraft.getSystemTime();
		}
		else if (namespace.equals("rr")) {
			if (args.length == 0) {
				PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX + PlexUtilChat.chatStyleText("GRAY", "Please specify somebody to message."));
				return;
			}
			List<String> matchUsers = PlexUtil.matchStringToList(args[0], new ArrayList<>(instance.contacts));
			if (matchUsers.size() == 0) {
				PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX + PlexUtilChat.chatStyleText("GRAY", "You haven't messaged anybody matching the ign ") + PlexUtilChat.chatStyleText("BLUE", args[0]) + PlexUtilChat.chatStyleText("GRAY", "."));
				return;
			}
			if (matchUsers.size() > 1) {
				String outputString = PlexUtilChat.PLEX + PlexUtilChat.chatStyleText("GRAY", "You have messaged with multiple people matching the ign ") + PlexUtilChat.chatStyleText("BLUE", args[0]) + PlexUtilChat.chatStyleText("GRAY", ": ");
				StringJoiner joiner = new StringJoiner(PlexUtilChat.chatStyleText("GRAY", ", "), "", "");
				for (String ign : matchUsers) {
					joiner.add(PlexUtilChat.chatStyleText("GOLD", ign));
				}
				PlexUtilChat.chatAddMessage(outputString + joiner.toString() + PlexUtilChat.chatStyleText("GRAY", "."));
				return;
			}
			Plex.minecraft.thePlayer.sendChatMessage("/msg " + matchUsers.get(0) + " " + PlexUtil.buildCommand(Arrays.copyOfRange(args, 1, args.length)));
			instance.currentConversation = matchUsers.get(0);
			instance.lastConversationTime = Minecraft.getSystemTime();
		}
		else {
			if (args.length == 0) {
				Plex.minecraft.thePlayer.sendChatMessage("/msg");
				return;
			}
			Plex.minecraft.thePlayer.sendChatMessage("/msg " + PlexUtil.buildCommand(args));
			instance.currentConversation = args[0];
			instance.lastConversationTime = Minecraft.getSystemTime();
		}
	}
}
