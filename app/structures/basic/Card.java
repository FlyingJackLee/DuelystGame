package structures.basic;


import structures.GameState;
import utils.BasicObjectBuilders;

import java.util.*;
import java.util.function.BiFunction;

/**
 * This is the base representation of a Card which is rendered in the player's hand.
 * A card has an id, a name (cardname) and a manacost. A card then has a large and mini
 * version. The mini version is what is rendered at the bottom of the screen. The big
 * version is what is rendered when the player clicks on a card in their hand.
 * 
 * @author Dr. Richard McCreadie
 *
 */

public class Card {



 	int id;

	String cardname;
	int manacost;

	MiniCard miniCard;
	BigCard bigCard;

	public Card() {}

	public Card(int id, String cardname, int manacost, MiniCard miniCard, BigCard bigCard) {
		super();
		this.id = id;
		this.cardname = cardname;
		this.manacost = manacost;
		this.miniCard = miniCard;
		this.bigCard = bigCard;


	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCardname() {
		return cardname;
	}
	public void setCardname(String cardname) {
		this.cardname = cardname;
	}
	public int getManacost() {
		return manacost;
	}
	public void setManacost(int manacost) {
		this.manacost = manacost;
	}
	public MiniCard getMiniCard() {
		return miniCard;
	}
	public void setMiniCard(MiniCard miniCard) {
		this.miniCard = miniCard;
	}
	public BigCard getBigCard() {
		return bigCard;
	}
	public void setBigCard(BigCard bigCard) {
		this.bigCard = bigCard;
	}

	public Unit cardToUnit() {
		String unit_path = this.cardname.split(" ")[0].toLowerCase(Locale.ROOT);
		if (this.cardname.split(" ")[1].toLowerCase(Locale.ROOT) != "") {
			unit_path += "_" + this.cardname.split(" ")[1].toLowerCase(Locale.ROOT);
		}
		unit_path = "conf/gameconfs/units/" + unit_path + ".json";

		// create unit
		Unit unit = BasicObjectBuilders.loadUnit(unit_path, id, Unit.class);

		// register unit
		GameState.getInstance().add(unit);

		// set health and attack
		unit.setHealth(this.bigCard.getHealth());
		unit.setAttack(this.bigCard.getAttack());

		unit.setMaxHealth(this.bigCard.getHealth());

		return unit;
	}


	private Unit cardToUnit(){
		String unit_path = this.cardname.split(" ")[0].toLowerCase(Locale.ROOT);
		if (this.cardname.split(" ").length > 1 && this.cardname.split(" ")[1].toLowerCase(Locale.ROOT) != ""){
			unit_path += "_" + this.cardname.split(" ")[1].toLowerCase(Locale.ROOT);
		}
		unit_path = "conf/gameconfs/units/" + unit_path + ".json";

		//create unit
		Unit unit = BasicObjectBuilders.loadUnit(unit_path,id,Unit.class);


		//register unit
		GameState.getInstance().add(unit);

		//set health and attack
		unit.setHealth(this.bigCard.getHealth());
		unit.setAttack(this.bigCard.getAttack());

		unit.setMaxHealth(this.bigCard.getHealth());

		return unit;
	}


	public int isCreatureOrSpell(){

		//if it is a spell
		if (this.getBigCard().getAttack() == -1){
			return -1;
		}
		//if it is a creature
		else {
			return 1;
		}
		// if it is a creature
		else {
			return 1;
		}
	}

	// when a creature card is used, call this method
	public void creatureCardUsed(int tilex, int tiley) {

		// Callback Point: <BeforeSummonCallbacks>
		// run callbacks before summon
		int id = this.id;
		if (GameState.getInstance().getBeforeSummonCallbacks().get(String.valueOf(id)) != null) {
			// call the callback
			GameState.getInstance().getBeforeSummonCallbacks().get(String.valueOf(id)).apply(id);
		}

		Map<String, Object> parameters = new HashMap<>();

		Unit unit = this.cardToUnit();

		// summon unit
		parameters.put("type", "summon");
		parameters.put("tilex", tilex);
		parameters.put("tiley", tiley);
		parameters.put("unit", unit);
		GameState.getInstance().broadcastEvent(Tile.class, parameters);

		// set attack and health
		parameters = new HashMap<>();
		parameters.put("type", "setUnit");
		parameters.put("unitId", this.getId());
		GameState.getInstance().broadcastEvent(Unit.class, parameters);

		GameState.getInstance().setCurrentState(GameState.CurrentState.READY);
	}




	/*
	 *
	 * When the creature card is going to use, call this method
	 *
	 */

	public void creatureCardUsed(int tilex,int tiley){

		//Callback Point:<BeforeSummonCallbacks>
		//run callbacks before summon
		int id = this.id;
		if (GameState.getInstance().getBeforeSummonCallbacks().get(String.valueOf(id)) != null){
			//call the callback
			GameState.getInstance().getBeforeSummonCallbacks().get(String.valueOf(id)).apply(id);
		}

		Map<String,Object> parameters = new HashMap<>();

		Unit unit = this.cardToUnit();

		//summon unit
		parameters.put("type","summon");
		parameters.put("tilex",tilex);
		parameters.put("tiley",tiley);
		parameters.put("unit",unit);
		GameState.getInstance().broadcastEvent(Tile.class,parameters);



		//set attack and health
		parameters = new HashMap<>();
		parameters.put("type","setUnit");
		parameters.put("unitId",this.getId());

		GameState.getInstance().broadcastEvent(Unit.class,parameters);

		GameState.getInstance().setCurrentState(GameState.CurrentState.READY);


	}




//	/*
//	 *
//	 * Find out if a card has special ability to be call before summon
//	 *
//	 */
//	public void registerCallbacksBeforeSummon(){
////		//1.Azure Herald
////		if (this.cardname.toLowerCase(Locale.ROOT).equals("Azure Herald".toLowerCase(Locale.ROOT))){
////			//a. heal your avatar(of current player) + 3
////			this.beforeSummonCallbackcs.add(
////					new BiFunction<Object, Object,Boolean>() {
////						@Override
////						public Boolean apply(Object o, Object o2) {
////
////							Map<String, Object> parameters = new HashMap<>();
////							parameters.put("type", "modifyUnit");
////
////							int unitId = -1;
////
////							//human player play now
////							if (GameState.getInstance().getCurrentPlayer().isHumanOrAI()) {
////								//human avatar id:99
////								unitId = 99;
////							} else {
////								//ai avatar id:99
////								unitId = 100;
////							}
////
////							parameters.put("unitId", unitId);
////							parameters.put("attack", 0);
////							parameters.put("health", 3);
////							parameters.put("limit","max");
////
////							GameState.getInstance().broadcastEvent(Unit.class, parameters);
////
////							return true;
////						}
////					}
////			);
////
////
////			//.Ironcliff Guardian
////			if (this.cardname.toLowerCase(Locale.ROOT).equals("Ironcliff Guardian".toLowerCase(Locale.ROOT))){
////				//a. heal your avatar(of current player) + 3
////				this.cardUsedCallback.add(
////						new BiFunction<Object, Object,Boolean>() {
////							@Override
////							public Boolean apply(Object o, Object o2) {
////
////								Map<String,Object> parameters = new HashMap<>();
////								parameters.put("type","validSummonRangeHighlight");
////								parameters.put("airdrop","activate");
////
////								GameState.getInstance().broadcastEvent(Tile.class,parameters);
////
////								return true;
////							}
////						}
////				);
////
////
////		}
//
//
//
//
////
////		//draft version
////		else if (rules.contains("draw")){
////			this.beforeSummonCallbackcs.add(
////					new BiFunction<Object, Object,Boolean>() {
////						@Override
////						public Boolean apply(Object o, Object o2) {
////							GameState.getInstance().getPlayerContainers()[0].drawCard();
////							GameState.getInstance().getPlayerContainers()[1].drawCard();
////							return true;
////						}
////					}
////			);
////		}
//
//
//	}
//


}

