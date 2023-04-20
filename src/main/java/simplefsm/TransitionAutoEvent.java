package simplefsm;

public class TransitionAutoEvent implements NamedEntity {

    @Override
    public String getName() {
        return TransitionAutoEvent.NAME;
    }

    public final static String NAME = "AUTO";
}