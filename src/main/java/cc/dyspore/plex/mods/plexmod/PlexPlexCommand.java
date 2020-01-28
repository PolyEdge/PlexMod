package cc.dyspore.plex.mods.plexmod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.commands.client.PlexCommandHandler;
import cc.dyspore.plex.commands.client.PlexCommandListener;
import cc.dyspore.plex.core.PlexCore;

public class PlexPlexCommand extends PlexCommandHandler {
	public Map<String, PlexCommandHandler> plexNamespace = new HashMap<String, PlexCommandHandler>();
	public List<String> plexHelpMenu = new ArrayList<String>();
	
	public void registerPlexCommand(String namespace, PlexCommandHandler handler) {
		plexNamespace.put(namespace, handler);
	}
	
	public PlexCommandHandler getPlexCommand(String namespace) {
		if (plexNamespace.containsKey(namespace)) {
			return plexNamespace.get(namespace);
		}
		return null;
	}
	
	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return new ArrayList<>();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		if (args.length == 0) {
			PlexCore.displayUIScreen(new PlexPlexUI());
		}
		else if (args[0].equalsIgnoreCase("info")) {
			PlexUtilChat.chatAddMessage(PlexUtilChat.getUiChatMessage("plex.modInfo"));
		}
		else if (!Plex.gameState.isMineplex) {
			PlexUtilChat.chatAddMessage(PlexUtilChat.getUiChatMessage("plex.unsupportedServer"));
		}
		else if (plexNamespace.containsKey(args[0])) {
			plexNamespace.get(args[0]).processCommand(sender, "plex." + PlexCommandListener.getCommandNamespace(args), PlexCommandListener.getCommandArgs(args));
		}
		else if (args[0].equalsIgnoreCase("help")) {
			displayHelpMenu();
		}
		else {
			PlexUtilChat.chatAddMessage(PlexUtilChat.getUiChatMessage("plex.nullModCommand"));
		}
	}
	
	public void addPlexHelpCommand(String name, String ...aliasInformation) {
		String information = aliasInformation[(aliasInformation.length - 1)];
		List<String> alias = Arrays.asList(aliasInformation).subList(0, (aliasInformation.length - 1));
		String item = PlexUtilChat.chatStyleText("DARK_GRAY", "> ") + PlexUtilChat.chatStyleText("GOLD", "/plex " + name + "");
		String currentArgColour;
		int aliasColour = 0;
		int argColour = 0;
		for (String commandAlias : alias) {
			currentArgColour = argColour == 0 ? "DARK_GRAY" : "GOLD";
			if (commandAlias.startsWith("$")) {
				item = item + PlexUtilChat.chatStyleText(currentArgColour, " (") + PlexUtilChat.chatStyleText("GRAY", commandAlias.substring(1, commandAlias.length())) + PlexUtilChat.chatStyleText(currentArgColour, ")");
				argColour = 1 - argColour;	
			}
			else if (commandAlias.startsWith("%")) {
				item = item + PlexUtilChat.chatStyleText(currentArgColour, " <") + PlexUtilChat.chatStyleText("GRAY", commandAlias.substring(1, commandAlias.length())) + PlexUtilChat.chatStyleText(currentArgColour, ">");
				argColour = 1 - argColour;	
			}
			else {
				item = item + PlexUtilChat.chatStyleText(aliasColour == 0 ? "GRAY" : "GOLD", " /" + commandAlias);
				aliasColour = 1 - aliasColour;				
			}

		}
		item = item + PlexUtilChat.chatStyleText("DARK_GRAY", "BOLD", " - ") + PlexUtilChat.chatStyleText("GRAY", information);
		plexHelpMenu.add(item);
	}
	
	public void addHelpCommand(String ...commandInformation) {
		String information = commandInformation[(commandInformation.length - 1)];
		List<String> alias = Arrays.asList(commandInformation).subList(0, (commandInformation.length - 1));
		String item = PlexUtilChat.chatStyleText("DARK_GRAY", ">");
		String currentArgColour;
		int aliasColour = 1;
		int argColour = 0;
		for (String commandAlias : alias) {
			currentArgColour = argColour == 0 ? "DARK_GRAY" : "GOLD";
			if (commandAlias.startsWith("$")) {
				item = item + PlexUtilChat.chatStyleText(currentArgColour, " (") + PlexUtilChat.chatStyleText("GRAY", commandAlias.substring(1, commandAlias.length())) + PlexUtilChat.chatStyleText(currentArgColour, ")");
				argColour = 1 - argColour;	
			}
			else if (commandAlias.startsWith("%")) {
				item = item + PlexUtilChat.chatStyleText(currentArgColour, " <") + PlexUtilChat.chatStyleText("GRAY", commandAlias.substring(1, commandAlias.length())) + PlexUtilChat.chatStyleText(currentArgColour, ">");
				argColour = 1 - argColour;	
			}
			else {
				item = item + PlexUtilChat.chatStyleText(aliasColour == 0 ? "GRAY" : "GOLD", " /" + commandAlias);
				aliasColour = 1 - aliasColour;
			}
		}
		item = item + PlexUtilChat.chatStyleText("DARK_GRAY", "BOLD", " - ") + PlexUtilChat.chatStyleText("GRAY", information);
		plexHelpMenu.add(item);	
	}
	
	public void displayHelpMenu() {
		PlexUtilChat.chatAddMessage(PlexUtilChat.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========") + PlexUtilChat.chatStyleText("GOLD", " Plex Help ") + PlexUtilChat.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========"));
		for (String helpLine : plexHelpMenu) {
			PlexUtilChat.chatAddMessage(helpLine);
		}
		PlexUtilChat.chatAddMessage(PlexUtilChat.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========================"));
	}
}
