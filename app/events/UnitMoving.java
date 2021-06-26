package events;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Indicates that a unit instance has started a move. 
 * The event reports the unique id of the unit.
 * 
 * { 
 *   messageType = “unitMoving”
 *   id = <unit id>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class UnitMoving implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		
		int unitid = message.get("id").asInt();
				
	}

}
