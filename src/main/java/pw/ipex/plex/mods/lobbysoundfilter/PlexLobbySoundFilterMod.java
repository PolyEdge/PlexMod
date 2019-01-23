package pw.ipex.plex.mods.lobbysoundfilter;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ci.PlexCommandListener;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.mineplex.PlexCoreLobbyType;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.core.PlexCoreValue;
import pw.ipex.plex.mod.PlexModBase;
import pw.ipex.plex.mods.chatmod.PlexHideStreamCommand;

public class PlexLobbySoundFilterMod extends PlexModBase {
	public static Integer MAX_LOBBY_FILTRATION = 5;

	public PlexCoreValue lobbyFiltrationLevel = new PlexCoreValue("lobbySoundFilter_lobbyFiltrationLevel", 0);
	public PlexCoreValue maxSoundRadius = new PlexCoreValue("lobbySoundFilter_maxSoundRadius", 0);
	public PlexCoreValue minSoundInRadiusLevel = new PlexCoreValue("lobbySoundFilter_minSoundInRadiusLevel", 0);
	
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
		
		
			
		this.lobbyFiltrationLevel.set(this.modSetting("lobby_filtration_level", 0).getInt());
		this.maxSoundRadius.set(this.modSetting("sounds_allowed_within", 0).getInt());
		this.minSoundInRadiusLevel.set(this.modSetting("levels_allowed_within", 0).getInt());

		PlexCore.registerCommandListener(new PlexCommandListener("ts"));
		PlexCore.registerCommandHandler("ts", new PlexHideStreamCommand());
		Plex.plexCommand.registerPlexCommand("sounds", new PlexHideStreamCommand());
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
		Float soundPitch = e.sound.getPitch();
		Double soundDistance = Math.sqrt(Math.pow((e.sound.getXPosF() - Plex.minecraft.thePlayer.posX), 2) + Math.pow((e.sound.getYPosF() - Plex.minecraft.thePlayer.posY), 2) + Math.pow((e.sound.getZPosF() - Plex.minecraft.thePlayer.posZ), 2));
		PlexCoreUtils.chatAddMessage(soundName + ": " + soundPitch + " d=" + soundDistance);
		if ((soundDistance <= maxSoundRadius.integerValue) && (maxSoundRadius.integerValue != 0)) {
			return;
		}
		if (lobbyFiltrationLevel.integerValue >= 1) {

		}
	}
	
	@Override
	public void saveModConfig() {
		this.modSetting("lobby_filtration_level", 0).set(this.lobbyFiltrationLevel.integerValue);
		this.modSetting("sounds_allowed_within", 0).set(this.maxSoundRadius.integerValue);
		this.modSetting("levels_allowed_within", 0).set(this.minSoundInRadiusLevel.integerValue);
	}

	@Override
	public void lobbyUpdated(PlexCoreLobbyType type) {
	}
}
