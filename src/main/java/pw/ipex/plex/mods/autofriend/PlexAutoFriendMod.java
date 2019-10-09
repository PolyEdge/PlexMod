package pw.ipex.plex.mods.autofriend;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.cq.PlexCommandQueue;
import pw.ipex.plex.cq.PlexCommandQueueCommand;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.mineplex.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.PlexCoreValue;
import pw.ipex.plex.mod.PlexModBase;

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
	public PlexCommandQueue friendQueue = new PlexCommandQueue("autoFriendMod", Plex.plexCommandQueue);

	@Override
	public void modInit() {
		this.modEnabled = this.modSetting("autofriend_enabled", false).getBoolean(false);
		this.blacklistSetting = Plex.config.get("AutoFriend.Blacklist", "blacklist", new String[0]);
		blacklist.addAll(Arrays.asList(this.blacklistSetting.getStringList()));
		
		Plex.plexCommand.registerPlexCommand("autofriend", new PlexAutoFriendCommand());
		
		Plex.plexCommand.addPlexHelpCommand("autofriend", "Displays AutoFriend options");
		
		PlexCore.registerUiTab("AutoFriend", PlexAutoFriendUI.class);
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
		String minified = PlexCoreUtils.chatMinimalizeLowercase(e.message.getFormattedText());
		if (minified.matches(MATCH_REQUEST)) {
			Matcher playerMatcher = PATTERN_REQUEST.matcher(minified);
			playerMatcher.find();
			String friendName = playerMatcher.group(1);
			if (!blacklist.contains(friendName)) {
				PlexCommandQueueCommand command = new PlexCommandQueueCommand("autoFriendMod", "/f " + friendName, 4000L);
				command.setCompleteOnSend(true);
				friendQueue.addCommand(command);
			}
		}
	}

	
	@Override
	public void saveModConfig() {
		this.modSetting("autofriend_enabled", false).set(this.modEnabled);
	}

	@Override
	public void lobbyUpdated(PlexCoreLobbyType type) {
	}
}
