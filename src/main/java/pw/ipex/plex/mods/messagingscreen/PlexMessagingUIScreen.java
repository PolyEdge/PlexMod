package pw.ipex.plex.mods.messagingscreen;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
//import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.input.Mouse;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.PlexCore;

import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mods.messagingscreen.translate.PlexMessagingChatMessageConstructor;
import pw.ipex.plex.mods.messagingscreen.render.PlexMessagingMessageHoverState;
import pw.ipex.plex.mods.messagingscreen.ui.PlexMessagingUICreateNewChatWindow;
import pw.ipex.plex.mods.messagingscreen.ui.PlexMessagingUIMessageWindow;
import pw.ipex.plex.ui.*;
import pw.ipex.plex.ui.widget.*;
import pw.ipex.plex.ui.widget.autocomplete.PlexUIAutoCompleteTextField;
import pw.ipex.plex.ui.widget.itemlist.PlexUIScrolledItem;
import pw.ipex.plex.ui.widget.itemlist.PlexUIScrolledItemList;

import java.util.ArrayList;
import java.util.List;

public class PlexMessagingUIScreen extends PlexUIBase {
	public static String lastTextInBox = "";
	public String lastSearchText = "";
	public static Float lastContactsScroll = 0.0F;
	public static List<String> previousSentMessages = new ArrayList<>();
	public PlexUIAutoCompleteTextField messageField;
	public PlexUITextField searchBox;
	public PlexUIScrolledItemList contactsList;
	public PlexMessagingUIMessageWindow chatWindow;
	public PlexUIProgressBar channelProgressBar;
	public PlexUIStaticLabel channelStatusLabel;
	public PlexUIStaticLabel emoteTooltip;
	public GuiButton sendButton;
	public PlexUIScaledButton newConversationButton;
	public PlexUIScaledButton hideChannelButton;
	public PlexMessagingUICreateNewChatWindow newConversationWindow;

	public Boolean isQuickChat = false;
	public Boolean hasControlSwitch = false;


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

	public Long lastControlPressed = 0L;
	public Long controlShortcutTimeout = 620L;

	public String setMessageFieldNextFrame = null;
	
	@Override
	public String uiGetTitle() {
		return "Messaging";
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
		
		this.getChannelManager().deleteMessageRenderCache();
		
		this.messageField = new PlexUIAutoCompleteTextField(6, this.parentUI.getFontRenderer(), startX, bottom - 21, sizeX, 18);
		this.messageField.setAutoCompleteItems(this.getMessagingMod().autoCompleteContainer.autoCompleteItems);
		this.messageField.text.setMaxStringLength(100);
		this.messageField.text.setFocused(true);
		this.messageField.text.setCanLoseFocus(false);
		this.messageField.text.setText(PlexMessagingUIScreen.lastTextInBox);
		this.messageField.listBackgroundColour = 0xff454545;
		this.messageField.setPreviousSentMessages(previousSentMessages);
		this.messageField.excludeGroup("emote");
		
		this.searchBox = new PlexUITextField(7, this.parentUI.getFontRenderer(), ui.zoneEndX() - this.getContactsPaneSize() + 4, ui.zoneStartY() + 4, (ui.zoneEndX() - 2) - (ui.zoneEndX() - this.getContactsPaneSize() + 2) - 40, 14);
		this.searchBox.text.setFocused(false);
		this.searchBox.text.setCanLoseFocus(true);
		this.searchBox.text.setText(this.lastSearchText);
		
		this.newConversationButton = new PlexUIScaledButton(11, (ui.zoneEndX() - 34), ui.zoneStartY() + 4, 14, 14, null, "+", false);
		this.parentUI.addElement(this.newConversationButton);

		this.hideChannelButton = new PlexUIScaledButton(12, (ui.zoneEndX() - 16), ui.zoneStartY() + 4, 14, 14, null, "-", false);
		this.parentUI.addElement(this.hideChannelButton);
		
		this.contactsList = new PlexUIScrolledItemList(this.getChannelManager().displayedChannels, this.parentUI.zoneEndX() - this.getContactsPaneSize(), this.parentUI.zoneStartY() + 22, this.parentUI.zoneEndX(), this.parentUI.zoneEndY() - 22);
		this.contactsList.setPadding(10, 0);
		this.contactsList.scrollbar.setScroll(lastContactsScroll, true);
		this.contactsList.scrollbar.hiddenForcedScroll = 0.0F;
		
		this.chatWindow = new PlexMessagingUIMessageWindow(this.parentUI.zoneStartX(), this.parentUI.zoneStartY(), this.parentUI.zoneEndX() - this.getContactsPaneSize(), this.parentUI.zoneEndY() - 30);
		this.chatWindow.paddingLeft = 2;
		this.chatWindow.paddingRight = 2;
		this.chatWindow.paddingRightWithScrollbar = 1;
		this.chatWindow.paddingTop = 5;
		this.chatWindow.paddingBottom = 6;
		this.chatWindow.hoverEventsEnabled = true;

		this.newConversationWindow = new PlexMessagingUICreateNewChatWindow(this.parentUI.zoneStartX(), this.parentUI.zoneStartY() + 12, this.parentUI.zoneEndX() - this.getContactsPaneSize(), this.parentUI.zoneEndY() - 35);
		this.newConversationWindow.headerText = "Welcome to the Chat";
		this.newConversationWindow.setItems(new ArrayList<>(PlexMessagingChatMessageConstructor.groupClassMapping.values()));
		
		this.channelProgressBar = new PlexUIProgressBar(this.parentUI.zoneStartX() + 5, this.parentUI.zoneEndY() - 6, this.parentUI.horizontalPixelCount() - (getContactsPaneSize()) - 25 - 5, 1);
		this.channelProgressBar.setBarSpeed(250);
		this.channelProgressBar.setColourSpeed(500);
		this.channelProgressBar.setProgress(1.0F, true);
		this.channelProgressBar.setColour(progressColourIdle, true);
		
		this.channelStatusLabel = new PlexUIStaticLabel(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() - 12, 12);
		this.channelStatusLabel.setText("", true);

		this.emoteTooltip = new PlexUIStaticLabel(this.parentUI.zoneStartX() + 8, this.parentUI.zoneEndY() - 40, 12);
		this.emoteTooltip.setText("", true);
		this.emoteTooltip.setTextColour(0xff5555, true);
		
		this.sendButton = new GuiButton(10, startX + sizeX + 3, bottom - 22, 20, 20, ">");
		this.parentUI.addElement(this.sendButton); //String.valueOf((char) 8594))
		
		//for (int i = 0; i < 8; i++) {
		//	PlexMessagingPartyChatChannel pchannel = new PlexMessagingPartyChatChannel();
		//	this.getChannelManager().addChannel(pchannel);
		//}
	}

	public PlexMessagingMod getMessagingMod() {
		return PlexCore.modInstance(PlexMessagingMod.class);
	}
	
	public PlexMessagingChannelManager getChannelManager() {
		return this.getMessagingMod().channelManager;
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
		if (button.id == 10 && this.getChannelManager().selectedChannel != null) {
			this.handleSendButton();
		}
		if (button.id == 11 && this.getChannelManager().selectedChannel != null) {
			this.getChannelManager().deselectChannel();
			this.setMessageFieldNextFrame = this.searchBox.text.getText();
			this.searchBox.text.setText("");
			this.searchBox.text.setFocused(false);
			this.messageField.text.setFocused(true);
			return;
		}
		if (button.id == 12 && this.getChannelManager().selectedChannel != null) {
			this.getChannelManager().selectedChannel.hiddenFromList = true;
			this.getChannelManager().setSelectedChannel(null);
		}
	}

	@Override
	public void uiSliderInteracted(PlexUISlider slider) {
	}
	
	@Override
	public void mouseClicked(int par1, int par2, int btn) {
		this.messageField.mouseClicked(par1, par2, btn);
		this.contactsList.mouseClicked(par1, par2, btn);
		this.chatWindow.mouseClicked(par1, par2, btn);
		this.searchBox.mouseClicked(par1, par2, btn);
		this.newConversationWindow.mouseClicked(par1, par2, btn);
	}
	
	@Override
	public void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		this.contactsList.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		this.chatWindow.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		this.messageField.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		this.newConversationWindow.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		this.contactsList.mouseReleased(mouseX, mouseY, state);
		this.chatWindow.mouseReleased(mouseX, mouseY, state);
		this.messageField.mouseReleased(mouseX, mouseY, state);
		this.newConversationWindow.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void updateScreen() {
		this.messageField.updateScreen();
		this.searchBox.updateScreen();
	}
	
	@Override 
	public void handleMouseInput(int x, int y) {
		int wheel = Mouse.getEventDWheel() != 0 ? (0 - (Mouse.getEventDWheel() / Math.abs(Mouse.getEventDWheel()))) : 0;
		if (Keyboard.isKeyDown(29) && wheel != 0) {
			this.getChannelManager().autoReady = false;
			this.hasControlSwitch = true;
			this.scrollContactsBy(wheel);
			this.selectMessageField();
			return;
		}
		if (!Keyboard.isKeyDown(29)) {
			this.getChannelManager().autoReady = true;
			this.hasControlSwitch = false;
		}
		this.messageField.handleMouseInput(x, y);
		this.contactsList.handleMouseInput(x, y);
		this.newConversationWindow.handleMouseInput(x, y);
		if (!this.messageField.getAutoCompleteListVisible()) {
			this.chatWindow.handleMouseInput(x, y);
			if (this.getChannelManager().selectedChannel == null && wheel != 0) {
				this.selectMessageField();
				this.scrollChatSelectionBy(wheel);
			}
		}
	}
	
	@Override
	public void keyTyped(char par1, int par2) {
		if (((Integer) par2).equals(this.getMessagingMod().toggleChatUI.getKeyCode()) && !Character.isLetterOrDigit(par1)) {
			this.uiClosed();
			PlexCore.displayUIScreen(null);
		}
		if (((Integer) par2).equals(this.getMessagingMod().quickChat.getKeyCode()) && !Character.isLetterOrDigit(par1)) {
			this.uiClosed();
			PlexCore.displayUIScreen(null);
		}
		if (((Integer) par2).equals(29) && !Keyboard.isRepeatEvent()) {
			if (Minecraft.getSystemTime() < this.lastControlPressed + this.controlShortcutTimeout) {
				this.lastControlPressed = 0L;
				if (this.searchBox.text.isFocused()) {
					this.selectMessageField();
				}
				else {
					this.selectSearchBox();
				}
				return;
			}
			this.lastControlPressed = Minecraft.getSystemTime();
		}
		if (par2 == 200 || par2 == 208) {
			if (Keyboard.isKeyDown(29)) {
				this.hasControlSwitch = true;
				this.getChannelManager().autoReady = false;
				this.selectMessageField();
				this.scrollContactsBy(par2 == 200 ? -1 : 1);
				return;
			}
			if (this.getChannelManager().selectedChannel == null && !this.messageField.getAutoCompleteListVisible()) {
				this.selectMessageField();
				this.scrollChatSelectionBy(par2 == 200 ? -1 : 1);
				return;
			}
		}
		if (this.messageField.keyTyped(par1, par2)) {
			return;
		}
		if (par1 == ':') {
			if (this.messageField.getLastWordInBox().equals(":")) {
				this.messageField.setAutoCompleteListVisible(true);
				this.messageField.includeGroup("emote");
			}
		}
		else {
			if (!this.messageField.getLastWordInBox().startsWith(":")) {
				this.messageField.excludeGroup("emote");
			}
		}
		if (((Integer) par2).equals(28)) {
			if (this.searchBox.text.isFocused()) {
				this.getChannelManager().deselectChannel();
				this.setMessageFieldNextFrame = this.searchBox.text.getText();
				this.searchBox.text.setText("");
				this.searchBox.text.setFocused(false);
				this.messageField.text.setFocused(true);
				return;
			}
			this.handleSendButton();
			return;
		}

		this.searchBox.keyTyped(par1, par2);
	}

	@Override
	public boolean escapeTyped() {
		if (this.messageField.keyTyped((char) 27, 1)) {
			return false;
		}
		return true;
	}

	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		if (this.searchBox.text.isFocused()) {
			this.messageField.text.setCanLoseFocus(true);
			this.messageField.text.setFocused(false);
		}
		else {
			this.messageField.text.setCanLoseFocus(false);
			this.messageField.text.setFocused(true);
		}

		if (!Keyboard.isKeyDown(29)) {
			this.hasControlSwitch = false;
			this.getChannelManager().autoReady = true;
		}

		this.getChannelManager().updateDisplayedChannels();
		this.contactsList.searchText = this.searchBox.text.getText();

		if (this.getChannelManager().selectedChannel != null) {
			this.chatWindow.isEnabled = true;
			this.newConversationWindow.isEnabled = false;
			this.newConversationButton.enabled = true;
			this.hideChannelButton.enabled = true;
			if (!this.hasControlSwitch) {
				this.getChannelManager().selectedChannel.readingChannel();
				this.getChannelManager().selectedChannel.loopReady();
			}
			if (this.isSelectedChannelErrored()) {
				this.channelProgressBar.setColour(this.progressColourFailed);
				this.channelStatusLabel.setText("Connection to " + this.getChannelManager().selectedChannel.getDisplayName() + " failed.");
				this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() + 2, false);
				this.channelStatusLabel.setTextColour(this.progressColourFailed, true);
				this.contactsList.setEnabled(true);
				this.sendButton.enabled = false;
			}
			else if (!this.isSelectedChannelReady()) { //&& this.getChannelManager().selectedChannel.awaitingReady) {
				if (this.hasControlSwitch) {
					this.channelProgressBar.setColour(this.progressColourLoading);
					this.channelStatusLabel.setText("Release Control to connect to " + this.getChannelManager().selectedChannel.getDisplayName() + "");
					this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() + 2, false);
					this.channelStatusLabel.setTextColour(this.progressColourLoading, true);
					this.contactsList.setEnabled(true);
					this.sendButton.enabled = false;
				}
				else if (!this.getChannelManager().autoReady) {
					this.channelProgressBar.setColour(this.progressColourFailed);
					this.channelStatusLabel.setText("Not scheduled to connect to " + this.getChannelManager().selectedChannel.getDisplayName() + "");
					this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() + 2, false);
					this.channelStatusLabel.setTextColour(this.progressColourFailed, true);
					this.contactsList.setEnabled(true);
					this.sendButton.enabled = false;
				}
				else if (this.getChannelManager().selectedChannel.connectionAttempts > 1) {
					this.channelProgressBar.setColour(this.progressColourUnresponsive);
					this.channelStatusLabel.setText("Connecting to > " + this.getChannelManager().selectedChannel.getDisplayName() + " [attempt " + this.getChannelManager().selectedChannel.connectionAttempts + "]");
					this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() + 2, false);
					this.channelStatusLabel.setTextColour(this.progressColourUnresponsive, true);
					this.contactsList.setEnabled(true);
					this.sendButton.enabled = false;

				}
				else {
					this.channelProgressBar.setColour(this.progressColourLoading);
					this.channelStatusLabel.setText("Connecting to channel > " + this.getChannelManager().selectedChannel.getDisplayName() + "...");
					this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() + 2, false);
					this.channelStatusLabel.setTextColour(this.progressColourLoading, true);
					this.contactsList.setEnabled(true);
					this.sendButton.enabled = false;
				}
			}
			else if (this.isSelectedChannelReady()) {
				this.contactsList.setEnabled(true);
				this.channelProgressBar.setColour(progressColourReady);
				this.channelStatusLabel.setText("Channel Ready!");
				this.channelStatusLabel.setTextColour(this.progressColourReady, true);
				this.sendButton.enabled = true;
				if (Minecraft.getSystemTime() > this.getChannelManager().selectedChannel.readyTime + 500L) {
					this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() - 12, false);
				}
				else {
					this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() + 2, false);
				}
			}
			else {
				this.sendButton.enabled = false;
				this.contactsList.setEnabled(true);
				this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() - 12, false);
				this.channelStatusLabel.setText("");
				this.channelProgressBar.setColour(progressColourIdle);
			}

		}
		else {
			this.chatWindow.isEnabled = false;
			this.newConversationButton.enabled = false;
			this.newConversationWindow.isEnabled = true;
			this.hideChannelButton.enabled = false;
			this.contactsList.setEnabled(true);
			this.channelStatusLabel.setPosition(this.parentUI.zoneStartX() + 2, this.parentUI.zoneStartY() - 12, false);
			this.channelStatusLabel.setText("");
			this.channelProgressBar.setColour(progressColourIdle);
		}
		
		this.chatWindow.setChannel(this.getChannelManager().selectedChannel);
		
		if (this.getChannelManager().lastChannelChange != this.lastChannelChange) {
			this.lastChannelChange = this.getChannelManager().lastChannelChange;
			this.messageField.resetSentMessagesIndex();
			if (this.getChannelManager().selectedChannel != null) {
				this.messageField.text.setText(this.getChannelManager().selectedChannel.lastTextTyped);
				this.chatWindow.scrollbar.setScroll(this.getChannelManager().selectedChannel.lastChannelScroll, true);
			}
			else {
				if (this.setMessageFieldNextFrame != null) {
					this.messageField.text.setText(this.setMessageFieldNextFrame);
					this.setMessageFieldNextFrame = null;
				}
				else {
					this.messageField.text.setText("");
				}
			}
		}

		if (this.messageField.autoCompleteListVisible && this.messageField.getLastWordInBox().startsWith(":") && !Plex.serverState.canUseEmotes) {
			this.emoteTooltip.setText("Upgrade to TITAN rank to use chat emotes.");
			this.emoteTooltip.setHeight(12, false);
		}
		else {
			this.emoteTooltip.setText("");
			this.emoteTooltip.setHeight(0, false);
		}
		
	
		//PlexUIScreen.drawRect(this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneStartY(), this.parentUI.zoneEndX(), this.parentUI.zoneEndY(), 0xaa10100f);
		//PlexUIModMenuScreen.drawRect(this.parentUI.zoneStartX(), this.parentUI.zoneStartY(), this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneEndY() - 30, 0x23ffffff);
		GuiScreen.drawRect(this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneStartY(), this.parentUI.zoneEndX(), this.parentUI.zoneEndY(), 0x65000000);
		
		this.contactsList.drawScreen(par1, par2, par3);
		this.chatWindow.drawScreen(par1, par2, par3);
		this.channelStatusLabel.drawScreen(par1, par2, par3);
		
		GuiScreen.drawRect(this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneStartY(), this.parentUI.zoneEndX(), this.parentUI.zoneStartY() + 20, 0xff000000);
		Plex.renderUtils.staticDrawGradientRect(this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneStartY() + 20, this.parentUI.zoneEndX(), this.parentUI.zoneStartY() + 22, 0xff000000, 0x00000000);
		GuiScreen.drawRect(this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneEndY() - 20, this.parentUI.zoneEndX(), this.parentUI.zoneEndY(), 0xff000000);
		Plex.renderUtils.staticDrawGradientRect(this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneEndY() - 22, this.parentUI.zoneEndX(), this.parentUI.zoneEndY() - 20, 0x00000000, 0xff000000);
		
		GuiScreen.drawRect(this.parentUI.zoneStartX(), this.parentUI.zoneStartY() - 25, this.parentUI.zoneEndX(), this.parentUI.zoneStartY(), 0xff000000);
		Plex.renderUtils.staticDrawGradientRect(this.parentUI.zoneStartX(), this.parentUI.zoneStartY(), this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneStartY() + 2, 0xff000000, 0x00000000);
		GuiScreen.drawRect(this.parentUI.zoneStartX(), this.parentUI.zoneEndY() - 30, this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneEndY(), 0xff000000);
		Plex.renderUtils.staticDrawGradientRect(this.parentUI.zoneStartX(), this.parentUI.zoneEndY() - 32, this.parentUI.zoneEndX() - getContactsPaneSize(), this.parentUI.zoneEndY() - 30, 0x00000000, 0xff000000);
		
		//PlexUIScreen.drawRect(this.parentUI.zoneStartX(), this.parentUI.zoneStartX() - 30, this.parentUI.zoneEndX(), this.parentUI.zoneEndY(), 0xff000000);
		Plex.renderUtils.drawScaledHorizontalLine(this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneEndX(), this.parentUI.zoneStartY(), 1.0F, PlexCoreUtils.globalChromaCycle());
		Plex.renderUtils.drawScaledHorizontalLine(this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneEndX(), this.parentUI.zoneEndY() - 1, 1.0F, PlexCoreUtils.globalChromaCycle());
		Plex.renderUtils.drawScaledHorizontalLine(this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneEndX(), this.parentUI.zoneEndY() - 2, 1.0F, PlexCoreUtils.globalChromaCycle());
		Plex.renderUtils.drawScaledVerticalLine(this.parentUI.zoneEndX() - (getContactsPaneSize()), this.parentUI.zoneStartY(), this.parentUI.zoneEndY(), 1.0F, PlexCoreUtils.globalChromaCycle());

		this.newConversationWindow.drawScreen(par1, par2, par3);
		this.messageField.drawScreen(par1, par2, par3);
		this.searchBox.drawScreen(par1, par2, par3);
		this.channelProgressBar.drawScreen(par1, par2, par3);
		this.emoteTooltip.drawScreen(par1, par2, par3);
		
		
		if (false) {
			PlexMessagingMessageHoverState selectedMessage = this.chatWindow.getMouseOverMessage(par1, par2);
			if (selectedMessage != null) {
				if (selectedMessage.selectedLine != null) {
					Plex.renderUtils.drawScaledString(selectedMessage.selectedLine.text, (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 5, 0xffffff, 0.5F, false);
				}
				if (selectedMessage.selectedWord != null) {
					Plex.renderUtils.drawScaledString(selectedMessage.selectedWord, (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 10, 0xffffff, 0.5F, false);
				}
				if (selectedMessage.messageSelected) {
					Plex.renderUtils.drawScaledString("m", (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 15, 0xffffff, 0.5F, false);
				}
				if (selectedMessage.localStringOffset != null) {
					Plex.renderUtils.drawScaledString("l " + selectedMessage.localStringOffset, (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 20, 0xffffff, 0.5F, false);
				}
				if (selectedMessage.globalStringOffset != null) {
					Plex.renderUtils.drawScaledString("g " + selectedMessage.globalStringOffset, (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 25, 0xffffff, 0.5F, false);
					Plex.renderUtils.drawScaledString("\"" + selectedMessage.message.getBreakdownItemByIndex(selectedMessage.globalStringOffset) + "\"", (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 30, 0xffffff, 0.5F, false);

				}
			}
		}
		
		//String drawText = "";
		//Plex.renderUtils.drawScaledString(drawText, (float)this.parentUI.zoneStartX() + 5, (float)this.parentUI.zoneStartY() + 5, 0xffffff, 0.5F, false);

		previousSentMessages = this.messageField.getPreviousSentMessages();
		lastContactsScroll = this.contactsList.scrollbar.realScrollValue;
		this.lastSearchText = this.searchBox.text.getText();
		
		if (this.getChannelManager().selectedChannel != null) {
			this.getChannelManager().selectedChannel.lastTextTyped = this.messageField.text.getText();
			this.getChannelManager().selectedChannel.lastChannelScroll = this.chatWindow.scrollbar.realScrollValue;
		}
	}

	public void selectMessageField() {
		this.searchBox.text.setFocused(false);
		this.messageField.text.setFocused(true);
		this.messageField.text.setCanLoseFocus(false);
	}

	public void selectSearchBox() {
		this.searchBox.text.setFocused(true);
		this.messageField.text.setCanLoseFocus(true);
		this.messageField.text.setFocused(false);
	}

	public PlexMessagingUIScreen setQuickChat() {
		this.isQuickChat = true;
		return this;
	}

	public void handleSendButton() {
		if (this.getChannelManager().selectedChannel == null) {
			PlexMessagingChannelClassWrapper selectedType = this.newConversationWindow.getSelectedItem();
			if (selectedType == null) {
				return;
			}
			String text = this.messageField.text.getText().trim();
			String channelName = selectedType.getChannelNameFromText(text);
			String recipientEntityName = selectedType.getRecipientEntityNameFromText(text);
			this.getChannelManager().setSelectedChannel(this.getMessagingMod().getChannel(channelName, selectedType.channelClass, recipientEntityName));
			this.getChannelManager().selectedChannel.hiddenFromList = false;
			if (selectedType.getAutoCommandFromText(text) != null) {
				Plex.minecraft.thePlayer.sendChatMessage(selectedType.getAutoCommandFromText(text));
			}
			this.messageField.text.setText("");
			return;
		}
		this.sendMessage();
		this.searchBox.text.setFocused(false);
		if (this.isQuickChat) {
			PlexCore.displayUIScreen(null);
		}
	}
	
	public void sendMessage() {
		if (this.getChannelManager().selectedChannel != null) {
			if (this.getChannelManager().selectedChannel.channelReady) {
				this.getChannelManager().selectedChannel.sendMessage(this.messageField.text.getText());
				this.messageField.addToSentMessages(this.messageField.text.getText());
				this.messageField.text.setText("");
				this.messageField.resetSentMessagesIndex();
				this.getChannelManager().selectedChannel.lastTextTyped = "";
			}
		}
	}
	
	public Boolean isSelectedChannelReady() {
		if (this.getChannelManager().selectedChannel != null) {
			return this.getChannelManager().selectedChannel.channelReady;
		}
		return false;
	}

	public Boolean isSelectedChannelErrored() {
		if (this.getChannelManager().selectedChannel != null) {
			return this.getChannelManager().selectedChannel.connectFailed;
		}
		return false;
	}

	public void scrollContactsBy(int by) {
		this.scrollListBy(this.contactsList, by);
	}

	public void scrollChatSelectionBy(int by) {
		this.scrollListBy(this.newConversationWindow.channelSelection, by);
	}

	public void scrollListBy(PlexUIScrolledItemList list, int by) {
		this.scrollListBy(list, by, false);
	}

	public void scrollListBy(PlexUIScrolledItemList list, int by, boolean reselect) {
		int selectedIndex = -1;
		List<? extends PlexUIScrolledItem> items = list.getVisibleItems();
		if (items.size() == 0) {
			return;
		}
		for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
			if (items.get(itemIndex).listItemIsSelected() && selectedIndex == -1) {
				selectedIndex = itemIndex;
			}
		}
		PlexUIScrolledItem selectItem = items.get(PlexCoreUtils.clamp(selectedIndex + by, 0, items.size() - 1));
		for (PlexUIScrolledItem item : list.getAllItems()) {
			if (item.equals(selectItem)) {
				if (!item.listItemIsSelected() || reselect) {
					item.listItemClick();
					item.listItemSelect();
				}
			}
			else {
				item.listItemOtherItemClicked();
			}
		}
		list.scrollToItemIfNotCompletelyInView(selectItem);
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
		//PlexDirectMessagingUI.lastTextInBox = this.messageField.text.getText();
	//}
	
	// this was a hack i wrote because i dont know how to use lwjgl
	
//	@SubscribeEvent
//	public void clientTick(ClientTickEvent event) {
//		if (lastKeyTyped != null && lastTypedTime != null && lastKeyCodeTyped != null) {
//			if (Keyboard.isKeyDown(lastKeyCodeTyped)) {
//				if (Minecraft.getSystemTime() > lastTypedTime + 600L && Minecraft.getSystemTime() > lastRepetition + 0L) {
//					this.messageField.keyTyped(lastKeyTyped, lastKeyCodeTyped);
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
