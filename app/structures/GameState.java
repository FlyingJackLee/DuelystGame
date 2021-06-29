package structures;

import akka.actor.ActorRef;
import events.EventProcessor;
import state.State;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.smartcardio.Card;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState extends Subject {

    private ActorRef out; // The ActorRef can be used to send messages to the front-end UI
    private String state; //The state variable is used to store current state
    
    private Unit unitClicked;
    private Tile tileClicked;
    private Player currentPlayer;
    
    List<Tile> tileList;
    List<Unit> unitList;

	public void setOut(ActorRef out) {
        this.out = out;
    }

    public ActorRef getOut() {
        return out;
    }

    public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Unit getUnitClicked() {
		return unitClicked;
	}
	
	public Tile getTileClicked() {
		return tileClicked;
	}

	public void setTileClicked(Tile tileClicked) {
		this.tileClicked = tileClicked;
		this.unitClicked = tileClicked.getUnitOnTile();
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentPlayer(Player currentPlayer) {
		this.currentPlayer = currentPlayer;
	}

	public List<Unit> getUnitList() {
		return unitList;}

	public List<Tile> getTileList() {
		return tileList;
	}


	//make GameState as a subject.
    private static GameState instance= new GameState();

    public static GameState getInstance(){
        return instance;
    }

    private GameState(){

    }

    public void clear(){
        instance= new GameState();
        super.clearObservers();
        this.out = null;
    }

    @Override
    public void broadcastEvent(Class target, Map<String,Object> parameters){
        for (Observer observer:observers){
            observer.trigger(target,parameters);
        }
    }
          
    public void clearClickedTile() {
		unitClicked = null;
		tileClicked = null;
	}
	

}
