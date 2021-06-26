package state;

import structures.GameState;

/**
 * 2 * @Author: flyingjack
 * 3 * @Date: 2021/6/25 5:19 pm
 * 4
 */
public class ReadyState extends State{
    private final int level = 1;

    public ReadyState(){
    }

    public void next(State nextState) throws Exception{
        if (nextState.level == 2){
            GameState.getInstance().setCurrentState(nextState);
        }

        else {
            throw new Exception("Bad Flow");
        }
    }



}
