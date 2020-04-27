package cc.dyspore.plex.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cc.dyspore.plex.core.mineplex.PlexMPLobby;
import cc.dyspore.plex.ui.PlexUIModMenuScreen;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.ui.PlexUIBase;

/**
 * The module core class for PlexMod. Use the static methods in this class to
 * register the various aspects of your add-on mod.
 * 
 * @since 1.0
 */
public class PlexCore {
	private static Map<Class<? extends PlexModBase>, PlexModBase> modules = new ConcurrentHashMap<>();
	private static List<PlexUITab> uiTabList = new ArrayList<>();

	private static PlexCoreEventLoop eventLoopMods = PlexCoreEventLoop.create("modLoop").setClock(50);
	private static PlexCoreEventLoop eventLoopInternal = PlexCoreEventLoop.create("internalLoop").setClock(25);

	private PlexCore() {
	}

	/**
	 * Registers the mod task loop
	 *
	 * @return The task loop, {@link PlexCoreEventLoop}
	 */
	public static PlexCoreEventLoop getModLoop() {
		return eventLoopMods;
	}

	/**
	 * Registers the internal task loop
	 *
	 * @return The task loop, {@link PlexCoreEventLoop}
	 */
	public static PlexCoreEventLoop getInternalLoop() {
		return eventLoopInternal;
	}

	/**
	 * Registers a mod
	 * 
	 * @param mod The mod to register
	 */
	public static void register(final PlexModBase mod) {
		modules.put(mod.getClass(), mod);
		mod.modInit();
		long loopDelay = mod.getLoopDelay();
		if (loopDelay > 0) {
			getModLoop().addTask(mod::doModLoop, loopDelay);
		}
		else {
			getModLoop().addTask(mod::doModLoop);
		}
		PlexCore.saveAllConfig();
	}

	/**
	 * Returns a mod
	 * 
	 * @param modClass The class of the mod which extends {@link PlexModBase}
	 * @return The mod, a new registered instance if one is not already loaded
	 */
	public static <T extends PlexModBase> T modInstance(Class<T> modClass) {
		return Objects.requireNonNull(modInstanceOrNull(modClass));
	}

	/**
	 * Returns a mod
	 *
	 * @param modClass The class of the mod which extends {@link PlexModBase}
	 * @return The instance of the mod, or null if not loaded
	 */
	public static <T extends PlexModBase> T modInstanceOrNull(Class<T> modClass) {
		if (modules.containsKey(modClass)) {
			try {
				return modClass.cast(modules.get(modClass));
			}
			catch (Throwable e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Registers a UI tab under the given title
	 * 
	 * @param name  Title of the UI tab
	 * @param clazz Class of the UI tab
	 */
	public static PlexUITab registerUiTab(String name, Class<? extends PlexUIBase> clazz) {
		PlexUITab tab = new PlexUITab(clazz, name);
		uiTabList.add(tab);
		return tab;
	}

	/**
	 * Gets the title of the UI tab registered at the given index
	 * 
	 * @param pos Index of the tab
	 * @return Title of the tab if it exists, null otherwise
	 * @see PlexUIBase
	 */
	public static PlexUITab getUiTabAt(Integer pos) {
		if (pos >= uiTabList.size()) {
			return null;
		}
		return uiTabList.get(pos);
	}

	/**
	 * Returns the UI tab list
	 * 
	 * @return the UI tab list
	 * @see PlexUIBase
	 */
	public static List<PlexUITab> getUiTabList() {
		return uiTabList;
	}

	/**
	 * Save all mod config files
	 */
	public static void saveAllConfig() {
		for (PlexModBase mod : modules.values()) {
			mod.saveModConfig();
		}
		Plex.config.save();
	}

	/**
	 * Call the joinedMineplex() method in all registered mods
	 * 
	 * @see PlexModBase
	 */
	public static void joinedMineplex() {
		for (final PlexModBase mod : modules.values()) {
			mod.joinedMineplex();
		}
	}

	/**
	 * Call the leftMineplex() method in all registered mods
	 * 
	 * @see PlexModBase
	 */
	public static void leftMineplex() {
		for (final PlexModBase mod : modules.values()) {
			mod.leftMineplex();
		}
	}

	/**
	 * Displays a UI screen
	 * 
	 * @param screen Screen to display
	 */
	public static void displayUIScreen(PlexUIBase screen) {
		if (screen != null) {
			Plex.listeners.setTargetGuiScreen(new PlexUIModMenuScreen(screen));
		}
		else {
			Plex.listeners.setTargetGuiScreen(null);
		}
	}

	/**
	 * Gets the current player's username
	 * 
	 * @return The player's IGN, or null if not in-game
	 */
	public static String getPlayerIGN() {
		return Plex.minecraft.getSession().getUsername();
	}

	/**
	 * Gets a list of players' usernames in the world. Equivalent to
	 * getPlayerIGNList(false).
	 * 
	 * @return List of players' usernames in the current world
	 */
	public static List<String> getPlayerIGNList() {
		return getPlayerIGNList(false);
	}

	/**
	 * Gets the list of players in the world, optionally lowercasing all names
	 * 
	 * @param lowercase Make all player names lowercase
	 * @return The list of names, or null if not in a world
	 */
	public static List<String> getPlayerIGNList(boolean lowercase) {
		try {
			List<String> result = new ArrayList<String>();
			for (EntityPlayer player : Plex.minecraft.theWorld.playerEntities) {
				result.add(lowercase ? player.getName().toLowerCase() : player.getName());
			}
			return result;
		} 
		catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Gets a list of players' usernames in the tablist. Equivalent to
	 * getPlayerIGNTabList(false).
	 *
	 * @return List of players' usernames in the current world
	 */
	public static List<String> getPlayerIGNTabList() {
		return getPlayerIGNTabList(false);
	}

	/**
	 * Gets the list of players in the tablist, optionally lowercasing all names
	 *
	 * @param lowercase Make all player names lowercase
	 * @return The list of names, or null if not in a world
	 */
	public static List<String> getPlayerIGNTabList(boolean lowercase) {
		List<String> playerNameList = new ArrayList<>();
		for (NetworkPlayerInfo player : Plex.minecraft.thePlayer.sendQueue.getPlayerInfoMap()) {
			String name = player.getGameProfile().getName();
			if (!name.matches("^[a-zA-Z0-9_]{1,20}$")) {
				continue;
			}
			playerNameList.add(lowercase ? name.toLowerCase() : name);
		}
		return playerNameList;
	}


	/**
	 * Dispatches to all mods that the lobby has been updated
	 * 
	 * @param lobbyType       Type of the lobby or LobbyType.E_LOBBY_SWITCH for indiciating exactly when a change occurs.
	 */
	public static void dispatchLobbyChanged(PlexMPLobby.LobbyType lobbyType) {
		for (PlexModBase mod : modules.values()) {
			mod.lobbyUpdated(lobbyType);
		}		
	}

	public static class PlexUITab {
		private Class<? extends PlexUIBase> guiClass;
		private String label;
		private int id;

		public PlexUITab(Class<? extends PlexUIBase> uiClass, String label) {
			this.guiClass = uiClass;
			this.label = label;
		}

		public Class<? extends PlexUIBase> getGuiClass() {
			return this.guiClass;
		}

		public String getLabel() {
			return this.label;
		}

		public int getID() {
			return this.id;
		}

		public PlexUITab setID(int id) {
			this.id = id;
			return this;
		}

		public PlexUITab getShallowCopy() {
			return new PlexUITab(this.guiClass, this.label);
		}
	}
}