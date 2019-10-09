package pw.ipex.plex.mods.replycommand;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ci.PlexCommandListener;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.mineplex.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.regex.PlexCoreRegex;
import pw.ipex.plex.mod.PlexModBase;

public class PlexBetterReplyMod extends PlexModBase {
	public Pattern PATTERN_DIRECT_MESSAGE = Pattern.compile(PlexCoreRegex.MATCH_DIRECT_MESSAGE);

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
		if (!PlexCoreUtils.chatIsMessage(e.type)) {
			return;
		}
		String filtered = PlexCoreUtils.chatCondense(e.message.getFormattedText());
		if (filtered.matches(PlexCoreRegex.MATCH_DIRECT_MESSAGE)) {
			Matcher matcher = PATTERN_DIRECT_MESSAGE.matcher(filtered);
			matcher.find();
			if (matcher.group(1).equalsIgnoreCase(PlexCore.getPlayerIGN())) {
				this.lastConversationTime = Minecraft.getSystemTime();
				this.currentConversation = matcher.group(2);
			}
			else if (matcher.group(2).equalsIgnoreCase(PlexCore.getPlayerIGN())) {
				if (this.currentConversation == null) {
					this.currentConversation = matcher.group(1);
				}
				if (matcher.group(1).equalsIgnoreCase(this.currentConversation)) {
					this.lastConversationTime = Minecraft.getSystemTime();
				}
				if (Minecraft.getSystemTime() > this.lastConversationTime + (long)this.replyTimeoutSeconds * 1000L) {
					this.currentConversation = matcher.group(1);
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

	@Override
	public void lobbyUpdated(PlexCoreLobbyType type) {
	}
}
