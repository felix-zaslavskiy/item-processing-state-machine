package com.hexadevlabs.simplefsm;


import com.hexadevlabs.simplefsm.supporting.ExceptionHandler;
import com.hexadevlabs.simplefsm.testSteps.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleFSMTest {
    @Test
    public void testStart() {
        SimpleFSM simpleFSM = new SimpleFSM();
        simpleFSM.addState(new State("START", new Step1(), false));
        simpleFSM.addState(new State("STEP2", new Step2(), true));
        simpleFSM.addState(new State("STEP3", new Step3(), false));
        simpleFSM.addState(new State("END", new Step4(), false));

        // Define transitions
        simpleFSM.getState("START").addTransition("STEP2", "STEP2", false);
        simpleFSM.getState("START").addTransition("STEP3", "STEP3", false);

        simpleFSM.getState("STEP2").addTransition("PROCEED", "END", false);
        simpleFSM.getState("STEP3").addTransition("AUTO", "END", false);

        ProcessingData data = new ProcessingData();
        data.set("value", 5);

        assertFalse(simpleFSM.isStarted());
        simpleFSM.start("START", data);
        assertTrue(simpleFSM.isStarted());
    }

    @Test
    public void testTriggerEvent() {
        SimpleFSM simpleFSM = new SimpleFSM();
        simpleFSM.setTraceMode(true);
        simpleFSM.addState(new State("START", new Step1(), false));
        simpleFSM.addState(new State("STEP2", new Step2(), true));
        simpleFSM.addState(new State("STEP3", new Step3(), false));
        simpleFSM.addState(new State("END", new Step4(), false));
        simpleFSM.addFinalState("END");

        // Define transitions
        simpleFSM.getState("START").addTransition("STEP2", "STEP2", false);
        simpleFSM.getState("START").addTransition("STEP3", "STEP3", false);

        simpleFSM.getState("STEP2").addTransition("PROCEED", "END", false);
        simpleFSM.getState("STEP3").addTransition("AUTO", "END", false);

        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        simpleFSM.start("START", data);

        assertTrue(simpleFSM.isPaused());

        simpleFSM.triggerEvent("PROCEED", data);

        simpleFSM.getTrace().print();
        assertTrue(simpleFSM.isConcluded());
        assertEquals(8, data.get("value"));
    }

    @Test
    public void testTrace() {
        SimpleFSM simpleFSM = new SimpleFSM();
        simpleFSM.addState(new State("START", new Step1(), false));
        simpleFSM.addState(new State("STEP2", new Step2(), true));
        simpleFSM.addState(new State("STEP3", new Step3(), false));
        simpleFSM.addState(new State("END", new Step4(), false));

        // Define transitions
        simpleFSM.getState("START").addTransition("STEP2", "STEP2", false);
        simpleFSM.getState("START").addTransition("STEP3", "STEP3", false);

        simpleFSM.getState("STEP2").addTransition("PROCEED", "END", false);
        simpleFSM.getState("STEP3").addTransition("AUTO", "END", false);
        simpleFSM.addFinalState("END");

        simpleFSM.setTraceMode(true);
        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        simpleFSM.start("START", data);

        simpleFSM.triggerEvent("PROCEED", data);
        Trace trace = simpleFSM.getTrace();
        Assertions.assertNotNull(trace);
    }

    @Test
    public void testExceptionHandler(){
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("START", new Step1())
                    .auto().goTo("STEP2")
                .state("STEP2", new ProcessingStep() {
                    @Override
                    protected void process(ProcessingData data) {
                        throw new RuntimeException("Test");
                    }
                })
                .state("exception", new ExceptionHandler())
                .onExceptionGoTo("exception")
                .withTrace()
                .build();

        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        simpleFSM.start("START", data);
        Trace trace = simpleFSM.getTrace();
        trace.print();

        assertNotNull(data.get("error"));
        assertTrue(data.hadException());
        assertEquals("Test", data.getException().getMessage());
        assertTrue(simpleFSM.isConcluded());

    }

    @Test
    public void testExceptionWithoutHandler(){
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("START", new Step1())
                .auto().goTo("STEP2")
                .state("STEP2", new ProcessingStep() {
                    @Override
                    protected void process(ProcessingData data) {
                        throw new RuntimeException("Test");
                    }
                })
                .build();

        simpleFSM.setTraceMode(true);
        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        simpleFSM.start("START", data);

        assertTrue(data.hadException());
        assertTrue(simpleFSM.isConcluded());

    }

    @Test
    public void noFinalStateAndNoExceptions(){
        // This is good example where there is no finalState but the FSM
        // can continue running indefinitely as long as triggering event
        // is happening periodically.
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("START", new NoopStep())
                    .auto().goTo("STEP2")
                .state("STEP2", new NoopStep(), true)
                    .on("transition").goTo("START")
                .build();

        simpleFSM.setTraceMode(true);
        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        simpleFSM.start("START", data);

        assertFalse(simpleFSM.isConcluded());
        assertTrue(simpleFSM.isPaused());

        // Trigger event to transition back to START and stop on STEP2 again
        simpleFSM.triggerEvent("transition", data);

        assertFalse(simpleFSM.isConcluded());
        assertTrue(simpleFSM.isPaused());

    }
}