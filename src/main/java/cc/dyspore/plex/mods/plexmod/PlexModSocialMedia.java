package cc.dyspore.plex.mods.plexmod;

import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.util.PlexUtilTextures;
import net.minecraft.util.ResourceLocation;

public enum PlexModSocialMedia {
    DISCORD_SERVER("discord_server", "https://discord.gg", PlexUtilTextures.GUI_SM_DISCORD_ICON, 264, 264),
    TWITTER("twitter", "https://twitter.com", PlexUtilTextures.GUI_SM_TWITTER_ICON, 264, 264),
    NAMEMC("namemc", "https://namemc.com", PlexUtilTextures.GUI_SM_NAMEMC_ICON, 264, 264),
    ;

    public String name;
    public String linkPrefix;
    public ResourceLocation icon;
    public int iconWidth;
    public int iconHeight;

    public String link;
    public boolean available;

    PlexModSocialMedia(String serializedName, String linkFormat, ResourceLocation icon, int iconWidth, int iconHeight) {
        this.name = serializedName;
        this.linkPrefix = linkFormat;
        this.icon = icon;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;

        this.link = null;
        this.available = false;
    }

    public static void activate(String serializedName, String urlPath) {
        for (PlexModSocialMedia socialMedia : PlexModSocialMedia.values()) {
            if (socialMedia.name.trim().equalsIgnoreCase(serializedName.trim())) {
                socialMedia.load(urlPath);
                return;
            }
        }
        Plex.logger.info("[PlexMod] failed to load social media link \"" + serializedName + "\": website name not registered");
    }

    private void load(String linkPath) {
        this.link = this.linkPrefix + linkPath;
        this.available = true;
    }
}
