import java.util.HashMap;
import java.util.Map;

public class State {
    private final String name;
    final Map<String, String> transitions;
    private final ProcessingStep processingStep;
    private final boolean waitForEvent;

    public State(String name, ProcessingStep processingStep, boolean waitForEvent) {
        this.name = name;
        this.processingStep = processingStep;
        this.transitions = new HashMap<>();
        this.waitForEvent = waitForEvent;
    }



    public void addTransition(String eventName, String nextState) {
        transitions.put(eventName, nextState);
    }

    public String getNextState(String eventName) {
        return transitions.get(eventName);
    }

    public void execute(ProcessingData data) {
        processingStep.process(data);
    }

    public boolean shouldWaitForEvent() {
        return waitForEvent;
    }

    public String getName() {
        return name;
    }
}
