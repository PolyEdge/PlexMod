package pw.ipex.plex.mods.messaging;

import java.util.List;
import java.util.Map;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
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
		String chatMessageContent = PlexCoreUtils.condenseChatFilter(event.message.getFormattedText());
		this.handleMessage(chatMessageContent);
	}
	
	public void handleMessage(String message) {
		this.handleChatMessasge(message);
		this.handleOtherMessage(message);
	}
	
	public void handleChatMessasge(String chatMessage) {
		PlexMessagingChatMessageAdapter messageAdapter = PlexMessagingChatMessageConstructor.getAdapterForChatMessageWithRegexTag(chatMessage, "chatMessage");
		if (messageAdapter == null) {
			return;
		}
		PlexMessagingChannelBase channel = getChannel(messageAdapter.getChannelName(chatMessage), messageAdapter.getChannelClass());
		
		//PlexMessagingMessage message = new PlexMessagingMessage()
//		if (PlexCoreChatRegex.getEntryNamed("party_chat").matches(chatMessage)) {
//			Map<String, String> messageData = PlexCoreChatRegex.getEntryNamed("party_chat").getAllFields(chatMessage);
//			PlexMessagingMessage message = new PlexMessagingMessage().setChatMessage().setContent(messageData.get("message")).setNow().setUser(messageData.get("ign")).setLeft().setHead(messageData.get("ign"));
//			if (messageData.get("ign").equalsIgnoreCase(PlexCore.getPlayerIGN())) {
//				message.setRight();
//			}
//			getChannel("@Party", PlexMessagingPartyChatChannel.class).addAgressiveMessage(message);
//			channelManager.bumpChannelToTop(getChannel("@Party", PlexMessagingPartyChatChannel.class));		
//		}
	}
	
	public void handleOtherMessage(String chatMessage) {
		
	}
	
	public PlexMessagingChannelBase getChannel(String name, Class<? extends PlexMessagingChannelBase> type) {
		if (channelManager.getChannel(name) == null) {
			PlexMessagingChannelBase channel;
			try {
				channel = (PlexMessagingPartyChatChannel) type.newInstance();
				channel.setName(name);
				channelManager.addChannel(channel);
			} 
			catch (InstantiationException | IllegalAccessException e) {}
		}
		return channelManager.getChannel(name);
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
