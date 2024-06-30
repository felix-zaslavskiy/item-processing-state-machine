package com.hexadevlabs.simplefsm;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The ProcessingStep class represents an abstract processing step that needs to be executed
 * in a state of a finite state machine. It provides an abstract process method that must
 * be implemented by concrete subclasses. It also provides utility methods to set the next
 * state and log messages during processing.
 */
public abstract class ProcessingStep {

    // Collects log entries during step execution.
    List<LogEntry> logs;

    /**
     * The main processing method that must be implemented by concrete subclasses.
     * This method contains the logic to be executed for a specific state.
     *
     * @param data The ProcessingData instance containing data relevant to the current state.
     */
    protected abstract void process(ProcessingData data);

    /**
     * Sets the next state for the FSM to transition to after the completion of the current step.
     *
     * @param data      The ProcessingData instance containing data relevant to the current state.
     * @param nextState The name of the next state.
     */
    protected void nextState(ProcessingData data, String nextState) {
        data.setNextState(nextState);
    }

    String getClassName() {
        String name = this.getClass().getSimpleName();
        return name.isEmpty() ? this.getClass().getName() : name;
    }

    /**
     * During Step execution a log can be made to the FSM.
     * The logs will be available via SimpleFSM.getTrace() at the completion of execution.
     * These logs will be added to Trace regardless of Trace mode.
     *
     * @param log Individual log message.
     */
    protected void log(String log){
        if(logs == null){
            logs = new ArrayList<>();
        }
        logs.add(new LogEntry(LocalDateTime.now(), log));
    }

}