package cc.dyspore.plex.mods.friendslist;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.PlexModBase;

public class PlexFriendsListMod extends PlexModBase {
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
	
	public boolean hideOfflineEnabled = false;
	public boolean hideIncomingRequestsEnabled = false;
	public boolean hideOutgoingRequestsEnabled = false;
	
	public String itemTarget = null;
	public String searchTerm = null;
	
	public boolean listTop = false;
	public int hiddenOutgoing = 0;
	public int hiddenIncoming = 0;
	public int hiddenOffline = 0;
	
	public String getModName() {
		return "Friends List Enchancements";
	}
	
	@Override
	public void modInit() {
		this.hideOfflineEnabled = this.configValue("hide_offline_friends", false).getBoolean();
		this.hideIncomingRequestsEnabled = this.configValue("hide_incoming_friend_requests", false).getBoolean();
		this.hideOutgoingRequestsEnabled = this.configValue("hide_outgoing_friend_requests", false).getBoolean();

		//PlexCore.registerCommandListener(new PlexClientCommandListener("ff"));
		//PlexCore.registerCommandListener(new PlexClientCommandListener("fs"));
		//PlexCore.registerCommandHandler("ff", new PlexFriendsListCommand());
		//PlexCore.registerCommandHandler("fs", new PlexFriendsListCommand());
		Plex.plexCommand.registerPlexCommand("friends", new PlexFriendsListCommand());
		
		Plex.plexCommand.addPlexHelpCommand("friends", "Displays friends list options");
		Plex.plexCommand.addHelpCommand("ff search", "fs", "Searches your friends list for a player (chat only)");
		
		PlexCore.registerMenuTab("Friends", PlexFriendsListUI.class);
	}
	
	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent e) {
		if (!PlexUtilChat.chatIsMessage(e.type)) {
			return;
		}
		String filtered = PlexUtilChat.chatCondenseAndAmpersand(e.message.getFormattedText());
		if (filtered.matches(this.MATCH_LIST_BOTTOM)) {
			if (this.searchTerm == null && this.itemTarget == null && this.listTop) {
				this.showHiddenStats();
				PlexUtilChat.chatAddMessage(PlexUtilChat.chatStyleText("GRAY", "(No online friends)"));
			}
			this.listTop = false;
			this.itemTarget = null;
			this.searchTerm = null;
			return;
		}
		if (filtered.matches(this.MATCH_LIST_TOP)) {
			this.hiddenOutgoing = 0;
			this.hiddenIncoming = 0;
			this.hiddenOffline = 0;
			this.listTop = true;
			if (this.itemTarget != null) {
				e.setCanceled(true);
				PlexUtilChat.chatAddMessage(e.message.getFormattedText());
				PlexUtilChat.chatAddMessage(PlexUtilChat.chatStyleText("GRAY", "Showing ") + PlexUtilChat.chatStyleText("DARK_GRAY", this.itemTarget) + PlexUtilChat.chatStyleText("GRAY", " friends."));
			}
			if (this.searchTerm != null) {
				e.setCanceled(true);
				PlexUtilChat.chatAddMessage(e.message.getFormattedText());
				PlexUtilChat.chatAddMessage(PlexUtilChat.chatStyleText("GRAY", "Showing results for: ") + PlexUtilChat.chatStyleText("DARK_GRAY", this.searchTerm));
			}
			return;
		}
		if (filtered.matches(this.MATCH_OFFLINE)) {
			if (this.searchTerm != null) {
				Matcher matcher = PATTERN_OFFLINE.matcher(filtered);
				matcher.find();
				if (!matcher.group(1).toLowerCase().contains(this.searchTerm.toLowerCase())) {
					e.setCanceled(true);
				}
				return;	
			}
			if ((this.hideOfflineEnabled && this.itemTarget == null) || (!this.itemTarget.equals("offline") && this.itemTarget != null)) {
				e.setCanceled(true);
				this.hiddenOffline += 1;
				return;				
			}
		}
		if (filtered.matches(this.MATCH_REQUEST_INCOMING)) {
			if (this.searchTerm != null) {
				Matcher matcher = PATTERN_REQUEST_INCOMING.matcher(filtered);
				matcher.find();
				if (!matcher.group(1).toLowerCase().contains(this.searchTerm.toLowerCase())) {
					e.setCanceled(true);
				}
				return;	
			}
			if ((this.hideIncomingRequestsEnabled && this.itemTarget == null) || (!this.itemTarget.equals("incoming") && this.itemTarget != null)) {
				e.setCanceled(true);
				this.hiddenIncoming += 1;
				return;				
			}
		}
		if (filtered.matches(this.MATCH_REQUEST_OUTGOING)) {
			if (this.searchTerm != null) {
				Matcher matcher = PATTERN_REQUEST_OUTGOING.matcher(filtered);
				matcher.find();
				if (!matcher.group(1).toLowerCase().contains(this.searchTerm.toLowerCase())) {
					e.setCanceled(true);
				}
				return;
			}
			if ((this.hideOutgoingRequestsEnabled && this.itemTarget == null) || (!this.itemTarget.equals("outgoing") && this.itemTarget != null)) {
				e.setCanceled(true);
				this.hiddenOutgoing += 1;
				return;				
			}
		}
		if (filtered.matches(this.MATCH_ONLINE)) {
			if (this.searchTerm != null) {
				Matcher matcher = PATTERN_ONLINE.matcher(filtered);
				matcher.find();
				if (!matcher.group(1).toLowerCase().contains(this.searchTerm.toLowerCase())) {
					e.setCanceled(true);
				}
				return;	
			}
			if (this.itemTarget == null) {
				if (this.listTop) {
					this.showHiddenStats();
					this.listTop = false;					
				}
			}
			else if (!this.itemTarget.equals("online")) {
				e.setCanceled(true);
				return;
			}
		}
	}
	public void showHiddenStats() {
		List<String> items = new ArrayList<String>();
		if (this.hiddenOutgoing > 0) {
			items.add(PlexUtilChat.chatStyleText("DARK_GRAY", "" + this.hiddenOutgoing) + PlexUtilChat.chatStyleText("GRAY", " outgoing"));
		}
		if (this.hiddenIncoming > 0) {
			items.add(PlexUtilChat.chatStyleText("DARK_GRAY", "" + this.hiddenIncoming) + PlexUtilChat.chatStyleText("GRAY", " incoming"));
		}
		if (this.hiddenOffline > 0) {
			items.add(PlexUtilChat.chatStyleText("DARK_GRAY", "" + this.hiddenOffline) + PlexUtilChat.chatStyleText("GRAY", " offline"));
		}
		if (items.size() == 0) {
			return;
		}
	    StringJoiner joiner = new StringJoiner(PlexUtilChat.chatStyleText("GRAY", ", "), PlexUtilChat.chatStyleText("GRAY", "(Hidden: "), PlexUtilChat.chatStyleText("GRAY", ")"));
	    for (String item : items) {
	    	joiner.add(item);
	    }
	    PlexUtilChat.chatAddMessage(joiner.toString());
	}

	@Override
	public void saveConfig() {
		this.configValue("hide_offline_friends", false).set(this.hideOfflineEnabled);
		this.configValue("hide_incoming_friend_requests", false).set(this.hideIncomingRequestsEnabled);
		this.configValue("hide_outgoing_friend_requests", false).set(this.hideOutgoingRequestsEnabled);
	}
}
