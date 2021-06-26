package structures;

import java.util.Map;

import structures.basic.Unit;

public abstract class State {
	
	public void next(Map<String, Object> context) {
	}
	
	public String getStateName() {
		return null;
	}
	
}
