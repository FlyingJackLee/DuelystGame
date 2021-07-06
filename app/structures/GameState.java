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
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState extends Subject {


    //format: "unitID": callback

    //Integer: the id of card to be used.
    private Map<String, Function<Integer,Boolean>> cardSelectedCallbacks = new HashMap<>();
    //Integer: the id of card to be used.
    private Map<String,Function<Integer,Boolean>> beforeSummonCallbacks = new HashMap<>();
    //Integer: the id of unit to be attacked.
    private Map<String,Function<Integer,Boolean>> avatarAttackCallbacks  = new HashMap<>();
    //Integer: the id of unit has dead.
    private Map<String,Function<Integer,Boolean>> unitDeathCallbacks  = new HashMap<>();

    public Map<String, Function<Integer, Boolean>> getCardSelectedCallbacks() {
        return cardSelectedCallbacks;
    }

    public Map<String, Function<Integer, Boolean>> getAvatarAttackCallbacks() {
        return avatarAttackCallbacks;
    }

    public Map<String, Function<Integer, Boolean>> getBeforeSummonCallbacks() {
        return beforeSummonCallbacks;
    }
    public Map<String, Function<Integer, Boolean>> getUnitDeathCallbacks() {
        return unitDeathCallbacks;
    }


    private int turnCount = 0;

    private Player[] playerContainers = new Player[2];

    public Player[] getPlayerContainers() {
        return playerContainers;
    }

    public void addPlayers(Player humanPlayer, Player AIPlayer){
        //make sure only allocate once
        if (playerContainers[0] == null && playerContainers[1] ==null){
            playerContainers[0] = humanPlayer;
            playerContainers[1] = AIPlayer;

            this.currentPlayer = humanPlayer;
            turnCount ++;
            this.currentPlayer.setMana((int) Math.ceil(turnCount/2.0));
        }
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }





    //switch player
    public void switchPlayer() {

        //let all unit be ready for this player
        Map<String,Object> parameters =  new HashMap<>();
        parameters.put("type","unitBeReady");
        GameState.getInstance().broadcastEvent(Unit.class,parameters);


        if (this.currentPlayer == playerContainers[0]){
            //clear mana of previous player
            this.currentPlayer.setMana(0);
            this.currentPlayer = playerContainers[1];


        }
        else {
            //clear mana of previous player
            this.currentPlayer.setMana(0);
            this.currentPlayer = playerContainers[0];
        }

        //update turn and mana
        turnCount ++;
        this.currentPlayer.setMana((int) Math.ceil(turnCount/2.0));

        //draw a card
        this.currentPlayer.drawCard();

    }



    //state description
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

    //store the card selected

    private Card cardSelected = null;

    public void setCardSelected(Card cardSelected) {

        this.cardSelected = cardSelected;
        if (cardSelected == null){
            this.currentState = CurrentState.READY;
        }
        else
        {
            this.currentState = CurrentState.CARD_SELECT;

        }

    }

    public Card getCardSelected() {
        return cardSelected;
    }

    private Player currentPlayer;







    // selected tile
    private Tile tileSelected = null;

    public void setTileSelected(Tile tileSelected) { this.tileSelected = tileSelected; }
    public Tile getTileSelected() { return tileSelected; }



    // current turn
    private int currentTurn;
    public int getCurrentTurn() { return currentTurn;}


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

    public void clear(){
        this.playerContainers = new Player[2];
        this.currentPlayer = null;
        this.currentState = CurrentState.READY;
        this.turnCount = 0;
        this.cardSelected = null;
        super.clearObservers();
    }

    @Override
    public void broadcastEvent(Class target, Map<String,Object> parameters){
        for (Observer observer:observers){
            observer.trigger(target,parameters);
        }
    }


    public void registerCallbacks(){
        //"Azure Herald"
        this.beforeSummonCallbacks.put(String.valueOf("3"), new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) {

                            Map<String, Object> parameters = new HashMap<>();
							parameters.put("type", "modifyUnit");

							int unitId = -1;

							//human player play now
							if (GameState.getInstance().getCurrentPlayer().isHumanOrAI()) {
								//human avatar id:99
								unitId = 99;
							} else {
								//ai avatar id:99
								unitId = 100;
							}

							parameters.put("unitId", unitId);
							parameters.put("attack", 0);
							parameters.put("health", 3);
							parameters.put("limit","max");

							GameState.getInstance().broadcastEvent(Unit.class, parameters);

							return true;
            }
        });


    }



}
