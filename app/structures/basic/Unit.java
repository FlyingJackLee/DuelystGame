package structures.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import commands.BasicCommands;
import structures.GameState;
import structures.Observer;
import utils.ToolBox;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a representation of a Unit on the game board.
 * A unit has a unique id (this is used by the front-end.
 * Each unit has a current UnitAnimationType, e.g. move,
 * or attack. The position is the physical position on the
 * board. UnitAnimationSet contains the underlying information
 * about the animation frames, while ImageCorrection has
 * information for centering the unit on the tile. 
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Unit extends Observer {




	private int maxHealth;

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}

	enum UnitState{
		//the unit is ready after the next turn of summon
		//TODO: switch in turn change
		NOT_READY,READY,HAS_MOVED,HAS_ATTACKED,READY_ATTACK

	}



	private UnitState currentState= UnitState.NOT_READY;

	public void setCurrentState(UnitState currentState) {
		this.currentState = currentState;
	}


	private Player owner = GameState.getInstance().getCurrentPlayer();

	public UnitState getCurrentState() { return currentState;}


	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}


	private int remainAttackTime = 1;
	public int getRemainAttackTime() {return remainAttackTime;}
	public void setRemainAttackTime(int remainAttackTime) {	this.remainAttackTime = remainAttackTime;}


	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file




	private int attack;
	public void setAttack(int attack) {
		this.attack = attack;
	}
	public int getAttack() {return attack;}

	private int health;
	public void setHealth(int health) {
		this.health = health;
	}
	public int getHealth() { return health;	}

	int id;
	UnitAnimationType animation;
	Position position;
	UnitAnimationSet animations;
	ImageCorrection correction;

	public Unit() {}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		position = new Position(0,0,0,0);
		this.animations = animations;
		this.correction = correction;
	}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		position = new Position(currentTile.getXpos(), currentTile.getYpos(), currentTile.getTilex(), currentTile.getTiley());
		this.animations = animations;
		this.correction = correction;
	}

	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations, ImageCorrection correction) {
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

	public void addAttack(int attackChange){
		this.setAttack(this.attack + attackChange);
		BasicCommands.setUnitAttack(GameState.getInstance().getOut(), this,this.attack);

	}

	public void addHealth(int healthChange){
		this.setHealth(this.health + healthChange);
		BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this,this.health);

	}



	/**
	 * This command sets the position of the Unit to a specified
	 * tile.
	 * @param tile
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(), tile.getYpos(), tile.getTilex(), tile.getTiley());
	}

	@Override
	public void trigger(Class target, Map<String,Object> parameters) {
		if (this.getClass().equals(target)){
			if(parameters.get("type").equals("setUnit")){
				if ((Integer)parameters.get("unitId") == this.id){
					BasicCommands.setUnitAttack(GameState.getInstance().getOut(), this,this.attack);
					BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this,this.health);
				}
			}

			else if(parameters.get("type").equals("beAttacked")){
				if(parameters.get("unit").equals(this)){
					Unit attacker = (Unit) parameters.get("attacker");
					BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), attacker, UnitAnimationType.attack);
					try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

					BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), attacker, UnitAnimationType.idle);

					this.setHealth(this.health - attacker.getAttack());
					// enemy die
					if(this.health < 1){
						Map<String, Object> newParameters = new HashMap<>();
						newParameters.put("type","unitDead");
						newParameters.put("tilex",this.getPosition().getTilex());
						newParameters.put("tiley",this.getPosition().getTiley());
						GameState.getInstance().broadcastEvent(Tile.class, newParameters);
						BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this, 0);
						BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), this, UnitAnimationType.death);
						BasicCommands.deleteUnit(GameState.getInstance().getOut(), this);

						//Callback Point:<UnitDeathCallBacks>
						int id = this.id;
						if (GameState.getInstance().getUnitDeathCallbacks().get(String.valueOf(id)) != null){
							//call the callback
							GameState.getInstance().getUnitDeathCallbacks().get(String.valueOf(id)).apply(id);
						}
					}
					else {
						// if enemy alive
						BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this, this.health);
						// defense
						if(!this.getCurrentState().equals(UnitState.HAS_ATTACKED)
								&& !this.getCurrentState().equals(UnitState.READY_ATTACK)){
							Map<String, Object> newParameters = new HashMap<>();
							newParameters.put("type", "beAttacked");
							newParameters.put("unit",attacker);
							newParameters.put("attacker",this);
							GameState.getInstance().broadcastEvent(Unit.class, newParameters);
						}
					}
				}
			}
			//TODO
			else if(parameters.get("type").equals("unitBeReady")){
				if (this.owner == GameState.getInstance().getCurrentPlayer()){
					this.currentState =  UnitState.READY;
				}
			}

			else if(parameters.get("type").equals("modifyUnit")){
				if (this.id == (Integer) parameters.get("unitId")){
					int newHealth = this.health + (Integer) parameters.get("health");
					int newAttack = this.attack + (Integer) parameters.get("attack");

					if (parameters.get("limit") != null
							&& parameters.get("limit").equals("max")
							&& newHealth > maxHealth){
						ToolBox.logNotification("Cannot exceed the max health");
						newHealth = maxHealth;
					}

					this.setAttack(newHealth);
					this.setAttack(newAttack);

				}
			}

	}
	}

}
