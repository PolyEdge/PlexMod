package pw.ipex.plex.mods.messaging;

public class PlexMessagingMessageHoverState {
	public boolean IS_SELECTED = false;
	
	public String selectedWord = null;
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
}
