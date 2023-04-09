class Step1 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        System.out.println("Processing Step 1");
        Integer value = (Integer) data.get("value");
        data.set("value", value + 1);
    }
}

class Step2 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        System.out.println("Processing Step 2");
        Integer value = (Integer) data.get("value");
        data.set("value", value * 2);
    }
}

class Step3 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        System.out.println("Processing Step 3");
        Integer value = (Integer) data.get("value");
        data.set("value", value - 3);
    }
}

class Step4 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        System.out.println("Processing Step 4");
        Integer value = (Integer) data.get("value");
        data.set("value", value / 2);
    }
}

public class NFSMDemo {
    public static void main(String[] args) {
        // Create states with processing steps
        State startState = new State("start", new Step1(), false);
        State step2State = new State("step2", new Step2(), true);
        State step3State = new State("step3", new Step3(), false);
        State endState = new State("end", new Step4(), false);

        // Define transitions
        startState.addTransition("auto", "step2");
        step2State.addTransition("proceed", "step3");
        step3State.addTransition("auto", "end");

        // Create NFSM and add states
        NFSM nfsm = new NFSM();
        nfsm.addState(startState);
        nfsm.addState(step2State);
        nfsm.addState(step3State);
        nfsm.addState(endState);

        // Start processing with initial data
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        nfsm.start(data);

        // Trigger external event
        nfsm.onEvent("proceed", data);

        // Output final result
        System.out.println("Final result: " + data.get("value"));
    }
}
