package pw.ipex.plex.mods.chatmod;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ci.PlexCommandListener;
import pw.ipex.plex.core.*;
import pw.ipex.plex.core.mineplex.PlexCoreLobbyType;
import pw.ipex.plex.core.regex.PlexCoreRegex;
import pw.ipex.plex.core.regex.PlexCoreRegexEntry;
import pw.ipex.plex.mod.PlexModBase;

public class PlexHideStreamMod extends PlexModBase {
	public static Integer MAX_LOBBY_FILTRATION = 2;
	public static List<String> AD_MATCHERS = new ArrayList<String>();
	public static List<java.util.Map.Entry<Integer, Integer>> BAR_REDUCTION = new ArrayList<java.util.Map.Entry<Integer, Integer>>();
	public static Integer MAX_BAR_REDUCTION;

	public PlexCoreValue hidePlayerChat = new PlexCoreValue("hideStream_hidePlayerChat", false);
	public PlexCoreValue hidePartyChat = new PlexCoreValue("hideStream_hidePartyChat", false);
	public PlexCoreValue hideTeamChat = new PlexCoreValue("hideStream_hideTeamChat", false);
	public PlexCoreValue hideComChat = new PlexCoreValue("hideStream_hideComChat", false);
	public PlexCoreValue lobbyFiltrationLevel = new PlexCoreValue("hideStream_lobbyFiltrationLevel", 0);
	public PlexCoreValue hideCommunityInvites = new PlexCoreValue("hideStream_hideInvites", false);
	public PlexCoreValue attemptedAdblocking = new PlexCoreValue("hideStream_adBlock", false);
	public PlexCoreValue barReductionIndex = new PlexCoreValue("hideStream_barReductionIndex", 0);
	
	public List<Pattern> adPatterns = new ArrayList<Pattern>();
	public Boolean adBlockPadding = false;

	@Override
	public String getModName() {
		return "Chat Cleaner";
	}
	
	@Override
	public void modInit() {
		AD_MATCHERS.add("mineplex.com.shop");
		AD_MATCHERS.add("rank sale");
		AD_MATCHERS.add("get your (?:new )?(?:rank)");
		AD_MATCHERS.add("all ranks are");
		AD_MATCHERS.add("on sale");
		AD_MATCHERS.add("shop.mineplex.com");
		AD_MATCHERS.add("rank upgrade");
		AD_MATCHERS.add("treasure chest");
		AD_MATCHERS.add("\\$[0-9]+\\.[0-9]{2}");
		AD_MATCHERS.add("[0-9]{1,3}\\% off");
		
		BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<Integer, Integer>(8, 8));
		BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<Integer, Integer>(8, 7));
		BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<Integer, Integer>(8, 6));
		BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<Integer, Integer>(8, 5));
		BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<Integer, Integer>(8, 4));
		BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<Integer, Integer>(8, 3));
		BAR_REDUCTION.add(new java.util.AbstractMap.SimpleEntry<Integer, Integer>(8, 2));
		MAX_BAR_REDUCTION = BAR_REDUCTION.size() - 1;
		
		this.hidePlayerChat.set(this.modSetting("hide_player_chat", false).getBoolean());
		this.hidePartyChat.set(this.modSetting("hide_team_chat", false).getBoolean());
		this.hideTeamChat.set(this.modSetting("hide_team_chat", false).getBoolean());
		this.hideComChat.set(this.modSetting("hide_community_chat", false).getBoolean());
		this.lobbyFiltrationLevel.set(this.modSetting("lobby_filtration_level", 0).getInt());
		this.hideCommunityInvites.set(this.modSetting("hide_community_invites", false).getBoolean());
		this.attemptedAdblocking.set(this.modSetting("attempted_adblocking", false).getBoolean());
		this.barReductionIndex.set(this.modSetting("bar_reduction_index", 0).getInt());
		this.barReductionIndex.set(0);

		PlexCore.registerCommandListener(new PlexCommandListener("tc"));
		PlexCore.registerCommandHandler("tc", new PlexHideStreamCommand());
		Plex.plexCommand.registerPlexCommand("chat", new PlexHideStreamCommand());
		
		Plex.plexCommand.addPlexHelpCommand("chat", "tc", "Displays chat options");
		
		PlexCore.registerUiTab("Chat", PlexHideStreamUI.class);
		
		for (String exp : AD_MATCHERS) {
			adPatterns.add(Pattern.compile(exp));
		}
	}
	
	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent e) {
		if (!PlexCoreUtils.isChatMessage(e.type)) {
			return;
		}
		String message = PlexCoreUtils.condenseChatFilter(e.message.getFormattedText());
		String ampMessage = PlexCoreUtils.condenseChatAmpersandFilter(e.message.getFormattedText());
		PlexCoreRegexEntry entry = PlexCoreRegex.getEntryMatchingText(message);
		String potentialType = "";
		Boolean isOwnPlayer = false;
		if (entry != null) {
			potentialType = entry.entryName;
			if (entry.hasField("author")) {
				isOwnPlayer = PlexCore.getPlayerIGN().equals(entry.getField(message, "author"));
			}
		}

		String min = PlexCoreUtils.minimalize(e.message.getFormattedText());

		//PlexCoreUtils.chatAddMessage(potentialType);
		if (hidePlayerChat.booleanValue) {
			if (!isOwnPlayer && (potentialType.equals("player_chat"))) {
				e.setCanceled(true);
				return;
			}
		}
		if (hidePartyChat.booleanValue) {
			if (!isOwnPlayer && (potentialType.equals("party_chat"))) {
				e.setCanceled(true);
				return;
			}
		}
		if (hideTeamChat.booleanValue) {
			if (!isOwnPlayer && (potentialType.equals("team_chat"))) {
				e.setCanceled(true);
				return;
			}
		}
		if (hideComChat.booleanValue) {
			if (!isOwnPlayer && (potentialType.equals("community_chat"))) {
				e.setCanceled(true);
				return;
			}
		}
		if (lobbyFiltrationLevel.integerValue >= 1) {
			if (ampMessage.startsWith("&9Treasure>") && !ampMessage.startsWith("&9Treasure> &e" + PlexCore.getPlayerIGN())) {
				e.setCanceled(true);
				return;
			}
			if (ampMessage.toLowerCase().startsWith("&2&lcarl the creeper>")) {
				e.setCanceled(true);
				return;
			}
		}
		if (lobbyFiltrationLevel.integerValue >= 2) {
			if (ampMessage.startsWith("&9Stats Manager>")) {
				e.setCanceled(true);
				return;
			}
			if (ampMessage.startsWith("&9Friends> &7You have &e") && ampMessage.toLowerCase().contains("pending friend requests")) {
				e.setCanceled(true);
				return;
			}
		}
		if (hideCommunityInvites.booleanValue) {
			if (ampMessage.startsWith("&9Communities> &7You have been invited to join &e") && ampMessage.trim().endsWith("&7 communities!")) {
				e.setCanceled(true);
				return;
			}
		}
		if (this.attemptedAdblocking.booleanValue) {
			if (this.adBlockPadding) {
				if ((min.equals("\n")) || (min.contains("========")) || (min.trim().equals(""))) {
					e.setCanceled(true);
					return;
					//Plex.minecraft.ingameGUI.getChatGUI().deleteChatLine(Plex.minecraft.ingameGUI.getChatGUI().getLineCount() - 1); 
				}
				else {
					this.adBlockPadding = false;
				}
			}
			else if (entry == null) {
				for (Pattern pat : adPatterns) {
					Matcher m = pat.matcher(min);
					if (m.find()) {
						e.setCanceled(true);
						this.adBlockPadding = true;
						return;
					}
				}
			}
		}
		java.util.Map.Entry<Integer, Integer> barReductionValues = BAR_REDUCTION.get(barReductionIndex.integerValue);
		if ((Float.valueOf(barReductionValues.getKey()) / Float.valueOf(barReductionValues.getValue())) != 1.0F) {
			if (message.contains("=======") && message.contains("&m")) {
				//e.setCanceled(true);
				//PlexCoreUtils.chatAddMessage(e.message.getFormattedText().replace(new String(new char[barReductionValues.getKey()]).replace("\0", "="), new String(new char[barReductionValues.getValue()]).replace("\0", "=")));
			}
		}
	}
	
	@Override
	public void saveModConfig() {
		this.modSetting("hide_player_chat", false).set(this.hidePlayerChat.booleanValue);
		this.modSetting("hide_party_chat", false).set(this.hidePartyChat.booleanValue);
		this.modSetting("hide_team_chat", false).set(this.hideTeamChat.booleanValue);
		this.modSetting("hide_community_chat", false).set(this.hideComChat.booleanValue);
		this.modSetting("lobby_filtration_level", 0).set(this.lobbyFiltrationLevel.integerValue);
		this.modSetting("hide_community_invites", false).set(this.hideCommunityInvites.booleanValue);
		this.modSetting("attempted_adblocking", false).set(this.attemptedAdblocking.booleanValue);
		this.modSetting("bar_reduction_index", 0).set(this.barReductionIndex.integerValue);
	}

	@Override
	public void switchedLobby(PlexCoreLobbyType type) {
	}
}
