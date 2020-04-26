package cc.dyspore.plex.mods.autogg;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.mineplex.PlexMPLobby;
import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.PlexModBase;

public class PlexAutoGGMod extends PlexModBase {
	public boolean modEnabled = false;
	public double ggDelay = 0.5D;

	public List<PlexAutoGGMessage> ggMessages;

	public boolean ggWaitUntilSilenceEnd = true;
	public int ggMode = 0; // 0 = in order, 1 = random

	public int MAX_GG_MODE = 1;
	public double MIN_DELAY = 0.0D;
	public double MAX_DELAY = 5.0D;
	
	public String subtitleText = "";
	public int useGGMessage = 0;
	public PlexAutoGGMessage lastGGMessage;
	public Long gameOverTime = null;
	public Long ggSendTime = null;
	public Long ggCooldown = null;
	public boolean sentGG = false;
	public boolean scheduleGGatChatUnsilence = true;

	@Override
	public void modInit() {
		this.modEnabled = this.modSetting("autogg_enabled", false).getBoolean(false);
		this.ggDelay = this.modSetting("autogg_delay", 0.5D).getDouble();

		this.ggMessages = new ArrayList<>();
		this.handleVersionCrossover();
		List<String> messages = Arrays.asList(Plex.config.get(this.getModName(), "autogg_messages_list", new String[0]).getStringList());
		for (String message : messages) {
			this.ggMessages.add(new PlexAutoGGMessage(message));
		}

		this.ggWaitUntilSilenceEnd = this.modSetting("autogg_wait_until_silence_over", true).getBoolean(true);
		this.ggMode = this.modSetting("autogg_message_rotation_mode", 0).getInt();
		
		Plex.plexCommand.registerPlexCommand("autogg", new PlexAutoGGCommand());
		Plex.plexCommand.addPlexHelpCommand("autogg", "Displays AutoGG options");
		
		PlexCore.registerUiTab("AutoGG", PlexAutoGGUI.class);
	}
	
	@Override
	public String getModName() {
		return "AutoGG";
	}

	public void handleVersionCrossover() {
		this.handleLegacyGGString("autogg_secondary_message");
		this.handleLegacyGGString("autogg_primary_message");
	}

	public void handleLegacyGGString(String name) {
		String value = this.modSetting(name, "").getString();
		if (value != null && !value.trim().equals("")) {
			this.ggMessages.add(0, new PlexAutoGGMessage(value));
			this.modSetting(name, "").set("");
		}
	}

	public void onlineModLoop() {
		if (!this.modEnabled) {
			return;
		}
		try {
			Field subtitleField = Plex.minecraft.ingameGUI.getClass().getDeclaredField("field_175200_y");
			subtitleField.setAccessible(true);
			this.subtitleText = (String) subtitleField.get(Plex.minecraft.ingameGUI);
		}
		catch (Throwable e1) {
			try {
				Field subtitleField = Plex.minecraft.ingameGUI.getClass().getDeclaredField("displayedSubTitle");
				subtitleField.setAccessible(true);
				this.subtitleText = (String) subtitleField.get(Plex.minecraft.ingameGUI);
			}
			catch (Throwable e2) {
				this.subtitleText = null;
			}
		}
		
		if (this.sentGG) {
			this.sentGG = false;
			this.ggSendTime = null;
			this.scheduleGGatChatUnsilence = false; 
		}

		if (this.subtitleText != null && this.gameOverTime == null) {
			if (this.subtitleText.contains("won the game")) {
				this.gameOverTime = Minecraft.getSystemTime();
			}
			else {
				//this.gameOverTime = null;
			}
		}
		if (this.isGameOver()) {
			if (!this.ggWaitUntilSilenceEnd && Minecraft.getSystemTime() < this.gameOverTime + 1000L) {
				this.scheduleGG((long) (this.ggDelay * 1000.0D));
			}
			//else if (Minecraft.getSystemTime() < this.gameOverTime + 1000L && Plex.gameState.currentLobbyName != null && Plex.gameState.currentLobbyName.startsWith("NANO")) {
			//	this.scheduleGG((long) (this.ggDelay * 1000.0D)); they added chat silence in nano
			//}
			else if (this.ggWaitUntilSilenceEnd) {
				scheduleGGatChatUnsilence = true;
			}
		}

		if (this.gameOverTime != null) {
			if (Minecraft.getSystemTime() > this.gameOverTime + 12000L) {
				this.gameOverTime = null;
			}
		}
		
		if (this.ggSendTime != null) {
			if (Minecraft.getSystemTime() > this.ggSendTime) {
				this.sendGG();
			}
		}
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent e) {
		if (!this.modEnabled) {
			return;
		}
		String minified = PlexUtilChat.chatMinimalizeLowercase(e.message.getFormattedText());
		if (this.gameOverTime == null) {
			if (minified.matches("^1st place -? ?(.*)$")) {
				this.gameOverTime = Minecraft.getSystemTime();
			}
			else if (minified.matches("^([a-z]+) (.* )?won the game!$")) {
				this.gameOverTime = Minecraft.getSystemTime();
			}
		}
		if (minified.contains("chat> chat is no longer silenced")) {
			if (this.scheduleGGatChatUnsilence) {
				scheduleGGatChatUnsilence = false;
				this.scheduleGG((long) (this.ggDelay * 1000.0D));
			}
		}
	}

	/*
	@SubscribeEvent
	public void onOverlay(RenderGameOverlayEvent event) {
		if (this.subtitleText != null) {
			Plex.renderUtils.drawScaledString("\"" + this.subtitleText + "\"", 25, 45, 0xffffffff, 0.5F, false);
		}
		else {
			Plex.renderUtils.drawScaledString("\"\"", 25, 45, 0xffffffff, 0.5F, false);
		}

	}
	*/
	
	public boolean isGameOver() {
		if (this.gameOverTime == null) {
			return false;
		}
		return Minecraft.getSystemTime() < this.gameOverTime + 20000L;
	}
	
	public void scheduleGG(long delay) {
		if (this.ggCooldown != null) {
			if (Minecraft.getSystemTime() < this.ggCooldown) {
				return;
			}
		}
		if (this.ggSendTime == null) {
			this.ggSendTime = Minecraft.getSystemTime() + delay;
			this.ggCooldown = Minecraft.getSystemTime() + 10000L;
			this.gameOverTime = null;
		}
	}

	public List<String> getGGMessages() {
		List<String> messages = new ArrayList<>();
		for (PlexAutoGGMessage message : this.ggMessages) {
			if (message.message != null) {
				messages.add(message.message);
			}
		}
		return messages;
	}

	public List<PlexAutoGGMessage> getValidGGMessages() { //safeguard due to weird forge settings handler
		List<PlexAutoGGMessage> messages = new ArrayList<>();
		for (PlexAutoGGMessage message : this.ggMessages) {
			if (message.message != null) {
				messages.add(message);
			}
		}
		return messages;
	}

	public PlexAutoGGMessage getGG() {
		List<PlexAutoGGMessage> messages = this.getValidGGMessages();
		if (messages.size() == 0) {
			return null;
		}
		if (this.ggMode == 0) {
			if (this.useGGMessage >= messages.size()) {
				this.useGGMessage = 0;
			}
			this.useGGMessage += 1;
			return messages.get(this.useGGMessage - 1);
		}
		if (this.ggMode == 1) {
			if (messages.size() == 1) {
				return messages.get(0);
			}
			List<PlexAutoGGMessage> selection = new ArrayList<>();
			for (PlexAutoGGMessage message : messages) {
				if (message != this.lastGGMessage) {
					selection.add(message);
				}
			}
			Random rand = new Random();
			return selection.get(rand.nextInt(selection.size()));
		}
		return null;
	}
	
	public void sendGG() {
		PlexAutoGGMessage ggMessage = this.getGG();
		if (ggMessage != null) {
			Plex.minecraft.thePlayer.sendChatMessage(ggMessage.message);
		}
		else {
			PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX + PlexUtilChat.chatStyleText("RED", "You do not have any GG messages added, so there are none to send!"));
		}
		this.lastGGMessage = ggMessage;
		this.sentGG = true;
	}
	
	@Override
	public void saveModConfig() {
		this.modSetting("autogg_enabled", false).set(this.modEnabled);
		this.modSetting("autogg_delay", 0.5F).set(this.ggDelay);
		this.modSetting("autogg_wait_until_silence_over", true).set(this.ggWaitUntilSilenceEnd);
		this.modSetting("autogg_message_rotation_mode", 0).set(this.ggMode);
		Plex.config.get(this.getModName(), "autogg_messages_list", new String[0]).set(this.getGGMessages().toArray(new String[0]));
	}

	@Override
	public void lobbyUpdated(PlexMPLobby.LobbyType type) {
		this.gameOverTime = null;
		this.sentGG = false;
		this.scheduleGGatChatUnsilence = false;
	}
}
