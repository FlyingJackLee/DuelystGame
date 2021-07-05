package structures.basic;

import commands.BasicCommands;
import structures.GameState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    @Override
    public void drawCard() {

    }


    public AIPlayer(int health, int mana){
        super(health,mana);
    }

    public void startup(){
        Map<String, Object> parameters;

        // find all of unit
        parameters = new HashMap<>();
        parameters.put("type","findUnitList");
        GameState.getInstance().broadcastEvent(Unit.class, parameters);
    }
}
