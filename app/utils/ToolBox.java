package utils;

import commands.BasicCommands;
import structures.GameState;

/**
 * 2 * @Author: flyingjack
 * 3 * @Date: 2021/6/30 2:53 pm
 * 4
 */
public class ToolBox {

    //display tips for human player
    public static void logNotification(String message){
        BasicCommands.addPlayer1Notification(GameState.getInstance().getOut(), message, 2);
    }
}
