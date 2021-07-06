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
public class TileClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();

		Map<String,Object> parameters;

		if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)) {
			// get selected card
			Card cardSelected = GameState.getInstance().getCardSelected();

			// if it is a creature
			if (cardSelected.isCreatureOrSpell() == 1) {
				// create unit
				Unit new_unit = cardSelected.cardToUnit();

				// summon unit
				parameters = new HashMap<>();
				parameters.put("type", "summon");
				parameters.put("tilex", tilex);
				parameters.put("tiley", tiley);
				parameters.put("card", cardSelected);
				parameters.put("unit", new_unit);
				parameters.put("cardName", cardSelected.getCardname());
				GameState.getInstance().broadcastEvent(Tile.class, parameters);

				// set attack and health
				parameters = new HashMap<>();
				parameters.put("type", "setUnit");
				parameters.put("unitId", cardSelected.getId());
				GameState.getInstance().broadcastEvent(Unit.class, parameters);

				// card: Pureblade Enforcer
				if (cardSelected.getCardname().equals("Pureblade Enforcer")) {
					parameters = new HashMap<>();
					parameters.put("type", "gainAttackAndHealth");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
				}

				// card: Azure Herald
				else if (cardSelected.getCardname().equals("Azure Herald")) {
					parameters = new HashMap<>();
					parameters.put("type", "heal");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
				}

				// card: Silverguard Knight
				else if (cardSelected.getCardname().equals("Silverguard Knight")) {
					parameters = new HashMap<>();
					parameters.put("type", "provoke");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
					parameters = new HashMap<>();
					parameters.put("type", "gainAttack");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
				}

				// card: Azurite Lion & Serpenti
				else if (cardSelected.getCardname().equals("Azurite Lion") || cardSelected.getCardname().equals("Serpenti")) {
					parameters = new HashMap<>();
					parameters.put("type", "attackTwice");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
				}

				// card: Fire Spitter & Pyromancer
				else if (cardSelected.getCardname().equals("Fire Spitter") || cardSelected.getCardname().equals("Pyromancer")) {
					parameters = new HashMap<>();
					parameters.put("type", "ranged");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
				}

				// card: Ironcliff Guardian
				else if (cardSelected.getCardname().equals("Ironcliff Guardian")) {
					parameters = new HashMap<>();
					parameters.put("type", "summonedAnywhere");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
					parameters = new HashMap<>();
					parameters.put("type", "provoke");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
				}

				// card: Planar Scout
				else if (cardSelected.getCardname().equals("Planar Scout")) {
					parameters = new HashMap<>();
					parameters.put("type", "summonedAnywhere");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
				}

				// card: Rock Pulveriser
				else if (cardSelected.getCardname().equals("Rock Pulveriser")) {
					parameters = new HashMap<>();
					parameters.put("type", "provoke");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
				}

				// card: Blaze Hound
				else if (cardSelected.getCardname().equals("Blaze Hound")) {
					parameters = new HashMap<>();
					parameters.put("type", "bothDrawCard");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
				}

				// card: Windshrike
				else if (cardSelected.getCardname().equals("Windshrike")) {
					parameters = new HashMap<>();
					parameters.put("type", "flying");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
					parameters = new HashMap<>();
					parameters.put("type", "drawCard");
					GameState.getInstance().broadcastEvent(Unit.class, parameters);
				}
			}

			// if it is a spell
			else {

			}
		}

		else if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.READY)) {
			parameters = new HashMap<>();
			parameters.put("type", "firstClickTile");
			parameters.put("tilex", tilex);
			parameters.put("tiley", tiley);
			GameState.getInstance().broadcastEvent(Tile.class, parameters);
		}

		else if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.UNIT_SELECT)) {
			parameters = new HashMap<>();
			parameters.put("type", "operateUnit");
			parameters.put("tilex", tilex);
			parameters.put("tiley", tiley);
			parameters.put("originTileSelected", GameState.getInstance().getTileSelected());
			GameState.getInstance().broadcastEvent(Tile.class, parameters);
		}
	}
}
