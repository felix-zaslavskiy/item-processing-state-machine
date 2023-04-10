package nfsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NFSM {
    private Map<String, State> states;
    private String currentState;
    private boolean traceMode;
    private Trace trace;

    public NFSM() {
        states = new HashMap<>();
        traceMode = false;
        trace = new Trace();
    }

    public void setTraceMode(boolean traceMode) {
        this.traceMode = traceMode;
    }

    public void addState(State state) {
        states.put(state.getName(), state);
    }

    public void start(String startingState, ProcessingData data) {
        currentState = startingState;
        process(data);
    }

    public void onEvent(String eventName, ProcessingData data) {
        if (currentState == null) {
            throw new IllegalStateException("State machine not started.");
        }
        State state = states.get(currentState);
        String nextState = state.getNextState(eventName);
        if (nextState != null) {
            if(traceMode){
                trace.add("onEvent, continuing to state: " + nextState);
            }
            currentState = nextState;
            process(data);
        }
    }

    public boolean isActive(){
        return currentState != null;
    }

    private void process(ProcessingData data) {
        State state = states.get(currentState);
        while (state != null && !state.shouldWaitForEvent()) {

            if (traceMode) {
                trace.add("Entering state: " + state.getName());
            }

            data.setNextState(null); // Reset the nextState before executing the step
            if(traceMode) {
                state.execute(data, trace);
            } else {
                state.execute(data);
            }

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

            if (traceMode) {
                trace.add("Exiting state: " + state.getName() + ", transitioning to: " + (nextState == null ? "terminated" : nextState));
            }

            state = nextState != null ? states.get(nextState) : null;
            if(traceMode && state != null && state.shouldWaitForEvent()){
                trace.add("Pausing because " + state.getName() + " requires a wait after completion");
            }
            currentState = nextState;
        }
    }

    public Trace getTrace() {
        return trace;
    }
}
