package cc.dyspore.plex.mods.messagingscreen;
import java.util.*;

import cc.dyspore.plex.mods.messagingscreen.callback.PlexMessagingMessageEventHandler;
import cc.dyspore.plex.mods.messagingscreen.channel.PlexMessagingChannelBase;
import cc.dyspore.plex.mods.messagingscreen.render.PlexMessagingMessageRenderData;
import cc.dyspore.plex.mods.messagingscreen.translate.PlexMessagingChatMessageAdapter;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import cc.dyspore.plex.core.regex.PlexCoreRegexEntry;

public class PlexMessagingMessage {
	public int TYPE_CHAT_MESSAGE = 0;
	public int TYPE_SYSTEM_MESSAGE = 1;
	
	public int POSITION_LEFT = 0;
	public int POSITION_RIGHT = 1;
	
	public Integer type = 0;
	public String content = "";
	public Integer defaultColour = 0xb5b5b5;
	public Integer colour;
	public Integer defaultBackgroundColour = 0x65757575;
	public Integer backgroundColour;
	public String author = "";
	public Long time = 0L;
	public Integer position = 0;
	public String playerHead = null;

	public Boolean countsAsUnread = true;
	public Boolean updatesChannelActivity = true;

	public PlexMessagingChatMessageAdapter parentAdapter = null;
	public PlexMessagingChannelBase channel;
	
	public Map<String, String> tags = new HashMap<String, String>();
	
	public List<PlexMessagingMessageEventHandler> callbacks = new ArrayList<PlexMessagingMessageEventHandler>();
	public PlexCoreRegexEntry chatRegex = null;
	
	public PlexMessagingMessageRenderData cachedRenderData;
	public Map<Integer, String> messageBreakdown;
	
	public PlexMessagingMessage setTag(String key, String value) {
		this.tags.put(key, value);
		return this;
	}
	
	public boolean hasTag(String key) {
		return this.tags.containsKey(key);
	}
	
	public String getTag(String key) {
		return this.tags.get(key);
	}
	
	public PlexMessagingMessage setNow() {
		this.setTime(Minecraft.getSystemTime());
		return this;
	}
	
	public PlexMessagingMessage setTime(Long time) {
		this.time = time;
		return this;
	}
	
	public PlexMessagingMessage setLeft() {
		this.position = 0;
		return this;
	}
	
	public PlexMessagingMessage setRight() {
		this.position = 1;
		return this;
	}
	
	public PlexMessagingMessage setAuthor(String user) {
		this.author = user;
		return this;
	}
	
	public PlexMessagingMessage setColour(Integer colour) {
		this.colour = colour;
		return this;
	}
	
	public PlexMessagingMessage setBackgroundColour(Integer colour) {
		this.backgroundColour = colour;
		return this;
	}
	
	public PlexMessagingMessage setContent(String content) {
		this.content = content;
		return this;
	}

	public PlexMessagingMessage setTimestampMessage() {
		this.type = 2;
		return this;
	}
	
	public PlexMessagingMessage setSystemMessage() {
		this.type = 1;
		return this;
	}
	
	public PlexMessagingMessage setChatMessage() {
		this.type = 0;
		return this;
	}
	
	public PlexMessagingMessage setHead(String head) {
		this.playerHead = head;
		return this;
	}
	
	public PlexMessagingMessage setChannel(PlexMessagingChannelBase channel) {
		this.channel = channel;
		return this;
	}
	
	public PlexMessagingMessage addCallback(PlexMessagingMessageEventHandler callback) {
		this.callbacks.add(callback);
		return this;
	}
	
	public int getColour() {
		if (this.colour != null) {
			return this.colour;
		}
		if (this.channel != null) {
			return this.channel.getDisplayColour();
		}
		return this.defaultColour;
	}
	
	public int getBackgroundColour() {
		if (this.backgroundColour != null) {
			return this.backgroundColour;
		}
		if (this.channel != null) {
			return this.channel.getMessageBackgroundColour();
		}
		return this.defaultColour;
	}

	public String getBreakdownItemByIndex(int index) {
		if (this.messageBreakdown == null || index < 0) {
			return null;
		}
		String item = null;
		for (int entryMin : this.messageBreakdown.keySet()) {
			if (index >= entryMin) {
				item = this.messageBreakdown.get(entryMin);
			}
		}
		return item;
	}
	
	public JsonObject toJson() {
		JsonObject output = new JsonObject();
		output.addProperty("t", this.type);
		output.addProperty("c", this.content);
		output.addProperty("p", this.position);
		output.addProperty("fg", this.colour);
		output.addProperty("bg", this.backgroundColour);
		output.addProperty("a", this.author);
		output.addProperty("_t", this.time);
		output.addProperty("h", this.playerHead);
		return output;
	}
}
