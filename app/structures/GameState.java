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

    // format: "unitID": callback

    // Integer: the id of card to be used
    private Map<String, Function<Integer, Boolean>> cardSelectedCallbacks = new HashMap<>();
    // Integer: the id of card to be used
    private Map<String, Function<Integer, Boolean>> beforeSummonCallbacks = new HashMap<>();
    // Integer: the id of unit to be attacked
    private Map<String, Function<Integer, Boolean>> avatarAttackCallbacks = new HashMap<>();
    // Integer: the id of unit has dead
    private Map<String, Function<Integer, Boolean>> unitDeathCallbacks = new HashMap<>();

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

    // selected card
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

    // selected tile
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

    public void registerCallbacks() {
        // Card: Azure Herald, id: 3
        // Unit Ability: Heal
        this.beforeSummonCallbacks.put(String.valueOf("3"), new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) {

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("type", "modifyUnit");

                int unitId = -1;

                // human player play now
                if (GameState.getInstance().getCurrentPlayer().isHumanOrAI()) {
                    // human avatar, id: 99
                    unitId = 99;
                } else {
                    // ai avatar, id: 100
                    unitId = 100;
                }

                parameters.put("unitId", unitId);
                parameters.put("attack", 0);
                parameters.put("health", 3);
                parameters.put("limit", "max");

                GameState.getInstance().broadcastEvent(Unit.class, parameters);

                return true;
            }
        });

        // Card: Ironcliff Guardian, id: 7
        // Unit Ability: Airdrop
        // TODO: Unit Ability: Provoke
        this.cardSelectedCallbacks.put(String.valueOf("7"), new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) {

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("type", "validSummonRangeHighlight");
                parameters.put("airdrop", "activate");

                GameState.getInstance().broadcastEvent(Tile.class, parameters);

                return true;
            }
        });

        // Card: Planar Scout, id: 10
        // Unit Ability: Airdrop
        this.cardSelectedCallbacks.put(String.valueOf("10"), new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) {

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("type", "validSummonRangeHighlight");
                parameters.put("airdrop", "activate");

                GameState.getInstance().broadcastEvent(Tile.class, parameters);

                return true;
            }
        });

        // TODO: Card: Pureblade Enforcer, id: 2

        // Card: Silverguard Knight, id: 4
        // If avatar is dealt damage, gain +2/+0
        // TODO: Unit Ability: Provoke
        this.avatarAttackCallbacks.put(String.valueOf("99"), new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) {

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("type", "modifyUnit");
                parameters.put("unitId", 4);
                parameters.put("attack", 2);
                parameters.put("health", 0);

                GameState.getInstance().broadcastEvent(Unit.class, parameters);

                return true;
            }
        });

        // Card: Blaze Hound, id: 14
        // On Summon: Both players draw a card
        this.beforeSummonCallbacks.put(String.valueOf("14"), new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) {

                GameState.getInstance().getPlayerContainers()[0].drawCard();
                GameState.getInstance().getPlayerContainers()[1].drawCard();

                return true;
            }
        });

        // Card: WindShrike, id: 15
        // On Death: Draw a card
        this.unitDeathCallbacks.put(String.valueOf("15"), new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) {

                GameState.getInstance().getCurrentPlayer().drawCard();

                return true;
            }
        });
    }
}
