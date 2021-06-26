package structures;

import akka.actor.ActorRef;
import events.EventProcessor;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState extends Subject {

    private ActorRef out; // The ActorRef can be used to send messages to the front-end UI
    private State state; //The state variable is used to store current state
    
    public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void setOut(ActorRef out) {
        this.out = out;
    }

    public ActorRef getOut() {
        return out;
    }

    //make GameState as a subject.
    private static GameState instance= new GameState();

    public static GameState getInstance(){
        return instance;
    }

    private GameState(){

    }

    public void clear(){
        instance= new GameState();
        super.clearObservers();
        this.out = null;
    }

    @Override
    public void broadcastEvent(Class target, Map<String,Object> parameters){
        for (Observer observer:observers){
            observer.trigger(target,parameters);
        }
    }
    
}
