package cc.dyspore.plex.mods.plexmod;

import java.io.IOException;
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
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.commands.client.PlexCommandListener;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.util.PlexUtilTextures;
//import PlexUtil;
import cc.dyspore.plex.core.PlexModBase;

public class PlexMod extends PlexModBase {
	public PlexCommandListener plexListener;
	public static Map<String, String> socialMediaLinks = new ConcurrentHashMap<>();
	public static Map<String, ResourceLocation> socialMediaRenderInformation = new LinkedHashMap<>();
	public static Map<String, String> socialMediaLinkMapping = new HashMap<>();

	@Override
	public String getModName() {
		return "Plex";
	}
	
	@Override
	public void modInit() {
		Plex.logger.info(":: PlexMod v" + Plex.VERSION + this.patchId(Plex.PATCHID) + " " + this.releaseNotice(Plex.RELEASENOTICE) + " (c) 2020 @Dyspore [dyspore.cc] ::");

		this.plexListener = new PlexCommandListener("plex").setAvailability(PlexCommandListener.Availability.GLOBAL);
		this.plexListener.setHandler(Plex.plexCommand);
		
		PlexCore.registerUiTab("Plex", PlexModUI.class);

		socialMediaRenderInformation.put("discord_server", PlexUtilTextures.GUI_SM_DISCORD_ICON);
		socialMediaRenderInformation.put("twitter", PlexUtilTextures.GUI_SM_TWITTER_ICON);
		socialMediaRenderInformation.put("namemc", PlexUtilTextures.GUI_SM_NAMEMC_ICON);

		socialMediaLinkMapping.put("discord_server", "https://discord.gg");
		socialMediaLinkMapping.put("twitter", "https://twitter.com");
		socialMediaLinkMapping.put("namemc", "https://namemc.com");

		new Thread(() -> {
			try {

			}
			catch (Throwable e) {
				Plex.logger.info("[PlexMod] exception getting social media links");
				Plex.logger.info(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
			}
		}).start();
	}

	public String patchId(String patchId) {
		return (patchId == null ? "" : "-" + patchId);
	}

	public String releaseNotice(String releaseNotice) {
		return (releaseNotice == null ? "" : " [" + releaseNotice + "]");
	}

	public void loadSocialMedia() throws IOException {
		URL socialMediaUrl = new URL("https://raw.githubusercontent.com/PolyEdge/PlexMod/master/social_media.json");
		InputStreamReader socialMediaInput = new InputStreamReader(socialMediaUrl.openStream());
		JsonReader jsonReader = new JsonReader(socialMediaInput);
		JsonParser jsonParser = new JsonParser();
		JsonElement apiResponse = jsonParser.parse(jsonReader);
		socialMediaInput.close();
		for (Map.Entry<String, JsonElement> object : apiResponse.getAsJsonObject().entrySet()) {
			try {
				socialMediaLinks.put(object.getKey(), object.getValue().getAsString());
			}
			catch (Throwable ignored) {}
		}
	}

	@Override
	public void saveModConfig() {}

	@Override
	public void joinedMineplex() {}

	@Override
	public void leftMineplex() {}
}
