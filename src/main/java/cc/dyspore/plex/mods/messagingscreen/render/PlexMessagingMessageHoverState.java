package cc.dyspore.plex.mods.messagingscreen.render;

import cc.dyspore.plex.mods.messagingscreen.PlexMessagingMessage;

public class PlexMessagingMessageHoverState {
	public boolean isSelected = false;

	public int mouseX = 0;
	public int mouseY = 0;
	public String selectedWord = null;
	public int localStringOffset;
	public int globalStringOffset;
	public PlexMessagingMessageTextData selectedLine = null;
	public boolean messageSelected = false;
	public boolean headSelected = false;
	public boolean authorSelected = false;
	public PlexMessagingMessage message;
	
	private PlexMessagingMessageHoverState setSelected(boolean selected) {
		this.isSelected = selected;
		return this;
	}
	
	private PlexMessagingMessageHoverState setSelectedIfTrue(boolean ifTrue) {
		if (ifTrue) {
			this.isSelected = true;
		}
		return this;
	}
	
	public PlexMessagingMessageHoverState setMessageSelected(boolean selected) {
		this.setSelectedIfTrue(selected);
		this.messageSelected = selected;
		return this;
	}
	
	public PlexMessagingMessageHoverState setAuthorSelected(boolean selected) {
		this.setSelectedIfTrue(selected);
		this.authorSelected = selected;
		return this;
	}
	
	public PlexMessagingMessageHoverState setHeadSelected(boolean selected) {
		this.setSelectedIfTrue(selected);
		this.headSelected = selected;
		return this;
	}
	
	public PlexMessagingMessageHoverState setMessage(PlexMessagingMessage message) {
		this.message = message;
		return this;
	}
	
	public PlexMessagingMessageHoverState setSelectedWord(String word) {
		this.setSelectedIfTrue(word != null);
		this.selectedWord = word;
		return this;
	}

	public PlexMessagingMessageHoverState setSelectedLine(PlexMessagingMessageTextData line) {
		this.setSelectedIfTrue(line != null);
		this.selectedLine = line;
		return this;
	}

	public PlexMessagingMessageHoverState setHoveredLocalStringOffset(int stringOffset) {
		this.setSelectedIfTrue(stringOffset != -1);
		this.localStringOffset = stringOffset;
		return this;
	}

	public PlexMessagingMessageHoverState setHoveredGlobalStringOffset(int stringOffset) {
		this.setSelectedIfTrue(stringOffset != -1);
		this.globalStringOffset = stringOffset;
		return this;
	}
}
