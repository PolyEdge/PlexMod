package pw.ipex.plex.mods.messaging;

import java.util.List;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
//import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreChatRegex;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mod.PlexModBase;

public class PlexMessagingMod extends PlexModBase {
	//private static ResourceLocation sendIcon = new ResourceLocation("PolyEdge_Plex", "chat/send.png");

	public static PlexMessagingChannelManager channelManager = new PlexMessagingChannelManager();
	public static KeyBinding toggleChatUI;
	
	@Override
	public String getModName() {
		return "Direct Messaging";
	}
	
	@Override
	public void modInit() {
	    toggleChatUI = new KeyBinding("Open Chat UI", 157, "Plex Mod");
	    ClientRegistry.registerKeyBinding(toggleChatUI);
	    
	    PlexCore.registerUiTab("Messaging", PlexMessagingUI.class);
	}

	@Override
	public void saveModConfig() {
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		String chatMessageContent = PlexCoreUtils.condenseChatAmpersandFilter(event.message.getFormattedText());
		String chatMessageType = PlexCoreChatRegex.determinePotentialChatType(chatMessageContent);
		if (chatMessageType.equals("party")) {
			if (channelManager.getChannel("@Party") == null) {
				PlexMessagingPartyChatChannel pchannel = new PlexMessagingPartyChatChannel();
				pchannel.setName("@Party");
				channelManager.addChannel(pchannel);
			}
			List<String> messageCi = PlexCoreChatRegex.determineRegularMessageData(chatMessageContent);
			if (!(messageCi == null)) {
				PlexMessagingPartyChatChannel partyChannel = (PlexMessagingPartyChatChannel) channelManager.getChannel("@Party");
				PlexMessagingMessage message = new PlexMessagingMessage().setChatMessage().setContent(PlexCoreChatRegex.getMessageField(messageCi, "message")).setNow().setUser(PlexCoreChatRegex.getMessageField(messageCi, "ign")).setColour(0xffe820e8).setLeft().setHead(PlexCoreChatRegex.getMessageField(messageCi, "ign"));
				if (PlexCoreChatRegex.getMessageField(messageCi, "ign").equalsIgnoreCase(PlexCore.getPlayerIGN())) {
					message.setRight();
				}
				partyChannel.addAgressiveMessage(message);
				channelManager.bumpChannelToTop(partyChannel);		
			}
		}
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if (Plex.minecraft.inGameHasFocus && toggleChatUI.isPressed()) {
			PlexCore.displayUIScreen(new PlexMessagingUI());
		}
	}
	@Override
	public void switchedLobby(String name) {
	}

}
