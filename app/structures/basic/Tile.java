package structures.basic;

import java.io.File;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.sslconfig.ssl.FakeChainedKeyStore;
import commands.BasicCommands;
import structures.GameState;
import structures.Observer;
import utils.StaticConfFiles;

/**
 * A basic representation of a tile on the game board. Tiles have both a pixel position
 * and a grid position. Tiles also have a width and height in pixels and a series of urls
 * that point to the different renderable textures that a tile might have.
 *
 * @author Dr. Richard McCreadie
 *
 */
public class Tile extends Observer {


	// Tile State
	enum TileState {
		NORMAL("normal", 0), WHITE("white", 1), RED("red", 2), LOCK_NORMAL("lock", 0);

		private String name;
		private int mode;

		private TileState(String name, int mode) {
			this.name = name;
			this.mode = mode;
		}
	}

	private TileState tileState = TileState.NORMAL;

	public TileState getTileState() {
		return tileState;
	}

	public void setTileState(TileState tileState) {

		this.tileState = tileState;
		//render the frontend
		BasicCommands.drawTile(GameState.getInstance().getOut(), this, this.tileState.mode);

	}


	private Unit unitOnTile;

	public Unit getUnitOnTile() {
		return unitOnTile;
	}

	public void setUnitOnTile(Unit unitOnTile) {
		this.unitOnTile = unitOnTile;
	}


	private Set<Tile> moveableTiles = new HashSet<>();

	public Set<Tile> getMoveableTiles() {
		return moveableTiles;
	}


	private Set<Tile> attackableTiles = new HashSet<>();

	public Set<Tile> getAttackableTiles() {
		return attackableTiles;
	}

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

		if (this.getClass().equals(target)) {

			//handle 1: find unit(all,avatar,(enemy) unit)
			if (parameters.get("type").equals("searchUnit")) {
				//if there is a unit on it
				if (this.unitOnTile != null){

					if (    //if we need a enemy unit and it is the one.
							(parameters.get("range").equals("enemy") && this.unitOnTile.getOwner() != GameState.getInstance().getCurrentPlayer())
									//if we need every unit.
									|| parameters.get("range").equals("all")
									//if we need all non-avtar unit and it is the one.
									|| (parameters.get("range").equals("non_avatar") && this.unitOnTile.id < 99)
									) {

						this.setTileState(TileState.WHITE);
					}
					else if (parameters.get("range").equals("your_avatar")
							//if it is a avatar
							&& this.unitOnTile.id >= 99
							//if it is the avatar of current player
							&& this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
						this.setTileState(TileState.WHITE);
					}
				}

			}

			//handle 2 -1 : find valid summon tile
			else if (parameters.get("type").equals("validSummonRangeHighlight")) {
		
				// a. find a friendly unit
				if (this.unitOnTile != null && this.unitOnTile.getOwner() == GameState.getInstance().getCurrentPlayer()) {

					int[] xpos = new int[]{
							-1, -1, -1, 0, 0, 1, 1, 1
					};
					int[] ypos = new int[]{
							-1, 0, 1, -1, 1, -1, 0, 1
					};

					//check all neighbour tiles
					for (int i = 0; i < xpos.length; i++) {
						parameters = new HashMap<>();
						parameters.put("type", "validSummonRangeHighlight-checkNeighbour");
						parameters.put("tilex", this.tilex + xpos[i]);
						parameters.put("tiley", this.tiley + ypos[i]);
						GameState.getInstance().broadcastEvent(Tile.class, parameters);
					}
				}

			}
			//handle 2 -2 : check if there is a unit on it
			else if (parameters.get("type").equals("validSummonRangeHighlight-checkNeighbour")) {
				if (this.unitOnTile == null
						&& (Integer) parameters.get("tilex") == this.tilex
						&& (Integer) parameters.get("tiley") == this.tiley
				) {

					//Change the backend texture state
					this.setTileState(TileState.WHITE);

				}
			}
			//hand 2 -3: if this card can be summoned anywhere on the board
			else if (parameters.get("type").equals("airdropSummonRangeHighlight")) {
				allBroadcast("airdropSummonRangeHighlight-checkAvaliable");
			}
			
			else if (parameters.get("type").equals("airdropSummonRangeHighlight-checkAvaliable")) {
				if(this.unitOnTile == null
						&& (Integer) parameters.get("tilex") == this.tilex
						&& (Integer) parameters.get("tiley") == this.tiley) {
					this.setTileState(TileState.WHITE);
				}
				
			}
			//handle 3: reset tile texture
			else if (parameters.get("type").equals("textureReset")) {
				if (!this.tileState.equals(TileState.NORMAL)) {
					//Change the backend texture state
					this.setTileState(TileState.NORMAL);

				}
			}

			//handle 4: summon a unit
			else if (parameters.get("type").equals("summon")) {
				if ((Integer) parameters.get("tilex") == this.tilex
						&& (Integer) parameters.get("tiley") == this.tiley) {


					//summon from hand
					Card cardSelected = (Card) parameters.get("card");
					if(cardSelected != null){
						//if it is not a valid tile, terminate
						if (!this.tileState.equals(TileState.WHITE)){
							return;
						}
					}

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

					//remove from hand
					if(cardSelected != null){
						GameState.getInstance().getCurrentPlayer().removeCardFromHand(cardSelected);
					}

				}
			}

			// first click a tile
			else if (parameters.get("type").equals("firstClickTile")) {
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley){
					// if there is a friendly unit on tile
					if (this.unitOnTile != null ) {
						if(this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
						// if the unit hasn't moved or attack, it can move and attack
						if (this.unitOnTile.getCurrentState().equals(Unit.UnitState.READY)) {
							GameState.getInstance().setTileSelected(this);
							
						// if the unit this tile has ranged attack ability
						     if(this.unitOnTile.rangedAttack){ // if the unit have ranged attack ability
						    	 allBroadcast("attackHighlight");
//								Map<String, Object> newParameters = new HashMap<>();
//								newParameters.put("type", "attackHighlight");
//								for(int i = 0; i < 9 ;i++) {
//										for(int j=0; j<5 ;j++) {
//										newParameters.put("tilex", i);
//										newParameters.put("tiley", j);
//										GameState.getInstance().broadcastEvent(Tile.class, newParameters);}
//								}
								this.moveHighlight();
							}else {
								this.attackHighlight();
								this.moveHighlight();
							}
							
						}
						// if the unit has moved, it can't move but can attack, only highlight attack unit
						else if (this.unitOnTile.getCurrentState().equals(Unit.UnitState.HAS_MOVED)) {
							GameState.getInstance().setTileSelected(this);

							this.attackHighlight();

						}
					}
				}
				}
			}

			// second click a tile (already selected a tile with a unit)
			else if (parameters.get("type").equals("operateUnit")) {
				Tile originTile = (Tile) parameters.get("originTileSelected");
				Unit unit = originTile.getUnitOnTile();
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley){
						
					// case 1: NORMAL - reset
					if (this.tileState.equals(tileState.NORMAL)) {
						this.resetTileSelected();
					}

					// case 2: WHITE - move
					else if (this.tileState.equals(tileState.WHITE)) {
						// move
						this.move(unit,originTile);
					}

					// case 3: RED - attack
					else if (this.tileState.equals(tileState.RED)) {
						
						// ranged attack
						if(unit.rangedAttack) {
							this.attackedBroadcast(unit);} //attack directly, no need to move
						
						// this is a normal unit
						else if(!unit.rangedAttack) {
							// case 3.1: attack after move
							if (unit.getCurrentState().equals(Unit.UnitState.HAS_MOVED)){
								//attack(unit, this.unitOnTile);
								this.attackedBroadcast(unit);
							}
							else{
								// case 3.2.1: attack directly
								if (distanceOfTiles(originTile, this) <= 2) {
									//attack(unit, this.unitOnTile); 
									this.attackedBroadcast(unit);}

								// case 3.2.2: automatically move and attack
								else {
									for (Tile x : originTile.getMoveableTiles()) {
										if (x.getTileState().equals(TileState.WHITE) && distanceOfTiles(x, this) <= 2) {
											x.move(unit,originTile);
											//attack(unit, this.unitOnTile);
											this.attackedBroadcast(unit);
											break;
										}
									}
								}
							}
						}
						
					}
				
				}
			}

			else if (parameters.get("type").equals("moveHighlight")) {
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley){

					if (this.unitOnTile == null && this.tileState == tileState.NORMAL) {
						GameState.getInstance().getTileSelected().getMoveableTiles().add(this);
						this.setTileState(tileState.WHITE);

						// set the tile highlight which the unit can attack after moving
						this.attackHighlight();
					}
				}
			}
			else if (parameters.get("type").equals("attackHighlight")) {
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley){
					// if the unit is enemy unit, highlight the tile to red
					if (this.unitOnTile != null){
						if(!this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())
								&& this.tileState == tileState.NORMAL){
							this.setTileState(tileState.RED);
							
							//Check if the tile is adjacent to the selectedTile.
							Tile selectedTile = GameState.getInstance().getTileSelected();
							Map<String,Object> newParameters = new HashMap<>();
							
							int[] offsetx = {-1, 0, 0, 1};
							int[] offsety = {0, -1, 1, 0};
							int[] offsetNewx = {-2, 0, 0, 2};
							int[] offsetNewy = {0, -2, 2, 0};
							for(int i=0; i<offsetx.length ; i++) {
								//if it is adjacent tile, lock the movable tiles behind it.
								if(tilex == selectedTile.getTilex() + offsetx[i] &&
										tiley == selectedTile.getTiley() + offsety[i]) {
									int newTilex = selectedTile.getTilex() + offsetNewx[i];
									int newTiley = selectedTile.getTiley() + offsetNewy[i];
									if(newTilex>=0 && newTiley>=0) {
										newParameters.put("type", "lock");
										newParameters.put("tilex", newTilex);
										newParameters.put("tiley", newTiley);
										GameState.getInstance().broadcastEvent(Tile.class, newParameters);
									}
								}
							}
						}
					}
				}
			}
			else if (parameters.get("type").equals("lock")) {
				if (Integer.parseInt(parameters.get("tilex").toString()) == this.tilex
						&& Integer.parseInt(parameters.get("tiley").toString()) == this.tiley)
				if(!this.tileState.equals(TileState.RED)) {
					this.setTileState(tileState.LOCK_NORMAL);
				}
			}
			
			else if (parameters.get("type").equals("deleteUnit")) {
					if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
							&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley){
						this.unitOnTile = null;}
			}
			
			//if the user has selected a spell and play it 
			else if (parameters.get("type").equals("spell")) {
				
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley) {
					
					Card spellCard = GameState.getInstance().getCardSelected();
					String rule = spellCard.getBigCard().getRulesTextRows()[0];
					
					//if this is not an empty tile 
					if(this.tileState.equals(tileState.WHITE)){
						Unit targetUnit  = this.unitOnTile;
						//if this is an enemy unit
						if (rule.toLowerCase(Locale.ROOT).contains("enemy")) {
							targetUnit.setHealth(targetUnit.getHealth() -2);}
						else if (rule.toLowerCase(Locale.ROOT).contains("non-avatar")) {
							if(targetUnit.getId() < 99) { //if this unit is not an enemy avatar
								targetUnit.setHealth(0);
							}
						}
						else if (rule.toLowerCase(Locale.ROOT).contains("health")) {
							targetUnit.setHealth(targetUnit.getHealth() + 5);
							}
						else if (rule.toLowerCase(Locale.ROOT).contains("gains")) {
							if(targetUnit.getId() >= 99) { // if this is a friend avatar
								targetUnit.setAttack(targetUnit.getAttack()+2);
							}
						}				
						
					}
					// remove card from hand
					if(spellCard != null){
						GameState.getInstance().getCurrentPlayer().removeCardFromHand(spellCard);
					}
					//clear the highlight
					this.resetTileSelected();
				}
			}
			
			else if (parameters.get("type").equals("rangedUnitAttackHighlight")) {
				if(this.unitOnTile!=null) {
					if(this.unitOnTile.getOwner() != GameState.getInstance().getCurrentPlayer()) {
						this.setTileState(tileState.RED);
					}
				}
			}
			
			}
				
		}	
		


	public void moveHighlight() {
		Map<String, Object> newParameters;

		int[] offsetx = new int[]{1, 1, -1, -1, 0, 0, 2, -2, 0, 0, 1, -1};
		int[] offsety = new int[]{1, -1, 1, -1, 2, -2, 0, 0, 1, -1, 0, 0};

		for (int i = 0; i < offsetx.length; i++) {

			int newTileX = tilex + offsetx[i];
			int newTileY = tiley + offsety[i];

			if (newTileX >= 0 && newTileY >= 0) {
				newParameters = new HashMap<>();
				newParameters.put("type", "moveHighlight");
				newParameters.put("tilex", newTileX);
				newParameters.put("tiley", newTileY);
				GameState.getInstance().broadcastEvent(Tile.class, newParameters);

			}
		}
	}


	public void attackHighlight () {
		Map<String, Object> newParameters;

		int[] offsetx = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
		int[] offsety = new int[]{0, 1, 1, 1, 0, -1, -1, -1};

		for (int i = 0; i < offsetx.length; i++) {

			int newTileX = tilex + offsetx[i];
			int newTileY = tiley + offsety[i];

			if (newTileX >= 0 && newTileY >= 0) {
				newParameters = new HashMap<>();
				newParameters.put("type", "attackHighlight");
				newParameters.put("tilex", newTileX);
				newParameters.put("tiley", newTileY);
				GameState.getInstance().broadcastEvent(Tile.class, newParameters);
			}
		}
		
	}

	public void attackedBroadcast (Unit attackerUnit){
		// set unit state - HAS_ATTACKED
		Unit attackedUnit = this.getUnitOnTile();
		attackerUnit.setCurrentState(Unit.UnitState.HAS_ATTACKED);

		Map<String, Object> newParameters = new HashMap<>();
		newParameters.put("type", "attacked");
		newParameters.put("attackedUnit", attackedUnit);
		newParameters.put("attackerUnit", attackerUnit);
		GameState.getInstance().broadcastEvent(Unit.class, newParameters);

		// reset the game state
		resetTileSelected();
		try { Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
	}
	
	
//	public void attack (Unit attacker, Unit beattacked){
//		// set unit state - HAS_ATTACKED
//		attacker.setCurrentState(Unit.UnitState.HAS_ATTACKED);
//
//		Map<String, Object> parameters = new HashMap<>();
//		parameters.put("type", "beAttacked");
//		parameters.put("unit", beattacked);
//		parameters.put("attacker", attacker);
//		GameState.getInstance().broadcastEvent(Unit.class, parameters);
//
//		// reset the game state
//		resetTileSelected();
//		try { Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
//	}

	public void move (Unit unit, Tile originTile){
		// move
		unit.setPositionByTile(this);
		this.setUnitOnTile(unit);
		originTile.setUnitOnTile(null);

		// play animation
		BasicCommands.moveUnitToTile(GameState.getInstance().getOut(), unit, this);

		// set unit state - HAS_MOVED
		unit.setCurrentState(Unit.UnitState.HAS_MOVED);

		originTile.getMoveableTiles().clear();
		originTile.getAttackableTiles().clear();

		// reset game state
		resetTileSelected();
		try { Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace(); }
	}

	// calculate the distance of two tiles
	public int distanceOfTiles (Tile tile1, Tile tile2){
		int x_1 = tile1.getTilex();
		int y_1 = tile1.getTiley();
		int x_2 = tile2.getTilex();
		int y_2 = tile2.getTiley();
		int distance = (x_1 - x_2) * (x_1 - x_2) + (y_1 - y_2) * (y_1 - y_2);
		return distance;
	}

	public void resetTileSelected(){
		if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.UNIT_SELECT)){
			// clear the tile selected
			GameState.getInstance().setTileSelected(null);

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("type","textureReset");
			GameState.getInstance().broadcastEvent(Tile.class, parameters);
		}
	}
	
	public static void allBroadcast(String type) {
		Map<String, Object> newParameters = new HashMap<>();
		newParameters.put("type", type);
		for(int i = 0; i < 9 ;i++) {
				for(int j=0; j<5 ;j++) {
				newParameters.put("tilex", i);
				newParameters.put("tiley", j);
				GameState.getInstance().broadcastEvent(Tile.class, newParameters);}
		}
	}


}
