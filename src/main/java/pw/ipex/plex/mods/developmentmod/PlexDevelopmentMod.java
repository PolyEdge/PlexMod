package pw.ipex.plex.mods.developmentmod;

import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.PlexCoreValue;
import pw.ipex.plex.mod.PlexModBase;

public class PlexDevelopmentMod extends PlexModBase {
	
	public PlexCoreValue chatStream = new PlexCoreValue("_plexDev_chatStream", false);
	public PlexCoreValue soundStream = new PlexCoreValue("_plexDev_soundStream", false);

	@Override
	public String getModName() {
		return "plex_development_mod";
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
		if (!PlexCoreUtils.isChatMessage(e.type)) {
			return;
		}
		if (!chatStream.booleanValue) {
			return;
		}
		Plex.logger.info(IChatComponent.Serializer.componentToJson(e.message));
		String filtered = PlexCoreUtils.condenseChatAmpersandFilter(e.message.getFormattedText());
		PlexCoreUtils.chatAddMessage("[plexDev] chat: " + filtered);
	}
	
	@SubscribeEvent 
	public void onSound(final PlaySoundEvent e) {
		if (!soundStream.booleanValue) {
			return;
		}
		if (e.name.contains("step") || e.name.contains("rain")) {
			return;
		}
		PlexCoreUtils.chatAddMessage("[plexDev] sound: " + e.name + ": " + e.sound.getPitch());
	}
	
	@Override
	public void joinedMineplex() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void leftMineplex() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@Override
	public void switchedLobby(String name) {
		// TODO Auto-generated method stub
		
	}

}
