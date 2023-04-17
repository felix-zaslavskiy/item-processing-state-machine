package nfsm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class NFSM {
    private final Map<String, State> states;
    private String currentState;
    private Set<String> finalStates;
    private boolean traceMode;
    private Trace trace;
    private boolean started;

    public NFSM() {
        states = new HashMap<>();
        traceMode = false;
        trace = new Trace();
        started = false;
        finalStates = new HashSet<>();
    }

    public void setTraceMode(boolean traceMode) {
        this.traceMode = traceMode;
    }

    public void addState(State state) {
        states.put(state.getName(), state);
    }

    public State getState(String name) {
        return states.get(name);
    }

    public void start(String startingState, ProcessingData data) {
        currentState = startingState;
        started = true;
        process(data);
    }

    public void triggerEvent(NamedEntity event, ProcessingData data) {
        String eventName = event.getName();
        triggerEvent(eventName, data);
    }

    public void triggerEvent(String eventName, ProcessingData data) {
        if (!started) {
            throw new IllegalStateException("State machine not started.");
        }
        State state = states.get(currentState);
        String nextState = state.getNextState(eventName);
        if (nextState == null) {
            throw new IllegalStateException("No transition found for event '" + eventName + "' in the current state '" + currentState + "'.");
        }

        if (traceMode) {
            trace.add("triggerEvent, continuing to state: " + nextState);
        }
        currentState = nextState;
        process(data);
    }

    /**
     * Paused means FSM is waiting on an event
     */
    public boolean isPaused() {
        if (!started) {
            return false;
        }
        State state = states.get(currentState);
        return state != null && state.shouldWaitForEventBeforeTransition();
    }

    /**
     * FSM is has been started.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * FSM has finished processing one of the finalStates.
     */
    public boolean isFinished() {
        return started && !finalStates.isEmpty() && finalStates.contains(currentState);
    }

    public void addFinalState(String finalState) {
        this.finalStates.add(finalState);
    }


    private void process(ProcessingData data) {
        State state = states.get(currentState);
        while (state != null) {

            if (traceMode) {
                trace.add("Entering state: " + state.getName());
            }

            data.setNextState(null); // Reset the nextState before executing the step
            if(traceMode) {
                state.execute(data, trace);
            } else {
                state.execute(data);
            }
            if(state.shouldWaitForEventBeforeTransition()){
                if (traceMode) {
                    trace.add("Processed state " + state.getName() + ". Pausing because " + state.getName() + " requires a wait after completion");
                }
                break;
            }

            String nextState = data.getNextState();
            if (nextState == null) {
                nextState = state.getNextState(TransitionAutoEvent.AUTO);
            }

            if (nextState == null) {
                Collection<String> possibleTransitions = state.getTransitions();
                if (possibleTransitions.size() == 1) {
                    nextState = possibleTransitions.iterator().next();
                } else if (possibleTransitions.size() > 1) {
                    throw new IllegalStateException("Next state is ambiguous. Please specify the next state in the processing step.");
                }
            }

            if (traceMode) {
                trace.add("Exiting state: " + state.getName() + ", transitioning to: " + (nextState == null ? "terminated" : nextState));
            }

            if(nextState != null){
                state = states.get(nextState);
                currentState = nextState;
            }else {
                state = null;
                // Either in finalState or no other transition available.
            }

        }
    }

    public Trace getTrace() {
        return trace;
    }

    public String toGraphviz() {
        StringBuilder dot = new StringBuilder("digraph G {\n");

        for (Map.Entry<String, State> entry : states.entrySet()) {
            String stateName = entry.getKey();
            State state = entry.getValue();
            dot.append("\t").append(stateName).append("[label=\"").append(stateName).append("\"];\n");
            for (Map.Entry<String, String> transition : state.getTransitionEntries()) {
                String eventName = transition.getKey();
                String targetState = transition.getValue();

                dot.append("\t").append(stateName).append(" -> ").append(targetState)
                        .append("[label=\"").append(eventName).append("\"];\n");

            }
        }

        dot.append("}");
        return dot.toString();
    }

    public String exportState() {
        ObjectMapper objectMapper = new ObjectMapper();
        FSMState fsmState = new FSMState();
        fsmState.setCurrentState(currentState);
        fsmState.setTraceMode(traceMode);
        fsmState.setTrace(trace);
        fsmState.setStarted(started);
        try {
            return objectMapper.writeValueAsString(fsmState);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void importState(String json)  {
        ObjectMapper objectMapper = new ObjectMapper();
        FSMState fsmState = null;
        try {
            fsmState = objectMapper.readValue(json, FSMState.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        currentState = fsmState.getCurrentState();
        traceMode = fsmState.isTraceMode();
        trace = fsmState.getTrace();
        started = fsmState.isStarted();
    }

    public State getFinalState() {
        if(!isFinished())
            throw new IllegalStateException("State machine must finish to have final state");
        return states.get(currentState);
    }


    public static class Builder {
        private NFSM nfsm;
        private String lastCreatedStateName;

        public Builder() {
            nfsm = new NFSM();
        }

        public StateBuilder state(String name, ProcessingStep processingStep) {
            return state(name, processingStep, false);
        }

        public StateBuilder state(String name, ProcessingStep processingStep, boolean waitForEventBeforeTransition) {
            validateStateName(name);
            nfsm.states.put(name, new State(name, processingStep, waitForEventBeforeTransition));
            lastCreatedStateName = name;
            return new StateBuilder(name, processingStep, waitForEventBeforeTransition, this);
        }

        public Builder finalState(String name, ProcessingStep processingStep) {
            state(name, processingStep, false);
            nfsm.addFinalState(name);
            return this;
        }

        public NFSM build() {
            if (nfsm.states.isEmpty()) {
                throw new IllegalStateException("At least one state must be defined.");
            }
            return nfsm;
        }

        private void validateStateName(String name) {
            if (nfsm.states.containsKey(name)) {
                throw new IllegalArgumentException("A state with the name '" + name + "' already exists.");
            }
        }
    }

    public static class StateBuilder {
        private String name;
        private ProcessingStep processingStep;
        private boolean waitForEventBeforeTransition;
        private Builder nfsmBuilder;

        public StateBuilder(String name, ProcessingStep processingStep, boolean waitForEventBeforeTransition, Builder nfsmBuilder) {
            this.name = name;
            this.processingStep = processingStep;
            this.waitForEventBeforeTransition = waitForEventBeforeTransition;
            this.nfsmBuilder = nfsmBuilder;
        }

        public TransitionBuilder on(String eventName) {
            return new TransitionBuilder(eventName, this);
        }

        public TransitionBuilder on(NamedEntity event){
            return new TransitionBuilder(event.getName(), this);
        }

        public TransitionBuilder onAuto() {
            return on(TransitionAutoEvent.AUTO);
        }

        public TransitionBuilder onConditional() {
            String nextState = name + "_to_";
            return new TransitionBuilder(nextState, this, true);
        }

        public Builder and() {
            return nfsmBuilder;
        }
        public Builder end() {
            return nfsmBuilder;
        }
    }

    public static class TransitionBuilder {
        private String eventName;
        private StateBuilder stateBuilder;
        private boolean isConditional;

        public TransitionBuilder(String eventName, StateBuilder stateBuilder) {
            this(eventName, stateBuilder, false);
        }

        public TransitionBuilder(String eventName, StateBuilder stateBuilder, boolean isConditional) {
            this.eventName = eventName;
            this.stateBuilder = stateBuilder;
            this.isConditional = isConditional;
        }

        public StateBuilder goTo(String nextState) {
            if (isConditional) {
                eventName += nextState;
            }
            stateBuilder.nfsmBuilder.nfsm.states.get(stateBuilder.name).addTransition(eventName, nextState);
            return stateBuilder;
        }
    }
}
