package nfsm;

public abstract class ProcessingStep {
    protected abstract void process(ProcessingData data);

    // Add a method nextState() in the nfsm.ProcessingStep class
    protected void nextState(ProcessingData data, String nextState) {
        data.setNextState(nextState);
    }

    protected String getClassName() {
        String name = this.getClass().getSimpleName();
        return name.isEmpty() ? this.getClass().getName() : name;
    }


}