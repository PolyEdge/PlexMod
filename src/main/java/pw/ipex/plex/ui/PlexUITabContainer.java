package pw.ipex.plex.ui;

public class PlexUITabContainer {
	public Class<? extends PlexUIBase> uiClass;
	public String tabLabel;
	public int id;
	
	public PlexUITabContainer(Class<? extends PlexUIBase> uiClass, String label) {
		this.uiClass = uiClass;
		this.tabLabel = label;
	}
	
	public String getLabel() {
		return this.tabLabel;
	}
	
	public String getUiClass() {
		return this.getUiClass();
	}
	
	public int getID() {
		return this.id;
	}
	
	public PlexUITabContainer setID(int id) {
		this.id = id;
		return this;
	}
	
	public PlexUITabContainer getShallowCopy() {
		return new PlexUITabContainer(this.uiClass, this.tabLabel);
	}
}
