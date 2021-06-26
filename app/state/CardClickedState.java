package state;

import structures.GameState;
import structures.basic.Card;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 2 * @Author: flyingjack
 * 3 * @Date: 2021/6/25 4:45 pm
 * 4
 */
public class CardClickedState extends State{
    private final int level = 2;

    private int cardPosition;

    private ArrayList<Tile> validTile = new ArrayList<>();


    public void addValidTile(Tile tile){
        this.validTile.add(tile);
    }

    public CardClickedState(Card card){

       //Calculate the target range

       //if it is a spell
       if (card.getBigCard().getAttack()<-1){
           String rule = card.getBigCard().getRulesTextRows()[0];


           //if the target is a unit
           if (rule.contains("unit")){
               Map<String,Object> parameters = new HashMap<>();
               parameters.put("type","searchUnit");

               //find all enemy Unit
                if (rule.contains("enemy")){
                    parameters.put("range","enemy");
                    //ask the tile to give the list of enemy units
                    GameState.getInstance().broadcastEvent(Tile.class,parameters);
                }
                //find all  Unit
                else {
                    parameters.put("range","all");
                    //ask the tile to give the list of all units
                    GameState.getInstance().broadcastEvent(Tile.class,parameters);
                }
           }

           //if the target is a unit
           else if (rule.contains("avatar")) {
               Map<String,Object> parameters = new HashMap<>();
               parameters.put("type","searchUnit");

               //find all non avatar
               //TODO
               if (rule.contains("non-avatar")){
                   parameters.put("range","non-avatar");
                   //ask the tile to give the list of enemy units
                   GameState.getInstance().broadcastEvent(Tile.class,parameters);

               }
           }
           }
       //if it is a creature
       else {

       }
    }


    public void next(Map<String,Object> parameters){
        //1.BROADCASTR


        //2,SET NEXT STATE
        GameState.getInstance().setCurrentState(new ReadyState());
    }
}
