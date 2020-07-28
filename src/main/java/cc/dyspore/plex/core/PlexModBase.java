package cc.dyspore.plex.core;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import cc.dyspore.plex.Plex;

public abstract class PlexModBase {
	public PlexModBase() {}
	
	public void modInit() {}
	
	public abstract String getModName();

	public void onJoin() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void onLeave() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	public void onLobbyUpdate(PlexMP.LobbyType lobbyType) {}

	public void saveConfig() {}

	public ConfigCategory getConfig() {
		return Plex.config.getCategory(this.getModName());
	}

	public Property configValue(String name, int def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property configValue(String name, String def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property configValue(String name, boolean def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property configValue(String name, float def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property configValue(String name, double def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	protected void doModLoop() {
		boolean isMineplex = PlexMP.session.isMineplex;
		this.modLoop(isMineplex);
		if (isMineplex) {
			this.onlineModLoop();
		}
	}

	public void onlineModLoop() {}

	public void modLoop(boolean isOnline) {}

	public long getLoopDelay() {
		return 0;
	}
}