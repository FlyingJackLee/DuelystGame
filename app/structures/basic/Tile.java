package structures.basic;

import java.io.File;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.sslconfig.ssl.FakeChainedKeyStore;
import commands.BasicCommands;
import structures.GameState;
import structures.Observer;

/**
 * A basic representation of a tile on the game board. Tiles have both a pixel position
 * and a grid position. Tiles also have a width and height in pixels and a series of urls
 * that point to the different renderable textures that a tile might have.
 *
 * @author Dr. Richard McCreadie
 *
 */
public class Tile extends Observer {



	enum TileState {
		NORMAL("normal", 0), WHITE("white", 1), RED("red", 2);

		private String name;
		private int mode;

		private TileState(String name, int mode) {
			this.name = name;
			this.mode = mode;
		}
	}


	public Set<Tile> getAroundTiles() {
		return aroundTiles;
	}

	private Set<Tile> aroundTiles = new HashSet<>();


	private Set<Tile> originMoveableTiles = new HashSet<>();

	public Set<Tile> getOriginMoveableTiles() {
		return originMoveableTiles;
	}


	public TileState getTileState() {
		return tileState;
	}


	private TileState tileState = TileState.NORMAL;


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

	public Unit getUnitOnTile() {
		return unitOnTile;
	}

	public void setUnitOnTile(Unit unitOnTile) {
		this.unitOnTile = unitOnTile;
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
				if (this.unitOnTile != null){

					if (    //if we need a enemy unit and it is the one.
							(parameters.get("range").equals("enemy") && this.unitOnTile.getOwner() != GameState.getInstance().getCurrentPlayer())
									//if we need every unit.
									|| parameters.get("range").equals("all")
									//if we need all non-avtar unit and ti is the one.
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
				//EX: highlight all
				if (parameters.get("airdrop") != null
						&& parameters.get("airdrop").equals("activate")){
					if (this.unitOnTile == null){
						//Change the  texture state
						this.setTileState(TileState.WHITE);
						return;
					}
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

				}
			}

			//handle 4: summon a unit
			else if (parameters.get("type").equals("summon")) {
				if ((Integer) parameters.get("tilex") == this.tilex
						&& (Integer) parameters.get("tiley") == this.tiley) {


					//if summon from hand, check if it is a valid tile
					if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)){
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
					if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)){
						GameState.getInstance().getCurrentPlayer().removeCardFromHand(GameState.getInstance().getCardSelected());
					}

				}
			}


			if (parameters.get("type").equals("tileClicked")) {
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley){
					// if there is a friend unit on this tile
					if (this.unitOnTile != null
							&& this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
						// if the unit hasn't moved or attack, it can move and attack
						if (this.unitOnTile.getCurrentState().equals(Unit.UnitState.NOT_READY)) {
							GameState.getInstance().setTileSelected(this);
							GameState.getInstance().setCurrentState(GameState.CurrentState.UNIT_SELECT);
							unitOnTile.setCurrentState(Unit.UnitState.READY);

							this.moveHighlight();
						}
						// if the unit has moved, it can't move but can attack, only highlight attack unit
						// READY_ATTACK state is only set for the unit that have moved and want to attack
						if (this.unitOnTile.getCurrentState().equals(Unit.UnitState.HAS_MOVED)) {
							GameState.getInstance().setTileSelected(this);
							GameState.getInstance().setCurrentState(GameState.CurrentState.UNIT_SELECT);
							unitOnTile.setCurrentState(Unit.UnitState.READY_ATTACK);

							this.attackHighlight();

						}

						GameState.getInstance().getCurrentPlayer().removeCardFromHand(cardSelected);
					}
				}
			}

			else if (parameters.get("type").equals("operateUnit")) {
				Unit unit = GameState.getInstance().getTileSelected().getUnitOnTile();
				if (Integer.parseInt(String.valueOf(parameters.get("tilex"))) == this.tilex
						&& Integer.parseInt(String.valueOf(parameters.get("tiley"))) == this.tiley){

					// if tile is normal and the Unit is READY
					if (this.tileState.equals(tileState.NORMAL)
							&& unit.getCurrentState().equals(Unit.UnitState.READY)) {
						unit.setCurrentState(Unit.UnitState.NOT_READY);
						resetGameState();
					}

					// if tile is white, and the unit state is READY, only move tile
					else if (this.tileState.equals(tileState.WHITE)
							&& unit.getCurrentState().equals(Unit.UnitState.READY)) {
						// move
						move(unit,this);
					}

					// if tile is red, and the unit state is READY
					// 这里会有两种情况： 1、目标对象在可直接攻击范围内 2、目标对象在可直接攻击范围外（攻击范围通过方法distanceOfTiles 判断）
					else if (this.tileState.equals(tileState.RED)
							&& unit.getCurrentState().equals(Unit.UnitState.READY)) {
						if (distanceOfTiles(GameState.getInstance().getTileSelected(), this) <= 2) {
							unit.setCurrentState(Unit.UnitState.HAS_ATTACKED);
							resetGameState();
							// only attack
							attack(unit, this.unitOnTile);}
						else {
							for (Tile x : GameState.getInstance().getTileSelected().getOriginMoveableTiles()) {
								if (x.getTileState().equals(TileState.WHITE) && distanceOfTiles(x, this) <= 2) {
									move(unit,x);
									unit.setCurrentState(Unit.UnitState.HAS_ATTACKED);
									resetGameState();
									attack(unit, this.unitOnTile);
									break;
								}
							}
						}
					}
					// if a unit want to move, then attack, or attack twice, use this logic
					else if (unit.getCurrentState().equals(Unit.UnitState.READY_ATTACK)) {
						if (this.tileState.equals(tileState.RED)) {
							unit.setCurrentState(Unit.UnitState.HAS_ATTACKED);
							this.resetGameState();
							attack(unit, this.unitOnTile);

						}
					}


				}
			}

			else if (parameters.get("type").equals("moveHighlight")) {
				if (Integer.parseInt(parameters.get("tilex").toString()) == this.tilex
						&& Integer.parseInt(parameters.get("tiley").toString()) == this.tiley){
					if (this.unitOnTile == null) {
						GameState.getInstance().getTileSelected().getOriginMoveableTiles().add(this);
						this.setTileState(tileState.WHITE);
						this.attackHighlight(); // set the move and attack position
					}
				}
			}
			else if (parameters.get("type").equals("attackHighlight")) {
				if (Integer.parseInt(parameters.get("tilex").toString()) == this.tilex
						&& Integer.parseInt(parameters.get("tiley").toString()) == this.tiley){
					// if the unit is enemy unit, highlight the tile to red
					if (this.unitOnTile != null &&
							!this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
						this.setTileState(tileState.RED);
					}
				}
			}
			else if (parameters.get("type").equals("clearHighlight")) {
				if(!this.tileState.equals(tileState.NORMAL)){
					this.setTileState(tileState.NORMAL);
				}
			}
			else if (parameters.get("type").equals("unitDead")
					&& Integer.parseInt(parameters.get("tilex").toString()) == this.tilex
					&& Integer.parseInt(parameters.get("tiley").toString()) == this.tiley){
					this.unitOnTile = null;
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

	public void attack (Unit attacker, Unit beattacked){

		//Callback Point:<AvatarAttackCallBacks>
		//if it is a avatar
		if (attacker.id == 99 || attacker.id == 100){
			int id = beattacked.id;
			if (GameState.getInstance().getAvatarAttackCallbacks().get(String.valueOf(id)) != null){
				//call the callback
				GameState.getInstance().getAvatarAttackCallbacks().get(String.valueOf(id)).apply(id);
			}
		}


		Map<String, Object> parameters = new HashMap<>();
		parameters.put("type", "beAttacked");
		parameters.put("unit", beattacked);
		parameters.put("attacker", attacker);
		GameState.getInstance().broadcastEvent(Unit.class, parameters);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void move (Unit unit, Tile tile){
		BasicCommands.moveUnitToTile(GameState.getInstance().getOut(), unit, tile);

		unit.setPositionByTile(tile);
		tile.setUnitOnTile(unit);
		GameState.getInstance().getTileSelected().setUnitOnTile(null);

		// change state
		unit.setCurrentState(Unit.UnitState.HAS_MOVED);
		tile.resetGameState();
		try { Thread.sleep(2000);	} catch (InterruptedException e) {e.printStackTrace(); }
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

	// After move or attack, clear the selected tile, and switch the current state
	public void resetGameState(){
		GameState.getInstance().setCurrentState(GameState.CurrentState.READY);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("type","clearHighlight");
		GameState.getInstance().broadcastEvent(Tile.class, parameters);
		try { Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace(); }
	}


}
