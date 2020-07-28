package cc.dyspore.plex.mods.autofriend;

import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.util.PlexUtilChat;
import cc.dyspore.plex.commands.queue.PlexCommandQueue;
import cc.dyspore.plex.core.PlexModBase;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlexAutoFriendMod extends PlexModBase {
	public String MATCH_REQUEST = "^friends> (.+) sen[dt] you a friend request!? accept deny!?$";

	public Pattern PATTERN_REQUEST = Pattern.compile(MATCH_REQUEST);

	public boolean modEnabled;
	public static List<String> blacklist = new ArrayList<>();

	public Property blacklistSetting;
	public PlexCommandQueue friendQueue = new PlexCommandQueue("autoFriendMod", Plex.queue);

	@Override
	public void modInit() {
		this.modEnabled = this.configValue("autofriend_enabled", false).getBoolean(false);
		this.blacklistSetting = Plex.config.get("AutoFriend.Blacklist", "blacklist", new String[0]);
		blacklist.addAll(Arrays.asList(this.blacklistSetting.getStringList()));
		
		Plex.plexCommand.registerPlexCommand("autofriend", new PlexAutoFriendCommand());
		
		Plex.plexCommand.addPlexHelpCommand("autofriend", "Displays AutoFriend options");
		
		PlexCore.registerMenuTab("AutoFriend", PlexAutoFriendUI.class);
	}
	
	@Override
	public String getModName() {
		return "AutoFriend";
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent e) {
		if (!this.modEnabled) {
			return;
		}
		String minified = PlexUtilChat.chatMinimalizeLowercase(e.message.getFormattedText());
		if (minified.matches(MATCH_REQUEST)) {
			Matcher playerMatcher = PATTERN_REQUEST.matcher(minified);
			playerMatcher.find();
			String friendName = playerMatcher.group(1);
			if (!blacklist.contains(friendName)) {
				PlexCommandQueue.Command command = new PlexCommandQueue.Command("autoFriendMod", "/f " + friendName, 4000L);
				command.setCompleteOnSend(true);
				friendQueue.add(command);
			}
		}
	}

	
	@Override
	public void saveConfig() {
		this.configValue("autofriend_enabled", false).set(this.modEnabled);
	}

}
