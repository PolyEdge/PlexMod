package cc.dyspore.plex;

import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.PlexCoreListeners;
import cc.dyspore.plex.core.util.PlexUtilPlayers;
import cc.dyspore.plex.core.mineplex.PlexMPState;
import cc.dyspore.plex.core.util.PlexUtilTextures;
import cc.dyspore.plex.commands.queue.PlexCommandQueueManager;
import cc.dyspore.plex.mods.autogg.PlexAutoGGMod;
import cc.dyspore.plex.mods.autothank.PlexAutoThankMod;
import cc.dyspore.plex.mods.chatmod.PlexChatStreamMod;
import cc.dyspore.plex.mods.developmentmod.PlexDevelopmentMod;
import cc.dyspore.plex.mods.discordrichstatus.PlexNewRichPresenceMod;
import cc.dyspore.plex.mods.messagingscreen.PlexMessagingMod;
import cc.dyspore.plex.mods.plexmod.PlexModCommand;
import cc.dyspore.plex.mods.plexmod.PlexMod;
import cc.dyspore.plex.mods.replycommand.PlexBetterReplyMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import cc.dyspore.plex.mods.autofriend.PlexAutoFriendMod;

import org.apache.logging.log4j.Logger;


@Mod(modid = Plex.MODID, version = Plex.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.8,1.8.9]", guiFactory = "cc.dyspore.plex.core.fml.PlexModGuiFactory", canBeDeactivated = false)
public class Plex {
	public static final String MODID = "polyedge_plex";
	public static final String VERSION = "0.4";
	public static final String PATCHID = "PATCH_8";
	public static final String RELEASENOTICE = "Welcome to 0.4!";

	public static Minecraft minecraft = Minecraft.getMinecraft();
	public static Configuration config;
	public static Logger logger;

	public static PlexMPState gameState = new PlexMPState();
	public static PlexCommandQueueManager queue = new PlexCommandQueueManager();
	public static PlexCoreListeners listeners = new PlexCoreListeners();
	public static PlexModCommand plexCommand = new PlexModCommand();
	public static PlexUtilPlayers playerManager = new PlexUtilPlayers();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(listeners);

		PlexCore.getInternalLoop().addTask(listeners::handleLobbyData);
		PlexCore.getInternalLoop().addTask(queue::processQueue);

		PlexCore.register(new PlexMod());
		PlexCore.register(new PlexBetterReplyMod());
		PlexCore.register(new PlexMessagingMod());
		PlexCore.register(new PlexChatStreamMod());
		PlexCore.register(new PlexAutoThankMod());
		PlexCore.register(new PlexDevelopmentMod());
		PlexCore.register(new PlexNewRichPresenceMod());
		PlexCore.register(new PlexAutoGGMod());
		PlexCore.register(new PlexAutoFriendMod());

		PlexCore.getInternalLoop().start();
		PlexCore.getModLoop().start();
	}
}