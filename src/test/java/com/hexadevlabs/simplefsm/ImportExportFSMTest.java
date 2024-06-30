package com.hexadevlabs.simplefsm;

import com.hexadevlabs.simplefsm.testSteps.Step1;
import com.hexadevlabs.simplefsm.testSteps.Step2;
import com.hexadevlabs.simplefsm.testSteps.Step3;
import com.hexadevlabs.simplefsm.testSteps.Step4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionState extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        throw new RuntimeException();
    }
}
public class ImportExportFSMTest {

    private SimpleFSM simpleFSM;
    private final NamedEntity proceedEvent = new MyCustomEvent("proceed");
    @BeforeEach
    public void setUp() {
        simpleFSM = buildNew();
    }

    private SimpleFSM buildNew(){
        return new SimpleFSM.Builder()
                .state("START", new Step1())
                    .conditional().goTo("STEP2")
                    .conditional().goTo("STEP3")
                .and()
                .state("STEP2", new Step2(), true)
                    .on(proceedEvent).goTo("END")
                    .on("alt_proceed").goTo("EXCEPTION")
                .and()
                .state("STEP3", new Step3())
                    .auto().goTo("END")
                .and()
                .state("EXCEPTION", new ExceptionState())
                .and()
                .finalState("END", new Step4())
                .withName("name")

                .build();
    }

    @Test
    public void testImportExportStateFinished() {

        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("START", data); // Optional event parameter

        String exportState = simpleFSM.exportState();

        assertEquals("END", simpleFSM.getFinalState().getName());
        assertTrue( simpleFSM.isConcluded());

        SimpleFSM importedFSM = buildNew();
        importedFSM.importState(exportState);

        assertEquals("END", importedFSM.getFinalState().getName());
        assertTrue( importedFSM.isConcluded());

        assertEquals("name", importedFSM.getName());

    }

    @Test
    public void testImportExportStatePausedOnAWait() {

        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        simpleFSM.start("START", data); // Optional event parameter

        String exportState = simpleFSM.exportState();

        // Wait after Step2
        assertTrue( simpleFSM.isStarted());
        assertFalse( simpleFSM.isConcluded());
        assertEquals("STEP2", simpleFSM.getPausedOnState().getName());

        SimpleFSM importedFSM = buildNew();
        importedFSM.importState(exportState);

        assertTrue( importedFSM.isStarted());
        assertFalse( importedFSM.isConcluded());
        assertEquals("STEP2", importedFSM.getPausedOnState().getName());

    }
    @Test
    public void testImportExportNotStarted() {

        String exportState = simpleFSM.exportState();
        assertFalse(simpleFSM.isStarted());
        assertFalse(simpleFSM.isPaused());
        assertFalse(simpleFSM.isConcluded());


        SimpleFSM importedFSM = buildNew();
        importedFSM.importState(exportState);

        assertFalse(importedFSM.isStarted());
        assertFalse(importedFSM.isPaused());
        assertFalse(importedFSM.isConcluded());
    }

    @Test
    public void testExportAndImportTerminated() {

        ProcessingData data = new ProcessingData();
        data.set("value", 4);
        simpleFSM.start("START", data); // Optional event parameter
        simpleFSM.setTraceMode(true);

        simpleFSM.triggerEvent("alt_proceed", data);
        String exportState = simpleFSM.exportState();
        assertTrue(simpleFSM.isStarted());
        assertTrue(simpleFSM.isConcluded());
        assertThrows(IllegalStateException.class, simpleFSM::getFinalState);

        SimpleFSM newStateMachine = new SimpleFSM();
        newStateMachine.importState(exportState);

        assertTrue(newStateMachine.isStarted());
        assertTrue(newStateMachine.isConcluded());
        assertThrows(IllegalStateException.class, newStateMachine::getFinalState);
    }

    @Test
    public void testExportAndImportTraceIsSaved(){
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.setTraceMode(true);
        simpleFSM.start("START", data); // Optional event parameter

        assertTrue(simpleFSM.isConcluded());
        String trace = simpleFSM.getTrace().toString();

        String exportState = simpleFSM.exportState();

        SimpleFSM newStateMachine = new SimpleFSM();
        newStateMachine.importState(exportState);
        String traceAfter = newStateMachine.getTrace().toString();

        assertEquals(trace, traceAfter);

    }

    @Test
    public void importError(){
        SimpleFSM newStateMachine = new SimpleFSM();
        assertThrows(RuntimeException.class, () -> newStateMachine.importState("{exportState}"));
    }

}

