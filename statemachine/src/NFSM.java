import java.util.HashMap;
import java.util.Map;

public class NFSM {
    private Map<String, State> states;
    private String currentState;

    public NFSM() {
        states = new HashMap<>();
    }

    public void addState(State state) {
        states.put(state.getName(), state);
    }

    public void start(ProcessingData data) {
        currentState = "start";
        process(data);
    }

    public void onEvent(String eventName, ProcessingData data) {
        if (currentState == null) {
            throw new IllegalStateException("State machine not started.");
        }
        State state = states.get(currentState);
        String nextState = state.getNextState(eventName);
        if (nextState != null) {
            currentState = nextState;
            process(data);
        }
    }

    private void process(ProcessingData data) {
        State state = states.get(currentState);
        while (state != null && !state.shouldWaitForEvent()) {
            state.execute(data);
            String nextState = state.getNextState("auto");
            state = nextState != null ? states.get(nextState) : null;
            currentState = nextState;
        }
    }
}
