package simplefsm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class SimpleFSM {
    private final Map<String, State> states;
    private String onExceptionState;
    private String currentState;
    private final Set<String> finalStates;
    private Trace trace;
    private ExecutionHooks executionHooks;
    private boolean onExecutionHookExceptionTerminate;
    private boolean started;


    public SimpleFSM() {
        states = new HashMap<>();
        trace = new Trace();
        started = false;
        finalStates = new HashSet<>();
    }

    public void setTraceMode(boolean traceMode) {
        this.trace.setTraceMode(traceMode);
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

    public void start(NamedEntity startingState, ProcessingData data){
        start(startingState.getName(), data);
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

        if (trace.isTraceMode()) {
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

    /**
     * If the state machine was terminated due to an exception without having finished to a finalState
     */
    public boolean wasTerminated() {
        return started && currentState == null;
    }

    public void addFinalState(String finalState) {
        this.finalStates.add(finalState);
    }


    private void process(ProcessingData data) {
        State state = states.get(currentState);
        while (state != null) {

            if (trace.isTraceMode()) {
                trace.add("Entering state: " + state.getName());
            }

            data.setNextState(null); // Reset the nextState before executing the step

            ExceptionInfo exceptionInfo;

            exceptionInfo = state.execute(data, trace , executionHooks);

            if(exceptionInfo.hadException()){
                // Have a transition for on Exception event
                data.setExceptionInfo(exceptionInfo);
                if(this.onExceptionState != null){
                    // If exception handler thrown exception itself.
                    if(this.onExceptionState.equals(currentState)){
                        if (trace.isTraceMode()) {
                            trace.add("Exception handler thru exception stopping.");
                            trace.add(exceptionInfo.exception.getMessage()!=null ? exceptionInfo.exception.getMessage() : "Exception message is null");
                        }
                        currentState=null;
                        break;
                    }

                    if(exceptionInfo.isOnHook() && this.onExecutionHookExceptionTerminate) {
                        if(trace.isTraceMode()) trace.add("Stopping because of exception in a execution hook and onExecutionHookExceptionTerminate = true");
                        currentState=null;
                        break;
                    }

                    if (trace.isTraceMode()) {
                        trace.add("Due to exception transitioning to state " + this.onExceptionState);
                    }

                    state = states.get(this.onExceptionState);
                    currentState = this.onExceptionState;

                    continue;


                } else{
                    // Don't have a transition for Exception event.
                    if(trace.isTraceMode()){
                        if(exceptionInfo.isOnHook()){
                            trace.add("Exception from a execution hook method");
                        }
                        trace.add("Stopping because of exception and no onExceptionState transition defined");
                    }
                    currentState=null;
                    break;
                }
            } else if(state.shouldWaitForEventBeforeTransition()){
                if (trace.isTraceMode()) {
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

            if (trace.isTraceMode()) {
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
            dot.append("\t").append(stateName)
                    .append("[label=\"")
                    .append(stateName)
                    .append("\\n")
                    .append("[").append(state.getProcessStepClassName()).append("]");

            if(state.shouldWaitForEventBeforeTransition()){
                dot.append("\\n").append("<wait>");
            }
            if(finalStates.contains(stateName)){
                dot.append("\\n").append("<final>");
            }
            dot.append("\"];\n");

            for (Map.Entry<String, String> transition : state.getTransitionEntries()) {
                String eventName = transition.getKey();
                String targetState = transition.getValue();

                dot.append("\t").append(stateName).append(" -> ").append(targetState)
                        .append("[label=\"").append(eventName).append("\"];\n");

            }
        }
        if(onExceptionState!= null){
            dot.append("\t").append("Exception [label=\"Exception\" shape=\"box\"];\n");
            dot.append("\t").append("Exception -> ").append(onExceptionState)
                    .append("[label=\"ON_EXCEPTION\"];\n");
        }

        dot.append("}");
        return dot.toString();
    }

    public String exportState() {
        ObjectMapper objectMapper = new ObjectMapper();
        FSMState fsmState = new FSMState();
        fsmState.setCurrentState(currentState);
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
        trace = fsmState.getTrace();
        started = fsmState.isStarted();
    }

    public State getPausedOnState(){
        if(isFinished())
            throw new IllegalStateException("State machine must not have finished to return Paused on state");

        return states.get(currentState);
    }

    public State getFinalState() {
        if(!isFinished())
            throw new IllegalStateException("State machine must finish to have final state");

        if(currentState==null)
            throw new IllegalStateException("State machine in terminated state can not have final state");

        return states.get(currentState);
    }




    public static class Builder {
        private final SimpleFSM simpleFSM;

        public Builder() {
            this.simpleFSM = new SimpleFSM();
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
            if (simpleFSM.states.containsKey(name)) {
                throw new IllegalArgumentException("A state with the name '" + name + "' already exists.");
            }
            this.simpleFSM.states.put(name, new State(name, processingStep, waitForEventBeforeTransition));
            return new StateBuilder(name, this);
        }

        public Builder finalState(NamedEntity name, ProcessingStep processingStep){
            return finalState(name.getName(), processingStep);
        }

        public Builder finalState(String name, ProcessingStep processingStep) {
            state(name, processingStep, false);
            this.simpleFSM.addFinalState(name);
            return this;
        }

        public Builder onExceptionGoTo(NamedEntity state) {
            return onExceptionGoTo(state.getName());
        }

        public Builder onExceptionGoTo(String state) {
            simpleFSM.onExceptionState = state;
            return this;
        }

        public Builder withExecutionHook(ExecutionHooks hook){
            simpleFSM.executionHooks = hook;
            return this;
        }

        /**
         * Override default. Terminate State machine if exception
         * in execution hook. By default, State machine will go to
         * Exception state.
         */
        public Builder onExecutionHookExceptionTerminate() {
            simpleFSM.onExecutionHookExceptionTerminate = true;
            return this;
        }

        public Builder withTrace(){
            simpleFSM.setTraceMode(true);
            return this;
        }

        public SimpleFSM build() {
            if (simpleFSM.states.isEmpty()) {
                throw new IllegalArgumentException("At least one state must be defined.");
            }
            // Make the onException set as finalState
            if(simpleFSM.onExceptionState != null){
                simpleFSM.addFinalState(simpleFSM.onExceptionState);
            }

            return simpleFSM;
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

        public TransitionBuilder auto() {
            return on(TransitionAutoEvent.NAME);
        }

        public TransitionBuilder conditional() {
            String nextState = name + "_TO_";
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
            stateBuilder.nfsmBuilder.simpleFSM.states.get(stateBuilder.name).addTransition(eventName, nextState);
            return stateBuilder;
        }
    }
}
