package structures.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.Observer;

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

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file

	private int health;
	private int attack;

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	int id;
	UnitAnimationType animation;
	Position position;
	UnitAnimationSet animations;
	ImageCorrection correction;
	Player master;
	
	public Unit() {}
	
	public Unit(ActorRef out, int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		
		position = new Position(0,0,0,0);
		this.correction = correction;
		this.animations = animations;
	}
	
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		
		position = new Position(currentTile.getXpos(),currentTile.getYpos(),currentTile.getTilex(),currentTile.getTiley());
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

	public Player getMaster() {
		return master;
	}

	public void setMaster(Player master) {
		this.master = master;
	}

	/**
	 * This command sets the position of the Unit to a specified
	 * tile.
	 * @param tile
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(),tile.getYpos(),tile.getTilex(),tile.getTiley());
	}


	@Override
	public void trigger(Class target, Map<String,Object> parameters) {
		if (this.getClass().equals(target)) {
			int pid = Integer.parseInt(parameters.get("unitId").toString());
			if(pid == this.id){
				if (parameters.get("type").equals("setUnit")){
					BasicCommands.setUnitAttack(GameState.getInstance().getOut(), this,this.attack);
					BasicCommands.setUnitHealth(GameState.getInstance().getOut(), this,this.health);
			    }
			}		
		}		
	}
	
	// move the unit to the tile, and set the tile's unit
	public void moveToTile(Tile t) {		
		BasicCommands.moveUnitToTile(GameState.getInstance().getOut(),this, t);
		this.setPositionByTile(t);
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
	}
	
}
