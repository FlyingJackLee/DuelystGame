package structures.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
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
			if (Integer.parseInt(parameters.get("tilex").toString()) == this.tilex
					&& Integer.parseInt(parameters.get("tiley").toString()) == this.tiley) {
				if (parameters.get("type").equals("tileClicked")) {
					// if there is a friend unit on this tile
					if (this.unitOnTile != null
							&& this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
						// if the unit hasn't moved or attack, it can move and attack
						if(this.unitOnTile.getCurrentState().equals(Unit.UnitState.NOT_READY)){
							GameState.getInstance().setTileSelected(this);
							GameState.getInstance().setCurrentState(GameState.CurrentState.UNIT_SELECT);
							unitOnTile.setCurrentState(Unit.UnitState.READY);

							this.moveHighlight();
						}
						// if the unit has moved, it can't move but can attack, only highlight attack unit
						if(this.unitOnTile.getCurrentState().equals(Unit.UnitState.HAS_MOVED)){
							GameState.getInstance().setTileSelected(this);
							GameState.getInstance().setCurrentState(GameState.CurrentState.UNIT_SELECT);
							unitOnTile.setCurrentState(Unit.UnitState.READY_ATTACK);

							this.attackHighlight();
						}
					}
				}
				else if (parameters.get("type").equals("operateUnit")) {
					Unit unit = (Unit) parameters.get("unit");
					if(unit.getCurrentState().equals(Unit.UnitState.READY)){
						// if tile is normal and there is a friend unit on tile, switch to new unit
						if (this.tileState.equals(tileState.NORMAL)
								&& this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())
								&& !this.equals(GameState.getInstance().getTileSelected())){
							unit.setCurrentState(Unit.UnitState.NOT_READY);
							GameState.getInstance().setStateReady();
							parameters.put("type","tileClicked");
							GameState.getInstance().broadcastEvent(Tile.class,parameters);
							try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
						}
						// if tile is white, and the unit state is READY, only move tile
						if (this.tileState.equals(tileState.WHITE)) {
							// move
							unit.setPositionByTile(this);
							BasicCommands.moveUnitToTile(GameState.getInstance().getOut(), unit, this);
							// after move
							unit.setCurrentState(Unit.UnitState.HAS_MOVED);

							this.setUnitOnTile(unit);
							GameState.getInstance().getTileSelected().setUnitOnTile(null);
							GameState.getInstance().setStateReady();
							try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
						}
						// if tile is red, and the unit state is READY
						if (this.tileState.equals(tileState.RED)){


							unit.setCurrentState(Unit.UnitState.HAS_ATTACKED);
							GameState.getInstance().setStateReady();
							try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
						}
					}
					if(unit.getCurrentState().equals(Unit.UnitState.READY_ATTACK)){
						if (this.tileState.equals(tileState.RED)){
							unit.setCurrentState(Unit.UnitState.HAS_ATTACKED);
							GameState.getInstance().setStateReady();
							try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
						}
					}


				}
				else if (parameters.get("type").equals("moveHighlight")) {
					if (this.unitOnTile == null) {
						this.setTileState(tileState.WHITE);
						this.attackHighlight(); // set the move and attack position
						try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
					}
				}
				else if (parameters.get("type").equals("attackHighlight")) {
					// if the unit is enemy unit, highlight the tile to red
					if (this.unitOnTile != null &&
							!this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
						this.setTileState(tileState.RED);
						try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
					}
				}
				else if (parameters.get("type").equals("clearHighlight")){
					this.setTileState(tileState.NORMAL);
					try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
				}
				else if (parameters.get("type").equals("summon")) {
					Unit unit = (Unit) parameters.get("unit");
					unit.setPositionByTile(this);
					this.unitOnTile = unit;
					// render front-end
					BasicCommands.drawUnit(GameState.getInstance().getOut(), unit, this);
					// wait for the creation of the unit
					try { Thread.sleep(500);}
					catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		}
	}






//		//handle 1: find unit(all,avatar,(enemy) unit)
//		if (parameters.get("type").equals("searchUnit"))
//		{
//			//if there is a unit on it
//			if (this.unitOnTile != null
//					//if we need a enemy unit and it is the one.
//			&& ((parameters.get("range").equals("enemy") && this.unitOnTile.getOwner() != GameState.getInstance().getCurrentPlayer())
//					//if we need every unit.
//					|| parameters.get("range").equals("all"))){
//
//					//Change the backend texture state
//					this.setTileState(TileState.WHITE);
//			}
//		}

//		//handle 2 -1 : find valid summon tile
//		else if (parameters.get("type").equals("validSummonRangeHighlight"))
//		{
//
//			// a. find a friendly unit
//			if (this.unitOnTile != null && this.unitOnTile.getOwner() == GameState.getInstance().getCurrentPlayer()){
//
//				int[] xpos = new int[]{
//						-1,-1,-1,0,0,1,1,1
//				};
//				int[] ypos = new int[]{
//						-1,0,1,-1,1,-1,0,1
//				};
//
//				//check all neighbour tiles
//				for (int i = 0; i < xpos.length; i++) {
//					parameters = new HashMap<>();
//					parameters.put("type","validSummonRangeHighlight-checkNeighbour");
//					parameters.put("tilex",this.tilex + xpos[i]);
//					parameters.put("tiley",this.tiley + ypos[i]);
//					GameState.getInstance().broadcastEvent(Tile.class,parameters);
//				}
//			}
//
//		}
//		//handle 2 -2 : check if there is a unit on it
//		else if (parameters.get("type").equals("validSummonRangeHighlight-checkNeighbour"))
//		{
//			if (this.unitOnTile == null
//					&& (Integer) parameters.get("tilex") == this.tilex
//					&& (Integer) parameters.get("tiley") == this.tiley
//			){
//
//				//Change the backend texture state
//				this.setTileState(TileState.WHITE);
//
//			}
//		}
//		//handle 3: reset tile texture
//		else if (parameters.get("type").equals("textureReset"))
//		{
//			if (!this.tileState.equals(TileState.NORMAL)){
//				//Change the backend texture state
//				this.setTileState(TileState.NORMAL);
//
//			}
//		}
//
//		else if (parameters.get("type").equals("summon")){
//			if ((Integer) parameters.get("tilex") == this.tilex
//					&& (Integer) parameters.get("tiley") == this.tiley) {
//
//					Unit unit = (Unit) parameters.get("unit");
//					unit.setPositionByTile(this);
//					this.unitOnTile = unit;
//					// render front-end
//					BasicCommands.drawUnit(GameState.getInstance().getOut(), unit, this);
//					// wait for the creation of the unit
//					try {
//						Thread.sleep(500);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//			else if (parameters.get("type").equals("clickUnit")
//					&& this.unitOnTile != null) {
//
//				Map<String, Object> newParameters;
//
//				int[] offsetx = new int[]{-1, 0, 0, 1, -2, -1, 1, 2, -1, 0, 1, 0};
//				int[] offsety = new int[]{-1, -2, -1, -1, 0, 0, 0, 0, 1, 1, 1, 2};
//
//				for (int i = 0; i < offsety.length; i++) {
//
//					int newTileX = tilex + offsetx[i];
//					int newTileY = tiley + offsety[i];
//
//					if (newTileX >= 0 && newTileY >= 0) {
//						newParameters = new HashMap<>();
//						newParameters.put("type", "tileHighlight");
//						newParameters.put("tilex", newTileX);
//						newParameters.put("tiley", newTileY);
//
//						GameState.getInstance().broadcastEvent(Tile.class, newParameters);
//					}
//				}
//
//			}
//			else if (parameters.get("type").equals("tileHighlight")) {
//				BasicCommands.drawTile(GameState.getInstance().getOut(), this, 1);
//			}
//			else if (this.unitOnTile != null && parameters.get("type").equals("searchUnit")) {
//				if (
//					//if this is a enemy unit
//						(parameters.get("range").equals("enemy")
//								&& !this.unitOnTile.getOwner().equals(
//								GameState.getInstance().getCurrentPlayer()))
//								||
//								//or need all unit
//								parameters.get("range").equals("all")
//
//				) {
//				}
//
//			}
//		}
//		}


//	}

	public void moveHighlight() {
		Map<String, Object> newParameters;

		int[] offsetx = new int[]{0,-2,-1,-1,-1, 0, 0, 0, 0, 1, 1, 1, 2};
		int[] offsety = new int[]{0, 0,-1, 0, 1,-2,-1, 1, 2,-1, 0, 1, 0};

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
		try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
	}

	public void attackHighlight() {
		Map<String, Object> newParameters;

		int[] offsetx = new int[]{1,1,0,-1,-1,-1,0,1};
		int[] offsety = new int[]{0,1,1,1,0,-1,-1,-1};

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
		try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
	}

	public void clearAllHighlight() {
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
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
	}

}
