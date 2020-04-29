package cc.dyspore.plex.core;

import cc.dyspore.plex.core.mineplex.PlexMPLobby;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import cc.dyspore.plex.Plex;

public abstract class PlexModBase {
	public PlexModBase() {}
	
	public void modInit() {}
	
	public abstract String getModName();

	public Property modSetting(String name, int def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property modSetting(String name, String def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property modSetting(String name, boolean def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property modSetting(String name, float def) {
		return Plex.config.get(this.getModName(), name, def);
	}

	public Property modSetting(String name, double def) {
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

	public void lobbyUpdated(PlexMPLobby.LobbyType lobbyType) {}

	protected void doModLoop() {
		boolean isMineplex = Plex.gameState.isMineplex;
		this.modLoop(isMineplex);
		if (isMineplex) {
			this.onlineModLoop();
		}
	}

	public void onlineModLoop() {}

	public void modLoop(boolean isOnline) {}

	public long getLoopDelay() {
		return -1;
	}
}