package pw.ipex.plex.mods.messaging;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import pw.ipex.plex.core.PlexCoreChatRegexEntry;

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
	public String fromUser = "";
	public Long time = 0L;
	public Integer position = 0;
	public String playerHead = null;
	public PlexMessagingChannelBase parentChannel;
	
	public Map<String, String> tags = new HashMap<String, String>();
	
	public List<PlexMessagingMessageClickCallback> callbacks = new ArrayList<PlexMessagingMessageClickCallback>();
	public PlexCoreChatRegexEntry chatRegex = null;
	
	public PlexMessagingMessageRenderData cachedRenderData;
	
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
		this.fromUser = user;
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
		this.parentChannel = channel;
		return this;
	}
	
	public PlexMessagingMessage addCallback(PlexMessagingMessageClickCallback callback) {
		this.callbacks.add(callback);
		return this;
	}
	
	public int getColour() {
		if (this.colour != null) {
			return this.colour;
		}
		if (this.parentChannel != null) {
			return this.parentChannel.getDisplayColour();
		}
		return this.defaultColour;
	}
	
	public int getBackgroundColour() {
		if (this.backgroundColour != null) {
			return this.backgroundColour;
		}
		if (this.parentChannel != null) {
			return this.parentChannel.getMessageBackgroundColour();
		}
		return this.defaultColour;
	}
}
