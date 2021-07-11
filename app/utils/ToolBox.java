package utils;

import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * 2 * @Author: flyingjack
 * 3 * @Date: 2021/6/30 2:53 pm
 * 4
 */
public class ToolBox {
    public static final int humanAvatarId = 99;
    public static final int AIAvatarID = 100;
    public static final int delay = 500;


    //display tips for human player
    public static void logNotification(String message){
        BasicCommands.addPlayer1Notification(GameState.getInstance().getOut(), message, 2);
    }


    public static<T> int findObjectInArray(T[] _6Elements, T element  ){
        for (int i = 0; i < _6Elements.length; i++) {
            if (_6Elements[i] == element){
                return i;
            }
        }
        return -1;
    }

    public static String currentPlayerName(){
        if (GameState.getInstance().getCurrentPlayer().isHumanOrAI()){
            return "Human Avatar";
        }
        else {
            return "AI Avatar";
        }
    }
}
