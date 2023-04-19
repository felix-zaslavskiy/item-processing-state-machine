package nfsm;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NFSMTest {
    @Test
    public void testStart() {
        NFSM nfsm = new NFSM();
        nfsm.addState(new State("START", new Step1(), false));
        nfsm.addState(new State("STEP2", new Step2(), true));
        nfsm.addState(new State("STEP3", new Step3(), false));
        nfsm.addState(new State("END", new Step4(), false));

        // Define transitions
        nfsm.getState("START").addTransition("STEP2", "STEP2");
        nfsm.getState("START").addTransition("STEP3", "STEP3");

        nfsm.getState("STEP2").addTransition("PROCEED", "END");
        nfsm.getState("STEP3").addTransition("AUTO", "END");

        ProcessingData data = new ProcessingData();
        data.set("value", 5);

        assertFalse(nfsm.isStarted());
        nfsm.start("START", data);
        assertTrue(nfsm.isStarted());
    }

    @Test
    public void testTriggerEvent() {
        NFSM nfsm = new NFSM();
        nfsm.setTraceMode(true);
        nfsm.addState(new State("START", new Step1(), false));
        nfsm.addState(new State("STEP2", new Step2(), true));
        nfsm.addState(new State("STEP3", new Step3(), false));
        nfsm.addState(new State("END", new Step4(), false));
        nfsm.addFinalState("END");

        // Define transitions
        nfsm.getState("START").addTransition("STEP2", "STEP2");
        nfsm.getState("START").addTransition("STEP3", "STEP3");

        nfsm.getState("STEP2").addTransition("PROCEED", "END");
        nfsm.getState("STEP3").addTransition("AUTO", "END");

        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        nfsm.start("START", data);

        nfsm.triggerEvent("PROCEED", data);

        nfsm.getTrace().print();
        assertTrue(nfsm.isFinished());
        assertEquals(8, data.get("value"));
    }

    @Test
    public void testTrace() {
        NFSM nfsm = new NFSM();
        nfsm.addState(new State("START", new Step1(), false));
        nfsm.addState(new State("STEP2", new Step2(), true));
        nfsm.addState(new State("STEP3", new Step3(), false));
        nfsm.addState(new State("END", new Step4(), false));

        // Define transitions
        nfsm.getState("START").addTransition("STEP2", "STEP2");
        nfsm.getState("START").addTransition("STEP3", "STEP3");

        nfsm.getState("STEP2").addTransition("PROCEED", "END");
        nfsm.getState("STEP3").addTransition("AUTO", "END");
        nfsm.addFinalState("END");

        nfsm.setTraceMode(true);
        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        nfsm.start("START", data);

        nfsm.triggerEvent("PROCEED", data);
        Trace trace = nfsm.getTrace();
        Assertions.assertNotNull(trace);
    }

    @Test
    public void testExceptionHandler(){
        NFSM nfsm = new NFSM.Builder()
                .state("START", new Step1())
                .auto().goTo("STEP2")
                .and()
                .state("STEP2", new ProcessingStep() {
                    @Override
                    protected void process(ProcessingData data) {
                        throw new RuntimeException("Test");
                    }
                })
                .and()
                .state("exception", new ExceptionHandler())
                .end()
                .onExceptionGoTo("exception")
                .withTrace()
                .build();

        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        nfsm.start("START", data);
        Trace trace = nfsm.getTrace();
        trace.print();

        assertNotNull(data.get("error"));
        assertTrue(data.hadException());
        assertEquals("Test", data.getException().getMessage());
        assertTrue(nfsm.isFinished());

    }

    @Test
    public void testExceptionWithoutHandler(){
        NFSM nfsm = new NFSM.Builder()
                .state("START", new Step1())
                .auto().goTo("STEP2")
                .and()
                .state("STEP2", new ProcessingStep() {
                    @Override
                    protected void process(ProcessingData data) {
                        throw new RuntimeException("Test");
                    }
                })
                .end()
                .build();

        nfsm.setTraceMode(true);
        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        nfsm.start("START", data);
        //Trace trace = nfsm.getTrace();
        //trace.print();

        assertTrue(data.hadException());
        assertTrue(nfsm.isFinished());

    }
}