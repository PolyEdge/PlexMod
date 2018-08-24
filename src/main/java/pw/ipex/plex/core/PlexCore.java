package pw.ipex.plex.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ci.PlexCommandHandler;
import pw.ipex.plex.ci.PlexCommandListener;
import pw.ipex.plex.mod.PlexModBase;
import pw.ipex.plex.ui.PlexUIBase;

/**
 * The core utility class for PlexMod. Use the static methods in this class to
 * register the various aspects of your add-on mod.
 * 
 * @since 1.0
 */
public class PlexCore {
	public static Map<String, PlexModBase> plexMods = new HashMap<String, PlexModBase>();
	public static Map<String, PlexCommandHandler> commandHandlerNamespace = new HashMap<String, PlexCommandHandler>();
	public static Map<String, PlexCommandListener> commandListenerNamespace = new HashMap<String, PlexCommandListener>();
	public static List<java.util.Map.Entry<String, Class<? extends PlexUIBase>>> uiTabList = new ArrayList<java.util.Map.Entry<String, Class<? extends PlexUIBase>>>();
	public static Map<String, PlexCoreValue> sharedValues = new HashMap<String, PlexCoreValue>();

	private PlexCore() {
	}

	/**
	 * Registers a mod
	 * 
	 * @param mod The mod to register
	 */
	public static void registerMod(PlexModBase mod) {
		plexMods.put(mod.getModName(), mod);
		mod.modInit();
		PlexCore.saveAllConfig();
	}

	/**
	 * Returns a mod
	 * 
	 * @param modName The name of the mod as returned by getModName()
	 * @return The mod if it exists, null otherwise
	 */
	public static PlexModBase getMod(String modName) {
		if (plexMods.containsKey(modName)) {
			return plexMods.get(modName);
		}
		return null;
	}

	/**
	 * Registers a command handler
	 * 
	 * @param namespace The name to register the command handler under
	 * @param handler   The command handler
	 * @see PlexCommandHandler
	 */
	public static void registerCommandHandler(String namespace, PlexCommandHandler handler) {
		commandHandlerNamespace.put(namespace, handler);
	}

	/**
	 * Gets a command handler
	 * 
	 * @param namespace Name of the command handler
	 * @return The command handler
	 */
	public static PlexCommandHandler getCommandHandler(String namespace) {
		if (commandHandlerNamespace.containsKey(namespace)) {
			return commandHandlerNamespace.get(namespace);
		}
		return null;
	}

	/**
	 * Registers a listener
	 * 
	 * @param listener The listener to register
	 */
	public static void registerCommandListener(PlexCommandListener listener) {
		commandListenerNamespace.put(listener.getCommandName(), listener);
		ClientCommandHandler.instance.registerCommand(listener);
	}

	/**
	 * Returns a specified command listener
	 * 
	 * @param name The name of the listener
	 * @return The listener if it exists, null otherwise
	 */
	public static PlexCommandListener getCommandListener(String name) {
		if (commandListenerNamespace.containsKey(name)) {
			return commandListenerNamespace.get(name);
		}
		return null;
	}

	/**
	 * Registers a shared value
	 * 
	 * @param value Shared value to register
	 */
	public static void registerSharedValue(PlexCoreValue value) {
		sharedValues.put(value.name, value);
	}

	/**
	 * Returns a specified shared value
	 * 
	 * @param name The name of the shared value
	 * @return The value if it exists, null otherwise
	 */
	public static PlexCoreValue getSharedValue(String name) {
		if (sharedValues.containsKey(name)) {
			return sharedValues.get(name);
		}
		return null;
	}

	/**
	 * Registers a UI tab under the given title
	 * 
	 * @param name   Title of the UI tab
	 * @param class1 Class of the UI tab
	 */
	public static void registerUiTab(String name, Class<? extends PlexUIBase> class1) {
		uiTabList.add(new java.util.AbstractMap.SimpleEntry<String, Class<? extends PlexUIBase>>(name, class1));
	}

	/**
	 * Gets the UI tab registered under the given title
	 * 
	 * @param name Title of the tab
	 * @return The UI tab
	 */
	public static Class<? extends PlexUIBase> getUiTab(String name) {
		for (java.util.Map.Entry<String, Class<? extends PlexUIBase>> tab : uiTabList) {
			if (tab.getKey().equals(name)) {
				return tab.getValue();
			}
		}
		return null;
	}

	/**
	 * Gets the title of the UI tab registered at the given index
	 * 
	 * @param pos Index of the tab
	 * @return Title of the tab if it exists, null otherwise
	 * @see PlexUIBase
	 */
	public static String getUiTabTitleAt(Integer pos) {
		if (pos >= uiTabList.size()) {
			return null;
		}

		Entry<String, Class<? extends PlexUIBase>> tab = uiTabList.get(pos);
		if (tab != null) {
			return tab.getKey();
		} else {
			return null;
		}
	}

	/**
	 * Gets the UI tab class registered at the given index
	 * 
	 * @param pos Index of the tab
	 * @return UI tab class if it exists, null otherwise
	 */
	public static Class<? extends PlexUIBase> getUiTabClassAt(Integer pos) {
		if (!(pos < uiTabList.size())) {
			return null;
		}

		Entry<String, Class<? extends PlexUIBase>> tab = uiTabList.get(pos);
		if (tab != null) {
			return tab.getValue();
		} else {
			return null;
		}
	}

	/**
	 * Returns the UI tab list
	 * 
	 * @return the UI tab list
	 * @see PlexUIBase
	 */
	public static List<java.util.Map.Entry<String, Class<? extends PlexUIBase>>> getUiTabList() {
		return uiTabList;
	}

	/**
	 * Save all mod config files
	 */
	public static void saveAllConfig() {
		for (PlexModBase mod : plexMods.values()) {
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
		for (final PlexModBase mod : plexMods.values()) {
			// new Timer().schedule(new TimerTask() {
			// public void run() {
			mod.joinedMineplex();
			// }
			// }, 0L);
		}
	}

	/**
	 * Call the leftMineplex() method in all registered mods
	 * 
	 * @see PlexModBase
	 */
	public static void leftMineplex() {
		for (final PlexModBase mod : plexMods.values()) {
			// new Timer().schedule(new TimerTask() {
			// public void run() {
			mod.leftMineplex();
			// }
			// }, 0L);
		}
	}

	/**
	 * Displays a UI screen
	 * 
	 * @param screen Screen to display
	 */
	public static void displayUIScreen(PlexUIBase screen) {
		if (screen == null) {
			Plex.plexListeners.resetUI = true;
		}
		Plex.plexListeners.targetUI = screen;
	}

	/**
	 * Gets the current player's username
	 * 
	 * @return The player's IGN, or null if not in-game
	 */
	public static String getPlayerIGN() {
		try {
			return Plex.minecraft.thePlayer.getDisplayNameString();
		} catch (NullPointerException e) {
			return null;
		}
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
	public static List<String> getPlayerIGNList(Boolean lowercase) {
		try {
			List<String> result = new ArrayList<String>();
			for (EntityPlayer player : Plex.minecraft.theWorld.playerEntities) {
				if (lowercase)
					result.add(player.getName().toLowerCase());
				else
					result.add(player.getName());
			}
			return result;
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Update the lobby type
	 * 
	 * @param lobbyType       Type of the new lobby
	 * @param dispatchChanged Send lobby update message to all {@link PlexModBase}s
	 */
	public static void updateLobby(String lobbyType, Boolean dispatchChanged) {
		if (lobbyType != Plex.currentLobbyType) {
			Plex.currentLobbyType = lobbyType;
		}
		if (dispatchChanged) {
			for (PlexModBase mod : plexMods.values()) {
				mod.switchedLobby(lobbyType);
			}
		}
	}

	/**
	 * Gets the current lobby type
	 * 
	 * @return The current lobby type
	 */
	public static String getCurrentLobbyType() {
		return Plex.currentLobbyType;
	}

	/**
	 * Sets the server name
	 * 
	 * @param serverName The new server name
	 */
	public static void updateServerName(String serverName) {
		Plex.currentLobbyName = serverName;
		for (PlexModBase mod : plexMods.values()) {
			mod.serverNameChanged(serverName);
		}
	}

	/**
	 * Gets the server name
	 * 
	 * @return The server name
	 */
	public static String getServerName() {
		return Plex.currentLobbyName;
	}

	/**
	 * Sets the game name
	 * 
	 * @param gameName The new game name
	 */
	public static void updateGameName(String gameName) {
		Plex.currentGameName = gameName;
	}

	/**
	 * Gets the game name
	 * 
	 * @return The current game's name
	 */
	public static String getGameName() {
		return Plex.currentGameName;
	}

	/**
	 * Handles tab completion
	 * 
	 * @param sender - command sender
	 * @param args   - command arguments
	 * @param pos    - block position
	 * @return
	 */
	public static List<String> commandTabCompletion(ICommandSender sender, String[] args, BlockPos pos) {
		String namespace = PlexCommandHandler.getCommandNamespace(args);
		String[] commandArgs = PlexCommandHandler.getCommandArgs(args);
		if (!commandHandlerNamespace.containsKey(namespace)) {
			return Collections.emptyList();
		}
		PlexCommandHandler commandObj = commandHandlerNamespace.get(namespace);
		if ((!commandObj.allowUnsupportedServers()) && (!Plex.onMineplex)) {
			return Collections.emptyList();
		}
		return commandHandlerNamespace.get(namespace).tabCompletion(sender, namespace, commandArgs, pos);
	}

	public static void processModCommand(ICommandSender sender, String[] args) throws CommandException {
		String namespace = PlexCommandHandler.getCommandNamespace(args);
		String[] commandArgs = PlexCommandHandler.getCommandArgs(args);
		if (!commandHandlerNamespace.containsKey(namespace)) {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.getUiChatMessage("plex.nullCommandGlobal"));
			return;
		}
		PlexCommandHandler commandObj = commandHandlerNamespace.get(namespace);
		if ((!commandObj.allowUnsupportedServers()) && (!Plex.onMineplex)) {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.getUiChatMessage("notOnMineplex"));
			return;
		}
		commandHandlerNamespace.get(namespace).processCommand(sender, namespace, commandArgs);
	}
}