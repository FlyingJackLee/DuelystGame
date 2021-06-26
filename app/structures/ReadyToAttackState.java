package structures;

import commands.BasicCommands;
import structures.basic.Unit;

public class ReadyToAttackState {
	
	public Unit previousUnit;
	public Unit currentUnit;
	public String stateName = "ReadyToAttack";
	
	public ReadyToAttackState(Unit previousUnit, Unit currentUnit) {
		this.previousUnit = previousUnit;
		this.currentUnit = currentUnit;
	}
	
	@Override
	public void next(Map<String, Object> context) {
		//Play animation.
		BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), previousUnit, UnitAnimationType.attack);
		
		//Set attacked unit health.
		int attackedHealth = currentUnit.getHealth()-previousUnit.getAttack();
		
		if(attackedHealth<1){
			
		}else{
		currentUnit.setHealth(attackedHealth);
		BasicCommands.setUnitHealth(GameState.getInstance().getOut(), currentUnit, attackedHealth);
		
		previousUnit = null;
		}
	}

	public String getStateName() {
		return stateName;
	}
}
