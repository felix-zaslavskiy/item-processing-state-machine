package nfsm;

import java.util.HashMap;
import java.util.Map;

public class State {
    private final String name;
    final Map<String, String> transitions;
    private final ProcessingStep processingStep;
    private final boolean waitForEventBeforeTransition;

    public State(String name, ProcessingStep processingStep, boolean waitForEventBeforeTransition) {
        this.name = name;
        this.processingStep = processingStep;
        this.transitions = new HashMap<>();
        this.waitForEventBeforeTransition = waitForEventBeforeTransition;
    }

    public void addTransition(String eventName, String nextState) {
        transitions.put(eventName, nextState);
    }

    public String getNextState(String eventName) {
        return transitions.get(eventName);
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
