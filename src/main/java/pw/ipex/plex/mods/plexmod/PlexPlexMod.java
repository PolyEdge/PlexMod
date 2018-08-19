package pw.ipex.plex.mods.plexmod;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import net.minecraft.util.ResourceLocation;
import pw.ipex.plex.Plex;
import pw.ipex.plex.ci.PlexCommandListener;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreTextures;
//import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mod.PlexModBase;

public class PlexPlexMod extends PlexModBase {
	public static Map<String, String> socialMediaLinks = new ConcurrentHashMap<String, String>();
	public static Map<String, ResourceLocation> socialMediaRenderInformation = new LinkedHashMap<String, ResourceLocation>();
	public static Map<String, String> socialMediaLinkMapping = new HashMap<String, String>();

	@Override
	public String getModName() {
		return "Plex";
	}
	
	@Override
	public void modInit() {
		PlexCore.registerCommandListener(new PlexCommandListener("plex").setGlobal(true));
		PlexCore.registerCommandHandler("plex", Plex.plexCommand);
		
		PlexCore.registerUiTab("Plex", PlexPlexUI.class);
		
		socialMediaRenderInformation.put("namemc", PlexCoreTextures.GUI_SM_NAMEMC_ICON);
		socialMediaRenderInformation.put("twitter", PlexCoreTextures.GUI_SM_TWITTER_ICON);
		socialMediaRenderInformation.put("discord_server", PlexCoreTextures.GUI_SM_DISCORD_ICON);
		
		socialMediaLinkMapping.put("namemc", "https://namemc.com");
		socialMediaLinkMapping.put("twitter", "https://twitter.com");
		socialMediaLinkMapping.put("discord_server", "https://discord.gg");

		new Thread(new Runnable() {
			public void run() {
				try {
					URL socialMediaUrl = new URL("https://raw.githubusercontent.com/PolyEdge/PlexMod/master/social_media.json");
					InputStreamReader socialMediaInput = new InputStreamReader(socialMediaUrl.openStream());
					JsonReader jsonReader = new JsonReader(socialMediaInput);
					JsonParser jsonParser = new com.google.gson.JsonParser();
					JsonElement apiResponse = jsonParser.parse(jsonReader);
					socialMediaInput.close();
					for (Map.Entry<String, JsonElement> object : apiResponse.getAsJsonObject().entrySet()) {
						try {
							socialMediaLinks.put(object.getKey(), object.getValue().getAsString());
						}
						catch (Throwable e) {}
					}
				}
				catch (Throwable e) {
					Plex.logger.info("[PlexPlexMod] exception getting social media links");
					Plex.logger.info(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
				}
			}
		}).start();		
	}

	@Override
	public void saveModConfig() {
	}

	@Override
	public void joinedMineplex() {
	}

	@Override
	public void leftMineplex() {
	}

	@Override
	public void switchedLobby(String name) {
	}

}
