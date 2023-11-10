package com.hexadevlabs.simplefsm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringEscapeUtils;

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

    // Tracking the split states that have been completed so far.
    private final List<String> completedSplitStates;

    private SplitHandler splitHandler;
    String name;


    public SimpleFSM() {
        states = new HashMap<>();
        trace = new Trace();
        started = false;
        finalStates = new HashSet<>();
        completedSplitStates = new ArrayList<>();
    }

    public void setTraceMode(boolean traceMode) {
        this.trace.setTraceMode(traceMode);
    }

    /**
     * Adds a State instance to the SimpleFSM.
     *
     * @param state The State instance to be added.
     */
    public void addState(State state) {
        states.put(state.getName(), state);
    }

    public State getState(String name) {
        return Objects.requireNonNull(states.get(name), "State with name '" + name + "' not found.");
    }

    public String getName() {
        return name;
    }

    /**
     * Name the instance of SimpleFSM. Optional.
     */
    public void setName(String name) {
        this.name = name;
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
            throw new IllegalArgumentException("No transition found for event '" + eventName + "' in the current state '" + currentState + "'.");
        }

        if (trace.isTraceMode()) {
            trace.add("triggerEvent, continuing to state: " + nextState);
        }
        currentState = nextState;
        process(data);
    }

    /**
     * Should be called by SplitHandler to initiate work on multiple states
     * of the State machine in parallel.
     *
     * @param splitStateTransition Name of transition for split state
     * @param data
     */
    public void continueOnSplitState(String splitStateTransition, ProcessingData data) {

        if (!started) {
            throw new IllegalStateException("State machine not started.");
        }

        // Start work on Split state...
        State state = states.get(currentState);

        if(!state.getSplitTransitions().contains(splitStateTransition)){
            throw new IllegalStateException("Not a valid splitState transition for this state machine: " + splitStateTransition);
        }

        // Process work on the state...
        String nextStateName = state.getNextState(splitStateTransition);
        State nextState = states.get(nextStateName);

        ExceptionInfo exceptionInfo;
        exceptionInfo = nextState.execute(data, trace , executionHooks);

        // TODO: handle exception

        // At the end of the work we need to check for state machine status and update it about the work done.
        SplitHandler.GetStateResult result = splitHandler.getStateAndUpdateWorkState(this, currentState, nextStateName);

        // Merge data and persist the data.
        data = splitHandler.mergeDataAndSave(this, data, result.otherSavedProcessingData);

        // If all the work is done continue with normal processing.
        if(result.completedOtherWork){

            String nextStateTransition;
            Collection<String> transitions =  nextState.getTransitions();
            if(transitions.size() == 1){
                nextStateTransition = transitions.iterator().next();
            } else {
                throw new IllegalStateException("Expected on transition to joined state");
            }
            currentState = nextStateTransition;

            process(data);
        }

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

    public void addSplitHandler(SplitHandler handleSplit) {
        this.splitHandler = handleSplit;
    }

    protected void recordCompletionSplitState(String completedSplitState) {
        this.completedSplitStates.add(completedSplitState);
    }

    protected Collection<String> getCompletionSplitStates(){
        return this.completedSplitStates;
    }

    /**
     * State machine processing loop.
     * The currentState variable tracks the current state the machine is in.
     * <p>
     * Called from either start() in which case currentState is set to starting state
     * or from triggerEvent() in which case currentState is preset to the next state that
     * that event is supposed to transition to.
     *
     * @param data
     */
    private void process(ProcessingData data) {
        // We get the current state object since we know what state
        // to execute as the loop starts.
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
                        if(trace.isTraceMode()){
                            trace.add("Stopping because of exception in a execution hook and onExecutionHookExceptionTerminate = true");
                            trace.add(exceptionInfo.exception.getMessage()!=null ? exceptionInfo.exception.getMessage() : "Exception message is null");
                        }
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
                        trace.add(exceptionInfo.exception.getMessage()!=null ? exceptionInfo.exception.getMessage() : "Exception message is null");
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

            // From here we figure out what the next State to transition to needs to be.
            // It can be directed by the Processor via data, be an auto transition,
            // or there is only one possibly transition available.

            // If there is a split transition from this state we can handle them.
            // For now if Split is happened we have to save state and pause State machine.
            Collection<String> splitTransitions = state.getSplitTransitions();
            if(!splitTransitions.isEmpty()){
                // Pause state machine.
                // currentState will what it was.
                splitHandler.handleSplit(this, data, splitTransitions);
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
                // State is updated to the nextState since we know the next state for next iteration of loop will be.
                state = states.get(nextState);
                // currentState is updates to the nextState so the state machine has moved to be in the next
                // state now. currentState is mostly used to introspect the state machine
                // while it si not running.
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

        if(name != null){
            dot.append("labelloc=\"t\";\n" + "label=<<B>")
                    .append(StringEscapeUtils.escapeHtml4(name)).append("</B>>;\n");
        }
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

    /**
     * Exports the current state of the FSM as a JSON string.
     *
     * @return A JSON string representing the current state of the FSM.
     */
    public String exportState() {
        ObjectMapper objectMapper = new ObjectMapper();
        FSMState fsmState = new FSMState();
        fsmState.setCurrentState(currentState);
        fsmState.completedSplitStates(completedSplitStates);
        fsmState.setTrace(trace);
        fsmState.setStarted(started);
        fsmState.setName(name);
        try {
            return objectMapper.writeValueAsString(fsmState);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Imports the state of the FSM from a JSON string.
     *
     * @param json The JSON string representing the state to be imported.
     */
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
        name = fsmState.getName();
    }

    /**
     * Retrieves the state on which the FSM is paused.
     *
     * @return The State instance on which the FSM is paused.
     * @throws IllegalStateException If the FSM is finished.
     */
    public State getPausedOnState(){
        if(isFinished())
            throw new IllegalStateException("State machine must not have finished to return Paused on state");

        return states.get(currentState);
    }

    /**
     * Retrieves the final state of the FSM if the FSM is finished.
     *
     * @return The State instance representing the final state.
     * @throws IllegalStateException If the FSM is not finished or is terminated.
     */
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

        /**
         * Sets the name of the state to transition to in case of an exception.
         *
         * @param state The name of the state to transition to.
         */
        public Builder onExceptionGoTo(NamedEntity state) {
            return onExceptionGoTo(state.getName());
        }

        /**
         * Sets the name of the state to transition to in case of an exception.
         *
         * @param state The name of the state to transition to.
         */
        public Builder onExceptionGoTo(String state) {
            simpleFSM.onExceptionState = state;
            return this;
        }

        /**
         * Sets the ExecutionHooks instance to be used for before and after hooks.
         *
         * @param hook The ExecutionHooks instance to be used.
         */
        public Builder withExecutionHook(ExecutionHooks hook){
            simpleFSM.executionHooks = hook;
            return this;
        }

        public Builder withName(String name){
            simpleFSM.setName(name);
            return this;
        }

        /**
         * FSM should terminate when an exception occurs during
         * the execution of a hook. By default, State machine will go to
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

        public Builder splitHander(SplitHandler handleSplit) {
            simpleFSM.addSplitHandler(handleSplit);
            return this;
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

        public TransitionBuilder split(){
            return on(TransitionSplitEvent.NAME);
        }

        /**
         * For events that are split one needs to tell
         * how to join to a common state.
         * @param joinToState The state that this split state joins to.
         * @return
         */
        public StateBuilder join(String joinToState) {
            if(!this.nfsmBuilder.simpleFSM.states.containsKey(joinToState)){
                throw new IllegalArgumentException("A state with the name '" + joinToState + "' must already be declared before using join() call.");
            }
            this.nfsmBuilder.simpleFSM.getState(joinToState).makeJoiningState();

            String eventName = name + "_TO_" + joinToState;
            // Add a Transition to the joinToState
            this.nfsmBuilder.simpleFSM.getState(name).addTransition(eventName, joinToState, false);

            return this;
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

       // private final boolean isJoin

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
            boolean partOfSplit = false;
            if(eventName.equals(TransitionSplitEvent.NAME)){
                eventName += '_' + nextState;
                partOfSplit = true;
            }
            stateBuilder.nfsmBuilder.simpleFSM.states.get(stateBuilder.name).addTransition(eventName, nextState, partOfSplit);
            return stateBuilder;
        }
    }

    @Override
    public String toString() {
        return "SimpleFSM{" +
                (name!=null ? "name='" + name + "'\n" : "") +
                "onExceptionState='" + onExceptionState + "'\n" +
                ", currentState='" + currentState + "'\n" +
                ", executionHooks=" + executionHooks + '\n' +
                ", onExecutionHookExceptionTerminate=" + onExecutionHookExceptionTerminate + '\n' +
                ", started=" + started + '\n' +
                ", trace=" + trace +
                '}';
    }
}
