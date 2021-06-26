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
<<<<<<< HEAD
		parameters.put("type","clickUnit");
		parameters.put("tilex",tilex);
		parameters.put("tiley",tiley);
		
		if(GameState.getInstance().getState().getStateName().equals("ReadyToAttack")) {
			
		}
		
		GameState.getInstance().broadcastEvent(Tile.class,parameters);
		
		

=======
		parameters.put("type", "tileClicked");
		parameters.put("tilex", tilex);
		parameters.put("tiley", tiley);
		GameState.getInstance().broadcastEvent(Tile.class, parameters);
>>>>>>> d186ff6ce9e7f7526377356e683b15d9a86e652b
	}
}
