package com.hexadevlabs.simplefsm;

import com.hexadevlabs.simplefsm.supporting.HandleSplitPlaceholder;
import com.hexadevlabs.simplefsm.testSteps.Step1;
import com.hexadevlabs.simplefsm.testSteps.Step2;
import com.hexadevlabs.simplefsm.testSteps.Step3;
import com.hexadevlabs.simplefsm.testSteps.Step4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidationTest {

    @Test
    public void finalStatesMustBeDefined(){

        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("START", new Step1())
                    .conditional().goTo("STEP2")
                    .conditional().goTo("STEP3")
                .state("STEP2", new Step2(), true)
                    .on("proceed").goTo("STEP3")
                    .on("alt_proceed").goTo("STEP3")
                .state("STEP3", new Step3())
                    .auto().goTo("START")
                .build();
        simpleFSM.addFinalState("notexist");
        assertThrows(SimpleFSMValidationException.class, () -> simpleFSM.start("START", null) );
    }

    @Test
    public void transitionsMustPointToDefinedStates() {

        assertThrows(SimpleFSMValidationException.class, () -> {
            SimpleFSM simpleFSM = new SimpleFSM.Builder()
                    .state("START", new Step1())
                        .conditional().goTo("STEP2")
                        .conditional().goTo("STEP3")
                    .state("STEP2", new Step2(), true)
                        .on("proceed").goTo("STEP3")
                        .on("alt_proceed").goTo("undefinedState") // Invalid transition
                    .state("STEP3", new Step3())
                    .   auto().goTo("START")
                    .build();
        });

    }

    @Test
    public void onExceptionStateMustBeDefined() {
        assertThrows(SimpleFSMValidationException.class, () -> {
            SimpleFSM simpleFSM = new SimpleFSM.Builder()
                    .state("START", new Step1())
                        .conditional().goTo("STEP2")
                    .state("STEP2", new Step2(), true)
                        .on("error").goTo("START")
                    .onExceptionGoTo("notDefinedExceptionState") // Invalid exception state
                    .build();
        });

    }

    @Test
    public void splitHandlerMustHaveDefinedSplitTransitions() {
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("START", new Step1())
                    .split().goTo("MIDDLE")
                .state("MIDDLE", new Step2(), true)
                    .on("continue").goTo("END")
                .state("END", new Step3())
                    .auto().goTo("START")
                .splitHandler(new HandleSplitPlaceholder()) // Define a split handler
                .build();

        // Missing split transitions, though split handler is defined
        simpleFSM.getState("START").getSplitTransitions().clear();

        assertThrows(SimpleFSMValidationException.class, () -> simpleFSM.start("START", null));
    }

     @Test
    public void statesReachedBySplitMustHaveAdditionalTransitions() {
         assertThrows(SimpleFSMValidationException.class, () -> {
                     SimpleFSM simpleFSM = new SimpleFSM.Builder()
                             .state("START", new Step1())
                                .split().goTo("SPLIT_STATE")
                             .state("SPLIT_STATE", new Step2(), true)
                                // No additional transitions defined for SPLIT_STATE
                             .build();
                 });
    }

}
