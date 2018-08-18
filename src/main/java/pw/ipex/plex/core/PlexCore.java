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

public class PlexCore {
	public static Map<String, PlexModBase> plexMods = new HashMap<String, PlexModBase>();
	public static Map<String, PlexCommandHandler> commandHandlerNamespace = new HashMap<String, PlexCommandHandler>();
	public static Map<String, PlexCommandListener> commandListenerNamespace = new HashMap<String, PlexCommandListener>();
	public static List<java.util.Map.Entry<String, Class<? extends PlexUIBase>>> uiTabList = new ArrayList<java.util.Map.Entry<String, Class<? extends PlexUIBase>>>();
	public static Map<String, PlexCoreValue> sharedValues = new HashMap<String, PlexCoreValue>();
	
	public static void registerMod(PlexModBase mod) {
		plexMods.put(mod.getModName(), mod);
		mod.modInit();
		PlexCore.saveAllConfig();
	}
	
	public static PlexModBase getMod(String modName) {
		if (plexMods.containsKey(modName)) {
			return plexMods.get(modName);
		}
		return null;
	}
	
	public static void registerCommandHandler(String namespace, PlexCommandHandler handler) {
		commandHandlerNamespace.put(namespace, handler);
	}
	
	public static PlexCommandHandler getCommandHandler(String namespace) {
		if (commandHandlerNamespace.containsKey(namespace)) {
			return commandHandlerNamespace.get(namespace);
		}
		return null;
	}
	
	public static void registerCommandListener(PlexCommandListener listener) {
		commandListenerNamespace.put(listener.getCommandName(), listener);
		ClientCommandHandler.instance.registerCommand(listener);
	}
	
	public static PlexCommandListener getCommandListener(String name) {
		if (commandListenerNamespace.containsKey(name)) {
			return commandListenerNamespace.get(name);
		}
		return null;
	}
	
	public static void registerSharedValue(PlexCoreValue value) {
		sharedValues.put(value.name, value);
	}
	
	public static PlexCoreValue getSharedValue(String name) {
		if (sharedValues.containsKey(name)) {
			return sharedValues.get(name);
		}
		return null;
	}
	
	public static void registerUiTab(String name, Class<? extends PlexUIBase> class1) {
		uiTabList.add(new java.util.AbstractMap.SimpleEntry<String, Class<? extends PlexUIBase>>(name, class1));
	}
	
	public static Class<? extends PlexUIBase> getUiTab(String name) {
		for (java.util.Map.Entry<String, Class<? extends PlexUIBase>> tab : uiTabList) {
			if (tab.getKey().equals(name)) {
				return tab.getValue();
			}
		}
		return null;		
	}
	
	public static String getUiTabTitleAt(Integer pos) {
		if (!(pos < uiTabList.size())) {
			return null;
		}
		return uiTabList.get(pos).getKey();
	}
	
	public static Class<? extends PlexUIBase> getUiTabClassAt(Integer pos) {
		if (!(pos < uiTabList.size())) {
			return null;
		}
		return uiTabList.get(pos).getValue();
	}
	
	public static List<java.util.Map.Entry<String, Class<? extends PlexUIBase>>> getUiTabList() {
		return uiTabList;
	}
	
	public static void saveAllConfig() {
		for (PlexModBase mod : plexMods.values()) {
			mod.saveModConfig();
		}
		Plex.config.save();
	}
	
	public static void joinedMineplex() {
		for (final PlexModBase mod : plexMods.values()) {
	        //new Timer().schedule(new TimerTask() {
	        //    public void run() {
			mod.joinedMineplex();    	
	        //    }
	        //}, 0L);
		}
	}
	
	public static void leftMineplex() {
		for (final PlexModBase mod : plexMods.values()) {
	    //    new Timer().schedule(new TimerTask() {
	    //        public void run() {
			mod.leftMineplex();
	    //        }
	    //    }, 0L);
		}
	}
	
	public static void displayUIScreen(PlexUIBase screen) {
		if (screen == null) {
			Plex.plexListeners.resetUI = true;
		}
		Plex.plexListeners.targetUI = screen;
	}
	
	public static String getPlayerIGN() {
		try {
			return Plex.minecraft.thePlayer.getDisplayNameString();
		}
		catch (NullPointerException e) {
			return null;
		}
	}
	
	public static List<String> getPlayerIGNList() {
		return getPlayerIGNList(false);
	}
	
	public static List<String> getPlayerIGNList(Boolean lowercase) {
		try {
			List<String> result = new ArrayList<String>();
			for (EntityPlayer player : Plex.minecraft.theWorld.playerEntities) {
				if (lowercase) {
					result.add(player.getName().toLowerCase());
				}
				else {
					result.add(player.getName());
				}
			}
			return result;
		}
		catch (NullPointerException e) {
			return null;
		}
	}
	
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
	
	public static String getCurrentLobbyType() {
		return Plex.currentLobbyType;
	}
	
	public static void updateServerName(String serverName) {
		Plex.currentLobbyName = serverName;
		for (PlexModBase mod : plexMods.values()) {
			mod.serverNameChanged(serverName);
		}		
	}
	
	public static String getServerName() {
		return Plex.currentLobbyName;
	}
	
	public static void updateGameName(String gameName) {
		Plex.currentGameName = gameName;
	}
	
	public static String getGameName() {
		return Plex.currentGameName;
	}
	
	
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