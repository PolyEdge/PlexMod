package pw.ipex.plex.mods.autogg;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.PlexCoreValue;
import pw.ipex.plex.mod.PlexModBase;

public class PlexAutoGGMod extends PlexModBase {
	public PlexCoreValue modEnabled = new PlexCoreValue("autoGG_enabled", false);
	public PlexCoreValue ggDelay = new PlexCoreValue("autoGG_delay", 0.5D);
	public PlexCoreValue ggMessagePrimary = new PlexCoreValue("autoGG_primaryMessage", "gg");
	public PlexCoreValue ggMessageSecondary = new PlexCoreValue("autoGG_secondaryMessage", "gg.");
	public PlexCoreValue ggWaitUntilSilenceEnd = new PlexCoreValue("autoGG_waitUntilSilenceOver", true);
	
	public static double MIN_DELAY = 0.0D;
	public static double MAX_DELAY = 5.0D;
	
	public String subtitleText = "";
	public int useGGMessage = 0;
	public Long gameOverTime = null;
	public Long ggSendTime = null;
	public Long ggCooldown = null;
	public boolean sentGG = false;
	public boolean scheduleGGatChatUnsilence = true;

	@Override
	public void modInit() {
		this.modEnabled.set(this.modSetting("autogg_enabled", false).getBoolean(false));
		this.ggDelay.set(this.modSetting("autogg_delay", 0.5D).getDouble());
		this.ggMessagePrimary.set(this.modSetting("autogg_primary_message", "gg").getString());
		this.ggMessageSecondary.set(this.modSetting("autogg_secondary_message", "gg.").getString());
		this.ggWaitUntilSilenceEnd.set(this.modSetting("autogg_wait_until_silence_over", true).getBoolean(true));
		
		Plex.plexCommand.registerPlexCommand("autogg", new PlexAutoGGCommand());
		
		Plex.plexCommand.addPlexHelpCommand("autogg", "Displays AutoGG options");
		
		PlexCore.registerUiTab("AutoGG", PlexAutoGGUI.class);
		
		
	}
	
	@Override
	public String getModName() {
		return "AutoGG";
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (!this.modEnabled.booleanValue) {
			return;
		}
		try {
			Field subtitleField = Plex.minecraft.ingameGUI.getClass().getDeclaredField("field_175200_y");
			subtitleField.setAccessible(true);
			this.subtitleText = (String) subtitleField.get(Plex.minecraft.ingameGUI);
		}
		catch (Throwable e1) {
			this.subtitleText = "";
		}
		
		if (this.sentGG) {
			this.sentGG = false;
			this.ggSendTime = null;
			this.scheduleGGatChatUnsilence = false; 
		}		
		
		if (this.subtitleText.contains("won the game") && this.gameOverTime == null) {
			this.gameOverTime = Minecraft.getSystemTime();
		}
		else {
			this.gameOverTime = null;
		}
		
		if (this.isGameOver()) {
			if (!this.ggWaitUntilSilenceEnd.booleanValue && Minecraft.getSystemTime() < this.gameOverTime + 1000L) {
				this.scheduleGG((long) (this.ggDelay.doubleValue * 1000.0D));
			}
			else if (this.ggWaitUntilSilenceEnd.booleanValue) {
				scheduleGGatChatUnsilence = true;
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
		if (!this.modEnabled.booleanValue) {
			return;
		}
		String minified = PlexCoreUtils.minimalize(e.message.getFormattedText());
		if (minified.contains("chat> chat is no longer silenced")) {
			if (this.scheduleGGatChatUnsilence) {
				scheduleGGatChatUnsilence = false;
				this.scheduleGG((long) (this.ggDelay.doubleValue * 1000.0D));
			}
		}
	}
	
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
	
	public void sendGG() {
		String ggMessage = this.ggMessagePrimary.stringValue;
		if (this.useGGMessage % 2 == 1) {
			ggMessage = this.ggMessageSecondary.stringValue;
		}
		Plex.minecraft.thePlayer.sendChatMessage(ggMessage);
		this.useGGMessage = (this.useGGMessage + 1) % 2;
		this.sentGG = true;
	}
	
	@Override
	public void saveModConfig() {
		this.modSetting("autogg_enabled", false).set(this.modEnabled.booleanValue);
		this.modSetting("autogg_delay", 0.5F).set(this.ggDelay.doubleValue);
		this.modSetting("autogg_primary_message", "").set(this.ggMessagePrimary.stringValue);
		this.modSetting("autogg_secondary_message", "").set(this.ggMessageSecondary.stringValue);
		this.modSetting("autogg_wait_until_silence_over", true).set(this.ggWaitUntilSilenceEnd.booleanValue);
	}

	@Override
	public void switchedLobby(PlexCoreLobbyType type) {
		this.gameOverTime = null;
		this.sentGG = false;
		this.scheduleGGatChatUnsilence = false;
		if (type.equals(PlexCoreLobbyType.SERVER_HUB)) {
			this.useGGMessage = 0;
		}
	}
}
