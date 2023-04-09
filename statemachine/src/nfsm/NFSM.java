package nfsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NFSM {
    private final Map<String, State> states;
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
            throw new IllegalStateException("nfsm.State machine not started.");
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
            data.setNextState(null); // Reset the nextState before executing the step
            state.execute(data);

            String nextState = data.getNextState();
            if (nextState == null) {
                nextState = state.getNextState("auto");
            }

            if (nextState == null) {
                Collection<String> possibleTransitions = state.transitions.values();
                if (possibleTransitions.size() == 1) {
                    nextState = possibleTransitions.iterator().next();
                } else if (possibleTransitions.size() > 1) {
                    throw new IllegalStateException("Next state is ambiguous. Please specify the next state in the processing step.");
                }
            }

            state = nextState != null ? states.get(nextState) : null;
            currentState = nextState;
        }
    }
}
