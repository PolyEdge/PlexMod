package pw.ipex.plex.mods.messaging;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import pw.ipex.plex.core.PlexCoreChatRegexEntry;

public class PlexMessagingMessage {
	public int TYPE_CHAT_MESSAGE = 0;
	public int TYPE_SYSTEM_MESSAGE = 1;
	
	public int POSITION_LEFT = 0;
	public int POSITION_RIGHT = 1;
	
	public Integer type = 0;
	public String content = "";
	public Integer colour = 0xb5b5b5;
	public Integer backgroundColour = 0x65757575;
	public String fromUser = "";
	public Long time = 0L;
	public Integer position = 0;
	public String playerHead = null;
	
	public List<PlexMessagingMessageClickCallback> callbacks = new ArrayList<PlexMessagingMessageClickCallback>();
	public PlexCoreChatRegexEntry chatRegex = null;
	
	public PlexMessagingMessageRenderData cachedRenderData;
	
	public PlexMessagingMessage setNow() {
	    Instant instant = Instant.now();
		this.setTime(instant.getEpochSecond());
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
	
	public PlexMessagingMessage setUser(String user) {
		fromUser = user;
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
}
