package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import events.EventProcessor;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;

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


    public enum CurrentState{
        READY,CARD_SELECT,UNIT_SELECT
    }

    private CurrentState currentState = CurrentState.READY;


    public CurrentState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(CurrentState currentState) {
        this.currentState = currentState;
    }


    private Card cardSelected = null;

    public void setCardSelected(Card cardSelected) {
        this.cardSelected = cardSelected;
    }


    private Tile tileSelected = null;

    public void setTileSelected(Tile tileSelected) { this.tileSelected = tileSelected; }
    public Tile getTileSelected() { return tileSelected; }



    private Player currentPlayer;

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }


    private ActorRef out; // The ActorRef can be used to send messages to the front-end UI

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

    private GameState(){

    }

    // After move or attack, clear the selected tile, and switch the current state
    public void setStateReady(){
        this.setCurrentState(currentState.READY);
        this.tileSelected.clearAllHighlight();
        this.setTileSelected(null);
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




}
