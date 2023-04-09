public abstract class ProcessingStep {
    protected abstract void process(ProcessingData data);

    // Add a method nextState() in the ProcessingStep class
    protected void nextState(ProcessingData data, String nextState) {
        data.setNextState(nextState);
    }
}