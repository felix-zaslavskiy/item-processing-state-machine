package nfsm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class NFSM {
    private final Map<String, State> states;
    private String onExceptionState;
    private String currentState;
    private final Set<String> finalStates;
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
        return Objects.requireNonNull(states.get(name), "State with name '" + name + "' not found.");
    }


    public void start(String startingState, ProcessingData data) {
        currentState = startingState;
        started = true;
        process(data);
    }

    public void triggerEvent(NamedEntity event, ProcessingData data) {
        triggerEvent(event.getName(), data);
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
     * FSM has finished processing one of the finalStates,
     * or it has terminated due to exception without a finalState.
     */
    public boolean isFinished() {
        return started && ( (!finalStates.isEmpty() && finalStates.contains(currentState))
                || currentState == null );
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

            ExceptionInfo exceptionInfo;
            if (traceMode) {
                exceptionInfo = state.execute(data, trace);
            } else {
                exceptionInfo = state.execute(data);
            }

            if(exceptionInfo.hadException()){
                // Have a transition for on Exception event
                data.setExceptionInfo(exceptionInfo);
                if(this.onExceptionState != null){
                    if(traceMode){
                        trace.add("Due to exception transitioning to state " + this.onExceptionState);
                    }

                    state = states.get(this.onExceptionState);
                    currentState = this.onExceptionState;

                    continue;

                } else{
                    // Don't have a transition for Exception event.
                    if(traceMode){
                        trace.add("Stopping because of exception and no onExceptionState transition defined");
                    }
                    currentState=null;
                    break;
                }
            } else if(state.shouldWaitForEventBeforeTransition()){
                if (traceMode) {
                    trace.add("Processed state " + state.getName() + ". Pausing because " + state.getName() + " requires a wait after completion");
                }
                break;
            }

            String nextState = data.getNextState();
            if (nextState == null) {
                nextState = state.getNextState(TransitionAutoEvent.NAME);
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
                // Either in finalState or no other transition available.
                // currentState remains on the last state set.
                state = null;
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
        FSMState fsmState;
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
        private final NFSM nfsm;

        public Builder() {
            this.nfsm = new NFSM();
        }

        public StateBuilder state(NamedEntity name, ProcessingStep processingStep) {
            return state(name.getName(), processingStep);
        }

        public StateBuilder state(String name, ProcessingStep processingStep) {
            return state(name, processingStep, false);
        }

        public StateBuilder state(NamedEntity name, ProcessingStep processingStep, boolean waitForEventBeforeTransition) {
            return state(name.getName(), processingStep, waitForEventBeforeTransition);
        }

        public StateBuilder state(String name, ProcessingStep processingStep, boolean waitForEventBeforeTransition) {
            validateStateName(name);
            this.nfsm.states.put(name, new State(name, processingStep, waitForEventBeforeTransition));
            return new StateBuilder(name, this);
        }

        public Builder finalState(NamedEntity name, ProcessingStep processingStep){
            return finalState(name.getName(), processingStep);
        }

        public Builder finalState(String name, ProcessingStep processingStep) {
            state(name, processingStep, false);
            this.nfsm.addFinalState(name);
            return this;
        }

        public Builder onExceptionGoTo(NamedEntity state) {
            return onExceptionGoTo(state.getName());
        }

        public Builder onExceptionGoTo(String state) {
            nfsm.onExceptionState = state;
            return this;
        }

        public NFSM build() {
            if (nfsm.states.isEmpty()) {
                throw new IllegalStateException("At least one state must be defined.");
            }
            // Make the onException set as finalState
            if(nfsm.onExceptionState != null){
                nfsm.addFinalState(nfsm.onExceptionState);
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
        private final String name;
        private final Builder nfsmBuilder;

        public StateBuilder(String name, Builder nfsmBuilder) {
            this.name = name;
            this.nfsmBuilder = nfsmBuilder;
        }

        public TransitionBuilder on(String eventName) {
            return new TransitionBuilder(eventName, this);
        }

        public TransitionBuilder on(NamedEntity event){
            return new TransitionBuilder(event.getName(), this);
        }

        public TransitionBuilder onAuto() {
            return on(TransitionAutoEvent.NAME);
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
        private final StateBuilder stateBuilder;
        private final boolean isConditional;

        public TransitionBuilder(String eventName, StateBuilder stateBuilder) {
            this(eventName, stateBuilder, false);
        }

        public TransitionBuilder(String eventName, StateBuilder stateBuilder, boolean isConditional) {
            this.eventName = eventName;
            this.stateBuilder = stateBuilder;
            this.isConditional = isConditional;
        }

        public StateBuilder goTo(NamedEntity nextState){
            return goTo(nextState.getName());
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
