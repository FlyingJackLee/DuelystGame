package structures.basic;

import commands.BasicCommands;
import structures.GameState;
import utils.ToolBox;

import java.util.*;

public class AIPlayer extends Player{

    Set<Unit> unitList = new HashSet<>();

    public void addUnitList (Unit unit) {
        unitList.add(unit);
    }

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

    public AIPlayer(int health, int mana){
        super(health,mana);
    }


    @Override
    public void drawCard() {
        int randomInt = new Random().nextInt(deck.size());
        Card card = this.deck.get(randomInt);
        this.deck.remove(card);

        int i;

        //find a blank space
        for (i = 0; i < 6; i++) {
            if (this.cardsOnHand[i] == null) {
                this.cardsOnHand[i] = card;
                try {
                    Thread.sleep(ToolBox.delay);
                }
                //wait for the frontend
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    public void cardSelected(int handPosition){


        if (handPosition <0 || handPosition > 5 ){
            return;
        }

        Card cardSelected = this.cardsOnHand[handPosition];

        //if the player has enough mana
        if(cardSelected.getManacost() <= this.mana){

            //highlight card
            //if the player have selected a card, reset the card highlight firstly
            if ( GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT) ){
                clearSelected();
            }

            //set backend
            GameState.getInstance().setCardSelected(cardSelected);

//            //render frontend
//            BasicCommands.drawCard(GameState.getInstance().getOut(),cardSelected,
//                    handPosition + 1
//                    ,1);

            //highlight valid tiles
            showValidRange(cardSelected);

        }
        else {
            ToolBox.logNotification("Mana not enough");
            return;
        }


    }

//    @Override
//    public void clearSelected(){
//        if (GameState.getInstance().getCurrentState().equals(GameState.CurrentState.CARD_SELECT)){
//            //clear backend
//            GameState.getInstance().setCardSelected(null);
//            GameState.getInstance().setTileSelected(null);
//        }
//    }

    Set<Tile> optionalTile = new HashSet<>();
    Set<Tile> whiteTiles = new HashSet<>();
    Set<Tile> redTiles = new HashSet<>();

    public void addOptionalTile(Tile tile){ optionalTile.add(tile);}
    public void addWhiteTiles(Tile tile){ whiteTiles.add(tile);}
    public void addRedTiles(Tile tile){ redTiles.add(tile);}

    public void startUpAIMode(){
        Map<String, Object> parameters;

        // unit move and attack
        // 1. find all of unit
        parameters = new HashMap<>();
        parameters.put("type","searchUnit");
        parameters.put("range","all_friends");
        GameState.getInstance().broadcastEvent(Tile.class, parameters);
        try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}

        // 2. store optional unit
        Iterator findUnit = optionalTile.iterator();
        while(findUnit.hasNext()){
            Tile tileClicked = (Tile) findUnit.next();

            // 3. first click
            if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.READY)){
                parameters = new HashMap<>();
                parameters.put("type","firstClickTile");
                parameters.put("tilex",tileClicked.getTilex());
                parameters.put("tiley",tileClicked.getTiley());
                GameState.getInstance().broadcastEvent(Tile.class,parameters);

                // get all of the tile that the AI can click
                parameters = new HashMap<>();
                parameters.put("type","AI_FindOperateTile");
                GameState.getInstance().broadcastEvent(Tile.class,parameters);

                // 4. operate Unit
                if(GameState.getInstance().getCurrentState().equals(GameState.CurrentState.UNIT_SELECT)){
                    // 4.1 if the unit can attack, attack firstly
                    Iterator searchAttack = redTiles.iterator();
                    while(searchAttack.hasNext()){
                        Tile y = (Tile) searchAttack.next();
                        parameters = new HashMap<>();
                        parameters.put("type","operateUnit");
                        parameters.put("tilex",y.getTilex());
                        parameters.put("tiley",y.getTiley());
                        parameters.put("originTileSelected", tileClicked);
                        GameState.getInstance().broadcastEvent(Tile.class,parameters);
                        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                        break;
                    }
                    // 4.2 move
                    Iterator searchMove = whiteTiles.iterator();
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

        // AI plays a card
        for(int i = 0; i < 6; i++){
            if(this.cardsOnHand[i] != null){
                // 1. chose a card
                this.cardSelected(i);
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

                // 2. find the placeable tile
                parameters = new HashMap<>();
                parameters.put("type","AI_FindOperateTile");
                GameState.getInstance().broadcastEvent(Tile.class,parameters);

                Iterator searchSummon = whiteTiles.iterator();
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

                            //summon unit
                            parameters.put("type","summon");
                            parameters.put("tilex",y.getTilex());
                            parameters.put("tiley",y.getTiley());
                            parameters.put("card",cardSelected);
                            parameters.put("unit",new_unit);
                            GameState.getInstance().broadcastEvent(Tile.class,parameters);

                            //set attack and health
                            parameters = new HashMap<>();
                            parameters.put("type","setUnit");
                            parameters.put("unitId",cardSelected.getId());

                            GameState.getInstance().broadcastEvent(Unit.class,parameters);
                            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                        }
                        //if it is a spell
                        else {

                        }
                    }
                    break;
                }
            }
        }

        parameters.put("type","textureReset");
        GameState.getInstance().broadcastEvent(Tile.class,parameters);

        optionalTile.clear();
        whiteTiles.clear();
        redTiles.clear();
        GameState.getInstance().switchPlayer();
    }
}
