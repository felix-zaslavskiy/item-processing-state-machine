package nfsm;

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

    public String getNextState(Event event) {
        return transitions.get(event.getName());
    }


    public String getNextState(String event) {
        return transitions.get(event);
    }

    public void execute(ProcessingData data, Trace trace) {
        trace.add("Before processing: " + processingStep.getClassName());
        processingStep.process(data);
        trace.add("After processing: " + processingStep.getClassName());
    }

    public void execute(ProcessingData data) {
        processingStep.process(data);
    }

    public boolean shouldWaitForEventBeforeTransition() {
        return waitForEventBeforeTransition;
    }

    public String getName() {
        return name;
    }


}
