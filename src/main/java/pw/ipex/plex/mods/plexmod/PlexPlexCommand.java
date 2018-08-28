package pw.ipex.plex.mods.plexmod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ci.PlexCommandHandler;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreUtils;

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
	public Boolean allowUnsupportedServers() {
		return true;
	}
	
	@Override
	public List<String> tabCompletion(ICommandSender sender, String namespace, String[] args, BlockPos pos) {
		return new ArrayList<String>();
	}

	@Override
	public void processCommand(ICommandSender sender, String namespace, String[] args) throws CommandException {
		if (args.length == 0) {
			PlexCore.displayUIScreen(new PlexPlexUI());
		}
		else if (args[0].equalsIgnoreCase("info")) {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.getUiChatMessage("plex.modInfo"));
		}
		else if (!Plex.serverState.onMineplex) {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.getUiChatMessage("plex.unsupportedServer"));
		}
		else if (plexNamespace.containsKey(args[0])) {
			plexNamespace.get(args[0]).processCommand(sender, "plex." + PlexCommandHandler.getCommandNamespace(args), PlexCommandHandler.getCommandArgs(args));
		}
		else if (args[0].equalsIgnoreCase("help")) {
			displayHelpMenu();
		}
		else {
			PlexCoreUtils.chatAddMessage(PlexCoreUtils.getUiChatMessage("plex.nullModCommand"));
		}
	}
	
	public void addPlexHelpCommand(String name, String ...aliasInformation) {
		String information = aliasInformation[(aliasInformation.length - 1)];
		List<String> alias = Arrays.asList(aliasInformation).subList(0, (aliasInformation.length - 1));
		String item = PlexCoreUtils.chatStyleText("DARK_GRAY", "> ") + PlexCoreUtils.chatStyleText("GOLD", "/plex " + name + "");
		String currentArgColour;
		Integer aliasColour = 0;
		Integer argColour = 0;
		for (String commandAlias : alias) {
			currentArgColour = argColour == 0 ? "DARK_GRAY" : "GOLD";
			if (commandAlias.startsWith("$")) {
				item = item + PlexCoreUtils.chatStyleText(currentArgColour, " (") + PlexCoreUtils.chatStyleText("GRAY", commandAlias.substring(1, commandAlias.length())) + PlexCoreUtils.chatStyleText(currentArgColour, ")");
				argColour = 1 - argColour;	
			}
			else if (commandAlias.startsWith("%")) {
				item = item + PlexCoreUtils.chatStyleText(currentArgColour, " <") + PlexCoreUtils.chatStyleText("GRAY", commandAlias.substring(1, commandAlias.length())) + PlexCoreUtils.chatStyleText(currentArgColour, ">");
				argColour = 1 - argColour;	
			}
			else {
				item = item + PlexCoreUtils.chatStyleText(aliasColour == 0 ? "GRAY" : "GOLD", " /" + commandAlias);
				aliasColour = 1 - aliasColour;				
			}

		}
		item = item + PlexCoreUtils.chatStyleText("DARK_GRAY", "BOLD", " - ") + PlexCoreUtils.chatStyleText("GRAY", information);
		plexHelpMenu.add(item);
	}
	
	public void addHelpCommand(String ...commandInformation) {
		String information = commandInformation[(commandInformation.length - 1)];
		List<String> alias = Arrays.asList(commandInformation).subList(0, (commandInformation.length - 1));
		String item = PlexCoreUtils.chatStyleText("DARK_GRAY", ">");
		String currentArgColour;
		Integer aliasColour = 1;
		Integer argColour = 0;
		for (String commandAlias : alias) {
			currentArgColour = argColour == 0 ? "DARK_GRAY" : "GOLD";
			if (commandAlias.startsWith("$")) {
				item = item + PlexCoreUtils.chatStyleText(currentArgColour, " (") + PlexCoreUtils.chatStyleText("GRAY", commandAlias.substring(1, commandAlias.length())) + PlexCoreUtils.chatStyleText(currentArgColour, ")");
				argColour = 1 - argColour;	
			}
			else if (commandAlias.startsWith("%")) {
				item = item + PlexCoreUtils.chatStyleText(currentArgColour, " <") + PlexCoreUtils.chatStyleText("GRAY", commandAlias.substring(1, commandAlias.length())) + PlexCoreUtils.chatStyleText(currentArgColour, ">");
				argColour = 1 - argColour;	
			}
			else {
				item = item + PlexCoreUtils.chatStyleText(aliasColour == 0 ? "GRAY" : "GOLD", " /" + commandAlias);
				aliasColour = 1 - aliasColour;
			}
		}
		item = item + PlexCoreUtils.chatStyleText("DARK_GRAY", "BOLD", " - ") + PlexCoreUtils.chatStyleText("GRAY", information);
		plexHelpMenu.add(item);	
	}
	
	public void displayHelpMenu() {
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========") + PlexCoreUtils.chatStyleText("GOLD", " Plex Help ") + PlexCoreUtils.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========"));
		for (String helpLine : plexHelpMenu) {
			PlexCoreUtils.chatAddMessage(helpLine);
		}
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GOLD", "STRIKETHROUGH", "BOLD", "========================"));
	}
}
