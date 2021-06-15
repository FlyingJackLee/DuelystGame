package structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Subject {
    protected List<Observer> observers = new ArrayList<Observer>();


    /**
     *
     * Add a observe to subject
     *
     * @param observer:  a observer
     */
    public void add(Observer observer) {
        observers.add(observer);
    }

    /**
     *
     * Remove a observe from subject
     *
     * @param observer:  a observer
     */
    public void remove(Observer observer) {
        observers.remove(observer);
    }

    /**
     *
     * Clear all observers register on this.
     *
     */
    public void clearObservers(){
        observers = new ArrayList<Observer>();
    }

    /**
     *
     * Broadcast the event to relating observers
     *
     * @param target: the class of target observer
     * @param parameters:  extra parameter the observer needs
     */
    public abstract void broadcastEvent(Class target,Map<String,Object> parameters);
}
