package utils;

import commands.BasicCommands;
import structures.GameState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

}
