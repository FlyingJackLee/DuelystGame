import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.BasicCommands;
import events.CardClicked;
import events.Initalize;
import events.TileClicked;
import org.checkerframework.checker.guieffect.qual.UI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import structures.GameState;
import structures.Observer;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;

import java.util.List;

/**
 * 2 * @Author: flyingjack
 * 3 * @Date: 2021/7/11 6:50 pm
 * 4
 */
public class FlowTest {

    private GameState gameState = GameState.getInstance();


    @Before
    public void setUp(){
        BasicCommands.altTell = new SimuTell();

    }


    @Test
    public void flowTest(){

        //call initialze event(test mode)
        Initalize initalizeEvent = new Initalize();
        ObjectNode eventMessage = Json.newObject();
        eventMessage.put("mode","test");
        initalizeEvent.processEvent(null,gameState,eventMessage);



        //A.Summon Test 1 -- Pureblade Enforcer 1/4
        drawCardCheat(2); // draw
        gameState.getCurrentPlayer().setMana(2);


        //1.selected a card
        eventMessage = Json.newObject();
        eventMessage.put("position",1);

        Assert.assertNull(gameState.getCardSelected());
        CardClicked cardClickedEvent = new CardClicked();
        cardClickedEvent.processEvent(null,gameState,eventMessage);
        Assert.assertNotNull(gameState.getCardSelected());



        //2.summon
        Card card = gameState.getCardSelected();
        eventMessage = Json.newObject();
        eventMessage.put("tilex",1);
        eventMessage.put("tiley",1);
        TileClicked tileClickedEvent = new TileClicked();
        tileClickedEvent.processEvent(null,gameState,eventMessage);


        //check if the unit has been summoned
        Unit unit_1 = findUnitByID(card.getId());
        Assert.assertNotNull(unit_1);
        Assert.assertEquals(1,unit_1.getAttack());
        Assert.assertEquals(4,unit_1.getHealth());



        //B.Summon Test 2 -- Azure Herald 1/4
        drawCardCheat(3); // draw Pureblade Enforcer 1/4
        gameState.getCurrentPlayer().setMana(3);

        //1.selected a card
        eventMessage = Json.newObject();
        eventMessage.put("position",1);

        Assert.assertNull(gameState.getCardSelected());
        cardClickedEvent = new CardClicked();
        cardClickedEvent.processEvent(null,gameState,eventMessage);
        Assert.assertNotNull(gameState.getCardSelected());




        //2.summon - Azure Herald 1/4 -- AI
        card = gameState.getCardSelected();
        eventMessage = Json.newObject();
        eventMessage.put("tilex",2);
        eventMessage.put("tiley",2);
        tileClickedEvent = new TileClicked();
        tileClickedEvent.processEvent(null,gameState,eventMessage);


        //check if the unit has been summoned
        Unit unit_2 = findUnitByID(card.getId());
        Assert.assertNotNull(unit_2);
        Assert.assertEquals(1,unit_2.getAttack());
        Assert.assertEquals(4,unit_2.getHealth());
        unit_2.setOwner(gameState.getPlayerContainers()[1]);


        //3.attack
        //let it ready right now
        unit_1.setCurrentState(Unit.UnitState.READY);
        unit_2.setCurrentState(Unit.UnitState.READY);

        //click the unit(tile)
        gameState.setCurrentState(GameState.CurrentState.READY);
        tileClickedEvent = new TileClicked();
        eventMessage = Json.newObject();
        eventMessage.put("tilex",1);
        eventMessage.put("tiley",1);
        tileClickedEvent.processEvent(null,gameState,eventMessage);


        Assert.assertEquals(GameState.getInstance().getCurrentState(),GameState.CurrentState.UNIT_SELECT);
        Assert.assertEquals(GameState.getInstance().getTileSelected().getTilex(),1);
        Assert.assertEquals(GameState.getInstance().getTileSelected().getTiley(),1);


        tileClickedEvent = new TileClicked();
        eventMessage = Json.newObject();
        eventMessage.put("tilex",2);
        eventMessage.put("tiley",2);
        tileClickedEvent.processEvent(null,gameState,eventMessage);

        Assert.assertEquals(unit_1.getHealth(),3);
        Assert.assertEquals(unit_2.getHealth(),3);


    }


    /**
     *
     * draw a card with id(only for test)
     *
     * @param id:  card id
     */
    private void drawCardCheat(int id){
        List<Card> deck = GameState.getInstance().getCurrentPlayer().getDeck();
        main:
        for (int i = 0; i < deck.size(); i++) {
            if (deck.get(i).getId() == id){
                //find a blank space
                for (int j = 0; j < 6; j++) {
                    if(gameState.getCurrentPlayer().getCardsOnHand()[j] == null){
                        gameState.getCurrentPlayer().getCardsOnHand()[j] = deck.get(i);
                        break main;
                    }
                }
            }
        }
    }



    /**
     *
     * Find the Unit in the observers
     *
     * @param id
     */
    private static Unit findUnitByID(int id){
        for (int i = 0; i < GameState.getInstance().getObservers().size(); i++) {
            Observer observer = GameState.getInstance().getObservers().get(i);
            if (observer instanceof structures.basic.Unit){
                Unit unit = (Unit) observer;
                if (unit.getId() == id){
                    return unit;
                }
            }
        }
        return null;
    }

    /**
     *
     * Find the tile in the observers
     *
     * @param
     */
    private static Tile findTile(int tilex,int tiley){
        for (int i = 0; i < GameState.getInstance().getObservers().size(); i++) {
            Observer observer = GameState.getInstance().getObservers().get(i);
            if (observer instanceof structures.basic.Tile){
                Tile tile = (Tile) observer;
                if (tile.getTilex() == tilex && tile.getTiley() == tiley){
                    return tile;
                }
            }
        }
        return null;
    }
}
