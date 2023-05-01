package simplefsm;

import demo.MyCustomEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MoreSimpleFSMTest {
    private SimpleFSM simpleFSM;
    private ProcessingData data;
    private final NamedEntity proceedEvent = new MyCustomEvent("proceed");

    @BeforeEach
    public void setup() {
        simpleFSM = new SimpleFSM.Builder()
                .state("START", new Step1())
                    .conditional().goTo("STEP2")
                    .conditional().goTo("STEP3")
                .and()
                .state("STEP2", new Step2(), true)
                    .on(proceedEvent).goTo("end")
                    .on("alt_proceed").goTo("STEP3")
                .and()
                .state("STEP3", new Step3())
                    .auto().goTo("end")
                .and()
                    .finalState("end", new Step4())
                .build();

        data = new ProcessingData();
        data.set("value", 5);
    }

    @Test
    public void testInitialState() {
        simpleFSM.start("START", data);
        assertTrue(simpleFSM.isStarted());
        assertFalse(simpleFSM.isPaused());
    }

    @Test
    public void testAutoTransition() {
        simpleFSM.start("STEP3", data);
        assertTrue(simpleFSM.isFinished());
        State finalState = simpleFSM.getFinalState();
        assertEquals("end", finalState.getName());
    }

    @Test
    public void testAlternateTransition() {
        data.set("value", 4); // will go step 2 and wait for
        simpleFSM.start("START", data);
        // At step 2 waiting
        assertTrue(simpleFSM.isPaused());
        assertThrows(IllegalStateException.class, () -> simpleFSM.getFinalState());

        simpleFSM.triggerEvent("alt_proceed", data); // trigger alt_proceed event, will go to step 3
        assertTrue(simpleFSM.isFinished());
        assertThrows(IllegalStateException.class, () -> simpleFSM.getPausedOnState());
        assertEquals("end", simpleFSM.getFinalState().getName());
    }


    @Test
    public void testStep2Processing() {
        data.set("value", 4);// will go step 2 and wait for
        simpleFSM.start("START", data);
        simpleFSM.triggerEvent(proceedEvent, data);
        Integer value = (Integer) data.get("value");
        assertEquals(Integer.valueOf(8), value);
    }

    @Test
    public void testIsFinished() {
        data.set("value", 4); // will go step 2 and wait for
        simpleFSM.start("START", data);
        simpleFSM.triggerEvent(proceedEvent, data);
        assertTrue(simpleFSM.isFinished());
    }

    @Test
    public void notStarted(){
        assertFalse(simpleFSM.isStarted());
        assertFalse(simpleFSM.isFinished());
        assertFalse(simpleFSM.wasTerminated());
        assertFalse(simpleFSM.isPaused());
        assertThrows(IllegalStateException.class, () -> simpleFSM.triggerEvent("alt_proceed", data));
    }

    @Test
    public void triggerInvalidEvent(){
        data.set("value", 4); // will go step 2 and wait for
        simpleFSM.start("START", data);
        assertThrows(IllegalArgumentException.class, () -> simpleFSM.triggerEvent("does_not_exist", data));
    }

    @Test
    public void testToString(){
        simpleFSM.start("STEP3", data);
        assertTrue(simpleFSM.isFinished());
        assertNotNull(simpleFSM.toString());
        assertTrue(simpleFSM.toString().length() > 1);
    }
}

