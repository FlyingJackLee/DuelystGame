package structures;

import javax.security.auth.Subject;

import akka.actor.ActorRef;

import java.util.Map;

public abstract class Observer {

    public abstract void trigger(Class target, Map<String,Object> parameters);
}
