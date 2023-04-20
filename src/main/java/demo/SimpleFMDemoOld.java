package demo;


import simplefsm.SimpleFSM;
import simplefsm.ProcessingData;
import simplefsm.State;


public class SimpleFMDemoOld {
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
        SimpleFSM simpleFSM = new SimpleFSM();
        simpleFSM.addState(startState);
        simpleFSM.addState(step2State);
        simpleFSM.addState(step3State);
        simpleFSM.addState(endState);
        simpleFSM.setTraceMode(true);

        // Start processing with initial data
        // Will go Start -> Step3 -> End
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("start", data);

        simpleFSM.getTrace().print();

        System.out.println("State machine is active: " + simpleFSM.isStarted());

        // Output final result
        System.out.println("Final result: " + data.get("value"));

        // Second example
        simpleFSM = new SimpleFSM();
        simpleFSM.addState(startState);
        simpleFSM.addState(step2State);
        simpleFSM.addState(step3State);
        simpleFSM.addState(endState);
        simpleFSM.setTraceMode(true);

        // Start processing with initial data
        // Will go Start -> Step2 -> Wait -> Proceed -> End
        ProcessingData data2 = new ProcessingData();
        data2.set("value", 4);
        simpleFSM.start("start", data2);

        // Trigger external event
        simpleFSM.triggerEvent("proceed", data2);

        simpleFSM.getTrace().print();

        System.out.println("State machine is active: " + simpleFSM.isPaused());


        // Output final result
        System.out.println("Final result: " + data2.get("value"));
    }



}
