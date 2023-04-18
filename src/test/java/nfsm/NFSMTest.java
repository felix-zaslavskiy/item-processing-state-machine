package nfsm;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NFSMTest {
    @Test
    public void testStart() {
        NFSM nfsm = new NFSM();
        nfsm.addState(new State("start", new Step1(), false));
        nfsm.addState(new State("step2", new Step2(), true));
        nfsm.addState(new State("step3", new Step3(), false));
        nfsm.addState(new State("end", new Step4(), false));

        // Define transitions
        nfsm.getState("start").addTransition("step2", "step2");
        nfsm.getState("start").addTransition("step3", "step3");

        nfsm.getState("step2").addTransition("proceed", "end");
        nfsm.getState("step3").addTransition("auto", "end");

        ProcessingData data = new ProcessingData();
        data.set("value", 5);

        assertFalse(nfsm.isStarted());
        nfsm.start("start", data);
        assertTrue(nfsm.isStarted());
    }

    @Test
    public void testTriggerEvent() {
        NFSM nfsm = new NFSM();
        nfsm.setTraceMode(true);
        nfsm.addState(new State("start", new Step1(), false));
        nfsm.addState(new State("step2", new Step2(), true));
        nfsm.addState(new State("step3", new Step3(), false));
        nfsm.addState(new State("end", new Step4(), false));
        nfsm.addFinalState("end");

        // Define transitions
        nfsm.getState("start").addTransition("step2", "step2");
        nfsm.getState("start").addTransition("step3", "step3");

        nfsm.getState("step2").addTransition("proceed", "end");
        nfsm.getState("step3").addTransition("auto", "end");

        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        nfsm.start("start", data);

        nfsm.triggerEvent("proceed", data);

        nfsm.getTrace().print();
        assertTrue(nfsm.isFinished());
        assertEquals(8, data.get("value"));
    }

    @Test
    public void testTrace() {
        NFSM nfsm = new NFSM();
        nfsm.addState(new State("start", new Step1(), false));
        nfsm.addState(new State("step2", new Step2(), true));
        nfsm.addState(new State("step3", new Step3(), false));
        nfsm.addState(new State("end", new Step4(), false));

        // Define transitions
        nfsm.getState("start").addTransition("step2", "step2");
        nfsm.getState("start").addTransition("step3", "step3");

        nfsm.getState("step2").addTransition("proceed", "end");
        nfsm.getState("step3").addTransition("auto", "end");

        nfsm.setTraceMode(true);
        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        nfsm.start("start", data);

        nfsm.triggerEvent("proceed", data);
        Trace trace = nfsm.getTrace();
        Assertions.assertNotNull(trace);
    }

    @Test
    public void testExceptionHandler(){
        NFSM nfsm = new NFSM.Builder()
                .state("start", new Step1())
                .onAuto().goTo("step2")
                .and()
                .state("step2", new ProcessingStep() {
                    @Override
                    protected void process(ProcessingData data) {
                        throw new RuntimeException("Test");
                    }
                })
                .and()
                .state("exception", new ExceptionHandler())
                .end()
                .onExceptionGoTo("exception")
                .build();

        nfsm.setTraceMode(true);
        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        nfsm.start("start", data);
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
                .state("start", new Step1())
                .onAuto().goTo("step2")
                .and()
                .state("step2", new ProcessingStep() {
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
        nfsm.start("start", data);
        Trace trace = nfsm.getTrace();
        trace.print();

        assertTrue(data.hadException());
        assertTrue(nfsm.isFinished());

    }
}