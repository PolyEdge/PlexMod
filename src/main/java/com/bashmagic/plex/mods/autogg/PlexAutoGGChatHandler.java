package com.bashmagic.plex.mods.autogg;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pw.ipex.plex.core.PlexCoreUtils;

public class PlexAutoGGChatHandler {

	public static final String PLACE_REPORT_REGEX = "&r&c&l1st Place&r&f - .*".replace('&', '\u00A7');
	public static final String TEAM_REPORT_REGEX = ".*won the game!&r".replace('&', '\u00A7');
	public static final String CHAT_UNSILENCED_REGEX = "&r&9Chat> &r&7Chat is no longer silenced.&r".replace('&',
			'\u00A7');

	private PlexAutoGGMod mod = PlexAutoGGMod.getInstance();
	private boolean gameOver = false;

	@SubscribeEvent
	public void onClientChatRecieved(ClientChatReceivedEvent event) {
		if (!PlexCoreUtils.isChatMessage(event.type)) {
			return;
		}

		String message = event.message.getFormattedText();
		if (message.matches(PLACE_REPORT_REGEX) || message.matches(TEAM_REPORT_REGEX)) {
			gameOver = true;
		}
		if (gameOver && message.matches(CHAT_UNSILENCED_REGEX)) {
			gameOver = false;
			Minecraft.getMinecraft().thePlayer.sendChatMessage(mod.getAutoGGMessage());
		}
	}

}
