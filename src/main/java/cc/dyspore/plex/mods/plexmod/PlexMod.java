package cc.dyspore.plex.mods.plexmod;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.dyspore.plex.ui.PlexUIModMenu;
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

		new Thread(() -> {
			try {
				this.loadSocialMedia();
				PlexUIModMenu.updateSocialMedia();
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
				PlexModSocialMedia.activate(object.getKey(), object.getValue().getAsString());
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
