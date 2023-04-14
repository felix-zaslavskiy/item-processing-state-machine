package nfsm;

public class TransitionAutoEvent implements Event {

    @Override
    public String getName() {
        return TransitionAutoEvent.AUTO;
    }

    public final static String AUTO = "auto";
}