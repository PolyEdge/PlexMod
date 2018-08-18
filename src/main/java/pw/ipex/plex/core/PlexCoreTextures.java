package pw.ipex.plex.core;

import net.minecraft.util.ResourceLocation;

public class PlexCoreTextures {
	public static ResourceLocation GUI_BANNER_ICON;
	public static ResourceLocation GUI_SM_NAMEMC_ICON;
	public static ResourceLocation GUI_SM_TWITTER_ICON;
	public static ResourceLocation GUI_SM_DISCORD_ICON;
	
	public static void loadTextures() {
		GUI_BANNER_ICON = new ResourceLocation("plex", "textures/gui/gui_banner.png");
		
		GUI_SM_DISCORD_ICON = new ResourceLocation("plex", "textures/gui/social/icon_discord.png");
		GUI_SM_NAMEMC_ICON = new ResourceLocation("plex", "textures/gui/social/icon_namemc.png");
		GUI_SM_TWITTER_ICON = new ResourceLocation("plex", "textures/gui/social/icon_twitter.png");
	}
}
