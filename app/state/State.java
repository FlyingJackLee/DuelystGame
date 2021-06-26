package state;

import structures.basic.Tile;

import java.util.ArrayList;
import java.util.Map;

/**
 * 2 * @Author: flyingjack
 * 3 * @Date: 2021/6/25 4:43 pm
 * 4
 */
public abstract class State {
    protected int level;
    protected int remainingMana;


    protected ArrayList<Tile> validTiles = new ArrayList<>();

     public void addValidTile(Tile tile){
      this.validTiles.add(tile);
     }

     abstract public void next(Map<String,Object> parameters);

}
