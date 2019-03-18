package pw.ipex.plex.ui.widget.itemlist;

public interface PlexUIScrolledItem {
	
	public boolean listItemIsSelected();
	
	public void listItemSelect();

	public void listItemClick();

	public void listItemOtherItemClicked();
	
	public int listItemGetHeight();
	
	public String listItemGetText();
	
	public String listItemGetSearchText();
	
	public void listItemRenderText(int x, int y, int cellWidth, int cellHeight, float alpha, boolean selected, boolean mouseOver);
	
	public int listItemGetForegroundColour();
	
}
