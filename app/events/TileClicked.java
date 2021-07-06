package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import play.api.Play;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;

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

		Map<String,Object> parameters;


		if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)){


			//get card selected
			Card cardSelected = GameState.getInstance().getCardSelected();


			//if it is a creature
			if (cardSelected.isCreatureOrSpell() == 1){

				cardSelected.creatureCardUsed(tilex,tiley);

			}
			//if it is a spell
			else {

			}
			}


		else if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.READY)){
			parameters = new HashMap<>();
			parameters.put("type","tileClicked");
			parameters.put("tilex",tilex);
			parameters.put("tiley",tiley);
			GameState.getInstance().broadcastEvent(Tile.class,parameters);
		}

		else if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.UNIT_SELECT)){
			parameters = new HashMap<>();
			parameters.put("type", "operateUnit");
			parameters.put("tilex",tilex);
			parameters.put("tiley",tiley);
			GameState.getInstance().broadcastEvent(Tile.class,parameters);

		}

	}
}
