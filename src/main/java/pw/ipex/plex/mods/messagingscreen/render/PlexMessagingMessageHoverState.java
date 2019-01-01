package pw.ipex.plex.mods.messagingscreen.render;

import pw.ipex.plex.mods.messagingscreen.PlexMessagingMessage;

public class PlexMessagingMessageHoverState {
	public boolean IS_SELECTED = false;

	public int mouseX = 0;
	public int mouseY = 0;
	public String selectedWord = null;
	public Integer localStringOffset = null;
	public Integer globalStringOffset = null;
	public PlexMessagingMessageTextData selectedLine = null;
	public boolean messageSelected = false;
	public boolean headSelected = false;
	public boolean authorSelected = false;
	public PlexMessagingMessage message;
	
	public PlexMessagingMessageHoverState SET_SELECTED(boolean selected) {
		this.IS_SELECTED = selected;
		return this;
	}
	
	public PlexMessagingMessageHoverState SET_SELECTED_IF_TRUE(boolean ifTrue) {
		if (ifTrue) {
			this.IS_SELECTED = true;
		}
		return this;
	}
	
	public PlexMessagingMessageHoverState setMessageSelected(boolean selected) {
		this.SET_SELECTED_IF_TRUE(selected);
		this.messageSelected = selected;
		return this;
	}
	
	public PlexMessagingMessageHoverState setAuthorSelected(boolean selected) {
		this.SET_SELECTED_IF_TRUE(selected);
		this.authorSelected = selected;
		return this;
	}
	
	public PlexMessagingMessageHoverState setHeadSelected(boolean selected) {
		this.SET_SELECTED_IF_TRUE(selected);
		this.headSelected = selected;
		return this;
	}
	
	public PlexMessagingMessageHoverState setMessage(PlexMessagingMessage message) {
		this.message = message;
		return this;
	}
	
	public PlexMessagingMessageHoverState setSelectedWord(String word) {
		this.SET_SELECTED_IF_TRUE(word != null);
		this.selectedWord = word;
		return this;
	}

	public PlexMessagingMessageHoverState setSelectedLine(PlexMessagingMessageTextData line) {
		this.SET_SELECTED_IF_TRUE(line != null);
		this.selectedLine = line;
		return this;
	}

	public PlexMessagingMessageHoverState setHoveredLocalStringOffset(int stringOffset) {
		this.SET_SELECTED_IF_TRUE(stringOffset != -1);
		this.localStringOffset = stringOffset;
		return this;
	}

	public PlexMessagingMessageHoverState setHoveredGlobalStringOffset(int stringOffset) {
		this.SET_SELECTED_IF_TRUE(stringOffset != -1);
		this.globalStringOffset = stringOffset;
		return this;
	}
}
