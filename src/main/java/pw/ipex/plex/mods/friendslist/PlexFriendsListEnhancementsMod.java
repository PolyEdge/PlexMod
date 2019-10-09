package pw.ipex.plex.mods.friendslist;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.mineplex.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.PlexCoreValue;
import pw.ipex.plex.mod.PlexModBase;

public class PlexFriendsListEnhancementsMod extends PlexModBase {
	public String MATCH_LIST_TOP = "&b&m=+\\[&lFriends&b&m]=+";
	public String MATCH_REQUEST_OUTGOING = "&c&lCancel - &7([a-zA-Z0-9_]{1,20}) Friendship Request";
	public String MATCH_OFFLINE = "&c&lDelete - &7([a-zA-Z0-9_]{1,20}) - &7Offline for &7[0-9]+\\.{0,1}[0-9]{0,1} (Day|Hour|Minute|Second)(s){0,1}";
	public String MATCH_REQUEST_INCOMING = "&a&lAccept - &c&lDeny - &7([a-zA-Z0-9_]{1,20}) Requested Friendship";
	public String OLD_MATCH_ONLINE = "&a&lTeleport - &c&lDelete - &a([a-zA-Z0-9_]{1,20}) - &2([a-zA-Z0-9_-]+)";
	public String MATCH_ONLINE = "(?:&a|&e)&l(?:Teleport|No Teleport) - &c&lDelete - &a([a-zA-Z0-9_]{1,20}) - &2([a-zA-Z0-9_ -]+)";
	public String MATCH_LIST_BOTTOM = "&b&m=+&3Toggle GUI&b&m=+";
	
	public Pattern PATTERN_REQUEST_OUTGOING = Pattern.compile(MATCH_REQUEST_OUTGOING);
	public Pattern PATTERN_OFFLINE = Pattern.compile(MATCH_OFFLINE);
	public Pattern PATTERN_REQUEST_INCOMING = Pattern.compile(MATCH_REQUEST_INCOMING);
	public Pattern PATTERN_ONLINE = Pattern.compile(MATCH_ONLINE);
	
	public PlexCoreValue hideOfflineEnabled = new PlexCoreValue("friendsListEnhancements_hideOffline", false);
	public PlexCoreValue hideIncomingRequestsEnabled = new PlexCoreValue("friendsListEnhancements_hideIncoming", false);
	public PlexCoreValue hideOutgoingRequestsEnabled = new PlexCoreValue("friendsListEnhancements_hideOutgoing", false);
	
	public PlexCoreValue itemTarget = new PlexCoreValue("friendsListEnhancements_itemTarget", "");
	public PlexCoreValue searchTerm = new PlexCoreValue("friendsListEnhancements_searchTerm", "");
	
	public Boolean listTop = false;
	public Integer hiddenOutgoing = 0;
	public Integer hiddenIncoming = 0;
	public Integer hiddenOffline = 0;
	
	public String getModName() {
		return "Friends List Enchancements";
	}
	
	@Override
	public void modInit() {
		this.hideOfflineEnabled.set(this.modSetting("hide_offline_friends", false).getBoolean());
		this.hideIncomingRequestsEnabled.set(this.modSetting("hide_incoming_friend_requests", false).getBoolean());
		this.hideOutgoingRequestsEnabled.set(this.modSetting("hide_outgoing_friend_requests", false).getBoolean());

		//PlexCore.registerCommandListener(new PlexCommandListenerClientCommandListener("ff"));
		//PlexCore.registerCommandListener(new PlexCommandListenerClientCommandListener("fs"));
		//PlexCore.registerCommandHandler("ff", new PlexFriendsListEnhancementsCommand());
		//PlexCore.registerCommandHandler("fs", new PlexFriendsListEnhancementsCommand());
		Plex.plexCommand.registerPlexCommand("friends", new PlexFriendsListEnhancementsCommand());
		
		Plex.plexCommand.addPlexHelpCommand("friends", "Displays friends list options");
		Plex.plexCommand.addHelpCommand("ff search", "fs", "Searches your friends list for a player (chat only)");
		
		PlexCore.registerUiTab("Friends", PlexFriendsListEnhancementsUI.class);
	}
	
	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent e) {
		if (!PlexCoreUtils.chatIsMessage(e.type)) {
			return;
		}
		String filtered = PlexCoreUtils.chatCondenseAndAmpersand(e.message.getFormattedText());
		if (filtered.matches(this.MATCH_LIST_BOTTOM)) {
			if (this.searchTerm.stringValue.equals("") && this.itemTarget.stringValue.equals("") && this.listTop) {
				this.showHiddenStats();
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GRAY", "(No online friends)"));
			}
			this.listTop = false;
			this.itemTarget.set("");
			this.searchTerm.set("");
			return;
		}
		if (filtered.matches(this.MATCH_LIST_TOP)) {
			this.hiddenOutgoing = 0;
			this.hiddenIncoming = 0;
			this.hiddenOffline = 0;
			this.listTop = true;
			if (!this.itemTarget.stringValue.equals("")) {
				e.setCanceled(true);
				PlexCoreUtils.chatAddMessage(e.message.getFormattedText());
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GRAY", "Showing ") + PlexCoreUtils.chatStyleText("DARK_GRAY", this.itemTarget.stringValue) + PlexCoreUtils.chatStyleText("GRAY", " friends."));
			}
			if (!this.searchTerm.stringValue.equals("")) {
				e.setCanceled(true);
				PlexCoreUtils.chatAddMessage(e.message.getFormattedText());
				PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GRAY", "Showing results for: ") + PlexCoreUtils.chatStyleText("DARK_GRAY", this.searchTerm.stringValue));
			}
			return;
		}
		if (filtered.matches(this.MATCH_OFFLINE)) {
			if (!this.searchTerm.stringValue.equals("")) {
				Matcher matcher = PATTERN_OFFLINE.matcher(filtered);
				matcher.find();
				if (!matcher.group(1).toLowerCase().contains(this.searchTerm.stringValue.toLowerCase())) {
					e.setCanceled(true);
				}
				return;	
			}
			if ((this.hideOfflineEnabled.booleanValue && this.itemTarget.stringValue.equals("")) || (!this.itemTarget.stringValue.equals("offline") && !this.itemTarget.stringValue.equals(""))) {
				e.setCanceled(true);
				this.hiddenOffline += 1;
				return;				
			}
		}
		if (filtered.matches(this.MATCH_REQUEST_INCOMING)) {
			if (!this.searchTerm.stringValue.equals("")) {
				Matcher matcher = PATTERN_REQUEST_INCOMING.matcher(filtered);
				matcher.find();
				if (!matcher.group(1).toLowerCase().contains(this.searchTerm.stringValue.toLowerCase())) {
					e.setCanceled(true);
				}
				return;	
			}
			if ((this.hideIncomingRequestsEnabled.booleanValue && this.itemTarget.stringValue.equals("")) || (!this.itemTarget.stringValue.equals("incoming") && !this.itemTarget.stringValue.equals(""))) {
				e.setCanceled(true);
				this.hiddenIncoming += 1;
				return;				
			}
		}
		if (filtered.matches(this.MATCH_REQUEST_OUTGOING)) {
			if (!this.searchTerm.stringValue.equals("")) {
				Matcher matcher = PATTERN_REQUEST_OUTGOING.matcher(filtered);
				matcher.find();
				if (!matcher.group(1).toLowerCase().contains(this.searchTerm.stringValue.toLowerCase())) {
					e.setCanceled(true);
				}
				return;
			}
			if ((this.hideOutgoingRequestsEnabled.booleanValue && this.itemTarget.stringValue.equals("")) || (!this.itemTarget.stringValue.equals("outgoing") && !this.itemTarget.stringValue.equals(""))) {
				e.setCanceled(true);
				this.hiddenOutgoing += 1;
				return;				
			}
		}
		if (filtered.matches(this.MATCH_ONLINE)) {
			if (!this.searchTerm.stringValue.equals("")) {
				Matcher matcher = PATTERN_ONLINE.matcher(filtered);
				matcher.find();
				if (!matcher.group(1).toLowerCase().contains(this.searchTerm.stringValue.toLowerCase())) {
					e.setCanceled(true);
				}
				return;	
			}
			if (this.itemTarget.stringValue.equals("")) {
				if (this.listTop) {
					this.showHiddenStats();
					this.listTop = false;					
				}
			}
			else if (!this.itemTarget.stringValue.equals("online")) {
				e.setCanceled(true);
				return;
			}
		}
	}
	public void showHiddenStats() {
		List<String> items = new ArrayList<String>();
		if (this.hiddenOutgoing > 0) {
			items.add(PlexCoreUtils.chatStyleText("DARK_GRAY", "" + this.hiddenOutgoing) + PlexCoreUtils.chatStyleText("GRAY", " outgoing"));
		}
		if (this.hiddenIncoming > 0) {
			items.add(PlexCoreUtils.chatStyleText("DARK_GRAY", "" + this.hiddenIncoming) + PlexCoreUtils.chatStyleText("GRAY", " incoming"));
		}
		if (this.hiddenOffline > 0) {
			items.add(PlexCoreUtils.chatStyleText("DARK_GRAY", "" + this.hiddenOffline) + PlexCoreUtils.chatStyleText("GRAY", " offline"));
		}
		if (items.size() == 0) {
			return;
		}
	    StringJoiner joiner = new StringJoiner(PlexCoreUtils.chatStyleText("GRAY", ", "), PlexCoreUtils.chatStyleText("GRAY", "(Hidden: "), PlexCoreUtils.chatStyleText("GRAY", ")"));
	    for (String item : items) {
	    	joiner.add(item);
	    }
	    PlexCoreUtils.chatAddMessage(joiner.toString());
	}

	@Override
	public void saveModConfig() {
		this.modSetting("hide_offline_friends", false).set(this.hideOfflineEnabled.booleanValue);
		this.modSetting("hide_incoming_friend_requests", false).set(this.hideIncomingRequestsEnabled.booleanValue);
		this.modSetting("hide_outgoing_friend_requests", false).set(this.hideOutgoingRequestsEnabled.booleanValue);
	}

	@Override
	public void lobbyUpdated(PlexCoreLobbyType type) {
	}
}
