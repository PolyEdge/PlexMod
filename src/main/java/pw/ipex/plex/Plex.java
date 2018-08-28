package pw.ipex.plex;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import pw.ipex.plex.commandqueue.PlexCommandQueue;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreListeners;
import pw.ipex.plex.core.PlexCoreServerState;
import pw.ipex.plex.core.PlexCoreTextures;
import pw.ipex.plex.mods.autogg.PlexAutoGGMod;
import pw.ipex.plex.mods.autothank.PlexAutoThankMod;
import pw.ipex.plex.mods.betterreply.PlexBetterReplyMod;
import pw.ipex.plex.mods.developmentmod.PlexDevelopmentMod;
import pw.ipex.plex.mods.friendslistenhancements.PlexFriendsListEnhancementsMod;
import pw.ipex.plex.mods.hidestream.PlexHideStreamMod;
import pw.ipex.plex.mods.messaging.PlexMessagingMod;
import pw.ipex.plex.mods.plexmod.PlexPlexCommand;
import pw.ipex.plex.mods.plexmod.PlexPlexMod;
import pw.ipex.plex.mods.richpresence.PlexNewRichPresenceMod;
//import pw.ipex.plex.mods.richpresence.PlexRichPresenceMod;

import org.apache.logging.log4j.Logger;

@Mod(modid = Plex.MODID, version = Plex.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.8,1.8.9]")
public class Plex {
	public static final String MODID = "PolyEdge_Plex";
	public static final String VERSION = "0.3";
	public static final String PATCHID = null;

	public static Minecraft minecraft = Minecraft.getMinecraft();
	public static Configuration config;
	public static Logger logger;

	public static PlexCoreServerState serverState = new PlexCoreServerState();
	public static PlexCoreListeners plexListeners = new PlexCoreListeners();
	public static PlexCommandQueue plexCommandQueue = new PlexCommandQueue();
	public static PlexPlexCommand plexCommand = new PlexPlexCommand();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		logger = event.getModLog();
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(plexListeners);
		MinecraftForge.EVENT_BUS.register(plexCommandQueue);
		
		PlexCoreTextures.loadTextures();
		PlexCore.registerMod(new PlexPlexMod());
		PlexCore.registerMod(new PlexFriendsListEnhancementsMod());
		PlexCore.registerMod(new PlexBetterReplyMod());
		PlexCore.registerMod(new PlexMessagingMod());
		PlexCore.registerMod(new PlexHideStreamMod());
		PlexCore.registerMod(new PlexAutoThankMod());
		PlexCore.registerMod(new PlexDevelopmentMod());
		//PlexCore.registerMod(new PlexRichPresenceMod());
		PlexCore.registerMod(new PlexNewRichPresenceMod());
		PlexCore.registerMod(new PlexAutoGGMod());
	}
}