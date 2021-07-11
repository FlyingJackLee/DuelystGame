package structures.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import commands.BasicCommands;
import structures.GameState;
import structures.Observer;
import utils.ToolBox;

import java.util.HashMap;
import java.util.Locale;

import java.util.Map;
import java.util.function.Function;
import java.util.Map.Entry;

/**
 * This is a representation of a Unit on the game board.
 * A unit has a unique id (this is used by the front-end.
 * Each unit has a current UnitAnimationType, e.g. move,f
 * or attack. The position is the physical position on the
 * board. UnitAnimationSet contains the underlying information
 * about the animation frames, while ImageCorrection has
 * information for centering the unit on the tile. 
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class  Unit extends Observer {

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file

	int id;
	UnitAnimationType animation;
	Position position;
	UnitAnimationSet animations;
	ImageCorrection correction;

	public Unit() {
	}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;

		position = new Position(0, 0, 0, 0);
		this.correction = correction;
		this.animations = animations;
	}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;

		position = new Position(currentTile.getXpos(), currentTile.getYpos(), currentTile.getTilex(), currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
	}


	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
				ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = animation;
		this.position = position;
		this.animations = animations;
		this.correction = correction;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UnitAnimationType getAnimation() {
		return animation;
	}

	public void setAnimation(UnitAnimationType animation) {
		this.animation = animation;
	}

	public ImageCorrection getCorrection() {
		return correction;
	}

	public void setCorrection(ImageCorrection correction) {
		this.correction = correction;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public UnitAnimationSet getAnimations() {
		return animations;
	}

	public void setAnimations(UnitAnimationSet animations) {
		this.animations = animations;
	}

	/**
	 * This command sets the position of the Unit to a specified
	 * tile.
	 *
	 * @param tile
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(), tile.getYpos(), tile.getTilex(), tile.getTiley());
	}


	private int maxHealth;

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}


	public enum UnitState {
		//the unit is ready after the next turn of summon
		NOT_READY, READY, HAS_MOVED, HAS_ATTACKED

	}


	private UnitState currentState = UnitState.NOT_READY;

	public void setCurrentState(UnitState currentState) {
		this.currentState = currentState;
	}


	private Player owner = GameState.getInstance().getCurrentPlayer();

	public UnitState getCurrentState() {
		return currentState;
	}


	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}


	private int attack = 0;

	public void setAttack(int attack) {
		this.attack = attack;
	}


	public int getAttack() {
		return attack;
	}


	private int health = 0;

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
		// if it is a avatar, set player's health
		if (this.getId() == 99 || this.getId() == 100) {
			this.getOwner().setHealth(health);
		}
	}

	public void changeHealth(int health, boolean canTakeOverMax) {
		//Unit is set to a health bigger than maxHealth
		if(health > maxHealth){
			if(canTakeOverMax){
				BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this, health);
			} else {
				health = maxHealth;
				BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this, health);
			}
		}
		//Unit dies
		else if (health <1) {
			// Callback Point: <UnitDeathCallBacks>
			// run callbacks when a unit is dead
			int id = this.getId();
			if (GameState.getInstance().getUnitDeathCallbacks().get(String.valueOf(id)) != null) {
				// call the callback
				GameState.getInstance().getUnitDeathCallbacks().get(String.valueOf(id)).apply(id);
			}

			health = 0;
			BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this, health);
			BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), this, UnitAnimationType.death);
			try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

			BasicCommands.deleteUnit(GameState.getInstance().getOut(), this);

			Map<String, Object> newParameters = new HashMap<>();
			newParameters.put("type", "deleteUnit");
			newParameters.put("tilex", this.getPosition().getTilex());
			newParameters.put("tiley", this.getPosition().getTiley());
			GameState.getInstance().broadcastEvent(Tile.class, newParameters);
			if (this.getId() == 100) {
				ToolBox.logNotification("Congratulations, You Win!!!");
			}
			else if (this.getId() == 99) {
				ToolBox.logNotification("Unfortunately, You Lost > <!!!");
				((AIPlayer)GameState.getInstance().getCurrentPlayer()).closeAIMode();
			}
		}

		else{ // if health is not bigger than max and does not die
			BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this, health);
		}
		this.setHealth(health);
	}

	public void changeAttack(int attack) {
		this.attack = attack;
		BasicCommands.setUnitAttack(GameState.getInstance().getOut(), this, this.attack);
	}

	boolean rangedAttack = false; // can rangedAttack
	boolean flying = false; // can move anywhere
	
	//By default every unit can only attack and move once each turn
	int attackNum = 1;
	int moveNum = 1;
	
	public int getAttackNum() {return attackNum;}
	public void setAttackNum(int attackNum) {this.attackNum = attackNum;}
	
	public int getMoveNum() {return moveNum;}
	public void setMoveNum(int moveNum) {this.moveNum = moveNum;}
	
	boolean canProvoke = false;
	public boolean getCanProvoke() {return this.canProvoke;}
	public void setCanProvoke(boolean canProvoke) {this.canProvoke = canProvoke;}
	
	boolean isProvoked = false;
	public boolean isProvoked() {return isProvoked;}
	public void setProvoked(boolean isProvoked) {this.isProvoked = isProvoked;}



	@Override
	public void trigger(Class target, Map<String, Object> parameters) {
		if (this.getClass().equals(target)) {
			if (parameters.get("type").equals("unitBeReady")) {
				if (this.owner == GameState.getInstance().getCurrentPlayer()) {
					this.currentState = UnitState.READY;
				} else {
					this.currentState = UnitState.NOT_READY;
				}
			}
			else if (parameters.get("type").equals("attacked")) {
				if (parameters.get("attackedUnit").equals(this)) {
					Unit attackedUnit = this;
					Unit attackerUnit = (Unit) parameters.get("attackerUnit");

					//Attack First time, allow counter attack.
					attackedUnit.attacked(attackerUnit, true);


				}
			}

			//modify a unit(backend and frontend(if it has rendered))
			else if (parameters.get("type").equals("modifyUnit")) {
				if (this.id == (Integer) parameters.get("unitId")) {
					int newHealth = this.health + (Integer) parameters.get("health");
					int newAttack = this.attack + (Integer) parameters.get("attack");

					if (parameters.get("limit") != null){
						if ( parameters.get("limit").equals("max") && newHealth > maxHealth)
						{
							ToolBox.logNotification("Cannot exceed the max health");
							newHealth = maxHealth;
						}
						//if the modification is intended to call in the enemy turn but it is not.
						if (parameters.get("limit").equals("enemyTurn") && GameState.getInstance().getCurrentPlayer() == this.owner){
							return;
						}
					}


					this.setHealth(newHealth);
					this.setAttack(newAttack);

					displayAttackAndHealth();

				}

			}
		}
	}

	/**
	 * Display the attack and health in the front Page
	 */
	protected void displayAttackAndHealth() {
		BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this, this.health);
		BasicCommands.setUnitAttack(GameState.getInstance().getOut(), this, this.attack);

	}


	private void attacked(Unit attacker, boolean allowCounterAttack) {
		// Callback Point: <AvatarAttackCallBacks>
		// run callbacks when a avatar is attacked
		int id = attacker.getId();
		if (id == 99) {
			if (GameState.getInstance().getAvatarAttackCallbacks().size() != 0) {
				// call the callback
				for (Entry<String,Function<Integer,Boolean>> entry:GameState.getInstance().getAvatarAttackCallbacks().entrySet()
					 ) {
					entry.getValue().apply(Integer.parseInt(entry.getKey()));
				}
			}
		}

		BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), attacker, UnitAnimationType.attack);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), attacker, UnitAnimationType.idle);

		this.changeHealth(this.getHealth() - attacker.getAttack(), false);

		// if this unit survives, is allow to counter attack, and the attacker is in the attack range
		if (allowCounterAttack && this.health >= 1
				&& this.targetIsInAttackRange(attacker.getPosition().getTilex(), attacker.getPosition().getTiley())) {
			attacker.attacked(this, false); //counter attack not allow attack.
		}
	}

	private boolean targetIsInAttackRange(int tilex, int tiley) {
		int[] offsetx = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
		int[] offsety = new int[]{0, 1, 1, 1, 0, -1, -1, -1};
		for (int i = 0; i < offsetx.length; i++) {
			int newTileX = this.getPosition().getTilex() + offsetx[i];
			int newTileY = this.getPosition().getTiley() + offsety[i];
			if (tilex == newTileX && tiley == newTileY) {
				return true;
			}
		}
		return false;
	}


}
