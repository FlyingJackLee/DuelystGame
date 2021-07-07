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

	public enum UnitState{
		//the unit is ready after the next turn of summon
		//TODO: switch in turn change
		NOT_READY,READY,HAS_MOVED,HAS_ATTACKED

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


	private int maxHealth;

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file


	private int attack;

	public void setAttack(int attack) {
		this.attack = attack;
		BasicCommands.setUnitAttack(GameState.getInstance().getOut(), 
				this, attack);}
	

	public int getAttack() {return attack;}


	private int health;

	public int getAttack() {return attack;}


	private int health;

	public void setHealth(int health) {
		this.health = health;
		if ( this.health >= 1 ) {
			if( this.health > maxHealth) {
				this.health = maxHealth;
			}
			BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this, this.health);
		}	
		else if (this.health <= 1){
			this.health = 0;
			BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this, 0);
			BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), this, UnitAnimationType.death);
			BasicCommands.deleteUnit(GameState.getInstance().getOut(), this);
			
			Map<String, Object> newParameters = new HashMap<>();
			newParameters.put("type","deleteUnit");
			newParameters.put("tilex",this.getPosition().getTilex());
			newParameters.put("tiley",this.getPosition().getTiley());
			GameState.getInstance().broadcastEvent(Tile.class, newParameters);
		}
	}
	
	

	public int getHealth() { return health;	}

	public int getHealth() { return health;	}

	int id;
	UnitAnimationType animation;
	Position position;
	UnitAnimationSet animations;
	ImageCorrection correction;
	
	public Unit() {
	}
	
	boolean rangedAttack = false; // can rangedAttack
	boolean airDrop = false; // can summon everywhere
	boolean flying = false; // can move anywhere
	boolean attackTwice = false; // can attack twice one turn
	
	

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


			//TODO
			else if(parameters.get("type").equals("unitBeReady")){
				if (this.owner == GameState.getInstance().getCurrentPlayer()){
					this.currentState =  UnitState.READY;
				}
                else {this.currentState = UnitState.NOT_READY;}
			}

//			else if(parameters.get("type").equals("findUnitList")){
//				if (this.owner.equals(GameState.getInstance().getCurrentPlayer())){
//					this.owner
//
//				}
//
//			}
			
			else if(parameters.get("type").equals("attacked")){
				if(parameters.get("attackedUnit").equals(this)){
					Unit attackedUnit = this;
					Unit attackerUnit = (Unit) parameters.get("attackerUnit");
					//Attack First time, allow counter attack.
					attackedUnit.attacked(attackerUnit,true);

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
	
	public void attacked (Unit attacker, boolean allowCounterAttack) {
		BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), attacker, UnitAnimationType.attack);
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
		BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), attacker, UnitAnimationType.idle);

		this.setHealth(this.getHealth()- attacker.getAttack());
			
		// if this unit survives, is allow to counter attack, and the attacker is in the attack range
		if(allowCounterAttack && this.health >=1
					&& this.targetIsInAttackRange(attacker.getPosition().getTilex(), attacker.getPosition().getTiley())) {
				attacker.attacked(this, false); //counter attack not allow attack.
			}		
		}

	public boolean targetIsInAttackRange(int tilex,int tiley) {
		int[] offsetx = new int[]{1,1,0,-1,-1,-1, 0, 1};
		int[] offsety = new int[]{0,1,1, 1, 0,-1,-1,-1};
		for (int i = 0; i < offsetx.length; i++) {
			int newTileX = this.getPosition().getTilex() + offsetx[i];
			int newTileY = this.getPosition().getTiley() + offsety[i];
			if(tilex == newTileX && tiley == newTileY) {
				return true;
			}
		}
		return false;
	}
	
}
