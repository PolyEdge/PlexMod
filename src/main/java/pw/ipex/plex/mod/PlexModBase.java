package pw.ipex.plex.mod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import pw.ipex.plex.Plex;

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
	
	public abstract void saveModConfig();

	public void joinedMineplex() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void leftMineplex() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	public abstract void switchedLobby(String name);
	
	public void communicate(Object ...args) {
	}
	
	public Object receive(Object ...args) {
		return null;
	}
	
	public void serverNameChanged(String name) {
	}
}