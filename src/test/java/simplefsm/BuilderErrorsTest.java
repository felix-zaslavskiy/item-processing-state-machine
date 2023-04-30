package simplefsm;

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
                        .and()
                        .state("STATE1", new Step1())
                        .end()
                        .build()
        );



    }
}
