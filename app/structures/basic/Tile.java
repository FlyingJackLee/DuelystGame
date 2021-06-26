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
	boolean isHighlight; // whether a tile is highlighted

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

	public boolean isHighlight() {
		return isHighlight;
	}

	public void setHighlight(boolean isHighlight) {
		this.isHighlight = isHighlight;
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
		if (this.getClass().equals(target)){
			if ((Integer) parameters.get("tilex") == this.tilex && (Integer) parameters.get("tiley") == this.tiley){
				if (parameters.get("type").equals("summon")) {
					Unit unit = (Unit) parameters.get("unit");
					unit.setPositionByTile(this);
					this.unitOnTile = unit;
					// render front-end
					BasicCommands.drawUnit(GameState.getInstance().getOut(), unit, this);
					// wait for the creation of the unit
					try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
				}			 
				
				else if(parameters.get("type").equals("tileClicked")) {
					// if there is a unit on tile, record this unit and highlight
					if(this.unitOnTile != null) {
						this.moveHighlight(); 
						GameState.getInstance().setTileClicked(this);;  // record the tile
						GameState.getInstance().setUnitClicked(unitOnTile);  // record the unit					
					}
					else if (this.unitOnTile == null) {
						// if the tile is movable, move to this tile
						if (GameState.getInstance().getUnitClicked() != null && GameState.getInstance().getCardClicked() == null) {
							if (this.isHighlight) {
								unitOnTile = GameState.getInstance().getUnitClicked(); // set the unit on this tile
								unitOnTile.moveToTile(this);
								this.clearHighlight();
								GameState.getInstance().getTileClicked().setUnitOnTile(null); // set the unit on origin tile "null"
								GameState.getInstance().clearClicked();
							}						
						}
						
						// play a card to a tile
						else if (GameState.getInstance().getCardClicked() != null && GameState.getInstance().getUnitClicked() == null) {
							Card card = GameState.getInstance().getCardClicked();
							GameState.getInstance().getCurrentPlayer().drawUnit(card,this);
							
						}
					}
				}				
				else if(parameters.get("type").equals("placeHighlight")) {
					this.placeHighlight();
				}
				
				// highlight the Avatar's movable tile
				else if (parameters.get("type").equals("tileHighlight")) {
					this.setHighlight(true);
					BasicCommands.drawTile(GameState.getInstance().getOut(), this, 1);
				}
				
				// clear the highlight
				else if (parameters.get("type").equals("clearHighlight")) {				
					this.setHighlight(false);
					BasicCommands.drawTile(GameState.getInstance().getOut(), this, 0);
				}
			}
		}
	}
	 
	// movable position
	public void moveHighlight() {
		Map<String, Object> newParameters;

		int[] offsetx = new int[]{0,-2,-1,-1,-1, 0, 0, 0, 0, 1, 1, 1, 2};
		int[] offsety = new int[]{0, 0,-1, 0, 1,-2,-1, 1, 2,-1, 0, 1, 0};

		for (int i = 0; i < offsety.length; i++) {

			int newTileX = tilex + offsetx[i];
			int newTileY = tiley + offsety[i];

			if (newTileX >= 0 && newTileY >= 0) {
					newParameters = new HashMap<>();
					newParameters.put("type", "tileHighlight");
					newParameters.put("tilex", newTileX);
					newParameters.put("tiley", newTileY);
					
					GameState.getInstance().broadcastEvent(Tile.class, newParameters);
			}
		}
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
	}
	
	// placeable position
	public void placeHighlight() {
		Map<String, Object> newParameters;

		int[] offsetx = new int[]{0,-1,-1,-1, 0, 0, 1, 1, 1};
		int[] offsety = new int[]{0,-1, 0, 1,-1, 1,-1, 0, 1};

		for (int i = 0; i < offsety.length; i++) {

			int newTileX = tilex + offsetx[i];
			int newTileY = tiley + offsety[i];

			if (newTileX >= 0 && newTileY >= 0) {
					newParameters = new HashMap<>();
					newParameters.put("type", "tileHighlight");
					newParameters.put("tilex", newTileX);
					newParameters.put("tiley", newTileY);
					
					GameState.getInstance().broadcastEvent(Tile.class, newParameters);
			}
		}
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
	}
	
	// clear highlight
	public void clearHighlight() {	
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
