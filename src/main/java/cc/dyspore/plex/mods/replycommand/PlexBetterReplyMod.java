package cc.dyspore.plex.mods.replycommand;

import java.util.*;

import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.commands.client.PlexCommandListener;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.regex.PlexCoreRegexManager;
import cc.dyspore.plex.core.regex.chat.PlexCoreRegexChatMatch;
import cc.dyspore.plex.core.regex.chat.PlexCoreRegexChatMatchItem;
import cc.dyspore.plex.core.PlexModBase;

public class PlexBetterReplyMod extends PlexModBase {
	public static int DEFAULT_REPLY_TIMEOUT = 300;
	public static int MAX_REPLY_TIMEOUT = 600;

	public String currentConversation = null;
	public long lastConversationTime;

	public Set<String> contacts = new HashSet<>();
	
	public boolean modEnabled;
	public int replyTimeoutSeconds = DEFAULT_REPLY_TIMEOUT;

	public PlexCommandListener replyListener;
	public PlexCommandListener modCommandsListener;
	
	public String getModName() {
		return "Better Reply";
	}
	
	@Override
	public void modInit() {
		this.modEnabled = this.modSetting("better_reply_enabled", false).getBoolean();
		this.replyTimeoutSeconds = this.modSetting("reply_timeout_seconds", DEFAULT_REPLY_TIMEOUT).getInt();

		this.replyListener = new PlexCommandListener("message", "whisper", "tell", "msg", "m", "t", "w", "r");
		this.modCommandsListener = new PlexCommandListener("rr");

		PlexBetterReplyCommand betterReplyCommand = new PlexBetterReplyCommand();
		this.replyListener.setHandler(betterReplyCommand);
		this.modCommandsListener.setHandler(betterReplyCommand);
		
		Plex.plexCommand.registerPlexCommand("reply", new PlexBetterReplyCommand());
		
		PlexCore.registerUiTab("Reply", PlexBetterReplyUI.class);
		
		Plex.plexCommand.addPlexHelpCommand("reply", "Displays messagingscreen enhancements options");
		Plex.plexCommand.addHelpCommand("rr", "$partial username", "Messages somebody who you have messaged with previously (without typing full ign)");
		Plex.plexCommand.addHelpCommand("dms", "%page", "Shows DM history");
		Plex.plexCommand.addHelpCommand("dms", "$user", "%page", "Shows previous DMs between you and a user.");

		this.replyListener.setEnabled(this.modEnabled);
	}
	
	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent e) {
		if (!PlexUtilChat.chatIsMessage(e.type)) {
			return;
		}
		PlexCoreRegexChatMatch match = PlexCoreRegexManager.getChatMatch(e.message);
		PlexCoreRegexChatMatchItem dm = match.get("direct_message");
		if (dm != null) {
			if (dm.getField("author").equalsIgnoreCase(PlexCore.getPlayerIGN())) {
				this.contacts.add(dm.getField("destination"));
				this.lastConversationTime = Minecraft.getSystemTime();
				this.currentConversation = dm.getField("destination");
			}
			else if (dm.getField("destination").equalsIgnoreCase(PlexCore.getPlayerIGN())) {
				this.contacts.add(dm.getField("author"));
				if (this.currentConversation == null) {
					this.currentConversation = dm.getField("author");
				}
				if (dm.getField("author").equalsIgnoreCase(this.currentConversation)) {
					this.lastConversationTime = Minecraft.getSystemTime();
				}
				if (Minecraft.getSystemTime() > this.lastConversationTime + (long)this.replyTimeoutSeconds * 1000L) {
					this.currentConversation = dm.getField("author");
				}
			}
		}
	}

	@Override
	public void saveModConfig() {
		this.modSetting("better_reply_enabled", false).set(this.modEnabled);
		this.modSetting("reply_timeout_seconds", 300).set(this.replyTimeoutSeconds);
		this.replyListener.setEnabled(this.modEnabled);
	}
}
