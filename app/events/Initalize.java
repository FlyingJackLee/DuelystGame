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
		Player AIPlayer = new AIPlayer(20, 0);

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

		for (int i = 0; i < 10; i++) {
			Card card = BasicObjectBuilders.loadCard(deck1Cards[i],i,Card.class );
			humanPlayer.setDeck(card);
			GameState.getInstance().add(card);
		}

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
			Card card = BasicObjectBuilders.loadCard(deck2Cards[i],i+10,Card.class );
			AIPlayer.setDeck(card);
			GameState.getInstance().add(card);
		}

		//register the players on gameState
		GameState.getInstance().add(humanPlayer);
		GameState.getInstance().add(AIPlayer);


		BasicCommands.addPlayer1Notification(out, "Your turn", 2);

		//4.update the mana
		Map<String,Object> parameters = new HashMap<>();
		parameters.put("type","increaseMana");
		parameters.put("mana","1");
		GameState.getInstance().broadcastEvent(Player.class,parameters);

		//4.creat avatar for both players
		Unit humanAvatar = BasicObjectBuilders.loadUnit(
				StaticConfFiles.humanAvatar,
				0,Unit.class
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
				1,Unit.class
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
		parameters.put("unitId","0");
		parameters.put("unit",humanAvatar);
		GameState.getInstance().broadcastEvent(Unit.class,parameters);

		//4.2 set attack/health of AiAvatar
		AiAvatar.setAttack(2);
		AiAvatar.setHealth(20);
		parameters = new HashMap<>();
		parameters.put("type","setUnit");
		parameters.put("unitId","1");
		parameters.put("unit",AiAvatar);
		GameState.getInstance().broadcastEvent(Unit.class,parameters);


		//5.set player
		GameState.getInstance().setCurrentPlayer(humanPlayer);

		//6.human player draw 3 cards
		parameters = new HashMap<>();
		parameters.put("type","draw3Cards");
		GameState.getInstance().broadcastEvent(Player.class,parameters);


		// test move and attack highlight
		// Case 1: friend in move range not in attack range
		Unit friend1 = BasicObjectBuilders.loadUnit(
				StaticConfFiles.u_ironcliff_guardian,
				2,Unit.class
		);
		GameState.getInstance().add(friend1);
		friend1.setOwner(humanPlayer);
		friend1.setAttack(3);
		friend1.setHealth(10);
		parameters = new HashMap<>();
		parameters.put("type","summon");
		parameters.put("tilex",3);
		parameters.put("tiley",2);
		parameters.put("unit",friend1);
		GameState.getInstance().broadcastEvent(Tile.class,parameters);

		parameters = new HashMap<>();
		parameters.put("type","setUnit");
		parameters.put("unitId","2");
		parameters.put("unit",friend1);
		GameState.getInstance().broadcastEvent(Unit.class,parameters);

		// Case 2: friend in attack range
		Unit friend2 = BasicObjectBuilders.loadUnit(
				StaticConfFiles.u_azurite_lion,
				3,Unit.class
		);
		GameState.getInstance().add(friend2);
		friend2.setOwner(humanPlayer);
		friend2.setAttack(2);
		friend2.setHealth(3);
		parameters = new HashMap<>();
		parameters.put("type","summon");
		parameters.put("tilex",1);
		parameters.put("tiley",3);
		parameters.put("unit",friend2);
		GameState.getInstance().broadcastEvent(Tile.class,parameters);

		parameters = new HashMap<>();
		parameters.put("type","setUnit");
		parameters.put("unitId","3");
		parameters.put("unit",friend2);
		GameState.getInstance().broadcastEvent(Unit.class,parameters);

		// Case 3: enemy in attack range
		Unit enemy1 = BasicObjectBuilders.loadUnit(
				StaticConfFiles.u_windshrike,
				4,Unit.class
		);
		GameState.getInstance().add(enemy1);
		enemy1.setOwner(AIPlayer);
		enemy1.setAttack(4);
		enemy1.setHealth(3);
		parameters = new HashMap<>();
		parameters.put("type","summon");
		parameters.put("tilex",1);
		parameters.put("tiley",1);
		parameters.put("unit",enemy1);
		GameState.getInstance().broadcastEvent(Tile.class,parameters);

		parameters = new HashMap<>();
		parameters.put("type","setUnit");
		parameters.put("unitId","4");
		parameters.put("unit",enemy1);
		GameState.getInstance().broadcastEvent(Unit.class,parameters);

		// case 4: enemy in move and attack range
		Unit enemy2 = BasicObjectBuilders.loadUnit(
				StaticConfFiles.u_windshrike,
				5,Unit.class
		);
		GameState.getInstance().add(enemy2);
		enemy2.setOwner(AIPlayer);
		enemy2.setAttack(4);
		enemy2.setHealth(3);
		parameters = new HashMap<>();
		parameters.put("type","summon");
		parameters.put("tilex",3);
		parameters.put("tiley",3);
		parameters.put("unit",enemy2);
		GameState.getInstance().broadcastEvent(Tile.class,parameters);

		parameters = new HashMap<>();
		parameters.put("type","setUnit");
		parameters.put("unitId","5");
		parameters.put("unit",enemy2);
		GameState.getInstance().broadcastEvent(Unit.class,parameters);


	}

}


