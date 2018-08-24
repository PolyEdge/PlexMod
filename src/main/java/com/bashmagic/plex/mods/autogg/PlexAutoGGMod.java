package com.bashmagic.plex.mods.autogg;

import net.minecraftforge.common.MinecraftForge;
import pw.ipex.plex.core.PlexCore;
import pw.ipex.plex.mod.PlexModBase;

public class PlexAutoGGMod extends PlexModBase {

	public static final String NAME = "AutoGG";
	public static final String UI_TITLE = "Auto GG";

	private static PlexAutoGGMod instance;
	private boolean autoGGEnabled;
	private String autoGGMessage;
	private PlexAutoGGChatHandler handler;

	@Override
	public String getModName() {
		return NAME;
	}

	@Override
	public void modInit() {
		instance = this;
		autoGGEnabled = modSetting("autogg", false).getBoolean();
		autoGGMessage = modSetting("autogg-message", "gg").getString();
		handler = new PlexAutoGGChatHandler();
		PlexCore.registerUiTab(UI_TITLE, PlexAutoGGUI.class);
	}

	@Override
	public void joinedMineplex() {
		MinecraftForge.EVENT_BUS.register(handler);
	}

	@Override
	public void leftMineplex() {
		MinecraftForge.EVENT_BUS.unregister(handler);
	}

	@Override
	public void saveModConfig() {
		modSetting("autogg", false).set(autoGGEnabled);
		modSetting("autogg-message", "gg").set(autoGGMessage);
	}

	@Override
	public void switchedLobby(String name) {
	}

	public static PlexAutoGGMod getInstance() {
		return instance;
	}

	public boolean isAutoGGEnabled() {
		return autoGGEnabled;
	}

	public void setAutoGGEnabled(boolean value) {
		autoGGEnabled = value;
	}

	public String getAutoGGMessage() {
		return autoGGMessage;
	}

	public void setAutoGGMessage(String value) {
		autoGGMessage = value;
	}

}
