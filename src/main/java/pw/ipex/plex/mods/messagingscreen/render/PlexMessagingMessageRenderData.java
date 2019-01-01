package pw.ipex.plex.mods.messagingscreen.render;

import java.util.ArrayList;
import java.util.List;

public class PlexMessagingMessageRenderData {
	public Integer relativeX = 0; // null = centered
	public int relativeY = 0;
	
	public int totalHeight = 0;
	public int maxWidth = 0;
	
	public String authorName = "";
	public boolean authorVisible = false;
	public int authorX = 0;
	public int authorY = 0;
	public float authorScale = 1.0F;
	
	public List<PlexMessagingMessageTextData> textLines;
	
	public String playerHead = null;
	public int playerHeadX = 0;
	public int playerHeadY = 0;
	public int playerHeadSize = 0;
	public boolean headsShown = false;
	
	public int textBackdropX = 0;
	public int textBackdropY = 0;
	public int textBackdropWidth = 0;
	public int textBackdropHeight = 0;
	public boolean displayBackdrop = false;
	
	public int textColour = 0xffffff;
	public int backdropColour = 0x85454545;
	
	public PlexMessagingMessageRenderState renderState = new PlexMessagingMessageRenderState();
	
	public PlexMessagingMessageRenderData() {
		this.textLines = new ArrayList<PlexMessagingMessageTextData>();
	}
	
	public void addTextLine(String text, float scale, int x, int y, int width, int colour) {
		PlexMessagingMessageTextData textData = new PlexMessagingMessageTextData();
		textData.text = text;
		textData.scale = scale;
		textData.x = x;
		textData.y = y;
		textData.width = width;
		textData.colour = colour;
		this.textLines.add(textData);
	}

	public void addTextLine(String text, float scale, int x, int y, int width, int colour, int charOffset) {
		PlexMessagingMessageTextData textData = new PlexMessagingMessageTextData();
		textData.text = text;
		textData.scale = scale;
		textData.x = x;
		textData.y = y;
		textData.width = width;
		textData.colour = colour;
		textData.stringOffset = charOffset;
		this.textLines.add(textData);
	}
	
	public int getXPosition(int startX, int endX) {
		if (this.relativeX == null) {
			return startX + ((endX - startX) / 2);
		}
		if (this.relativeX < 0) {
			return endX + this.relativeX;
		}
		return startX + this.relativeX;
	}

	public int getYPosition(int yPos) {
		return yPos + this.relativeY;
	}
	
	public int getItemXPosition(int startX, int endX, int itemXPos) {
		return this.getXPosition(startX, endX) + itemXPos;
	}
	
	public int getItemYPosition(int yPos, int itemYpos) {
		return this.getYPosition(yPos) + itemYpos;
	}
}
