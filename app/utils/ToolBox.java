package utils;

import commands.BasicCommands;
import structures.GameState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 2 * @Author: flyingjack
 * 3 * @Date: 2021/6/30 2:53 pm
 * 4
 */
public class ToolBox {
    public static final int humanAvatarId = 99;
    public static final int AIAvatarID = 100;

    //display tips for human player
    public static void logNotification(String message){
        BasicCommands.addPlayer1Notification(GameState.getInstance().getOut(), message, 2);
    }



}
