package com.hexadevlabs.simplefsm;

import java.util.ArrayList;
import java.util.List;

public abstract class ProcessingStep {

    // Collects log entries during step execution.
    List<String> logs;

    protected abstract void process(ProcessingData data);

    /**
     * Set the next state to have FSM to transition to after
     * completion of this step.
     */
    protected void nextState(ProcessingData data, String nextState) {
        data.setNextState(nextState);
    }

    /**
     * Set the next state to have FSM to transition to after
     * completion of this step.
     */
    protected void nextState(ProcessingData data, NamedEntity nextState){
        nextState(data, nextState.getName());
    }

    protected String getClassName() {
        String name = this.getClass().getSimpleName();
        return name.isEmpty() ? this.getClass().getName() : name;
    }

    protected void log(String log){
        if(logs == null){
            logs = new ArrayList<>();
        }
        logs.add(log);
    }

}