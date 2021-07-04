package structures.basic;

import commands.BasicCommands;
import structures.GameState;

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
    public void drawCard() {

    }

    public AIPlayer(int health, int mana){
        super(health,mana);
    }
}
