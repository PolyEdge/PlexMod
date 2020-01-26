package cc.dyspore.plex.mods.developmentmod;

import cc.dyspore.plex.core.mineplex.PlexLobbyType;
import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.regex.PlexCoreRegex;
import cc.dyspore.plex.core.regex.PlexCoreRegexEntry;
import cc.dyspore.plex.core.PlexModBase;

import java.util.Timer;
import java.util.TimerTask;

public class PlexDevelopmentMod extends PlexModBase {
	public boolean chatStream = false;
	public boolean chatMinify = true;
	public boolean soundStream = false;
	public boolean lobbySwitchStream = false;
	public boolean chatCharacterCodes = false;
	public boolean regexEntries = false;

	@Override
	public String getModName() {
		return "DevelopmentTools";
	}
	
	@Override
	public void modInit() {
		Plex.plexCommand.registerPlexCommand("_dev", new PlexDevelopmentCommand());
	}

	@Override
	public void saveModConfig() {
	}
	
	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent e) {
		if (!PlexUtilChat.chatIsMessage(e.type)) {
			return;
		}
		if (regexEntries) {
			StringBuilder builder = new StringBuilder();
			for (PlexCoreRegexEntry item : PlexCoreRegex.getEntriesMatchingText(e.message.getFormattedText())) {
				if (builder.length() != 0) {
					builder.append(", ");
				}
				builder.append(item.entryName);
			}
			if (builder.length() == 0) {
				builder.append("<null>");
			}
			PlexUtilChat.chatAddMessage(builder.toString());
		}
		if (!chatStream) {
			return;
		}
		Plex.logger.info(IChatComponent.Serializer.componentToJson(e.message));
		String filtered;
		if (this.chatMinify) {
			filtered = PlexUtilChat.chatCondenseAndAmpersand(e.message.getFormattedText());
		}
		else {
			filtered = PlexUtilChat.chatAmpersandFilter(e.message.getFormattedText());
		}
		PlexUtilChat.chatAddMessage(PlexUtilChat.chatStyleText("GOLD", "[plexdev chat]: ") + filtered);
		if (this.chatCharacterCodes) {
			StringBuilder output = new StringBuilder();
			output.append(PlexUtilChat.chatStyleText("GOLD", "[plexdev chat]: "));
			for (char i : PlexUtilChat.chatMinimalize(e.message.getFormattedText()).toCharArray()) {
				output.append((int)i);
				output.append(" ");
			}
			PlexUtilChat.chatAddMessage(output.toString());
		}
	}
	
	@SubscribeEvent 
	public void onSound(final PlaySoundEvent e) {
		if (!soundStream) {
			return;
		}
		if (e.name.contains("step") || e.name.contains("rain")) {
			return;
		}
		PlexUtilChat.chatAddMessage(PlexUtilChat.chatStyleText("GOLD", "[plexdev sound]: ") + e.name + ": " + e.sound.getPitch());
	}

	@Override
	public void lobbyUpdated(PlexLobbyType type) {
		if (!this.lobbySwitchStream) {
			return ;
		}
		String extra = "";
		if (type.equals(PlexLobbyType.E_GAME_UPDATED)) {
			extra += PlexUtilChat.chatStyleText("BLUE", "game -> " + Plex.gameState.currentLobby.currentGame.name);
		}
		final String finalExtra = extra;
		new Timer().schedule(new TimerTask() {
			public void run() {
				try {
					PlexUtilChat.chatAddMessage(PlexUtilChat.PLEX + PlexUtilChat.chatStyleText("GREEN", "lobby -> " + type.toString()) + " " + finalExtra);
				} catch (Throwable ee) {
				}
			}
		}, 2000L);
	}

}
