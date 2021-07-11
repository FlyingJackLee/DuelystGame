import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.BasicCommands;
import commands.DummyTell;
import events.Initalize;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import structures.GameState;
import structures.Observer;
import structures.basic.Unit;




public class InitializeTest {
    private GameState gameState = GameState.getInstance();


    @Before
    public void setUp(){
        BasicCommands.altTell = new SimuTell();

    }

    @Test
    public void initializeTest(){
        gameState.clear();


        //call initialze event
        Initalize initalizeEvent = new Initalize();
        ObjectNode eventMessage = Json.newObject();

        initalizeEvent.processEvent(null,gameState,eventMessage);

        //A. Player status Test
        //1.if two players has been initialized correctly
        Assert.assertEquals(2,gameState.getPlayerContainers().length);
        //first should be human
        Assert.assertTrue(gameState.getPlayerContainers()[0].isHumanOrAI());
        //second should be ai
        Assert.assertFalse(gameState.getPlayerContainers()[1].isHumanOrAI());
        //first player should be human
        Assert.assertEquals(gameState.getPlayerContainers()[0],gameState.getCurrentPlayer());
        //heath,attack,mana test
        Assert.assertEquals(20,gameState.getPlayerContainers()[0].getHealth());
        Assert.assertEquals(20,gameState.getPlayerContainers()[1].getHealth());
        Assert.assertEquals(1,gameState.getPlayerContainers()[0].getMana());
        Assert.assertEquals(0,gameState.getPlayerContainers()[1].getMana());

        //B. Deck Test
        //10 - 3(draw 3 cards)  = 7
        Assert.assertEquals(7,gameState.getPlayerContainers()[0].getDeck().size());
        Assert.assertEquals(7,gameState.getPlayerContainers()[0].getDeck().size());

        //C. avatar Test
        Unit humanAvatar = findUnitByID(99);
        Assert.assertNotNull(humanAvatar);
        Assert.assertEquals(humanAvatar.getHealth(),20);
        Assert.assertEquals(humanAvatar.getAttack(),2);

        Unit aiAvatar = findUnitByID(100);
        Assert.assertNotNull(aiAvatar);
        Assert.assertEquals(aiAvatar.getHealth(),20);
        Assert.assertEquals(aiAvatar.getAttack(),2);


        //D.draw card test
        gameState.getCurrentPlayer().drawCard();
        Assert.assertEquals(6,gameState.getCurrentPlayer().getDeck().size());
        Assert.assertNotNull(gameState.getCurrentPlayer().getCardsOnHand()[3]);
        Assert.assertNull(gameState.getCurrentPlayer().getCardsOnHand()[4]);


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



}
