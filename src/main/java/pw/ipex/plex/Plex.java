package pw.ipex.plex;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import pw.ipex.plex.core.loop.PlexCoreEventLoopManager;
import pw.ipex.plex.cq.PlexCommandQueueManager;
import pw.ipex.plex.core.*;
import pw.ipex.plex.core.mineplex.PlexCoreServerState;
import pw.ipex.plex.core.render.PlexCoreRenderUtils;
import pw.ipex.plex.core.render.PlexCoreTextures;
import pw.ipex.plex.mods.autofriend.PlexAutoFriendMod;
import pw.ipex.plex.mods.autogg.PlexAutoGGMod;
import pw.ipex.plex.mods.autothank.PlexAutoThankMod;
import pw.ipex.plex.mods.chatmod.PlexChatStreamMod;
import pw.ipex.plex.mods.replycommand.PlexBetterReplyMod;
import pw.ipex.plex.mods.developmentmod.PlexDevelopmentMod;
import pw.ipex.plex.mods.messagingscreen.PlexMessagingMod;
import pw.ipex.plex.mods.plexmod.PlexPlexCommand;
import pw.ipex.plex.mods.plexmod.PlexPlexMod;
import pw.ipex.plex.mods.discordrichstatus.PlexNewRichPresenceMod;
//import pw.ipex.plex.mods.discordrichstatus.PlexRichPresenceMod;

import org.apache.logging.log4j.Logger;

@Mod(modid = Plex.MODID, version = Plex.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.8,1.8.9]")
public class Plex {
	public static final String MODID = "PolyEdge_Plex";
	public static final String VERSION = "0.4";
	public static final String PATCHID = "PATCH_3";
	public static final String RELEASENOTICE = "Welcome to Plex 0.4!";

	public static Minecraft minecraft = Minecraft.getMinecraft();
	public static Configuration config;
	public static Logger logger;

	public static PlexCoreServerState serverState = new PlexCoreServerState();
	public static PlexCommandQueueManager plexCommandQueue = new PlexCommandQueueManager();
	public static PlexCoreListeners plexListeners = new PlexCoreListeners();
	public static PlexPlexCommand plexCommand = new PlexPlexCommand();
	public static PlexCorePersistentPlayerManager playerManager = new PlexCorePersistentPlayerManager();
	public static PlexCoreRenderUtils renderUtils = new PlexCoreRenderUtils();
	public static PlexCoreEventLoopManager eventLoop = new PlexCoreEventLoopManager();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(plexListeners);
		PlexCoreTextures.loadTextures();

		PlexCore.getInternalLoop().addTask(plexListeners::handleLobbySwitching);
		PlexCore.getInternalLoop().addTask(plexCommandQueue::processQueue);

		PlexCore.registerMod(new PlexPlexMod());
		//PlexCore.registerMod(new PlexFriendsListEnhancementsMod());  // (this module no longer works because they removed the offline section. press f to pay respects)
		PlexCore.registerMod(new PlexBetterReplyMod());
		PlexCore.registerMod(new PlexMessagingMod());
		PlexCore.registerMod(new PlexChatStreamMod());
		PlexCore.registerMod(new PlexAutoThankMod());
		PlexCore.registerMod(new PlexDevelopmentMod());
		//PlexCore.registerMod(new PlexRichPresenceMod()); // (the old jar file is excluded from the git repository because the libraries it uses are no longer included and it will not compile)
		PlexCore.registerMod(new PlexNewRichPresenceMod());
		PlexCore.registerMod(new PlexAutoGGMod());
		PlexCore.registerMod(new PlexAutoFriendMod());

		PlexCore.getInternalLoop().setClock(25).start();
		PlexCore.getModLoop().setClock(50).start();
	}
}