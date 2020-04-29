package cc.dyspore.plex.ui.widget.itemlist;

public interface PlexUIScrolledItem {
	
	boolean listItemIsSelected();
	
	void listItemSelect();

	void listItemClick();

	void listItemOtherItemClicked();
	
	int listItemGetHeight();
	
	String listItemGetText();
	
	String listItemGetSearchText();
	
	void listItemRenderText(int x, int y, int cellWidth, int cellHeight, float alpha, boolean selected, boolean mouseOver);
	
	int listItemGetForegroundColour();
}
