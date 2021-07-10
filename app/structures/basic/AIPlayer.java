package structures.basic;

import commands.BasicCommands;
import structures.GameState;
import utils.ToolBox;
import structures.basic.Card;

import java.util.*;

public class AIPlayer extends Player{

    @Override
    public void setMana(int mana) {
        this.mana = mana;
        BasicCommands.setPlayer2Mana(GameState.getInstance().getOut(),this);
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
        BasicCommands.setPlayer2Health(GameState.getInstance().getOut(),this);
    }

    @Override
    public void cardSelected(int handPosition){

        Card cardSelected = this.cardsOnHand[handPosition];


        //highlight card
        //if the player have selected a card, reset the card highlight firstly
        if ( GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT) ){
            clearSelected();
        }

        //set backend
        GameState.getInstance().setCardSelected(cardSelected);


        //highlight valid tiles
        showValidRange(cardSelected);

    }

    public AIPlayer(int health, int mana){
        super(health,mana);
    }


    // record the units that the AI player can option
    Set<Tile> optionalTiles = new HashSet<>();

    public void addToOptionalTile(Tile tile){ optionalTiles.add(tile);}

    // Record the Tile that the AI players can move or summon
    Set<Tile> whiteTileGroup = new HashSet<>();

    public void addToWhiteGroup(Tile tile){ whiteTileGroup.add(tile);}

    // Record the Tile that the AI players can attack
    Set<Tile> redTileGroup = new HashSet<>();

    public void addToRedGroup(Tile tile){ redTileGroup.add(tile);}

    public void startUpAIMode(){
        Map<String, Object> parameters;

        // unit move and attack
        // 1. find all of unit
        parameters = new HashMap<>();
        parameters.put("type","searchUnit");
        parameters.put("range","all_friends");
        GameState.getInstance().broadcastEvent(Tile.class, parameters);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

        // 2. store optional unit
        Iterator findUnit = optionalTiles.iterator();
        while(findUnit.hasNext()){
            Tile tileClicked = (Tile) findUnit.next();

            // 3. first click
            if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.READY)){
                parameters = new HashMap<>();
                parameters.put("type","firstClickTile");
                parameters.put("tilex",tileClicked.getTilex());
                parameters.put("tiley",tileClicked.getTiley());
                GameState.getInstance().broadcastEvent(Tile.class,parameters);
                try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

                // get all of the tile that the AI can click
                parameters = new HashMap<>();
                parameters.put("type","AI_FindOperateTile");
                GameState.getInstance().broadcastEvent(Tile.class,parameters);
                try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

                // 4. operate Unit
                if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.UNIT_SELECT)){
                    if(!redTileGroup.isEmpty()) {
                        // 4.1 if the unit can attack, attack firstly
                        Iterator searchAttack = this.redTileGroup.iterator();
                        while (searchAttack.hasNext()) {
                            Tile y = (Tile) searchAttack.next();
                            parameters = new HashMap<>();
                            parameters.put("type", "operateUnit");
                            parameters.put("tilex", y.getTilex());
                            parameters.put("tiley", y.getTiley());
                            parameters.put("originTileSelected", tileClicked);
                            GameState.getInstance().broadcastEvent(Tile.class, parameters);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    else{
                        // 4.2 only move
                        Iterator searchMove = this.whiteTileGroup.iterator();
                        while(searchMove.hasNext()){
                            Tile y = (Tile) searchMove.next();
                            parameters = new HashMap<>();
                            parameters.put("type","operateUnit");
                            parameters.put("tilex",y.getTilex());
                            parameters.put("tiley",y.getTiley());
                            parameters.put("originTileSelected", tileClicked);
                            GameState.getInstance().broadcastEvent(Tile.class,parameters);
                            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                            break;
                        }
                    }
                }
            }
        }
        clearTileRecord();
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

        // AI plays a card
        for(int i = 0; i < 6; i++){
            if(this.cardsOnHand[i] != null){
                // 1. chose a card
                if(this.cardsOnHand[i].getManacost() <= this.mana){
                    this.cardSelected(i);
                }
                else {continue;}

                // 2. find the placeable tile
                parameters = new HashMap<>();
                parameters.put("type","AI_FindOperateTile");
                GameState.getInstance().broadcastEvent(Tile.class,parameters);
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

                Iterator searchSummon = whiteTileGroup.iterator();
                while(searchSummon.hasNext()){
                    Tile y = (Tile) searchSummon.next();
                    if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)){
                        parameters = new HashMap<>();

                        //get card selected
                        Card cardSelected = cardsOnHand[i];

                        //if it is a creature
                        if (cardSelected.isCreatureOrSpell() == 1){

                            //create unit
                            Unit new_unit = cardSelected.cardToUnit();
                            ToolBox.logNotification("AI summons " + cardSelected.cardname);
                            //summon unit
                            parameters.put("type","summon");
                            parameters.put("tilex",y.getTilex());
                            parameters.put("tiley",y.getTiley());
                            parameters.put("card",cardSelected);
                            parameters.put("unit",new_unit);
                            GameState.getInstance().broadcastEvent(Tile.class,parameters);
                            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

                            //set attack and health
                            parameters = new HashMap<>();
                            parameters.put("type","setUnit");
                            parameters.put("unitId",cardSelected.getId());
                            GameState.getInstance().broadcastEvent(Unit.class,parameters);
                            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
                        }
                        //if it is a spell
                        else {
                            parameters.put("type", "spell");
                            parameters.put("tilex",y.getTilex());
                            parameters.put("tiley",y.getTiley());
                            GameState.getInstance().broadcastEvent(Tile.class,parameters);

                        }
                        this.clearTileRecord();
                        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                    }
                    break;
                }
            }
        }

        parameters.put("type","textureReset");
        GameState.getInstance().broadcastEvent(Tile.class,parameters);

        this.clearTileRecord();

        GameState.getInstance().switchPlayer();
    }

    public void clearTileRecord(){
        this.optionalTiles.clear();
        this.whiteTileGroup.clear();
        this.redTileGroup.clear();
    }
}
