package cc.dyspore.plex;

import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.core.PlexCoreListeners;
import cc.dyspore.plex.core.PlexCorePersistentPlayerManager;
import cc.dyspore.plex.core.loop.PlexCoreEventLoopManager;
import cc.dyspore.plex.core.mineplex.PlexServerState;
import cc.dyspore.plex.core.util.PlexUtilTextures;
import cc.dyspore.plex.commands.queue.PlexCommandQueueManager;
import cc.dyspore.plex.mods.autogg.PlexAutoGGMod;
import cc.dyspore.plex.mods.autothank.PlexAutoThankMod;
import cc.dyspore.plex.mods.chatmod.PlexChatStreamMod;
import cc.dyspore.plex.mods.developmentmod.PlexDevelopmentMod;
import cc.dyspore.plex.mods.discordrichstatus.PlexNewRichPresenceMod;
import cc.dyspore.plex.mods.messagingscreen.PlexMessagingMod;
import cc.dyspore.plex.mods.plexmod.PlexPlexCommand;
import cc.dyspore.plex.mods.plexmod.PlexPlexMod;
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
	public static final String PATCHID = "PATCH_6";
	public static final String RELEASENOTICE = "Welcome to 0.4!";

	public static Minecraft minecraft = Minecraft.getMinecraft();
	public static Configuration config;
	public static Logger logger;

	public static PlexServerState gameState = new PlexServerState();
	public static PlexCommandQueueManager queue = new PlexCommandQueueManager();
	public static PlexCoreListeners listeners = new PlexCoreListeners();
	public static PlexPlexCommand plexCommand = new PlexPlexCommand();
	public static PlexCorePersistentPlayerManager playerManager = new PlexCorePersistentPlayerManager();
	public static PlexCoreEventLoopManager eventLoop = new PlexCoreEventLoopManager();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(listeners);
		PlexUtilTextures.loadTextures();

		PlexCore.getInternalLoop().addTask(listeners::handleLobbyData);
		PlexCore.getInternalLoop().addTask(queue::processQueue);

		PlexCore.registerMod(new PlexPlexMod());
		PlexCore.registerMod(new PlexBetterReplyMod());
		PlexCore.registerMod(new PlexMessagingMod());
		PlexCore.registerMod(new PlexChatStreamMod());
		PlexCore.registerMod(new PlexAutoThankMod());
		PlexCore.registerMod(new PlexDevelopmentMod());
		PlexCore.registerMod(new PlexNewRichPresenceMod());
		PlexCore.registerMod(new PlexAutoGGMod());
		PlexCore.registerMod(new PlexAutoFriendMod());

		PlexCore.getInternalLoop().setClock(25).start();
		PlexCore.getModLoop().setClock(50).start();
	}
}