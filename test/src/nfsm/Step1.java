package nfsm;

class Step1 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        System.out.println("Processing Step 1");
        Integer value = (Integer) data.get("value");

        // Select the next state based on the value
        if (value % 2 == 0) {
            nextState(data, "step2");
        } else {
            nextState(data, "step3");
        }
    }
}