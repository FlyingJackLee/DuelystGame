package structures.basic;

import java.io.File;
import java.util.*;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import commands.BasicCommands;
import structures.GameState;
import structures.Observer;
import utils.ToolBox;


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

		NORMAL("normal", 0), WHITE("white", 1), RED("red", 2);

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
	 * The GameState will broadcast events and call this method
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
				if (this.unitOnTile != null) {

					if (    //if we need a enemy unit and it is the one.
							(parameters.get("range").equals("enemy") && this.unitOnTile.getOwner() != GameState.getInstance().getCurrentPlayer())
									//if we need every unit.
									|| parameters.get("range").equals("all")
									//if we need all non-avtar unit and ti is the one.
									|| (parameters.get("range").equals("non_avatar") && this.unitOnTile.id < 99)
					) {

						this.setTileState(TileState.WHITE);
					} else if (parameters.get("range").equals("your_avatar")
							//if it is a avatar
							&& this.unitOnTile.id >= 99
							//if it is the avatar of current player
							&& this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
						this.setTileState(TileState.WHITE);
					} else if (parameters.get("range").equals("all_friends")
							&& !this.unitOnTile.getOwner().isHumanOrAI()) {
						AIPlayer aiPlayer = (AIPlayer) GameState.getInstance().getCurrentPlayer();
						aiPlayer.addToOptionalTile(this);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}


			}

			//handle 2 -1 : find valid summon tile
			else if (parameters.get("type").equals("validSummonRangeHighlight")) {
				//EX: highlight all
				if (parameters.get("airdrop") != null
						&& parameters.get("airdrop").equals("activate")) {
					if (this.unitOnTile == null) {
						//Change the  texture state
						this.setTileState(TileState.WHITE);

						try {
							Thread.sleep(10);
						}catch (InterruptedException e){e.printStackTrace();}
					}
					return;
				}


				//normally:
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
			//handle 3: reset tile texture
			else if (parameters.get("type").equals("textureReset")) {
				if (!this.tileState.equals(TileState.NORMAL)) {
					//Change the backend texture state
					this.setTileState(TileState.NORMAL);

					try {
						Thread.sleep(10);
					}catch (InterruptedException e){e.printStackTrace();}
				}
			}


			//handle 4: summon a unit
			else if (parameters.get("type").equals("summon")) {
				if ((Integer) parameters.get("tilex") == this.tilex
						&& (Integer) parameters.get("tiley") == this.tiley) {


					//if summon from hand, check if it is a valid tile
					if (GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)) {
						//if it is not a valid tile, terminate
						if (!this.tileState.equals(TileState.WHITE)) {
							ToolBox.logNotification("Select a valid tile!");

							return;
						}
						ToolBox.logNotification(ToolBox.currentPlayerName() + " play a card: "
								+ GameState.getInstance().getCardSelected().getCardname());

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
					if (GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)) {
						GameState.getInstance().getCurrentPlayer().removeCardFromHand(GameState.getInstance().getCardSelected());
					}

					unit.displayAttackAndHealth();

					//Callback Point:<BeforeSummonCallbacks>
					//run callbacks before summon
					int id = unit.id;
					if (GameState.getInstance().getBeforeSummonCallbacks().get(String.valueOf(id)) != null){
						//call the callback
						GameState.getInstance().getBeforeSummonCallbacks().get(String.valueOf(id)).apply(id);

					}

					GameState.getInstance().broadcastEvent(Unit.class,parameters);

					GameState.getInstance().setCurrentState(GameState.CurrentState.READY);



				}
			}


			// first click a tile
			else if (parameters.get("type").equals("firstClickTile")) {
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley) {
					// if there is a friendly unit on tile
					if (this.unitOnTile != null) {
						// find if there is any adjacent provoking unit
						Map<String, Object> newParameters;
						newParameters = new HashMap<>();
						newParameters.put("provokedUnit", this.unitOnTile);

						int[] offsetx = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
						int[] offsety = new int[]{0, 1, 1, 1, 0, -1, -1, -1};

						for (int i = 0; i < offsetx.length; i++) {

							int newTileX = tilex + offsetx[i];
							int newTileY = tiley + offsety[i];

							if (newTileX >= 0 && newTileY >= 0) {
								newParameters.put("type", "searchUnitCanProvoke");
								newParameters.put("tilex", newTileX);
								newParameters.put("tiley", newTileY);
								GameState.getInstance().broadcastEvent(Tile.class, newParameters);
							}
						}
						
						if(!this.unitOnTile.isProvoked()) {
							if (this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
								// if the unit hasn't moved or attack, it can move and attack
								if (this.unitOnTile.getCurrentState().equals(Unit.UnitState.READY)) {
									GameState.getInstance().setTileSelected(this);
	
									// if the unit this tile has ranged attack ability
									if (this.unitOnTile.rangedAttack) { // if the unit have ranged attack ability
										allBroadcast("attackHighlight");
										this.moveHighlight(0);
									}
										
									// Unit Ability: Flying
									if (this.unitOnTile.flying) {
										allBroadcast("moveHighlight");
									} else {
										this.moveHighlight(0);
										this.attackHighlight();
									}
									GameState.getInstance().setCurrentState(GameState.CurrentState.UNIT_SELECT);
								}
								// if the unit has moved, it can't move but can attack, only highlight attack unit
								else if (this.unitOnTile.getCurrentState().equals(Unit.UnitState.HAS_MOVED)) {
									GameState.getInstance().setTileSelected(this);
	
									this.attackHighlight();
	
									GameState.getInstance().setCurrentState(GameState.CurrentState.UNIT_SELECT);
	
								}
							}
						}else {
							GameState.getInstance().setCurrentState(GameState.CurrentState.UNIT_SELECT);
							GameState.getInstance().setTileSelected(this);
						}
					}
				}
			}
			// show the move highlight, and record the highlight tile
			else if (parameters.get("type").equals("moveHighlight")) {
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley) {

					if (this.unitOnTile == null && this.tileState == TileState.NORMAL) {
						if(parameters.get("count") != null){
							this.setTileState(TileState.WHITE);
							GameState.getInstance().getTileSelected().getMoveableTiles().add(this);

							int count = Integer.parseInt(String.valueOf(parameters.get("count")));
							this.moveHighlight(count);
							// set the tile highlight which the unit can attack after moving
							this.attackHighlight();
						}
					}
				}
			}

			// show the attack highlight
			else if (parameters.get("type").equals("attackHighlight")) {
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley) {
					// if the unit is enemy unit, highlight the tile to red
					if (this.unitOnTile != null) {
						if (!this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())
								&& this.tileState == TileState.NORMAL) {
							this.setTileState(TileState.RED);
						}
					}
				}
			}
			else if (parameters.get("type").equals("deleteUnit")) {
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley) {
					this.unitOnTile = null;
				}
			}

			//if the user has selected a spell and play it
			else if (parameters.get("type").equals("spell")) {

				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley) {

					Card spellCard = GameState.getInstance().getCardSelected();
					String rule = spellCard.getBigCard().getRulesTextRows()[0];

					//<<SpellCallbacks> point
					//call when the spell has been casted
					if (GameState.getInstance().getSpellCastCallbacks().size() != 0) {
						// call the callback
						for (Map.Entry<String, Function<Integer,Boolean>> entry:GameState.getInstance().getSpellCastCallbacks().entrySet()
						) {
							entry.getValue().apply(Integer.parseInt(entry.getKey()));
						}
					}

					//if this is not an empty tile
					if (this.tileState.equals(TileState.WHITE)) {
						Unit targetUnit = this.unitOnTile;
						//if this is an enemy unit
						if (rule.toLowerCase(Locale.ROOT).contains("enemy")) {
							targetUnit.changeHealth(targetUnit.getHealth() - 2,false);
						} else if (rule.toLowerCase(Locale.ROOT).contains("non-avatar")) {
							if (targetUnit.getId() < 99) { //if this unit is not an enemy avatar
								targetUnit.changeHealth(0,false);
							}
						} else if (rule.toLowerCase(Locale.ROOT).contains("health")) {
							targetUnit.changeHealth(targetUnit.getHealth() + 5,false);
						} else if (rule.toLowerCase(Locale.ROOT).contains("gains")) {
							if (targetUnit.getId() >= 99) { // if this is a friend avatar
								targetUnit.changeAttack(targetUnit.getAttack() + 2);
							}
						}

					}
					// remove card from hand
					if (spellCard != null) {
						GameState.getInstance().getCurrentPlayer().removeCardFromHand(spellCard);
					}
					//clear the highlight
					this.resetTileSelected();
				}
			}
			else if (parameters.get("type").equals("rangedUnitAttackHighlight")) {
				if (this.unitOnTile != null) {
					if (this.unitOnTile.getOwner() != GameState.getInstance().getCurrentPlayer()) {
						this.setTileState(TileState.RED);
					}
				}
			}


			// second click a tile (already selected a tile with a unit)
			else if (parameters.get("type").equals("operateUnit")) {
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley) {
					Tile originTile = (Tile) parameters.get("originTileSelected");
					Unit unit = originTile.getUnitOnTile();

					// case 1: NORMAL - reset
					if (this.tileState.equals(TileState.NORMAL)) {
						ToolBox.logNotification(ToolBox.currentPlayerName() + "cancel unit select!");
						this.resetTileSelected();
					}

					// case 2: WHITE - move
					else if (this.tileState.equals(TileState.WHITE)) {
						// move
						this.checkMoveVertically(originTile);
					}

					// case 3: RED - attack
					else if (this.tileState.equals(TileState.RED)) {

						// ranged attack
						if (unit.rangedAttack) {
							this.attackedBroadcast(unit);
						} //attack directly, no need to move

						// this is a normal unit
						else if (!unit.rangedAttack) {
							// case 3.1: attack after move
							if (unit.getCurrentState().equals(Unit.UnitState.HAS_MOVED)) {
								//attack(unit, this.unitOnTile);
								this.attackedBroadcast(unit);
							} else {
								// case 3.2.1: attack directly
								if (distanceOfTiles(originTile, this) <= 2) {
									//attack(unit, this.unitOnTile);
									this.attackedBroadcast(unit);
								}

								// case 3.2.2: automatically move and attack
								else {
									for (Tile x : originTile.getMoveableTiles()) {
										if (x.getTileState().equals(TileState.WHITE) && distanceOfTiles(x, this) <= 2) {
											x.checkMoveVertically(originTile);
											try {
												Thread.sleep(2000);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}

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
			else if (parameters.get("type").equals("AI_FindOperateTile")) {
				if (this.tileState.equals(TileState.WHITE)) {
					((AIPlayer) GameState.getInstance().getCurrentPlayer()).addToWhiteGroup(this);
				} else if (this.tileState.equals(TileState.RED)) {
					((AIPlayer) GameState.getInstance().getCurrentPlayer()).addToRedGroup(this);
				}

			}
			else if (parameters.get("type").equals("checkMoveVertically")){
				if(Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley) {
					Tile originTile = (Tile) parameters.get("originTile");
					Tile aimTile = (Tile) parameters.get("aimTile");

					// if state is NORMAL, means can't move to aim tile by old route
					if (this.tileState.equals(TileState.NORMAL)) {
						aimTile.move(originTile.getUnitOnTile(), originTile, true);
					} else {
						aimTile.move(originTile.getUnitOnTile(), originTile, false);
					}
				}

			}
			else if (parameters.get("type").equals("searchUnitCanProvoke")) {
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley) {
					if(this.unitOnTile!=null) {
						Unit provokedUnit = (Unit) parameters.get("provokedUnit");
						if(this.unitOnTile.getCanProvoke() && 
								!this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
							this.setTileState(TileState.RED);
							provokedUnit.setProvoked(true);
						}
					}
				}
			}
			else if (parameters.get("type").equals("clearProvoke")){
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley) {
					if(this.unitOnTile!=null) {
						if(!this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
							this.unitOnTile.setProvoked(false);
						}
					}
				}
			}
		}

	}


	/**
	 * show the moveable highlight - white
	 * @param count move step
	 */
	private void moveHighlight(int count) {
		if (count > 1){
			return;
		}

		count ++;
		Map<String, Object> newParameters;

		int[] offsetx = new int[]{ 0, 1,-1, 0};
		int[] offsety = new int[]{ 1, 0, 0,-1};

		for (int i = 0; i < offsetx.length; i++) {

			int newTileX = tilex + offsetx[i];
			int newTileY = tiley + offsety[i];

			if (newTileX >= 0 && newTileY >= 0) {
				newParameters = new HashMap<>();
				newParameters.put("type", "moveHighlight");
				newParameters.put("tilex", newTileX);
				newParameters.put("tiley", newTileY);
				newParameters.put("count",count);
				newParameters.put("originTile",this);
				GameState.getInstance().broadcastEvent(Tile.class, newParameters);
			}
		}
	}

	/**
	 * show the attack highlight - red
	 */
	private void attackHighlight() {
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

	private void attackedBroadcast(Unit attackerUnit) {
		Unit attackedUnit = this.getUnitOnTile();

		ToolBox.logNotification(ToolBox.currentPlayerName() + ": " + attackerUnit.getId() + " >> " + attackedUnit.getId());

		Map<String, Object> newParameters = new HashMap<>();
		newParameters.put("type", "attacked");
		newParameters.put("attackedUnit", attackedUnit);
		newParameters.put("attackerUnit", attackerUnit);
		GameState.getInstance().broadcastEvent(Unit.class, newParameters);

		// set unit state - HAS_ATTACKED
		attackerUnit.setAttackNum(attackerUnit.getAttackNum()-1);
		if(attackerUnit.getAttackNum() < 1) {
			attackerUnit.setCurrentState(Unit.UnitState.HAS_ATTACKED);}
		
		// reset the game state
		resetTileSelected();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param unit       Unit: the unit ready to move
	 * @param originTile Tile: tile before moving
	 * @param mode       boolean false - move horizontally then vertically, true - vertically then horizontally
	 */
	private void move(Unit unit, Tile originTile, boolean mode) {
		// clear highlight
		resetTileSelected();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// front-end: play animation
		if (mode) {
			BasicCommands.moveUnitToTile(GameState.getInstance().getOut(), unit, this, true);
		} else {
			BasicCommands.moveUnitToTile(GameState.getInstance().getOut(), unit, this);
		}

		ToolBox.logNotification(ToolBox.currentPlayerName() + ": " + unit.getId() + " move to (" + this.tilex + "," + this.tiley + ")");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		// back-end: unit move to tile
		unit.setPositionByTile(this);
		this.setUnitOnTile(unit);
		originTile.setUnitOnTile(null);
		originTile.getMoveableTiles().clear();

		// reset game state
		originTile.getMoveableTiles().clear();

		// set unit state - HAS_MOVED
		unit.setMoveNum(unit.getMoveNum()-1);
		if(unit.getMoveNum() < 1) {
			unit.setCurrentState(Unit.UnitState.HAS_MOVED);}

		if(unit.getCanProvoke()) {
			originTile.adjacentBroadcast("clearProvoke");
		}

	}

	/**
	 * calculate the distance of two tiles
	 *
	 * @param tile1
	 * @param tile2
	 * @return square of distance
	 */
	private int distanceOfTiles(Tile tile1, Tile tile2) {
		int x_1 = tile1.getTilex();
		int y_1 = tile1.getTiley();
		int x_2 = tile2.getTilex();
		int y_2 = tile2.getTiley();
		return (x_1 - x_2) * (x_1 - x_2) + (y_1 - y_2) * (y_1 - y_2);
	}

	/**
	 * reset the state of game state to READY, and clear all highlight
	 */
	private void resetTileSelected() {
		// clear the tile selected
		GameState.getInstance().setTileSelected(null);
		GameState.getInstance().setCurrentState(GameState.CurrentState.READY);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("type", "textureReset");
		GameState.getInstance().broadcastEvent(Tile.class, parameters);

	}

	// broadcast to all tiles (ranged attack/flying)
	private static void allBroadcast(String type) {
		Map<String, Object> newParameters = new HashMap<>();
		newParameters.put("type", type);
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				newParameters.put("tilex", i);
				newParameters.put("tiley", j);
				if (type.equals("moveHighlight")) {
					newParameters.put("count", 0);
				}
				GameState.getInstance().broadcastEvent(Tile.class, newParameters);
			}
		}
	}

	/**
	 * check whether a unit need to move horizontally then vertically
	 *
	 * @param originTile
	 */
	private void checkMoveVertically(Tile originTile) {
		Map<String, Object> parameters;

		// if a tile move 2 steps, and every step in different direction
		if (distanceOfTiles(this, originTile) == 2) {
			// check the state of original tile's left or right
			int checkTileX = this.getTilex();
			int checkTileY = originTile.getTiley();

			parameters = new HashMap<>();
			parameters.put("type", "checkMoveVertically");
			parameters.put("tilex", checkTileX);
			parameters.put("tiley", checkTileY);
			parameters.put("originTile", originTile);
			parameters.put("aimTile", this);
			GameState.getInstance().broadcastEvent(Tile.class, parameters);
		} else this.move(originTile.getUnitOnTile(), originTile, false);

	}

	private void adjacentBroadcast(String type) {
		Map<String, Object> newParameters;

		int[] offsetx = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
		int[] offsety = new int[]{0, 1, 1, 1, 0, -1, -1, -1};

		for (int i = 0; i < offsetx.length; i++) {

			int newTileX = tilex + offsetx[i];
			int newTileY = tiley + offsety[i];

			if (newTileX >= 0 && newTileY >= 0) {
				newParameters = new HashMap<>();
				newParameters.put("type", type);
				newParameters.put("tilex", newTileX);
				newParameters.put("tiley", newTileY);
				GameState.getInstance().broadcastEvent(Tile.class, newParameters);
			}
		}
	}
}
