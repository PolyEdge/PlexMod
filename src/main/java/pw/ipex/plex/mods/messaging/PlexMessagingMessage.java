package pw.ipex.plex.mods.messaging;

import java.time.Instant;

public class PlexMessagingMessage {
	public int TYPE_CHAT_MESSAGE = 0;
	public int TYPE_SYSTEM_MESSAGE = 1;
	
	public int POSITION_LEFT = 0;
	public int POSITION_RIGHT = 1;
	
	public Integer type;
	public String content;
	public Integer colour;
	public Integer backgroundColour;
	public String fromUser;
	public Long time;
	public Integer position = 0;
	public String playerHead = null;
	
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
