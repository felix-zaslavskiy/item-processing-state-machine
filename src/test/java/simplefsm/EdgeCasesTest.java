package simplefsm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EdgeCasesTest {

    @Test
    public void manyPossibleTransitions(){
        SimpleFSM  simpleFSM = new SimpleFSM.Builder()
                .state("START", new ProcessingStep() {
                    @Override
                    protected void process(ProcessingData data) {

                    }
                })
                .conditional().goTo("STEP2")
                .conditional().goTo("STEP3")
                .and()
                .state("STEP2", new Step2(), true)
                .on("alt_proceed").goTo("STEP3")
                .and()
                .state("STEP3", new Step3())
                .auto().goTo("end")
                .and()
                .finalState("end", new Step4())
                .build();
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        assertThrows(IllegalStateException.class, () -> simpleFSM.start("START", data));

    }

    @Test
    public void autoTransition(){
        SimpleFSM  simpleFSM = new SimpleFSM.Builder()
                .state("START", new ProcessingStep() {
                    @Override
                    protected void process(ProcessingData data) {

                    }
                })
                .auto().goTo("STEP3")
                .and()
                .state("STEP2", new Step2(), true)
                .on("alt_proceed").goTo("STEP3")
                .and()
                .state("STEP3", new Step3())
                .auto().goTo("end")
                .and()
                .finalState("end", new Step4())
                .build();
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("START", data);
        assertTrue(simpleFSM.isFinished());

    }
}
