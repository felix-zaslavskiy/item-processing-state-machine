package simplefsm;

import demo.MyCustomEvent;
import demo.SimpleFSMDemo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static demo.DemoNames.START;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImportExportFSM {

    private SimpleFSM simpleFSM;
    private final NamedEntity proceedEvent = new MyCustomEvent("proceed");
    @BeforeEach
    public void setUp() {
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
    }

    @Test
    public void testImportState() {


        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start(START, data); // Optional event parameter

        String export = simpleFSM.exportState();
        simpleFSM.importState(export);

        // Check if the state has been imported correctly
        // Replace this with the actual assertion based on your state representation
        //assertEquals("SampleStateData", simpleFSM.getCurrentStateData());
    }

    @Test
    public void testExportState() {
        String stateData = "SampleStateData";
        simpleFSM.importState(stateData);

        String exportedStateData = simpleFSM.exportState();

        // Check if the state has been exported correctly
        // Replace this with the actual assertion based on your state representation
        assertEquals("SampleStateData", exportedStateData);
    }

    @Test
    public void testExportAndImport() {
        String initialStateData = "InitialStateData";
        simpleFSM.importState(initialStateData);

        String exportedStateData = simpleFSM.exportState();
        SimpleFSM newStateMachine = new SimpleFSM();
        newStateMachine.importState(exportedStateData);

        // Check if the state has been exported and imported correctly
        // Replace this with the actual assertion based on your state representation
        // assertEquals("InitialStateData", newStateMachine.getCurrentStateData());
    }
}

