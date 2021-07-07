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
public class Player {

	private List<Card> deck = new ArrayList<>();
	private Card[] cardsOnHand = new Card[6];

	public void setDeck(Card ...cards) {
		for (Card card : cards) {
			this.deck.add(card);
		}
	}

	/*
	 *
	 * draw card from deck
	 *
	 */
	public void drawCard() {
		int randomInt = new Random().nextInt(deck.size());
		Card card = this.deck.get(randomInt);
		this.deck.remove(card);

		int i;

		// find a blank space
		for (i = 0; i < 6; i++) {
			if(this.cardsOnHand[i] == null) {
				this.cardsOnHand[i] = card;
				BasicCommands.drawCard(GameState.getInstance().getOut(), card, i+1, 0);
				// wait for frontend
				try {Thread.sleep(ToolBox.delay);} catch (InterruptedException e) {e.printStackTrace();}
				break;
			}
		}

		if (i == 6) {
			ToolBox.logNotification("You cannot have more than 6 cards, discard this card.");
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
		BasicCommands.setPlayer1Health(GameState.getInstance().getOut(), this);
	}
	public int getMana() {
		return mana;
	}
	public void setMana(int mana) {
		this.mana = mana;
		BasicCommands.setPlayer1Mana(GameState.getInstance().getOut(), this);
	}

	// remove card from hand
	public void removeCardFromHand(Card card) {
		// update mana
		this.setMana(mana-card.getManacost());

		// clear highlight
		clearSelected();

		int index = ToolBox.findObjectInArray(this.cardsOnHand, card);

		// remove from hand (backend and frontend)
		BasicCommands.deleteCard(GameState.getInstance().getOut(), index+1);
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
		this.cardsOnHand[index] = null;

		// remove form GameState
		GameState.getInstance().setCardSelected(null);

		// clear range
		Map<String,Object> parameters = new HashMap<>();
		parameters.put("type", "textureReset");
		GameState.getInstance().broadcastEvent(Tile.class, parameters);
	}

	/**
	 *
	 * set the card selected
	 *
	 * @param handPosition: 0 ~ 6 (at specific position)
	 */
	public void cardSelected(int handPosition) {
		if (handPosition < 0 || handPosition > 5) {
			return;
		}

		Card cardSelected = this.cardsOnHand[handPosition];

		// if the player has enough mana
		if(cardSelected.getManacost() <= this.mana) {
			// highlight card
			// if player has selected a card, reset the card highlight firstly
			if (GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)) {
				clearSelected();
			}

			// set backend
			GameState.getInstance().setCardSelected(cardSelected);

			// render frontend
			BasicCommands.drawCard(GameState.getInstance().getOut(), cardSelected, handPosition+1, 1);

			// highlight valid tiles
			showValidRange(cardSelected);
		} else {
			ToolBox.logNotification("Mana not enough");
			return;
		}
	}

	public void clearSelected() {
		if (GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)) {
			BasicCommands.drawCard(GameState.getInstance().getOut(), GameState.getInstance().getCardSelected(),
					ToolBox.findObjectInArray(cardsOnHand, GameState.getInstance().getCardSelected())+1, 0);

			// clear backend
			GameState.getInstance().setCardSelected(null);
			GameState.getInstance().setTileSelected(null);
		}
	}

	public void showValidRange(Card cardSelected) {
		Map<String,Object> parameters = new HashMap<>();
		parameters.put("type", "textureReset");
		GameState.getInstance().broadcastEvent(Tile.class, parameters);

		// wait for completion of reset
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

		// calculate the target range of card
		// if it is a spell
		if (cardSelected.isCreatureOrSpell() == -1) {
			String rule = cardSelected.getBigCard().getRulesTextRows()[0];
			parameters = new HashMap<>();
			parameters.put("type", "searchUnit");
			// if the target is a unit
			if (rule.toLowerCase(Locale.ROOT).contains("unit")) {
				// find all enemy units
				if (rule.toLowerCase(Locale.ROOT).contains("enemy")) {
					parameters.put("range", "enemy");
					GameState.getInstance().broadcastEvent(Tile.class, parameters);
				}

				// find all non-avatar units
				else if (rule.toLowerCase(Locale.ROOT).contains("non-avatar")) {
					parameters.put("range", "non_avatar");
					GameState.getInstance().broadcastEvent(Tile.class, parameters);
				}

				// find all units
				else {
					parameters.put("range", "all");
					GameState.getInstance().broadcastEvent(Tile.class, parameters);
				}
			}

			// if the target is a avatar
			else if (rule.toLowerCase(Locale.ROOT).contains("avatar")) {
				// find current avatar
				if (rule.toLowerCase(Locale.ROOT).contains("your avatar")) {
					{
						parameters.put("range", "your_avatar");
						GameState.getInstance().broadcastEvent(Tile.class, parameters);
					}
				}
			}
		}

		// if it is a creature
		else {
			parameters = new HashMap<>();
			parameters.put("type", "validSummonRangeHighlight");
			GameState.getInstance().broadcastEvent(Tile.class, parameters);

			// Callback Point: <CardSelectedCallBacks>
			// run callbacks when a card is used
			int id = cardSelected.id;
			if (GameState.getInstance().getCardSelectedCallbacks().get(String.valueOf(id)) != null) {
				// call the callback
				GameState.getInstance().getCardSelectedCallbacks().get(String.valueOf(id)).apply(id);
			}
		}
	}
}
