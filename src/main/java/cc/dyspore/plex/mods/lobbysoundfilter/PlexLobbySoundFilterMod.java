package cc.dyspore.plex.mods.lobbysoundfilter;

import java.util.ArrayList;
import java.util.List;

import cc.dyspore.plex.core.util.PlexUtilChat;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.PlexModBase;
import cc.dyspore.plex.mods.chatmod.PlexChatStreamCommand;

public class PlexLobbySoundFilterMod extends PlexModBase {
	public static Integer MAX_LOBBY_FILTRATION = 5;

	public int lobbyFiltrationLevel = 0;
	public int maxSoundRadius = 0;
	public int minSoundInRadiusLevel = 0;
	
	public List<String> slTreasureChest = new ArrayList<String>();
	public List<ArrayList<String>> slLobbySounds = new ArrayList<ArrayList<String>>();

	@Override
	public String getModName() {
		return "Lobby Sound Filter";
	}
	
	@Override
	public void modInit() {
		addSound(1, "fire.fire");        // annoying particle effects, i bet u can agree
		addSound(1, "mob.cat.purreow");
		addSound(1, "mob.zombie.remedy");
		
		addSound(2, "mob.enderdragon.wings");
		addSound(2, "mob.enderdragon.growl");
		addSound(2, "mob.wither.spawn");
		
		
			
		this.lobbyFiltrationLevel = this.configValue("lobby_filtration_level", 0).getInt();
		this.maxSoundRadius = this.configValue("sounds_allowed_within", 0).getInt();
		this.minSoundInRadiusLevel = this.configValue("levels_allowed_within", 0).getInt();

		//PlexCore.registerCommandListener(new PlexClientCommandListener("ts"));
		//PlexCore.registerCommandHandler("ts", new PlexChatStreamCommand());
		Plex.plexCommand.registerPlexCommand("sounds", new PlexChatStreamCommand());
	}
	
	public void addSound(Integer level, String sound) {
		while (level >= slLobbySounds.size()) {
			slLobbySounds.add(new ArrayList<String>());
		}
		slLobbySounds.get(level).add(sound);
	}
	
	public List<String> getSoundList(Integer level) {
		if (level >= slLobbySounds.size()) {
			slLobbySounds.add(new ArrayList<String>());
		}
		return slLobbySounds.get(level);
	}
	
	@SubscribeEvent
	public void onSound(final PlaySoundEvent e) {
		String soundName = e.name;
		float soundPitch = e.sound.getPitch();
		double soundDistance = Math.sqrt(Math.pow((e.sound.getXPosF() - Plex.minecraft.thePlayer.posX), 2) + Math.pow((e.sound.getYPosF() - Plex.minecraft.thePlayer.posY), 2) + Math.pow((e.sound.getZPosF() - Plex.minecraft.thePlayer.posZ), 2));
		PlexUtilChat.chatAddMessage(soundName + ": " + soundPitch + " d=" + soundDistance);
		if ((soundDistance <= maxSoundRadius) && (maxSoundRadius != 0)) {
			return;
		}
		if (lobbyFiltrationLevel >= 1) {

		}
	}
	
	@Override
	public void saveConfig() {
		this.configValue("lobby_filtration_level", 0).set(this.lobbyFiltrationLevel);
		this.configValue("sounds_allowed_within", 0).set(this.maxSoundRadius);
		this.configValue("levels_allowed_within", 0).set(this.minSoundInRadiusLevel);
	}
}
