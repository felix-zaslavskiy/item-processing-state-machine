package com.hexadevlabs.simplefsm;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * The State class represents a state in a finite state machine (FSM). Each state has a name,
 * a processing step, a set of transitions, and a flag to determine if the state should wait
 * for an event before transitioning. The class provides methods for adding transitions,
 * retrieving the next state based on an event, and executing the processing step.
 */
public class State {
    private final String name;

    private final Map<String, String> transitions;
    private final ArrayList<String> splitTransitions;
    private boolean joiningState;
    private ProcessingStep processingStep;
    private final boolean waitForEventBeforeTransition;

    /**
     * Constructs a new State instance with the given name, processing step, and
     * waitForEventBeforeTransition flag.
     *
     * @param name                      The name of the state.
     * @param processingStep            The processing step to be executed in this state.
     * @param waitForEventBeforeTransition Indicates whether the state should wait for an event
     *                                  before transitioning to the next state.
     */
    public State(String name, ProcessingStep processingStep, boolean waitForEventBeforeTransition) {
        this.name = name;
        this.processingStep = processingStep;
        this.transitions = new HashMap<>();
        this.splitTransitions = new ArrayList<>();
        this.joiningState = false; // Initially False until some Join calls will update it.
        this.waitForEventBeforeTransition = waitForEventBeforeTransition;
    }

    /**
     * Adds a transition to the state with the given event name and next state.
     *
     * @param eventName   The name of the event that triggers the transition.
     * @param nextState   The name of the next state to transition to.
     * @param partOfSplit If true this transition is part of split with other transitions.
     * @throws IllegalArgumentException If a transition with the same event name already exists.
     */
    public void addTransition(String eventName, String nextState, boolean partOfSplit) {
        if (transitions.containsKey(eventName)) {
            throw new IllegalArgumentException("A transition with the event name '" + eventName + "' already exists in the state '" + name + "'.");
        }
        transitions.put(eventName, nextState);

        if(partOfSplit)
            splitTransitions.add(eventName);
    }

    public void makeJoiningState(){
        this.joiningState = true;
    }

    Collection<String> getTransitions() {
        return transitions.values();
    }

    Set<Map.Entry<String, String>> getTransitionEntries() {
        return transitions.entrySet();
    }

    String getNextState(String event) {
        return transitions.get(event);
    }

    /**
     * Executes the processing step with the provided ProcessingData instance and
     * updates the Trace with the process execution log.
     *
     * @param data The ProcessingData instance containing data relevant to the current state.
     * @param trace The Trace instance for recording the execution log.
     * @param executionHooks The ExecutionHooks instance for before and after hooks.
     * @return An ExceptionInfo instance with exception details if an exception occurred,
     *         otherwise an empty ExceptionInfo instance.
     */
    ExceptionInfo execute(ProcessingData data, Trace trace, ExecutionHooks executionHooks) {
        // Call the before hook
        if( executionHooks != null) {
            try {
                if(trace.isTraceMode()) trace.add("Before execution hook: " + processingStep.getClassName());
                executionHooks.before(this, data);
            } catch (Exception e) {
                return new ExceptionInfo(e, true);
            }
        }
        if(trace.isTraceMode()) trace.add("Before processing: " + processingStep.getClassName());
        try {
            processingStep.process(data);
            trace.addAll(processingStep.logs);
        }catch (Exception e){
            if(trace.isTraceMode())trace.add("Exception occurred in "+ processingStep.getClassName() + ".process()");
            return new ExceptionInfo(e, false);
        }
        if(trace.isTraceMode())trace.add("After processing: " + processingStep.getClassName());

        // Call the after hook
        if( executionHooks != null) {
            try {
                if(trace.isTraceMode()) trace.add("After execution hook: " + processingStep.getClassName());
                executionHooks.after(this, data);
            } catch (Exception e) {
                return new ExceptionInfo(e, true);
            }
        }
        return new ExceptionInfo();
    }

    public boolean shouldWaitForEventBeforeTransition() {
        return waitForEventBeforeTransition;
    }

    public String getName() {
        return name;
    }


    public String getProcessStepClassName() {
        return processingStep.getClassName();
    }

    /**
     * Set the processingStep for this State.
     * @param processingStep What will be executed.
     */
    public void setProcessingStep(ProcessingStep processingStep){
        this.processingStep = processingStep;
    }
}
