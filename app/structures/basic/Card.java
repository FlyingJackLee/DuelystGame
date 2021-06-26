package structures.basic;


import structures.GameState;
import structures.Observer;

import java.util.List;
import java.util.Map;

import commands.BasicCommands;



/**
 * This is the base representation of a Card which is rendered in the player's hand.
 * A card has an id, a name (cardname) and a manacost. A card then has a large and mini
 * version. The mini version is what is rendered at the bottom of the screen. The big
 * version is what is rendered when the player clicks on a card in their hand.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Card extends Observer{


    int id;
	
	String cardname;
	int manacost;
	
	MiniCard miniCard;
	BigCard bigCard;
	String confFiles;
	boolean isHighlight;
	
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

	public String getConfFiles() {
		return confFiles;
	}

	public void setConfFiles(String confFiles) {
		this.confFiles = confFiles;
	}

	public boolean isHighlight() {
		return isHighlight;
	}

	public void setHighlight(boolean isHighlight) {
		this.isHighlight = isHighlight;
	}

	@Override
	public void trigger(Class target, Map<String,Object> parameters) {
		if (this.getClass().equals(target)){
//			int handPosition = (Integer) parameters.get("position");
//			if(GameState.getInstance().getCurrentPlayer().getCardsOnHand().get(handPosition).getId() == this.id) {
//				if(parameters.get("type").equals("cardClicked")) {
//					cardHighlight(handPosition);
//				}
//			}
//			if(parameters.get("type").equals("cardClicked")) {
//				int handPosition = (Integer) parameters.get("position");
//				int cardId = GameState.getInstance().getCurrentPlayer().getCardsOnHand().get(handPosition).getId();
//				if(cardId == this.id) {
//					int uid = 0;
//					parameters.put("type", "findAvatar");
//					parameters.put("unitId", uid);
//					GameState.getInstance().broadcastEvent(Tile.class, parameters);
//				}
//			}
		}
	}
	
	public void cardHighlight(int position){
		if(this.isHighlight) {
			this.setHighlight(false);
			BasicCommands.drawCard(GameState.getInstance().getOut(), this,position,0);			
		}else {
			this.setHighlight(true);
			BasicCommands.drawCard(GameState.getInstance().getOut(), this,position,1);			
		}
		
	}
}
