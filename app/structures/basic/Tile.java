package structures.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import commands.BasicCommands;

import org.checkerframework.checker.guieffect.qual.UI;
import structures.GameState;
import structures.Observer;
import utils.BasicObjectBuilders;


/**
 * A basic representation of a tile on the game board. Tiles have both a pixel position
 * and a grid position. Tiles also have a width and height in pixels and a series of urls
 * that point to the different renderable textures that a tile might have.
 *
 * @author Dr. Richard McCreadie
 *
 */
public class Tile extends Observer {

	private Unit unitOnTile;

	@JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file

	List<String> tileTextures;
	int xpos;
	int ypos;
	int width;
	int height;
	int tilex;
	int tiley;

	public Tile() {
	}

	public Tile(String tileTexture, int xpos, int ypos, int width, int height, int tilex, int tiley) {
		super();
		tileTextures = new ArrayList<String>(1);
		tileTextures.add(tileTexture);
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
		this.tilex = tilex;
		this.tiley = tiley;
	}

	public Tile(List<String> tileTextures, int xpos, int ypos, int width, int height, int tilex, int tiley) {
		super();
		this.tileTextures = tileTextures;
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
		this.tilex = tilex;
		this.tiley = tiley;
	}

	public List<String> getTileTextures() {
		return tileTextures;
	}

	public void setTileTextures(List<String> tileTextures) {
		this.tileTextures = tileTextures;
	}

	public int getXpos() {
		return xpos;
	}

	public void setXpos(int xpos) {
		this.xpos = xpos;
	}

	public int getYpos() {
		return ypos;
	}

	public void setYpos(int ypos) {
		this.ypos = ypos;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getTilex() {
		return tilex;
	}

	public void setTilex(int tilex) {
		this.tilex = tilex;
	}

	public int getTiley() {
		return tiley;
	}

	public void setTiley(int tiley) {
		this.tiley = tiley;
	}
	
	public Unit getUnitOnTile() {
		return unitOnTile;
	}

	public void setUnitOnTile(Unit unitOnTile) {
		this.unitOnTile = unitOnTile;
	}
	

	/**
	 * Loads a tile from a configuration file
	 * parameters.
	 *
	 * @param configFile
	 * @return
	 */
	public static Tile constructTile(String configFile) {
		try {
			Tile tile = mapper.readValue(new File(configFile), Tile.class);
			return tile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 *
	 * The gameState will broadcast events and call this method
	 *
	 * @param target: The Class of target object
	 * @param parameters: extra parameters.
	 */
	@Override
	public void trigger(Class target, Map<String, Object> parameters) {
		Player currentPlayer = GameState.getInstance().getCurrentPlayer();
	
		if (this.getClass().equals(target)
				&& (Integer) parameters.get("tilex") == this.tilex
				&& (Integer) parameters.get("tiley") == this.tiley) {
		
			if (parameters.get("type").equals("summon")) {
				Unit unit = (Unit) parameters.get("unit");
				unit.setPositionByTile(this);
				this.unitOnTile = unit;
			
				// render front-end
				BasicCommands.drawUnit(GameState.getInstance().getOut(), unit, this);
				
				// wait for the creation of the unit
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			//One tile is clicked without any conditions.
			else if (parameters.get("type").equals("tileClicked")) {
				
				if(this.unitOnTile != null) {
					//If this is a friendly unit, highlight the movable tiles and attackable tiles
					if(this.unitOnTile.getOwner() == currentPlayer) {
						for(Tile temp:this.movableTiles()) {
							BasicCommands.drawTile(GameState.getInstance().getOut(), temp, 1);
						}
						for(Tile temp: this.attackableTiles()) {
							BasicCommands.drawTile(GameState.getInstance().getOut(), temp, 2);
						}
						//Record current game state and parameters
						GameState.getInstance().setTileClicked(this);
						GameState.getInstance().setState("unitClicked");	
					}
				}
			} 
			
			//A unit is already selected, it acts according to this tile's state.
			else if(parameters.get("type").equals("action")) {
				Map<String, Object> newParameters;
				Unit selectedUnit = GameState.getInstance().getUnitClicked();
				Tile selectedTile = GameState.getInstance().getTileClicked();
				
				newParameters = new HashMap<>();
				newParameters.put("tilex", tilex);
				newParameters.put("tiley", tiley);
				
				//If this is not an empty tile, find if it is in the previous unit's attack range.
				if(this.unitOnTile != null && !this.unitOnTile.hasAttacked) {
					for(Tile temp: selectedTile.attackableTiles()) {
						if(this == temp) {
							newParameters.put("type", "attackEnemy");
							GameState.getInstance().broadcastEvent(Tile.class, newParameters);
							break;}
					}
					this.unitOnTile.hasAttacked = true;
				} else if(!this.unitOnTile.hasAttacked && !this.unitOnTile.hasMoved){
					//If this is an empty tile, find if it is movable.
					for(Tile temp: selectedTile.movableTiles()) {
						if(this == temp) {
							newParameters.put("type", "moveUnitToTile");
							GameState.getInstance().broadcastEvent(Tile.class, newParameters);
							break;}
					this.unitOnTile.hasMoved = true;
					}
				}
				//Restore game state.
				this.clearTileHighlight();
				GameState.getInstance().setState("readyState");
				GameState.getInstance().clearClickedTile();
			}
			
			/****************************************************/
			
			else if (parameters.get("type").equals("moveUnitToTile")) {
				BasicCommands.moveUnitToTile(GameState.getInstance().getOut(), 
						GameState.getInstance().getUnitClicked(), this);
				GameState.getInstance().getTileClicked().getUnitOnTile().setPositionByTile(this); 
			}
			
			else if (parameters.get("type").equals("attackEnemy")) {
				Unit selectedUnit = GameState.getInstance().getUnitClicked();
				
				attack(selectedUnit, this.unitOnTile);
				//If attacked unit survives, counter attack.
				if(this.unitOnTile.getHealth()- selectedUnit.getAttack() > 1) {
					attack(this.unitOnTile, selectedUnit);
				}
				
			}
			
			else if (parameters.get("type").equals("movableTileHighlight")) {
				BasicCommands.drawTile(GameState.getInstance().getOut(), this, 1);
			}
			
			else if (parameters.get("type").equals("attackableTileHighlight")) {
				BasicCommands.drawTile(GameState.getInstance().getOut(), this, 2);
			}
			
			else if (parameters.get("type").equals("clearHightlight")) {
				BasicCommands.drawTile(GameState.getInstance().getOut(), this, 0);
			}
			
		}
	}
	
	//Highlight the tiles the current unit can move to.
	public List<Tile> movableTiles() {
		List<Tile> tiles = GameState.getInstance().getTileList();
		List<Tile> movable;
		
		int[] offsetx = new int[]{-2,-1,-1,-1, 0, 0, 0, 0, 1, 1, 1, 2};
		int[] offsety = new int[]{ 0,-1, 0, 1,-2,-1, 1, 2,-1, 0, 1, 0};
		
		for (int i = 0; i < offsety.length; i++) {
			int newTileX = tilex + offsetx[i];
			int newTileY = tiley + offsety[i];
			
			//Find the tile which is in the move range and has no unit on it
			if (newTileX >= 0 && newTileY >= 0) {
				for(Tile temp: tiles) {
					if(newTileX == temp.getTilex() && newTileY== temp.getTiley() 
							&& temp.getUnitOnTile()==null) {
						movable.add(temp);
					}
				}
			}
		}
		
		return movable;
	}
	
	//Return the enemy tiles in the attack range
	public List<Tile> attackableTiles() {
		Player unitOwner = this.unitOnTile.getOwner();
		Player currentPlayer = GameState.getInstance().getCurrentPlayer();
		
		List<Tile> tiles = GameState.getInstance().getTileList();
		List<Tile> attackable;
		
		int[] offsetx = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
		int[] offsety = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};

		for (int i = 0; i < offsety.length; i++) {
			int newTileX = tilex + offsetx[i];
			int newTileY = tiley + offsety[i];
			
			//Find the tile who has a unit on tile which is not null and is an enemy
			if (newTileX >= 0 && newTileY >= 0) {
				for(Tile temp: tiles) {
					//Go through the list to find a tile in the attack range
					if(temp.getTilex() == newTileX && temp.getTiley() == newTileY 
							&& temp.getUnitOnTile() != null) {
						//An enemy is a unit whose player is not current player
						if(temp.getUnitOnTile().getOwner() != currentPlayer) {
							attackable.add(temp);
						}
					}
				}
			}
		}
		return attackable;
	}
	
	//Re-draw all the tiles with the original texture (clear its highlight)
	public void clearTileHighlight() {
		Map<String, Object> newParameters;
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 5; j++) {
				newParameters = new HashMap<>();
				newParameters.put("type", "clearHighlight");
				newParameters.put("tilex", i);
				newParameters.put("tiley", j);
				
				GameState.getInstance().broadcastEvent(Tile.class, newParameters);
			}
		}
	}
	
	public static void attack(Unit attackUnit, Unit attackedUnit) {
		int attackedHealth = attackedUnit.getHealth() - attackUnit.getAttack();
		
		//Play animation.
		BasicCommands.playUnitAnimation(GameState.getInstance().getOut(), attackUnit, 
				UnitAnimationType.attack);
		
		//If attacked Unit dies
		if(attackedHealth < 1){
			BasicCommands.playUnitAnimation(GameState.getInstance().getOut(),
					attackUnit, UnitAnimationType.death);
			BasicCommands.deleteUnit(GameState.getInstance().getOut(), attackedUnit);
			
		}else{
			//If the attacked unit survives, set its health.
			attackedUnit.setHealth(attackedHealth);
			BasicCommands.setUnitHealth(GameState.getInstance().getOut(), 
					attackedUnit, attackedHealth);
		}
	}

}
