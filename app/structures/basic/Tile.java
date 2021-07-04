package structures.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		NORMAL("normal", 0), GREY("grey", 1), RED("red", 2);

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
									//if we need all non-avtar unit and ti is the one.
									|| (parameters.get("range").equals("non_avatar") && this.unitOnTile.id < 99)
									) {

						this.setTileState(TileState.GREY);
					}
					else if (parameters.get("range").equals("your_avatar")
							//if it is a avatar
							&& this.unitOnTile.id >= 99
							//if it is the avatar of current player
							&& this.unitOnTile.getOwner().equals(GameState.getInstance().getCurrentPlayer())) {
						this.setTileState(TileState.GREY);
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
					this.setTileState(TileState.GREY);

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
						if (!this.tileState.equals(TileState.GREY)){
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

		}

	}
}