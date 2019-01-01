package pw.ipex.plex.core.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pw.ipex.plex.core.PlexCoreUtils;

public class PlexCoreRenderColourState {
	public Map<String, Integer> colours = new HashMap<String, Integer>();
		
	public int getRealColour(int colour) {
		return colour == -1 ? PlexCoreUtils.globalChromaCycle() : colour;
	}
	
	public void setColour(String name, int colour) {
		this.colours.put(name, colour);
	}
	
	public Integer getColour(String name) {
		return this.getColour(name, null);
	}
	
	public List<String> getColourList() {
		return new ArrayList<>(this.colours.keySet());
	}
	
	public Integer getColour(String name, Integer def) {
		if (this.colours.containsKey(name)) {
			return this.getRealColour(this.colours.get(name));
		}
		return def;
	}
	
	public Integer colourBetween(Integer colour1, Integer colour2, Float between) {
		return this.colourBetween(colour1, colour2, between, null);
	}
	
	public Integer colourBetween(Integer colour1, Integer colour2, Float between, Integer def) {
		between = between == null ? 1.0F : PlexCoreUtils.floatRange(between, 0.0F, 1.0F);
		if (colour1 == null && colour2 == null) {
			return def;
		}
		if (colour1 == null) {
			return colour2;
		}
		if (colour2 == null) {
			return colour1;
		}
		if (between == 0.0F) {
			return colour1;
		}
		if (between == 1.0F) {
			return colour2;
		}
		return PlexCoreUtils.betweenColours(this.getRealColour(colour1), this.getRealColour(colour2), between);
	}
	
	public Integer colourBetweenStates(String colour, PlexCoreRenderColourState state1, PlexCoreRenderColourState state2, Float between) {
		return this.colourBetweenStates(colour, state1, state2, between, null);
	}
	
	public Integer colourBetweenStates(String colour, PlexCoreRenderColourState state1, PlexCoreRenderColourState state2, Float between, Integer def) {
		between = between == null ? 1.0F : PlexCoreUtils.floatRange(between, 0.0F, 1.0F);
		if ((state1 == null && state2 == null) || colour == null) {
			return def;
		}
		if (state1 == null) {
			return state2.getColour(colour, def);
		}
		if (state2 == null) {
			return state1.getColour(colour, def);
		}
		if (between == 0.0F) {
			return state1.getColour(colour, def);
		}
		if (between == 1.0F) {
			return state2.getColour(colour, def);
		}
		if (state1.getColour(colour, null) == null) {
			return state2.getColour(colour, def);
		}
		if (state2.getColour(colour, null) == null) {
			return state1.getColour(colour, def);
		}
		return PlexCoreUtils.betweenColours(state1.getColour(colour, def), state2.getColour(colour, def), between);
	}
	
	public PlexCoreRenderColourState colourStateBetween(PlexCoreRenderColourState state1, PlexCoreRenderColourState state2, Float between) {
		between = between == null ? 1.0F : PlexCoreUtils.floatRange(between, 0.0F, 1.0F);
		if (state1 == null && state2 == null) {
			return null;
		}
		if (state1 == null) {
			return state2;
		}
		if (state2 == null) {
			return state1;
		}
		if (between == 0.0F) {
			return state1;
		}
		if (between == 1.0F) {
			return state2;
		}
		List<String> allColours = new ArrayList<String>();
		allColours.addAll(state1.getColourList());
		allColours.addAll(state2.getColourList());
		PlexCoreRenderColourState newState = new PlexCoreRenderColourState();
		
		for (String colour : allColours) {
			if (newState.getColourList().contains(colour)) {
				continue;
			}
			newState.setColour(colour, this.colourBetween(state1.getColour(colour, null), state2.getColour(colour, null), between));
		}
		
		return newState;
	}
}
