package nfsm;

import demo.MyCustomEvent;
import nfsm.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NFSMTest2 {
    private NFSM nfsm;
    private ProcessingData data;
    private final Event proceedEvent = new MyCustomEvent("proceed");

    @BeforeEach
    public void setup() {
        nfsm = new NFSM.Builder()
                .state("start", new Step1())
                .onConditional().goTo("step2")
                .onConditional().goTo("step3")
                .and()
                .state("step2", new Step2(), true)
                .on(proceedEvent).goTo("end")
                .on("alt_proceed").goTo("step3")
                .and()
                .state("step3", new Step3())
                .onAuto().goTo("end")
                .and()
                .finalState("end", new Step4())
                .build();

        data = new ProcessingData();
        data.set("value", 5);
    }

    @Test
    public void testInitialState() {
        nfsm.start("start", data);
        assertTrue(nfsm.isStarted());
        assertEquals("start", nfsm.getState("start").getName());
    }

    @Test
    public void testAutoTransition() {
        nfsm.start("start", data);
        nfsm.triggerEvent(proceedEvent, data);
        assertEquals("end", nfsm.getState("end").getName());
    }

    @Test
    public void testAlternateTransition() {
        nfsm.start("start", data);
        nfsm.triggerEvent("alt_proceed", data);
        assertEquals("step3", nfsm.getState("step3").getName());
    }

    @Test
    public void testStep1Processing() {
        nfsm.start("start", data);
        nfsm.triggerEvent(proceedEvent, data);
        Integer value = (Integer) data.get("value");
        assertEquals(Integer.valueOf(3), value);
    }

    @Test
    public void testStep2Processing() {
        nfsm.start("start", data);
        nfsm.triggerEvent(proceedEvent, data);
        nfsm.triggerEvent(proceedEvent, data);
        Integer value = (Integer) data.get("value");
        assertEquals(Integer.valueOf(1), value);
    }

    @Test
    public void testStep4Processing() {
        nfsm.start("start", data);
        nfsm.triggerEvent(proceedEvent, data);
        nfsm.triggerEvent(proceedEvent, data);
        nfsm.triggerEvent(proceedEvent, data);
        Integer value = (Integer) data.get("value");
        assertEquals(Integer.valueOf(0), value);
    }

    @Test
    public void testIsFinished() {
        nfsm.start("start", data);
        nfsm.triggerEvent(proceedEvent, data);
        nfsm.triggerEvent(proceedEvent, data);
        nfsm.triggerEvent(proceedEvent, data);
        assertTrue(nfsm.isFinished());
    }
}

