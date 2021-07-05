package structures.basic;


import structures.GameState;
import structures.Observer;
import utils.BasicObjectBuilders;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
	
	public Card() {};
	
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







	public Unit cardToUnit(){
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
	}

}
