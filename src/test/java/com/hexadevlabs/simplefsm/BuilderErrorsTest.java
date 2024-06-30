package com.hexadevlabs.simplefsm;

import com.hexadevlabs.simplefsm.testSteps.Step1;
import com.hexadevlabs.simplefsm.testSteps.Step2;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class BuilderErrorsTest {

    @Test
    public void failBuild(){

        // Build empty State machine without any states.
         assertThrows( IllegalArgumentException.class, () -> new SimpleFSM.Builder().build());

         // Add duplicate state
        assertThrows( IllegalArgumentException.class,
                () -> new SimpleFSM.Builder()
                        .state("STATE1", new Step1())
                        .newState()
                        .state("STATE1", new Step1())
                        .endStates()
                        .build()
        );

        // Duplicate transitions should throw exception
        assertThrows( IllegalArgumentException.class,
                () -> new SimpleFSM.Builder()
                        .state("STATE1", new Step1())
                        .on("EVENT1").goTo("STATE2")
                        .on("EVENT1").goTo("STATE2")
                        .newState()
                        .state("STATE2", new Step2())
                        .endStates()
                        .build()
        );

        assertThrows( IllegalArgumentException.class,
                () -> new SimpleFSM.Builder()
                        .state("STATE1", new Step1())
                        .auto().goTo("STATE2")
                        .auto().goTo("STATE2")
                        .newState()
                        .state("STATE2", new Step2())
                        .endStates()
                        .build()
        );
    }

}
