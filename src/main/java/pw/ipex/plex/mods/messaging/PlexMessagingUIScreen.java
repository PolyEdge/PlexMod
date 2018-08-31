package pw.ipex.plex.mods.messaging;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
//import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.core.PlexCoreRenderUtils;
import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mods.messaging.render.PlexMessagingMessageHoverState;
import pw.ipex.plex.ui.PlexUIBase;
import pw.ipex.plex.ui.PlexUIModMenuScreen;
import pw.ipex.plex.ui.PlexUIProgressBar;
import pw.ipex.plex.ui.PlexUIScaledButton;
import pw.ipex.plex.ui.PlexUIScrolledItemList;
import pw.ipex.plex.ui.PlexUISlider;
import pw.ipex.plex.ui.PlexUIStaticLabel;
import pw.ipex.plex.ui.PlexUITextField;

public class PlexMessagingUIScreen extends PlexUIBase {
	public static String lastTextInBox = "";
	public static String lastSearchText = "";
	public static Float lastContactsScroll = 0.0F;
	public PlexUITextField textField;
	public PlexUITextField searchBox;
	public PlexUIScrolledItemList contactsList;
	public PlexMessagingUIMessageWindow chatWindow;
	public PlexUIProgressBar channelProgressBar;
	public PlexUIStaticLabel channelStatusLabel;
	public GuiButton sendButton;
	public PlexUIScaledButton newConversationButton;
	//public Character lastKeyTyped = null;
	//public Integer lastKeyCodeTyped = null;
	//public Long lastTypedTime = null;
	//public Long lastRepetition = null;
	public Long createdTime = Minecraft.getSystemTime();
	
	public Integer progressColourReady = 0x1eff43;
	public Integer progressColourLoading = 0xfff31e;
	public Integer progressColourFailed = 0xff301e;
	public Integer progressColourUnresponsive = 0xff871e;
	public Integer progressColourIdle = 0x4286f4;
	
	public Long lastChannelChange = 0L;
	
	
	@Override
	public String uiGetTitle() {
		return "Direct Messages";
	}
	
	@Override
	public void uiOpened() {
		Keyboard.enableRepeatEvents(true);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void uiClosed() {
		MinecraftForge.EVENT_BUS.unregister(this);
		Keyboard.enableRepeatEvents(false);
	}
	
	public static boolean isChatOpen() {
		if (Plex.minecraft.currentScreen == null) {
			return false;
		}
		if (!Plex.minecraft.currentScreen.getClass().equals(PlexUIModMenuScreen.class)) {
			return false;
		}
		if (!((PlexUIModMenuScreen) Plex.minecraft.currentScreen).baseUiScreen.getClass().equals(PlexMessagingUIScreen.class)) {
			return false;
		}
		return true;
	}

	@Override
	public void uiAddButtons(PlexUIModMenuScreen ui) {
		Integer bottom = ui.zoneEndY() - 4;
		Integer startX = ui.zoneStartX() + 6;
		Integer sizeX = ui.horizontalPixelCount() - (getContactsPaneSize()) - 8 - 24;
		
		PlexMessagingMod.channelManager.deleteMessageRenderCache();
		
		this.textField = new PlexUITextField(6, this.parentUI.getFontRenderer(), startX, bottom - 21, sizeX, 18);
		this.textField.text.setMaxStringLength(100);
		this.textField.text.setFocused(true);
		this.textField.text.setCanLoseFocus(false);
		this.textField.text.setText(PlexMessagingUIScreen.lastTextInBox);
		
		this.searchBox = new PlexUITextField(7, this.parentUI.getFontRenderer(), ui.zoneEndX() - this.getContactsPaneSize() + 4, ui.zoneStartY() + 4, (ui.zoneEndX() - 2) - (ui.zoneEndX() - this.getContactsPaneSize() + 2) - 22, 14);
		this.searchBox.text.setFocused(false);
		this.searchBox.text.setCanLoseFocus(true);
		this.searchBox.text.setText(PlexMessagingUIScreen.lastSearchText);
		
		this.newConversationButton = new PlexUIScaledButton(11, (ui.zoneEndX() - 16), ui.zoneStartY() + 4, 14, 14, null, "+", false);
		this.parentUI.addElement(this.newConversationButton);
		
		this.contactsList = new PlexUIScrolledItemList(PlexMessagingMod.channelManager.channels, this.parentUI.zoneEndX() - this.getContactsPaneSize(), this.parentUI.zoneStartY() + 22, this.parentUI.zoneEndX(), this.parentUI.zoneEndY() - 22);
		this.contactsList.setPadding(10, 0);
		this.contactsList.scrollbar.setScroll(lastContactsScroll, true);
		
		this.chatWindow = new PlexMessagingUIMessageWindow(this.parentUI.zoneStartX(), this.parentUI.zoneStartY(), this.parentUI.zoneEndX() - this.getContactsPaneSize(), this.parentUI.zoneEndY() - 30);
		this.chatWindow.paddingLeft = 2;
		this.chatWindow.paddingRight = 2;
		this.chatWindow.paddingRightWithScrollbar = 1;
		this.chatWindow.paddingTop = 5;
		this.chatWindow.paddingBottom = 6;
		
		this.channelProgressBar = new PlexUIProgressBar(this.parentUI.zoneStartX() + 5, this.parentUI.zoneEndY() - 6, this.parentUI.horizontalPixelCount() - (getContactsPaneSize()) - 25 - 5, 1);
		this.channelProgressBar.setBarSpeed(250);
		this.channelProgressBar.setColourSpeed(500);
		this.channelProgressBar.setProgress(1.0F, true);
		this.channelProgressBar.setColour(progressColourIdle, true);
		
		this.channelStatusLabel = new PlexUIStaticLabel(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() - 12, 12);
		this.channelStatusLabel.setText("", true);
		
		this.sendButton = new GuiButton(10, startX + sizeX + 3, bottom - 22, 20, 20, ">");
		this.parentUI.addElement(this.sendButton); //String.valueOf((char) 8594))
		
		//for (int i = 0; i < 8; i++) {
		//	PlexMessagingPartyChatChannel pchannel = new PlexMessagingPartyChatChannel();
		//	PlexMessagingMod.channelManager.addChannel(pchannel);
		//}
	}
	
	public Integer getContactsPaneSize() {
		return (this.parentUI.horizontalPixelCount() / 3);
	}
	
	@Override
	public Boolean disableDoneButton() {
		return true;
	}

	@Override
	public Integer pageForegroundColour() {
		return -1;
	}

	@Override
	public void uiButtonClicked(GuiButton button) {
		if (button.id == 10 && PlexMessagingMod.channelManager.selectedChannel != null) {
			this.sendMessage();
		}
		if (button.id == 11 && PlexMessagingMod.channelManager.selectedChannel != null) {
			PlexMessagingMod.channelManager.setSelectedChannel(null);
		}
	}

	@Override
	public void uiSliderInteracted(PlexUISlider slider) {		
	}
	
	@Override
	public void mouseClicked(int par1, int par2, int btn) {
		this.textField.mouseClicked(par1, par2, btn);
		this.contactsList.mouseClicked(par1, par2, btn);
		this.chatWindow.mouseClicked(par1, par2, btn);
		this.searchBox.mouseClicked(par1, par2, btn);
	}
	
	@Override
	public void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		this.contactsList.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		this.chatWindow.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		this.contactsList.mouseReleased(mouseX, mouseY, state);
		this.chatWindow.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void updateScreen() {
		this.textField.updateScreen();
		this.searchBox.updateScreen();
	}
	
	@Override 
	public void handleMouseInput(int x, int y) {
		this.contactsList.handleMouseInput(x, y);
		this.chatWindow.handleMouseInput(x, y);
	}
	
	@Override
	public void keyTyped(char par1, int par2) {
		if (((Integer) par2).equals(PlexMessagingMod.toggleChatUI.getKeyCode())) {
			this.uiClosed();
			PlexCore.displayUIScreen(null);
		}
		if (((Integer) par2).equals(28)) {
			this.sendMessage();
			this.searchBox.text.setFocused(false);
			return;
		}
		this.textField.keyTyped(par1, par2);
		this.searchBox.keyTyped(par1, par2);
	}

	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.sendButton.enabled = this.isSelectedChannelReady();
		if (this.searchBox.text.isFocused()) {
			this.textField.text.setCanLoseFocus(true);
			this.textField.text.setFocused(false);
		}
		else {
			this.textField.text.setCanLoseFocus(false);
			this.textField.text.setFocused(true);
		}
		
		this.contactsList.searchText = this.searchBox.text.getText();
		
		if (PlexMessagingMod.channelManager.selectedChannel != null) {
			this.newConversationButton.enabled = true;
			PlexMessagingMod.channelManager.selectedChannel.readingChannel();
			if (!isSelectedChannelReady()) { //&& PlexMessagingMod.channelManager.selectedChannel.awaitingReady) {
				if (Minecraft.getSystemTime() > PlexMessagingMod.channelManager.selectedChannel.selectTime + 5000L) {
					this.channelProgressBar.setColour(this.progressColourUnresponsive);
					this.channelStatusLabel.setText("Connection to " + PlexMessagingMod.channelManager.selectedChannel.getDisplayName() + " taking too long...");
					this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() + 2, false);
					this.channelStatusLabel.setTextColour(this.progressColourUnresponsive, true);
					this.contactsList.setEnabled(true);
				}
				else {
					this.channelProgressBar.setColour(this.progressColourLoading);
					this.channelStatusLabel.setText("Connecting to channel > " + PlexMessagingMod.channelManager.selectedChannel.getDisplayName() + "...");
					this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() + 2, false);
					this.channelStatusLabel.setTextColour(this.progressColourLoading, true);
					this.contactsList.setEnabled(false);
				}
			}
			else if (isSelectedChannelReady()) {
				this.contactsList.setEnabled(true);
				this.channelProgressBar.setColour(progressColourReady);
				this.channelStatusLabel.setText("Channel Ready!");
				this.channelStatusLabel.setTextColour(this.progressColourReady, true);
				if (Minecraft.getSystemTime() > PlexMessagingMod.channelManager.selectedChannel.readyTime + 500L) {
					this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() - 12, false);
				}
				else {
					this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() + 2, false);
				}
			}
			else {
				this.contactsList.setEnabled(true);
				this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() - 12, false);
				this.channelStatusLabel.setText("");
				this.channelProgressBar.setColour(progressColourIdle);
			}

		}
		else {
			this.newConversationButton.enabled = false;
			this.contactsList.setEnabled(true);
			this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() - 12, false);
			this.channelStatusLabel.setText("");
			this.channelProgressBar.setColour(progressColourIdle);
		}
		
		this.chatWindow.setChannel(PlexMessagingMod.channelManager.selectedChannel);
		
		if (PlexMessagingMod.channelManager.lastChannelChange != this.lastChannelChange) {
			this.lastChannelChange = PlexMessagingMod.channelManager.lastChannelChange;
			if (PlexMessagingMod.channelManager.selectedChannel != null) {
				this.textField.text.setText(PlexMessagingMod.channelManager.selectedChannel.lastTextTyped);
				this.chatWindow.scrollbar.setScroll(PlexMessagingMod.channelManager.selectedChannel.lastChannelScroll, true);
			}
			else {
				this.textField.text.setText("");
			}
		}
		
		
	
		//PlexUIScreen.drawRect(this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneStartY(), this.parentUI.zoneEndX(), this.parentUI.zoneEndY(), 0xaa10100f);
		//PlexUIModMenuScreen.drawRect(this.parentUI.zoneStartX(), this.parentUI.zoneStartY(), this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneEndY() - 30, 0x23ffffff);
		PlexUIModMenuScreen.drawRect(this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneStartY(), this.parentUI.zoneEndX(), this.parentUI.zoneEndY(), 0x65000000);
		
		this.contactsList.drawScreen(par1, par2, par3);
		this.chatWindow.drawScreen(par1, par2, par3);
		this.channelStatusLabel.drawScreen(par1, par2, par3);
		
		GuiScreen.drawRect(this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneStartY(), this.parentUI.zoneEndX(), this.parentUI.zoneStartY() + 20, 0xff000000);
		PlexCoreRenderUtils.staticDrawGradientRect(this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneStartY() + 20, this.parentUI.zoneEndX(), this.parentUI.zoneStartY() + 22, 0xff000000, 0x00000000);
		GuiScreen.drawRect(this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneEndY() - 20, this.parentUI.zoneEndX(), this.parentUI.zoneEndY(), 0xff000000);
		PlexCoreRenderUtils.staticDrawGradientRect(this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneEndY() - 22, this.parentUI.zoneEndX(), this.parentUI.zoneEndY() - 20, 0x00000000, 0xff000000);
		
		GuiScreen.drawRect(this.parentUI.zoneStartX(), this.parentUI.zoneStartY() - 25, this.parentUI.zoneEndX(), this.parentUI.zoneStartY(), 0xff000000);
		PlexCoreRenderUtils.staticDrawGradientRect(this.parentUI.zoneStartX(), this.parentUI.zoneStartY(), this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneStartY() + 2, 0xff000000, 0x00000000);
		GuiScreen.drawRect(this.parentUI.zoneStartX(), this.parentUI.zoneEndY() - 30, this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneEndY(), 0xff000000);
		PlexCoreRenderUtils.staticDrawGradientRect(this.parentUI.zoneStartX(), this.parentUI.zoneEndY() - 32, this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneEndY() - 30, 0x00000000, 0xff000000);
		
		//PlexUIScreen.drawRect(this.parentUI.zoneStartX(), this.parentUI.zoneStartX() - 30, this.parentUI.zoneEndX(), this.parentUI.zoneEndY(), 0xff000000);
		PlexCoreRenderUtils.drawScaledHorizontalLine(this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneEndX(), this.parentUI.zoneStartY(), 1.0F, PlexCoreUtils.globalChromaCycle());
		PlexCoreRenderUtils.drawScaledHorizontalLine(this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneEndX(), this.parentUI.zoneEndY() - 1, 1.0F, PlexCoreUtils.globalChromaCycle());
		PlexCoreRenderUtils.drawScaledHorizontalLine(this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneEndX(), this.parentUI.zoneEndY() - 2, 1.0F, PlexCoreUtils.globalChromaCycle());
		PlexCoreRenderUtils.drawScaledVerticalLine(this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneStartY(), this.parentUI.zoneEndY(), 1.0F, PlexCoreUtils.globalChromaCycle());
		
		this.textField.drawScreen(par1, par2, par3);
		this.searchBox.drawScreen(par1, par2, par3);
		this.channelProgressBar.drawScreen(par1, par2, par3);
		
		
		
		
		PlexMessagingMessageHoverState selectedMessage = this.chatWindow.getMouseOverMessage(par1, par2);
		if (selectedMessage != null) {
			if (selectedMessage.selectedLine != null) {
				PlexCoreRenderUtils.drawScaledString(selectedMessage.selectedLine.text, (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 5, 0xffffff, 0.5F, false);
			}
			if (selectedMessage.selectedWord != null) {
				PlexCoreRenderUtils.drawScaledString(selectedMessage.selectedWord, (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 10, 0xffffff, 0.5F, false);
			}
			if (selectedMessage.messageSelected) {
				PlexCoreRenderUtils.drawScaledString("ms", (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 15, 0xffffff, 0.5F, false);
			}
		}
		
		//String drawText = "";

		//PlexCoreRenderUtils.drawScaledString(drawText, (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 5, 0xffffff, 0.5F, false);
		
		
		lastContactsScroll = contactsList.scrollbar.realScrollValue;
		
		if (PlexMessagingMod.channelManager.selectedChannel != null) {
			PlexMessagingMod.channelManager.selectedChannel.lastTextTyped = this.textField.text.getText();
			PlexMessagingMod.channelManager.selectedChannel.lastChannelScroll = this.chatWindow.scrollbar.realScrollValue;
		}
	}
	
	public void sendMessage() {
		if (PlexMessagingMod.channelManager.selectedChannel != null) {
			if (PlexMessagingMod.channelManager.selectedChannel.channelReady) {
				PlexMessagingMod.channelManager.selectedChannel.sendMessage(this.textField.text.getText());
				this.textField.text.setText("");				
			}
		}

	}
	
	public Boolean isSelectedChannelReady() {
		if (PlexMessagingMod.channelManager.selectedChannel != null) {
			return PlexMessagingMod.channelManager.selectedChannel.channelReady;
		}
		return false;
	}
	
	@Override
	public Integer pageBackgroundTransparency() {
		return 35;
	}
	
	public Integer pageBackgroundColour() {
		return 0xffffff;
	}
	
	//@SubscribeEvent
	//public void clientTick(ClientTickEvent event) {
	//	Keyboard.enableRepeatEvents(true);
		//PlexDirectMessagingUI.lastTextInBox = this.textField.text.getText();
	//}
	
	// this was a hack i wrote because i dont know how to use lwjgl
	
//	@SubscribeEvent
//	public void clientTick(ClientTickEvent event) {
//		if (lastKeyTyped != null && lastTypedTime != null && lastKeyCodeTyped != null) {
//			if (Keyboard.isKeyDown(lastKeyCodeTyped)) {
//				if (Minecraft.getSystemTime() > lastTypedTime + 600L && Minecraft.getSystemTime() > lastRepetition + 0L) {
//					this.textField.keyTyped(lastKeyTyped, lastKeyCodeTyped);
//					lastRepetition = Minecraft.getSystemTime();
//				}
//			}
//			else {
//				lastKeyTyped = null;
//				lastTypedTime = null;
//				lastRepetition = null;
//			}
//		}
//		if (Plex.minecraft.currentScreen != null) {
//			if (!Plex.minecraft.currentScreen.equals(this.parentUI)) {
//				MinecraftForge.EVENT_BUS.unregister(this);
//			}			
//		}
//	}
}
