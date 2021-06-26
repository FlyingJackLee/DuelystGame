package structures.basic;

import commands.BasicCommands;
import structures.GameState;
import structures.Observer;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.HashMap;
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
	public List<Card> getDeck() {
		return deck;
	}

	public List<Card> getCardsOnHand() {
		return cardsOnHand;
	}
	public void setCardsOnHand(List<Card> cardsOnHand) {
		this.cardsOnHand = cardsOnHand;
	}

	public void setDeck(Card ...cards){
		for (Card card:cards) {
			this.deck.add(card);
		}
	}

	@Override
	public void trigger(Class target, Map<String,Object> parameters) {
		if (this.getClass().equals(target)){
			if (parameters.get("type").equals("increaseMana")){
				this.setMana(mana + Integer.parseInt((String) parameters.get("mana")));
			}
			else if (parameters.get("type").equals("drawCard")) {
				int num = Integer.parseInt(parameters.get("cardNum").toString());
				drawCard(num);
			}
			else if (parameters.get("type").equals("cardClicked")) {
				// 找到card，高亮card
				GameState.getInstance().getCardClicked().cardHighlight(health);
				int handPosition = (Integer) parameters.get("position");
				Card card = cardsOnHand.get(handPosition);
				card.cardHighlight(handPosition);
				// 把card存进GameState
				GameState.getInstance().setCardClicked(cardsOnHand.get(handPosition));
				// 找到可放置位置
				Unit unit = GameState.getInstance().getUnitList().get(0);
				parameters.put("type", "placeHighlight");
				parameters.put("tilex",unit.getPosition().getTilex());
				parameters.put("tiley", unit.getPosition().getTiley());
				GameState.getInstance().broadcastEvent(Tile.class, parameters);
			}
		}
	}
	
	/*
	 *
	 * TODO
	 *
	 * @param  TODO
	 * @return void TODO
	 */
	private void drawCard(int n){
		for(int i = 1; i <= n; i++) {
			int randomInt = new Random().nextInt(deck.size());
			Card card = this.deck.get(randomInt);
			if(cardsOnHand.size() <= 6) {
				this.deck.remove(card);
				this.cardsOnHand.add(card);
				BasicCommands.drawCard(GameState.getInstance().getOut(),
						card,cardsOnHand.size()-1,0);
			}
			else {
				this.deck.remove(card);
			}
		}
	}
	
//	private void cardHighlight(int position){
//		for (int i = 0; i < cardsOnHand.size(); i++) {
//			if (i == position){
//				BasicCommands.drawCard(GameState.getInstance().getOut(),
//						cardsOnHand.get(i),i,1);				
//			}
//			else{
//				BasicCommands.drawCard(GameState.getInstance().getOut(),
//						cardsOnHand.get(i),i,0);
//			}
//		}
//	}
	
	public void drawUnit(Card card, Tile tile) {
		Map<String,Object> parameters = new HashMap<>();
		int unitId = GameState.getInstance().getUnitList().size(); // 获取全场unit总数
		String confFile = card.getConfFiles();
		Unit unit = BasicObjectBuilders.loadUnit(confFile,unitId,Unit.class); // 生成新的unit
		GameState.getInstance().getUnitList().add(unit); // 在Game State里记录的unit列表加上这个unit
		unit.setMaster(this); // 给unit打标，说明unit属于这个玩家
		unit.setAttack(card.getBigCard().getAttack()); // 设置攻击
		unit.setHealth(card.getBigCard().getHealth()); // 设置血量
		parameters = new HashMap<>();
		parameters.put("type","summon");
		parameters.put("tilex",tile.getTilex());
		parameters.put("tiley", tile.getTiley());
		parameters.put("unit",unit);
		
		GameState.getInstance().broadcastEvent(Tile.class,parameters);
	}
		
	


}
