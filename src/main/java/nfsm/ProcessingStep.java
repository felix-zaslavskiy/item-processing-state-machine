package nfsm;

public abstract class ProcessingStep {
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


}