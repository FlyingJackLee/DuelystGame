package structures.basic;

import commands.BasicCommands;
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
	private Card[] cardsOnHand  = new Card[6];

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

		int i;

		//find a blank space
		for (i = 0; i < 6; i++) {
			if(this.cardsOnHand[i] == null){
				this.cardsOnHand[i] = card;
				BasicCommands.drawCard(GameState.getInstance().getOut(),
						card,i +1,0);
				try {
					Thread.sleep(ToolBox.delay);
				}
				//wait for the frontend
				catch (InterruptedException e){e.printStackTrace();}
				break;
			}
		}


		if (i == 6){
			//TODO : need to be test.
			ToolBox.logNotification("You can have more card(exceed 6), discard this card.");
		}


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

				int handPosition = (Integer) parameters.get("position") - 1;

				if (handPosition <0 || handPosition > 5 ){
					return;
				}

				Card card = this.cardsOnHand[handPosition];

				//if the player has enough mana
				if(card.getManacost() <= this.mana){

					//highlight card
					cardSelected(handPosition);

					//highlight valid tiles
					showValidRange(card);

				}
				else {
					ToolBox.logNotification("Mana not enough");
				}

			}

			else if (parameters.get("type").equals("cardSelectedReset")) {
				clearSelected();
			}


		}
	}

	//clear card from hand
	public void removeCardFromHand(Card card){


		//clear highlight
		clearSelected();


		int index = ToolBox.findObjectInArray(this.cardsOnHand,card);


		//remove from hand(backend and frontend)
		BasicCommands.deleteCard(GameState.getInstance().getOut(),index+1);
		try {
			Thread.sleep(500);
		}catch (InterruptedException e){e.printStackTrace();}
		this.cardsOnHand[index] = null;

		//remove form gameState
		GameState.getInstance().setCardSelected(null);


		//clear the range
		Map<String,Object> parameters = new HashMap<>();
		parameters.put("type","textureReset");
		GameState.getInstance().broadcastEvent(Tile.class,parameters);



	}


	/**
	 *
	 * set the card selected
	 *
	 * @param position:  0 ~ 6( at specific position)
	 */
	private void cardSelected(int position){

		Card cardSelected = this.cardsOnHand[position];

		//if the player have selected a card, reset the card highlight firstly
		if ( GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT) ){
			clearSelected();
		}

		//set backend
		GameState.getInstance().setCardSelected(cardSelected);

		//render frontend
		BasicCommands.drawCard(GameState.getInstance().getOut(),cardSelected,
					position + 1
					,1);


	}

	private void clearSelected(){
		if (GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)){

			BasicCommands.drawCard(GameState.getInstance().getOut(),GameState.getInstance().getCardSelected(),
					ToolBox.findObjectInArray(cardsOnHand,GameState.getInstance().getCardSelected()) + 1
					,0);

			//clear backend
			GameState.getInstance().setCardSelected(null);


		}
	}


	private void showValidRange(Card cardSelected){
		Map<String,Object> parameters = new HashMap<>();

		parameters.put("type","textureReset");
		GameState.getInstance().broadcastEvent(Tile.class,parameters);

		//waiting for completion of reset
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}


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

