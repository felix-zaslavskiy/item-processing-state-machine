package nfsm;

import demo.MyCustomEvent;
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
        data.set("value", 4); // will go step 2 and wait for
        nfsm.start("start", data);
        // At step 2 waiting
        assertTrue(nfsm.isPaused());
        nfsm.triggerEvent("alt_proceed", data); // trigger alt_proceed event, will go to step 3
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
        data.set("value", 4); // will go step 2 and wait for
        nfsm.start("start", data);
        nfsm.triggerEvent(proceedEvent, data);
        // Will end
        Integer value = (Integer) data.get("value");
        assertEquals(Integer.valueOf(8), value);
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

