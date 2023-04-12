package nfsm;


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

        assertFalse(nfsm.isRunning());
        nfsm.start("start", data);
        assertFalse(nfsm.isRunning());
    }

    @Test
    public void testOnEvent() {
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
        data.set("value", 4);
        nfsm.start("start", data);

        nfsm.onEvent("proceed", data);

        assertEquals(false, nfsm.isRunning());
        assertEquals(4, data.get("value"));
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

        nfsm.onEvent("proceed", data);
        Trace trace = nfsm.getTrace();
        assertNotNull(trace);
    }
}