package pw.ipex.plex.core;

import java.util.Map;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * @since 1.0
 */
public class PlexCore {
	public static Map<String, PlexModBase> plexMods = new HashMap<String, PlexModBase>();
	public static Map<String, PlexCommandHandler> commandHandlerNamespace = new HashMap<String, PlexCommandHandler>();
	public static Map<String, PlexCommandListener> commandListenerNamespace = new HashMap<String, PlexCommandListener>();
	public static List<java.util.Map.Entry<String, Class<? extends PlexUIBase>>> uiTabList = new ArrayList<java.util.Map.Entry<String, Class<? extends PlexUIBase>>>();
	public static Map<String, PlexCoreValue> sharedValues = new HashMap<String, PlexCoreValue>();


    /**
     * Registers a mod
     * @param mod - the mod to register
     * @see PlexModBase
     */
	public static void registerMod(PlexModBase mod) {
		plexMods.put(mod.getModName(), mod);
		mod.modInit();
		PlexCore.saveAllConfig();
	}


    /**
     * Returns a mod
     * @param modName - mod looking for
     * @return the mod if exists
     * @see PlexModBase
     */
	public static PlexModBase getMod(String modName) {
		if (plexMods.containsKey(modName)) {
			return plexMods.get(modName);
		}
		return null;
	}


    /**
     * Registers a command handler
     * @param namespace - key to the command handler
     * @param handler - command handler
     * @see PlexCommandHandler
     */
	public static void registerCommandHandler(String namespace, PlexCommandHandler handler) {
		commandHandlerNamespace.put(namespace, handler);
	}

    /**
     * Gets a command handler
     * @param namespace - key to the command handler
     * @return the command handler
     * @see PlexCommandHandler
     */
	public static PlexCommandHandler getCommandHandler(String namespace) {
		if (commandHandlerNamespace.containsKey(namespace)) {
			return commandHandlerNamespace.get(namespace);
		}
		return null;
	}

    /**
     * Registers a listener
     * @param listener - listener to register
     * @see PlexCommandListener
     */
	public static void registerCommandListener(PlexCommandListener listener) {
		commandListenerNamespace.put(listener.getCommandName(), listener);
		ClientCommandHandler.instance.registerCommand(listener);
	}

    /**
     * Returns a specified command listener
     * @param name - name of the listener
     * @return the listener is exists
     * @see PlexCommandListener
     */
	public static PlexCommandListener getCommandListener(String name) {
		if (commandListenerNamespace.containsKey(name)) {
			return commandListenerNamespace.get(name);
		}
		return null;
	}

    /**
     * Registers a shared value
     * @param value - shared value to register
     * @see PlexCoreValue
     */
	public static void registerSharedValue(PlexCoreValue value) {
		sharedValues.put(value.name, value);
	}

    /**
     * Returns a specified shared value
     * @param name - name of the shared value
     * @return the value if exists
     * @see PlexCoreValue
     */
	public static PlexCoreValue getSharedValue(String name) {
		if (sharedValues.containsKey(name)) {
			return sharedValues.get(name);
		}
		return null;
	}

    /**
     * Register a UI tab
     * @param name - Name of the UI tab (usually the mod)
     * @param class1 - Class to register the UI tab for
     * @see PlexUIBase
     */
	public static void registerUiTab(String name, Class<? extends PlexUIBase> class1) {
		uiTabList.add(new java.util.AbstractMap.SimpleEntry<String, Class<? extends PlexUIBase>>(name, class1));
	}


    /**
     * Get UI tab class if exists
     * @param name - name of the UI tab
     * @return the UI tab is exists
     * @see PlexUIBase
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
     * Get the UI class name at position X
     * @param pos - get name of UI tab at pos X
     * @return the name of UI tab if exists
     * @see PlexUIBase
     */
	public static String getUiTabTitleAt(Integer pos) {
		if (!(pos < uiTabList.size())) {
			return null;
		}
		return uiTabList.get(pos).getKey();
	}

    /**
     * Gets the UI class at position X
     * @param pos - Get class of UI tab at pos X
     * @return class if exists
     * @see PlexUIBase
     */
	public static Class<? extends PlexUIBase> getUiTabClassAt(Integer pos) {
		if (!(pos < uiTabList.size())) {
			return null;
		}
		return uiTabList.get(pos).getValue();
	}

    /**
     * Returns the UI tab list
     * @return the UI tab list
     * @see PlexUIBase
     */
	public static List<java.util.Map.Entry<String, Class<? extends PlexUIBase>>> getUiTabList() {
		return uiTabList;
	}

    /**
     * Save the config
     */
	public static void saveAllConfig() {
		for (PlexModBase mod : plexMods.values()) {
			mod.saveModConfig();
		}
		Plex.config.save();
	}

    /**
     * Call the joinedMineplex() method in all registered mods
     * @see PlexModBase
     */
	public static void joinedMineplex() {
		for (final PlexModBase mod : plexMods.values()) {
	        //new Timer().schedule(new TimerTask() {
	        //    public void run() {
			mod.joinedMineplex();    	
	        //    }
	        //}, 0L);
		}
	}

    /**
     * Call the leftMineplex() method in all registered mods
     * @see PlexModBase
     */
	public static void leftMineplex() {
		for (final PlexModBase mod : plexMods.values()) {
	    //    new Timer().schedule(new TimerTask() {
	    //        public void run() {
			mod.leftMineplex();
	    //        }
	    //    }, 0L);
		}
	}

    /**
     * Displays a UI screen
     * @param screen - screen to display
     * @see PlexUIBase
     */
	public static void displayUIScreen(PlexUIBase screen) {
		if (screen == null) {
			Plex.plexListeners.resetUI = true;
		}
		Plex.plexListeners.targetUI = screen;
	}

    /**
     * Get the player IGN
     * @return the players IGN if possible
     */
	public static String getPlayerIGN() {
		try {
			return Plex.minecraft.thePlayer.getDisplayNameString();
		}
		catch (NullPointerException e) {
			return null;
		}
	}

    /**
     * Get a list of players in the world
     * @return list of players in the world
     */
	public static List<String> getPlayerIGNList() {
		return getPlayerIGNList(false);
	}

    /**
     * Get a list players in world
     * @param lowercase - are the names all lowercase
     * @return the list of names
     */
	public static List<String> getPlayerIGNList(Boolean lowercase) {
		try {
			List<String> result = new ArrayList<String>();
			for (EntityPlayer player : Plex.minecraft.theWorld.playerEntities) {
                if(lowercase)result.add(player.getName().toLowerCase());
                else result.add(player.getName());
			}
			return result;
		}
		catch (NullPointerException e) {
			return null;
		}
	}

    /**
     * Update the lobby type
     * @param lobbyType - type of the new lobby
     * @param dispatchChanged - update the mods
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
     * Get the current lobby type
     * @return the current lobby type
     */
	public static String getCurrentLobbyType() {
		return Plex.currentLobbyType;
	}

    /**
     * Set the new server name
     * @param serverName - new server name
     */
	public static void updateServerName(String serverName) {
		Plex.currentLobbyName = serverName;
		for (PlexModBase mod : plexMods.values()) {
			mod.serverNameChanged(serverName);
		}		
	}

    /**
     * Get the server name
     * @return server name
     */
	public static String getServerName() {
		return Plex.currentLobbyName;
	}

    /**
     * Set the game name
     * @param gameName - new game name
     */
	public static void updateGameName(String gameName) {
		Plex.currentGameName = gameName;
	}

    /**
     * Get the game name
     * @return the game name
     */
	public static String getGameName() {
		return Plex.currentGameName;
	}


    /**
     * Handles tab completion
     * @param sender - command sender
     * @param args - command arguments
     * @param pos - block position
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