package pw.ipex.plex.mods.autofriend;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.commandqueue.PlexCommandQueue;
import pw.ipex.plex.commandqueue.PlexCommandQueueCommand;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.PlexCoreValue;
import pw.ipex.plex.mod.PlexModBase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlexAutoFriendMod extends PlexModBase {
	public String MATCH_REQUEST = "^friends> (.+) sen[dt] you a friend request!? accept deny!?$";

	public Pattern PATTERN_REQUEST = Pattern.compile(MATCH_REQUEST);

	public PlexCoreValue modEnabled = new PlexCoreValue("autoFriend_enabled", false);
	public static List<String> blacklist = new ArrayList<>();

	public Property blacklistSetting;
	public PlexCommandQueue friendQueue = new PlexCommandQueue("autoFriendMod", Plex.plexCommandQueue);

	@Override
	public void modInit() {
		this.modEnabled.set(this.modSetting("autofriend_enabled", false).getBoolean(false));
		this.blacklistSetting = Plex.config.get("AutoFriend.Blacklist", "blacklist", new String[0]);
		for (String item : this.blacklistSetting.getStringList()) {
			blacklist.add(item);
		}
		
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
		if (!this.modEnabled.booleanValue) {
			return;
		}
		String minified = PlexCoreUtils.minimalize(e.message.getFormattedText());
		if (minified.matches(MATCH_REQUEST)) {
			Matcher playerMatcher = PATTERN_REQUEST.matcher(minified);
			playerMatcher.find();
			String friendName = playerMatcher.group(1);
			if (!blacklist.contains(friendName)) {
				PlexCommandQueueCommand command = new PlexCommandQueueCommand("autoFriendMod", "/f " + friendName);
				command.completeOnSend = true;
				friendQueue.addCommand(command);
			}
		}
	}

	
	@Override
	public void saveModConfig() {
		this.modSetting("autofriend_enabled", false).set(this.modEnabled.booleanValue);
	}

	@Override
	public void switchedLobby(PlexCoreLobbyType type) {
	}
}
