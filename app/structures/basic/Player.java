package structures.basic;

import commands.BasicCommands;
import structures.GameState;
import structures.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A basic representation of of the Player. A player
 * has health and mana.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Player extends Observer {

	private List<Card> deck = new ArrayList<>();
	private List<Card> cardsOnHand  = new ArrayList<>();

	public void setDeck(Card ...cards) {
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
	private void drawCard() {
		int randomInt = new Random().nextInt(deck.size());
		Card card = this.deck.get(randomInt);
		this.deck.remove(card);
		if (cardsOnHand.size() < 6) {
			this.cardsOnHand.add(card);
			BasicCommands.drawCard(GameState.getInstance().getOut(), card, cardsOnHand.size()-1, 0);
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
		BasicCommands.setPlayer1Mana(GameState.getInstance().getOut(), this);
	}

	@Override
	public void trigger(Class target, Map<String,Object> parameters) {
		if (this.getClass().equals(target)) {
			if (parameters.get("type").equals("increaseMana")) {
				this.setMana(mana + Integer.parseInt((String) parameters.get("mana")));
			}

			else if (parameters.get("type").equals("draw3Cards")) {
				drawCard();
				drawCard();
				drawCard();
			}

			else if (parameters.get("type").equals("cardClick")) {
				int handPosition = (Integer) parameters.get("position");
				highlightCardOnPosition(handPosition);
			}

			else if (parameters.get("type").equals("clearCardHighlight")) {
				highlightCardOnPosition(-1);
			}

			else if (parameters.get("type").equals("endTurnClicked")) {
				drawCard();
				this.setMana(0);
			}
		}
	}

	private void highlightCardOnPosition(int position) {
		for (int i = 0; i < cardsOnHand.size(); i++) {
			if (i == position) {
				BasicCommands.drawCard(GameState.getInstance().getOut(), cardsOnHand.get(i), i, 1);
			} else {
				BasicCommands.drawCard(GameState.getInstance().getOut(), cardsOnHand.get(i), i, 0);
			}
		}
	}
}
