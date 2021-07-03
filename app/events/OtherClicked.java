package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Player;
import structures.basic.Tile;

import java.util.HashMap;
import java.util.Map;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case
 * somewhere that is not on a card tile or the end-turn button.
 * 
 * { 
 *   messageType = “otherClicked”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class OtherClicked implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		if (GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)) {

			//clear card selected
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("type","cardSelectedReset");
			GameState.getInstance().broadcastEvent(Player.class,parameters);

			//clear valid tiles highlight
			parameters = new HashMap<>();
			parameters.put("type","textureReset");
			GameState.getInstance().broadcastEvent(Tile.class,parameters);

			//reset game current state
			GameState.getInstance().setCurrentState(GameState.CurrentState.READY);

		}
		}

	}




