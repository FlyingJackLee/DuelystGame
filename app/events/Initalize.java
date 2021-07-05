package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import demo.CommandDemo;
import org.checkerframework.checker.units.qual.A;
import structures.GameState;
import structures.basic.*;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;
import utils.ToolBox;

import javax.tools.Tool;
import java.util.HashMap;
import java.util.Map;

/**
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to recieve commands from the back-end.
 * 
 * { 
 *   messageType = “initalize”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Initalize implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		//clear the instance
		GameState.getInstance().clearObservers();
		//CommandDemo.executeDemo(out); // this executes the command demo, comment out this when implementing your solution

		//1. generate the tiles
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				Tile tile = BasicObjectBuilders.loadTile(i,j);
				//register on gameState
				GameState.getInstance().add(tile);
				BasicCommands.drawTile(out,tile,0);
			}
		}

		//2.generate Players
		Player humanPlayer = new Player(20, 0);
		AIPlayer AIPlayer = new AIPlayer(20, 0);

		BasicCommands.setPlayer1Health(out, humanPlayer);
		BasicCommands.setPlayer2Health(out, AIPlayer);

		//3.set the deck
		String[] deck1Cards = {
				StaticConfFiles.c_comodo_charger,
				StaticConfFiles.c_hailstone_golem,
				StaticConfFiles.c_pureblade_enforcer,
				StaticConfFiles.c_azure_herald,
				StaticConfFiles.c_silverguard_knight,
				StaticConfFiles.c_azurite_lion,
				StaticConfFiles.c_fire_spitter,
				StaticConfFiles.c_ironcliff_guardian,
				StaticConfFiles.c_truestrike,
				StaticConfFiles.c_sundrop_elixir
		};

		String[] deck2Cards = {
				StaticConfFiles.c_planar_scout,
				StaticConfFiles.c_rock_pulveriser,
				StaticConfFiles.c_pyromancer,
				StaticConfFiles.c_bloodshard_golem,
				StaticConfFiles.c_blaze_hound,
				StaticConfFiles.c_windshrike,
				StaticConfFiles.c_hailstone_golem,
				StaticConfFiles.c_serpenti,
				StaticConfFiles.c_staff_of_ykir,
				StaticConfFiles.c_entropic_decay
		};

		for (int i = 0; i < 10; i++) {
			Card card = BasicObjectBuilders.loadCard(deck1Cards[i],i,Card.class );
			humanPlayer.setDeck(card);
		}

		for (int i = 0; i < 10; i++) {
			Card card = BasicObjectBuilders.loadCard(deck2Cards[i],i+10,Card.class );
			AIPlayer.setDeck(card);
		}


		BasicCommands.addPlayer1Notification(out, "Your turn", 2);

		Map<String,Object> parameters = new HashMap<>();


		//4.creat avatar for both players
		Unit humanAvatar = BasicObjectBuilders.loadUnit(
				StaticConfFiles.humanAvatar,
				ToolBox.humanAvatarId,Unit.class
		);

		humanAvatar.setOwner(humanPlayer);

		GameState.getInstance().add(humanAvatar);
		parameters = new HashMap<>();
		parameters.put("type","summon");
		parameters.put("tilex",1);
		parameters.put("tiley",2);
		parameters.put("unit",humanAvatar);
		GameState.getInstance().broadcastEvent(Tile.class,parameters);

		Unit AiAvatar = BasicObjectBuilders.loadUnit(
				StaticConfFiles.aiAvatar,
				ToolBox.AIAvatarID,Unit.class
		);
		GameState.getInstance().add(AiAvatar);

		AiAvatar.setOwner(AIPlayer);
		parameters = new HashMap<>();
		parameters.put("type","summon");
		parameters.put("tilex",7);
		parameters.put("tiley",2);
		parameters.put("unit",AiAvatar);
		GameState.getInstance().broadcastEvent(Tile.class,parameters);

		//4.1 set attack/health of humanAvatar
		humanAvatar.setAttack(2);
		humanAvatar.setHealth(20);

		parameters = new HashMap<>();
		parameters.put("type","setUnit");

		parameters.put("unitId",ToolBox.humanAvatarId);


		GameState.getInstance().broadcastEvent(Unit.class,parameters);

		//4.2 set attack/health of AiAvatar
		AiAvatar.setAttack(2);
		AiAvatar.setHealth(20);
		parameters = new HashMap<>();
		parameters.put("type","setUnit");
		parameters.put("unitId",ToolBox.AIAvatarID);
		GameState.getInstance().broadcastEvent(Unit.class,parameters);


		//5.set player
		GameState.getInstance().addPlayers(humanPlayer,AIPlayer);
		GameState.getInstance().setAi(AIPlayer);



		//6.human player draw 3 cards
		humanPlayer.drawCard();
		humanPlayer.drawCard();
		humanPlayer.drawCard();

		//7. AI player draw 3 cards
		AIPlayer.drawCard();
		AIPlayer.drawCard();
		AIPlayer.drawCard();

		//8.set all unit READY
		parameters =  new HashMap<>();
		parameters.put("type","unitBeReady");
		GameState.getInstance().broadcastEvent(Unit.class,parameters);



	}

}


