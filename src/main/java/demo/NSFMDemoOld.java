package demo;


import nfsm.NFSM;
import nfsm.ProcessingData;
import nfsm.State;


public class NSFMDemoOld {
    public static void main(String[] args) {
        nonBuilder();
    }


    private static void nonBuilder() {
        // Create states with processing steps
        State startState = new State("start", new Step1(), false);
        State step2State = new State("step2", new Step2(), true);
        State step3State = new State("step3", new Step3(), false);
        State endState = new State("end", new Step4(), false);

        // Define transitions
        startState.addTransition("step2", "step2");
        startState.addTransition("step3", "step3");

        step2State.addTransition("proceed", "end");
        step3State.addTransition("auto", "end");

        // Create nfsm.NFSM and add states
        NFSM nfsm = new NFSM();
        nfsm.addState(startState);
        nfsm.addState(step2State);
        nfsm.addState(step3State);
        nfsm.addState(endState);
        nfsm.setTraceMode(true);

        // Start processing with initial data
        // Will go Start -> Step3 -> End
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        nfsm.start("start", data);

        nfsm.getTrace().print();

        System.out.println("State machine is active: " + nfsm.isRunning());

        // Output final result
        System.out.println("Final result: " + data.get("value"));

        // Second example
        nfsm = new NFSM();
        nfsm.addState(startState);
        nfsm.addState(step2State);
        nfsm.addState(step3State);
        nfsm.addState(endState);
        nfsm.setTraceMode(true);

        // Start processing with initial data
        // Will go Start -> Step2 -> Wait -> Proceed -> End
        ProcessingData data2 = new ProcessingData();
        data2.set("value", 4);
        nfsm.start("start", data2);

        // Trigger external event
        nfsm.onEvent("proceed", data2);

        nfsm.getTrace().print();

        System.out.println("State machine is active: " + nfsm.isRunning());


        // Output final result
        System.out.println("Final result: " + data2.get("value"));
    }



}
