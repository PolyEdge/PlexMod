package pw.ipex.plex.mods.developmentmod;

import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.mineplex.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mod.PlexModBase;

import java.util.Timer;
import java.util.TimerTask;

public class PlexDevelopmentMod extends PlexModBase {
	public boolean chatStream = false;
	public boolean chatMinify = true;
	public boolean soundStream = false;
	public boolean lobbySwitchStream = false;

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
		if (!PlexCoreUtils.chatIsMessage(e.type)) {
			return;
		}
		if (!chatStream) {
			return;
		}
		Plex.logger.info(IChatComponent.Serializer.componentToJson(e.message));
		String filtered;
		if (this.chatMinify) {
			filtered = PlexCoreUtils.chatCondenseAndAmpersand(e.message.getFormattedText());
		}
		else {
			filtered = PlexCoreUtils.chatAmpersandFilter(e.message.getFormattedText());
		}
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GOLD", "[plexdev chat]: ") + filtered);
	}
	
	@SubscribeEvent 
	public void onSound(final PlaySoundEvent e) {
		if (!soundStream) {
			return;
		}
		if (e.name.contains("step") || e.name.contains("rain")) {
			return;
		}
		PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatStyleText("GOLD", "[plexdev sound]: ") + e.name + ": " + e.sound.getPitch());
	}

	@Override
	public void lobbyUpdated(PlexCoreLobbyType type) {
		if (!this.lobbySwitchStream) {
			return ;
		}
		String extra = "";
		if (type.equals(PlexCoreLobbyType.E_GAME_UPDATED)) {
			extra += PlexCoreUtils.chatStyleText("BLUE", "game -> " + PlexCore.getGameName());
		}
		final String finalExtra = extra;
		new Timer().schedule(new TimerTask() {
			public void run() {
				try {
					PlexCoreUtils.chatAddMessage(PlexCoreUtils.chatPlexPrefix() + PlexCoreUtils.chatStyleText("GREEN", "lobby -> " + type.toString()) + " " + finalExtra);
				} catch (Throwable ee) {
				}
			}
		}, 2000L);
	}

}
