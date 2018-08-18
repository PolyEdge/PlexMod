package pw.ipex.plex.ui;

public interface PlexUIScrolledItem {
	
	public boolean listItemIsSelected();
	
	public void listItemSelect();
	
	public int listItemGetHeight();
	
	public String listItemGetText();
	
	public void listItemRenderText(int x, int y, int cellWidth, int cellHeight, boolean selected, boolean mouseOver);
	
	public int listItemGetForegroundColour();
	
}
