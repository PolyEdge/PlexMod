package pw.ipex.plex.core;

public class PlexCoreValue {
	public String name;
	public Boolean booleanValue;
	public String stringValue;
	public Float floatValue;
	public Integer integerValue;
	public Long longValue;
	public Double doubleValue;
	public Object objectValue;
	
	public PlexCoreValue(String name) {
		this.name = name;
		PlexCore.registerSharedValue(this);
	}
	
	public PlexCoreValue(String name, Boolean initVal) {
		this.name = name;
		this.booleanValue = initVal;
		PlexCore.registerSharedValue(this);
	}
	
	public PlexCoreValue(String name, String initVal) {
		this.name = name;
		this.stringValue = initVal;
		PlexCore.registerSharedValue(this);
	}
	
	public PlexCoreValue(String name, Float initVal) {
		this.name = name;
		this.floatValue = initVal;
		PlexCore.registerSharedValue(this);
	}
	
	public PlexCoreValue(String name, Integer initVal) {
		this.name = name;
		this.integerValue = initVal;
		PlexCore.registerSharedValue(this);
	}
	
	public PlexCoreValue(String name, Long initVal) {
		this.name = name;
		this.longValue = initVal;
		PlexCore.registerSharedValue(this);
	}
	
	public PlexCoreValue(String name, Double initVal) {
		this.name = name;
		this.doubleValue = initVal;
		PlexCore.registerSharedValue(this);
	}

	public void set(Boolean value) {
		this.booleanValue = value;
	}

	public void set(String value) {
		this.stringValue = value;
	}
	
	public void set(Float value) {
		this.floatValue = value;
	}
	
	public void set(Integer value) {
		this.integerValue = value;
	}
	
	public void set(Long value) {
		this.longValue = value;
	}

	public void set(Double value) {
		this.doubleValue = value;
	}
	
	public void setObject(Object value) {
		this.objectValue = value;
	}
}
