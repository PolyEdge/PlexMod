package cc.dyspore.plex.mods.chatmod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.regex.PlexCoreRegex;
import cc.dyspore.plex.core.regex.PlexCoreRegexEntry;
import cc.dyspore.plex.core.PlexModBase;

public class PlexChatStreamMod extends PlexModBase {
	public int MAX_LOBBY_FILTRATION = 2;
	public List<String> AD_MATCHERS = new ArrayList<String>();
	public List<java.util.Map.Entry<Integer, Integer>> BAR_REDUCTION = new ArrayList<java.util.Map.Entry<Integer, Integer>>();
	public int MAX_BAR_REDUCTION;

	public final List<QueuedChatComponent> paddingRemovalQueue = Collections.synchronizedList(new ArrayList<>());
	public long paddingLivedTime = 40L;

	public boolean hidePlayerChat = false;
	public boolean hidePartyChat = false;
	public boolean hideTeamChat = false;
	public boolean hideComChat = false;
	public int lobbyFiltrationLevel = 0;
	public boolean hideCommunityInvites = false;
	public boolean adblocking = false;
	public boolean hideGadgetDisable = false;
	public int barReductionIndex = 0;
	
	public List<Pattern> adPatterns = new ArrayList<>();
	public Boolean adBlockPostPadding = false;

	@Override
	public String getModName() {
		return "Chat Cleaner";
	}
	
	@Override
	public void modInit() {
		this.AD_MATCHERS.add("mineplex.com.shop");
		this.AD_MATCHERS.add("rank sale");
		this.AD_MATCHERS.add("get your (?:new )?(?:rank)");
		this.AD_MATCHERS.add("all ranks are");
		this.AD_MATCHERS.add("on sale");
		this.AD_MATCHERS.add("shop.mineplex.com");
		this.AD_MATCHERS.add("rank upgrade");
		this.AD_MATCHERS.add("treasure chest");
		this.AD_MATCHERS.add("\\$[0-9]+\\.[0-9]{2}");
		this.AD_MATCHERS.add("[0-9]{1,3}\\% off");
		
		this.BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<>(8, 8));
		this.BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<>(8, 7));
		this.BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<>(8, 6));
		this.BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<>(8, 5));
		this.BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<>(8, 4));
		this.BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<>(8, 3));
		this.BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<>(8, 2));
		this.MAX_BAR_REDUCTION = BAR_REDUCTION.size() - 1;
		
		this.hidePlayerChat = this.modSetting("hide_player_chat", false).getBoolean();
		this.hidePartyChat = this.modSetting("hide_team_chat", false).getBoolean();
		this.hideTeamChat = this.modSetting("hide_team_chat", false).getBoolean();
		this.hideComChat = this.modSetting("hide_community_chat", false).getBoolean();
		this.lobbyFiltrationLevel = this.modSetting("lobby_filtration_level", 0).getInt();
		this.hideCommunityInvites = this.modSetting("hide_community_invites", false).getBoolean();
		this.adblocking = this.modSetting("attempted_adblocking", false).getBoolean();
		this.hideGadgetDisable = this.modSetting("hide_gadget_disable", false).getBoolean();
		this.barReductionIndex = this.modSetting("bar_reduction_index", 0).getInt();
		this.barReductionIndex = 0;

		Plex.plexCommand.registerPlexCommand("chat", new PlexChatStreamCommand());

		PlexCore.registerUiTab("Chat", PlexChatStreamUI.class);
		
		for (String exp : AD_MATCHERS) {
			adPatterns.add(Pattern.compile(exp));
		}
	}
	
	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent e) {
		if (!PlexUtilChat.chatIsMessage(e.type)) {
			return;
		}
		String message = PlexUtilChat.chatCondense(e.message.getFormattedText());
		String ampMessage = PlexUtilChat.chatCondenseAndAmpersand(e.message.getFormattedText());
		PlexCoreRegexEntry entry = PlexCoreRegex.getEntryMatchingText(message);
		String potentialType = "";
		boolean isOwnPlayer = false;
		if (entry != null) {
			potentialType = entry.entryName;
			String playerName = PlexCore.getPlayerIGN();
			if (playerName != null && entry.hasField("author")) {
				isOwnPlayer = playerName.equals(entry.getField(message, "author"));
			}
		}

		String min = PlexUtilChat.chatMinimalizeLowercase(e.message.getFormattedText());

		if (this.adblocking) {
			if ((min.equals("\n")) || (min.contains("========")) || (min.trim().equals(""))) {
				e.setCanceled(true);
				if (!this.adBlockPostPadding) {
					this.paddingRemovalQueue.add(new QueuedChatComponent(e.message));
				}
				return;
			}
			else if (entry == null) {
				boolean isAd = false;
				for (Pattern pat : adPatterns) {
					Matcher m = pat.matcher(min);
					if (m.find()) {
						e.setCanceled(true);
						isAd = true;
						break;
					}
				}
				if (isAd) {
					this.adBlockPostPadding = true;
					this.paddingRemovalQueue.clear();
					return;
				}
			}
			this.adBlockPostPadding = false;
			this.flushPaddingQueue(true);
		}
		if (this.hidePlayerChat) {
			if (!isOwnPlayer && (potentialType.equals("player_chat"))) {
				e.setCanceled(true);
				return;
			}
		}
		if (this.hidePartyChat) {
			if (!isOwnPlayer && (potentialType.equals("party_chat"))) {
				e.setCanceled(true);
				return;
			}
		}
		if (this.hideTeamChat) {
			if (!isOwnPlayer && (potentialType.equals("team_chat"))) {
				e.setCanceled(true);
				return;
			}
		}
		if (this.hideComChat) {
			if (!isOwnPlayer && (potentialType.equals("community_chat"))) {
				e.setCanceled(true);
				return;
			}
		}
		if (this.hideGadgetDisable) {
			if (min.startsWith("gadget> you disabled")) {
				e.setCanceled(true);
				return;
			}
			if (min.startsWith("gadget> you enabled")) {
				e.setCanceled(true);
				return;
			}
		}
		if (this.lobbyFiltrationLevel >= 1) {
			if (ampMessage.startsWith("&9Treasure>") && !ampMessage.startsWith("&9Treasure> &e" + PlexCore.getPlayerIGN())) {
				e.setCanceled(true);
				return;
			}
			if (ampMessage.toLowerCase().startsWith("&2&lcarl the creeper>")) {
				e.setCanceled(true);
				return;
			}
		}
		if (this.lobbyFiltrationLevel >= 2) {
			if (ampMessage.startsWith("&9Stats Manager>")) {
				e.setCanceled(true);
				return;
			}
			if (ampMessage.startsWith("&9Friends> &7You have &e") && ampMessage.toLowerCase().contains("pending friend requests")) {
				e.setCanceled(true);
				return;
			}
		}
		if (this.hideCommunityInvites) {
			if (ampMessage.startsWith("&9Communities> &7You have been invited to join &e") && ampMessage.trim().endsWith("&7 communities!")) {
				e.setCanceled(true);
				return;
			}
		}
		//java.util.Map.Entry<Integer, Integer> barReductionValues = BAR_REDUCTION.get(barReductionIndex);
		//if ((Float.valueOf(barReductionValues.getKey()) / Float.valueOf(barReductionValues.getValue())) != 1.0F) {
		//	if (message.contains("=======") && message.contains("&m")) {
		//		e.setCanceled(true);
		//		PlexUtil.chatAddMessage(e.message.getFormattedText().replace(new String(new char[barReductionValues.getKey()]).replace("\0", "="), new String(new char[barReductionValues.getValue()]).replace("\0", "=")));
		//	}
		//}
	}

	public void flushPaddingQueue(boolean force) {
		List<QueuedChatComponent> send = new ArrayList<>();
		synchronized (this.paddingRemovalQueue) {
			for (QueuedChatComponent component : this.paddingRemovalQueue) {
				if (force || Minecraft.getSystemTime() > component.time + this.paddingLivedTime) {
					send.add(component);
				}
			}
			this.paddingRemovalQueue.removeAll(send);
		}
		for (QueuedChatComponent component : send) {
			PlexUtilChat.chatAddMessage(component.chatComponent);
		}
	}

	@Override
	public void onlineModLoop() {
		this.flushPaddingQueue(false);
	}

	@Override
	public void joinedMineplex() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void leftMineplex() {
		MinecraftForge.EVENT_BUS.unregister(this);
		this.paddingRemovalQueue.clear();
	}
	
	@Override
	public void saveModConfig() {
		this.modSetting("hide_player_chat", false).set(this.hidePlayerChat);
		this.modSetting("hide_party_chat", false).set(this.hidePartyChat);
		this.modSetting("hide_team_chat", false).set(this.hideTeamChat);
		this.modSetting("hide_community_chat", false).set(this.hideComChat);
		this.modSetting("lobby_filtration_level", 0).set(this.lobbyFiltrationLevel);
		this.modSetting("hide_community_invites", false).set(this.hideCommunityInvites);
		this.modSetting("attempted_adblocking", false).set(this.adblocking);
		this.modSetting("hide_gadget_disable", false).set(this.hideGadgetDisable);
		this.modSetting("bar_reduction_index", 0).set(this.barReductionIndex);
	}

	class QueuedChatComponent {
		public IChatComponent chatComponent;
		public long time;

		public QueuedChatComponent(IChatComponent chatComponent) {
			this.chatComponent = chatComponent;
			this.time = Minecraft.getSystemTime();
		}
	}
}
