package pw.ipex.plex.mods.betterreply;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		if (namespace.equals("plex.messages")) {
			PlexCore.displayUIScreen(new PlexBetterReplyUI());
		}
		else if (namespace.equals("r")) {
			if (PlexCore.getSharedValue("betterReply_currentConversation").stringValue.equals("")) {
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "You haven't messaged anybody yet!"));
				return;
			}
			//PlexCore.getMod("Better Reply").communicate("addSentUser", PlexCore.getSharedValue("betterReply_currentConversation").stringValue);
			//PlexCore.getMod("Better Reply").communicate("addToHistory", PlexCore.getSharedValue("betterReply_currentConversation"), PlexCoreUtils.buildCommand(args));
			Plex.minecraft.thePlayer.sendChatMessage("/msg " + PlexCore.getSharedValue("betterReply_currentConversation").stringValue + " " + PlexCoreUtils.buildCommand(args));
		}
		else if (namespace.equals("rr")) {
			if (args.length == 0) {
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GRAY", "Please specify somebody to message."));
				return;
			}
			String replyUser = String.valueOf(PlexCore.getMod("Better Reply").receive("getAutoCompleteUser", args[0]));
			if (replyUser.substring(0, 1).equals("$")) {
				PlexCoreUtils.chatAddMessage(replyUser.substring(1));
			}
			else {
				Plex.minecraft.thePlayer.sendChatMessage("/msg " + replyUser.substring(1) + " " + PlexCoreUtils.buildCommand(Arrays.copyOfRange(args, 1, args.length)));
				if (PlexCore.getSharedValue("betterReply_enabled").booleanValue) {
					//PlexCore.getMod("Better Reply").communicate("addSentUser", replyUser.substring(1));
					//PlexCore.getMod("Better Reply").communicate("addToHistory", replyUser.substring(1), PlexCoreUtils.buildCommand(Arrays.copyOfRange(args, 1, args.length)));
				}
				PlexCore.getSharedValue("betterReply_currentConversation").set(replyUser.substring(1));
				PlexCore.getSharedValue("betterReply_lastConversationReply").set(Minecraft.getSystemTime());				
			}
		}
		else if (namespace.equals("dms")) {
			if (args.length == 0) {
				PlexCore.getMod("Better Reply").communicate("dmHistory", "1");
				return;
			}
			if (args.length == 1) {
				PlexCore.getMod("Better Reply").communicate("dmHistory", args[0]);
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("u") || args[0].equalsIgnoreCase("user")) {
					PlexCore.getMod("Better Reply").communicate("dmHistoryUser", args[1], "1");
				}
				else {
					PlexCore.getMod("Better Reply").communicate("dmHistoryUser", args[0], args[1]);
				}
			}
			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("u") || args[0].equalsIgnoreCase("user")) {
					PlexCore.getMod("Better Reply").communicate("dmHistoryUser", args[1], args[2]);
				}
			}
		}
		else {
			if (args.length == 0) {
				Plex.minecraft.thePlayer.sendChatMessage("/msg");
				return;
			}
			Plex.minecraft.thePlayer.sendChatMessage("/msg " + PlexCoreUtils.buildCommand(args));
			if (PlexCore.getSharedValue("betterReply_enabled").booleanValue) {
				//PlexCore.getMod("Better Reply").communicate("addSentUser", args[0]);
				//PlexCore.getMod("Better Reply").communicate("addToHistory", args[0], PlexCoreUtils.buildCommand(args));
			}
			PlexCore.getSharedValue("betterReply_currentConversation").set(args[0]);
			PlexCore.getSharedValue("betterReply_lastConversationReply").set(Minecraft.getSystemTime());
		}
	}
}
