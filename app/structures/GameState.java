package structures;

import akka.actor.ActorRef;
import events.EventProcessor;
import structures.basic.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState extends Subject {

    private ActorRef out; // The ActorRef can be used to send messages to the front-end UI

    private Unit unitClicked;
    private Tile tileClicked;
    private Card cardClicked;
    private List<Player> playerList = new ArrayList<Player>();
    private Player currentPlayer;
    private int currentTurn;
    List<Unit> unitList;
    
    public void setOut(ActorRef out) {
        this.out = out;
    }

    public ActorRef getOut() {
        return out;
    }

    //make GameState as a subject.
    private static GameState instance= new GameState();

    public static GameState getInstance(){
        return instance;
    }
    
//    public void add(Player player) {
//    	playerList.add(player);
//    }

    private GameState(){

    }

    public void clear(){
        instance= new GameState();
        super.clearObservers();
        this.out = null;
    }

	public Unit getUnitClicked() {
		return unitClicked;
	}

	public void setUnitClicked(Unit unitClicked) {
		this.unitClicked = unitClicked;
	}

	public Tile getTileClicked() {
		return tileClicked;
	}

	public void setTileClicked(Tile tileClicked) {
		this.tileClicked = tileClicked;
	}

	public Card getCardClicked() {
		return cardClicked;
	}

	public void setCardClicked(Card cardClicked) {
		this.cardClicked = cardClicked;
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

//	public void setCurrentPlayer(int n) {
//		this.currentPlayer = playerList.get(n);
//	}

	public void clearClicked() {
		unitClicked = null;
		tileClicked = null;
		cardClicked = null;
	}
	

	public List<Unit> getUnitList() {
		return unitList;
	}

	@Override
    public void broadcastEvent(Class target, Map<String,Object> parameters){
        for (Observer observer:observers){
            observer.trigger(target,parameters);
        }
    }

}
