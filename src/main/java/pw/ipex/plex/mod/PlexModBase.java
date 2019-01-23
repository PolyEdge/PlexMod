package pw.ipex.plex.mod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.mineplex.PlexCoreLobbyType;

public abstract class PlexModBase {
	public PlexModBase() {
	}
	
	public void modInit() {
	}
	
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
	
	public void saveModConfig() {}

	public void joinedMineplex() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void leftMineplex() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	public void lobbyUpdated(PlexCoreLobbyType lobbyType) {};
	
	public void communicate(Object ...args) {}
	
	public Object receive(Object ...args) {
		return null;
	}
	
	public void serverNameChanged(String name) {
	}

	public void onlineModLoop() {
	}

	public void modLoop(boolean isOnline) {
	}

	public long getLoopDelay() {
		return 30;
	}
}