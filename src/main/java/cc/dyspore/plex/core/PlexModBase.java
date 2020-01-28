package cc.dyspore.plex.core;

import cc.dyspore.plex.core.mineplex.PlexLobbyType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import cc.dyspore.plex.Plex;

public abstract class PlexModBase {
	public PlexModBase() {}
	
	public void modInit() {}
	
	public abstract String getModName();

	public Property modSetting(String name, Integer def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property modSetting(String name, String def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property modSetting(String name, Boolean def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property modSetting(String name, Float def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property modSetting(String name, Double def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public ConfigCategory getConfig() {
		return Plex.config.getCategory(this.getModName());
	}
	
	public void saveModConfig() {}

	public void joinedMineplex() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void leftMineplex() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	public void lobbyUpdated(PlexLobbyType lobbyType) {}

	public void onlineModLoop() {}

	public void modLoop(boolean isOnline) {}

	public long getLoopDelay() {
		return 30;
	}
}