package simplefsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class State {
    private final String name;
    private final Map<String, String> transitions;
    private final ProcessingStep processingStep;
    private final boolean waitForEventBeforeTransition;

    public State(String name, ProcessingStep processingStep, boolean waitForEventBeforeTransition) {
        this.name = name;
        this.processingStep = processingStep;
        this.transitions = new HashMap<>();
        this.waitForEventBeforeTransition = waitForEventBeforeTransition;
    }

    public void addTransition(String eventName, String nextState) {
        if (transitions.containsKey(eventName)) {
            throw new IllegalArgumentException("A transition with the event name '" + eventName + "' already exists in the state '" + name + "'.");
        }
        transitions.put(eventName, nextState);
    }

    public Collection<String> getTransitions() {
        return transitions.values();
    }

    public Set<Map.Entry<String, String>> getTransitionEntries() {
        return transitions.entrySet();
    }

    public String getNextState(NamedEntity event) {
        return transitions.get(event.getName());
    }

    public String getNextState(String event) {
        return transitions.get(event);
    }

    public ExceptionInfo execute(ProcessingData data, Trace trace) {
        trace.add("Before processing: " + processingStep.getClassName());
        try {
            processingStep.process(data);
            trace.addAll(processingStep.logs);
        }catch (Exception e){
            trace.add("Exception occurred in "+ processingStep.getClassName() + ".process()");
            return new ExceptionInfo(e);
        }
        trace.add("After processing: " + processingStep.getClassName());
        return new ExceptionInfo();
    }

    public ExceptionInfo execute(ProcessingData data) {
        try {
            processingStep.process(data);
            return new ExceptionInfo();
        }catch(Exception e){
            return new ExceptionInfo(e);
        }
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
}
