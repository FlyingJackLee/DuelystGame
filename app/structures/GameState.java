package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import events.EventProcessor;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.ToolBox;

import java.util.ArrayList;
import java.util.HashMap;
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
    private int turnCount = 0;

    private Player[] playerContainers = new Player[2];
    public Player[] getPlayerContainers() {
        return playerContainers;
    }

    public void addPlayers(Player humanPlayer, Player AIPlayer) {
        // make sure only allocate once
        if (playerContainers[0] == null && playerContainers[1] == null) {
            playerContainers[0] = humanPlayer;
            playerContainers[1] = AIPlayer;

            this.currentPlayer = humanPlayer;
            turnCount ++;
            this.currentPlayer.setMana((int) Math.ceil(turnCount/2.0));
        }
    }

    private Player currentPlayer;
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    // switch player
    public void switchPlayer() {
        // let all units be ready for this player
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("type", "unitBeReady");
        GameState.getInstance().broadcastEvent(Unit.class, parameters);

        if (this.currentPlayer == playerContainers[0]) {
            // clear mana of previous player
            this.currentPlayer.setMana(0);
            this.currentPlayer = playerContainers[1];
        } else {
            // clear mana of previous player
            this.currentPlayer.setMana(0);
            this.currentPlayer = playerContainers[0];
        }

        // update turn and mana
        turnCount ++;
        this.currentPlayer.setMana((int) Math.ceil(turnCount/2.0));

        // draw a card
        this.currentPlayer.drawCard();
    }

    // state description
    public enum CurrentState {
        READY, CARD_SELECT, UNIT_SELECT
    }

    private CurrentState currentState = CurrentState.READY;
    public CurrentState getCurrentState() {
        return currentState;
    }
    public void setCurrentState(CurrentState currentState) {
        this.currentState = currentState;
    }

    // the card selected
    private Card cardSelected = null;
    public Card getCardSelected() {
        return cardSelected;
    }
    public void setCardSelected(Card cardSelected) {
        this.cardSelected = cardSelected;
        if (cardSelected == null) {
            this.currentState = CurrentState.READY;
        } else {
            this.currentState = CurrentState.CARD_SELECT;
        }
    }

    // the tile selected
    private Tile tileSelected = null;
    public Tile getTileSelected() {
        return tileSelected;
    }
    public void setTileSelected(Tile tileSelected) {
        this.tileSelected = tileSelected;
        if (this.tileSelected == null) {
            this.currentState = CurrentState.READY;
        } else {
            this.currentState = CurrentState.UNIT_SELECT;
        }
    }

    private ActorRef out; // The ActorRef can be used to send messages to the front-end UI
    public ActorRef getOut() {
        return out;
    }
    public void setOut(ActorRef out) {
        this.out = out;
    }

    // make GameState as a subject
    private static GameState instance= new GameState();
    public static GameState getInstance(){
        return instance;
    }

    private GameState() {}

    public void clear() {
        this.playerContainers = new Player[2];
        this.currentPlayer = null;
        this.currentState = CurrentState.READY;
        this.turnCount = 0;
        this.cardSelected = null;
        super.clearObservers();
    }

    @Override
    public void broadcastEvent(Class target, Map<String,Object> parameters) {
        System.out.println();
        for (Observer observer : observers) {
            observer.trigger(target, parameters);
        }
    }
}
