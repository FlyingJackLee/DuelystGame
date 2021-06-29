package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;

import java.util.HashMap;
import java.util.Map;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case a tile.
 * The event returns the x (horizontal) and y (vertical) indices of the tile that was
 * clicked. Tile indices start at 1.
 * 
 * { 
 *   messageType = “tileClicked”
 *   tilex = <x index of the tile>
 *   tiley = <y index of the tile>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class TileClicked implements EventProcessor{
	
	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();
		Map<String,Object> parameters = new HashMap<>();
		
		//Store the coordinate in parameters.
		parameters.put("tilex",tilex);
		parameters.put("tiley",tiley);
		
		if(GameState.getInstance().getState().equals("readyState")) {
			parameters.put("type", "tileClicked");
		}else if(GameState.getInstance().getState().equals("unitClicked")) {
			parameters.put("type", "action");
		}else if(GameState.getInstance().getState().equals("cardClicked")) {
			parameters.put("type", "summon");
		}
		
		GameState.getInstance().broadcastEvent(Tile.class, parameters);

	}
}
