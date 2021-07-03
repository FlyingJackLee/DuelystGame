package structures.basic;

import akka.util.Helpers;
import commands.BasicCommands;
import org.ietf.jgss.GSSManager;
import structures.GameState;
import structures.Observer;
import utils.ToolBox;

import java.util.*;

/**
 * A basic representation of of the Player. A player
 * has health and mana.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Player extends Observer {

	private List<Card> deck = new ArrayList<>();
	private List<Card> cardsOnHand  = new ArrayList<>();;

	public void setDeck(Card ...cards){
		for (Card card:cards) {
			this.deck.add(card);
		}
	}

	/*
	 *
	 * TODO
	 *
	 * @param  TODO
	 * @return void TODO
	 */
	private void drawCard(){
		int randomInt = new Random().nextInt(deck.size());
		Card card = this.deck.get(randomInt);
		this.deck.remove(card);
		this.cardsOnHand.add(card);
		BasicCommands.drawCard(GameState.getInstance().getOut(),
				card,cardsOnHand.size()-1,0);
	}


	int health;
	int mana;
	
	public Player() {
		super();
		this.health = 20;
		this.mana = 0;
	}
	public Player(int health, int mana) {
		super();
		this.health = health;
		this.mana = mana;
	}
	public int getHealth() {
		return health;
	}
	public void setHealth(int health) {
		this.health = health;
	}
	public int getMana() {
		return mana;
	}
	public void setMana(int mana) {
		this.mana = mana;
		BasicCommands.setPlayer1Mana(GameState.getInstance().getOut(),this);
	}

	@Override
	public void trigger(Class target, Map<String,Object> parameters) {
		if (this.getClass().equals(target)){
			if (parameters.get("type").equals("increaseMana")){
				this.setMana(mana + Integer.parseInt((String) parameters.get("mana")));
			}
			else if (parameters.get("type").equals("draw3Cards")) {
				drawCard();
				drawCard();
				drawCard();
			}

			else if (parameters.get("type").equals("cardClick")) {
				int handPosition = (Integer) parameters.get("position");
				//if the player has enough mana
				if(cardsOnHand.get(handPosition).getManacost() <= this.mana){
					//highlight card
					cardSelected(handPosition);
					//highlight valid tiles
					showValidRange(handPosition);

					//store the card into gameState
					GameState.getInstance().setCardSelected(cardsOnHand.get(handPosition));
				}
				else {
					ToolBox.logNotification("Mana not enough");
				}

			}

			else if (parameters.get("type").equals("cardSelectedReset")) {
				cardSelected(-1);
			}


		}
	}

	//clear card from hand
	public void removeCardFromHand(Card card){
		//remove from hand(backend and frontend)
		BasicCommands.deleteCard(GameState.getInstance().getOut(), cardsOnHand.indexOf(card));
		try{Thread.sleep(50);}catch (InterruptedException e){e.printStackTrace();}
		this.cardsOnHand.remove(card);

		//remove form gameState
		GameState.getInstance().setCardSelected(null);
		//clear highlight
		cardSelected(-1);

		//clear the range
		Map<String,Object> parameters = new HashMap<>();
		parameters = new HashMap<>();
		parameters.put("type","textureReset");
		GameState.getInstance().broadcastEvent(Tile.class,parameters);

		//reset game current state
		GameState.getInstance().setCurrentState(GameState.CurrentState.READY);

	}


	/**
	 *
	 * set the card selected
	 *
	 * @param position:  0 ~ 6( at specific position), or -1(reset selected)
	 */
	private void cardSelected(int position){

		for (int i = 0; i < cardsOnHand.size(); i++) {
			if (i == position){
				BasicCommands.drawCard(GameState.getInstance().getOut(),
						cardsOnHand.get(i),i,1);
				GameState.getInstance().setCardSelected(cardsOnHand.get(i));
			}
			else{
				BasicCommands.drawCard(GameState.getInstance().getOut(),
						cardsOnHand.get(i),i,0);
				GameState.getInstance().setCardSelected(null);

			}


		}
	}


	private void showValidRange(int position){
		Map<String,Object> parameters = new HashMap<>();

		parameters.put("type","textureReset");
		GameState.getInstance().broadcastEvent(Tile.class,parameters);

		//waiting for completion of reset
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}


		Card cardSelected = this.cardsOnHand.get(position);


		//Calculate the target range of card
		//if it is a spell
		if (cardSelected.isCreatureOrSpell() == -1) {
			String rule = cardSelected.getBigCard().getRulesTextRows()[0];
			parameters = new HashMap<>();
			parameters.put("type", "searchUnit");
			//if the target is a unit
			if (rule.toLowerCase(Locale.ROOT).contains("unit")) {

				//find all enemy Unit
				if (rule.toLowerCase(Locale.ROOT).contains("enemy")) {
					parameters.put("range", "enemy");

					//ask the tileS to give the list of enemy units and highlight them.
					GameState.getInstance().broadcastEvent(Tile.class, parameters);
				}
				//find all non-avatar Unit
				else if (rule.toLowerCase(Locale.ROOT).contains("non-avatar")) {
					parameters.put("range", "non_avatar");
					//ask the tileS to give the list of all units and highlight them.
					GameState.getInstance().broadcastEvent(Tile.class, parameters);
				}
				//find all Unit
				else {
					parameters.put("range", "all");
					//ask the tileS to give the list of all units and highlight them.
					GameState.getInstance().broadcastEvent(Tile.class, parameters);
				}
			}

			//if the target is a avatar
			else if (rule.toLowerCase(Locale.ROOT).contains("avatar")) {
				//find current avatar
				if (rule.toLowerCase(Locale.ROOT).contains("your avatar")) {
					{
						parameters.put("range", "your_avatar");
						//ask the tile to give the list of enemy units
						GameState.getInstance().broadcastEvent(Tile.class, parameters);
					}
				}
			}
		}
		//if it is a creature
		else {
			parameters = new HashMap<>();
			parameters.put("type","validSummonRangeHighlight");

			GameState.getInstance().broadcastEvent(Tile.class,parameters);

		}


	}

}

