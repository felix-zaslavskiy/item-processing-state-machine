package com.hexadevlabs.simplefsm;

import com.hexadevlabs.simplefsm.supporting.HandleSplitPlaceholder;
import com.hexadevlabs.simplefsm.testSteps.*;
import com.mysql.cj.exceptions.AssertionFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// this class uses HandleSplitPlaceholder which is trivial split handling
// without parallelism or persistence of the state machine.
public class SplitStateTest {
    private SimpleFSM simpleFSM;

    @BeforeEach
    public void setUp() {
        simpleFSM = buildNew();
    }

    private SimpleFSM buildNew(){
        return new SimpleFSM.Builder()
            .state("START", new NoopStep())
                .auto().goTo("STEP_SPLIT")
            .state( "STEP_SPLIT", new StepSplit() )
                .split().goTo("SPLIT1" )
                .split().goTo("SPLIT2" )
            .state("SPLIT1", new Split1() )
                .join( "SPLIT_END" )
            .state("SPLIT2", new Split2() )
                .join( "SPLIT_END" )
            .state("SPLIT_END", new SplitEnd())
                .auto().goTo("END")
            .finalState("END", new NoopStep())
            .onExceptionGoTo("END")
            .withName("Test FSM")
                .splitHandler(new ThreadBasedSplitHandler())
            .withTrace()
            .build();
    }

    @Test
    public void graphTest() {
        String graphviz = simpleFSM.toGraphviz();
        assertNotNull(graphviz);
        String expected = """
                digraph G {
                labelloc="t";
                label=<<B>Test FSM</B>>;
                	SPLIT_END[label="SPLIT_END\\n[SplitEnd]"];
                	SPLIT_END -> END[label="AUTO"];
                	SPLIT2[label="SPLIT2\\n[Split2]"];
                	SPLIT2 -> SPLIT_END[label="SPLIT2_TO_SPLIT_END"];
                	START[label="START\\n[NoopStep]"];
                	START -> STEP_SPLIT[label="AUTO"];
                	SPLIT1[label="SPLIT1\\n[Split1]"];
                	SPLIT1 -> SPLIT_END[label="SPLIT1_TO_SPLIT_END"];
                	END[label="END\\n[NoopStep]\\n<final>"];               	
                	STEP_SPLIT[label="STEP_SPLIT\\n[StepSplit]"];
                	STEP_SPLIT -> SPLIT2[label="SPLIT_SPLIT2"];
                	STEP_SPLIT -> SPLIT1[label="SPLIT_SPLIT1"];
                	Exception [label="Exception" shape="box"];
                	Exception -> END[label="ON_EXCEPTION"];
                }""";
        // a bit brittle but ok for now.
        assertEquals( expected , graphviz);
    }


    @Test
    public void runSimpleSplittingStateMachine(){
        ProcessingData data = new ProcessingData();
        simpleFSM.start("START", data);
        assertTrue(simpleFSM.isConcluded());
        assertTrue(simpleFSM.hasReachedFinalState());
        assertFalse(simpleFSM.wasTerminated());
        assertNotNull(simpleFSM.getFinalState());
        assertEquals("END", simpleFSM.getFinalState().getName());
        Integer result = (Integer) data.get("value_sum");
        assertEquals(5, result);
        simpleFSM.getTrace().print();
    }

    @Test
    public void withException(){
        simpleFSM.getState("SPLIT1").setProcessingStep(new Split1WithException());

        ProcessingData data = new ProcessingData();
        simpleFSM.start("START", data);

        assertTrue(data.hadException());
        assertTrue(simpleFSM.isConcluded());
        assertFalse(simpleFSM.wasTerminated());
        assertTrue(simpleFSM.hasReachedFinalState());

        // Step 1 had exception so should go to End without executing Split_end state.
        // This means the values of value1 and value2 could not be added.
        assertNull(data.get("value_sum"));

        System.out.println(data.toJson());
        simpleFSM.getTrace().print();
    }

    @Test
    public void noExceptionHandler(){
        // No onExceptionGoTo so FSM will terminate.
        SimpleFSM special =new SimpleFSM.Builder()
            .state("START", new NoopStep())
                .auto().goTo("STEP_SPLIT")
            .state( "STEP_SPLIT", new StepSplit() )
                .split().goTo("SPLIT1" )
                .split().goTo("SPLIT2" )
            .state("SPLIT1", new Split1WithException() )
                .join( "SPLIT_END" )
            .state("SPLIT2", new Split2() )
                .join( "SPLIT_END" )
            .state("SPLIT_END", new SplitEnd())
                .auto().goTo("END")
            .finalState("END", new NoopStep())
            .withName("Test FSM")
                .splitHandler(new ThreadBasedSplitHandler())
            .withTrace()
            .build();

        ProcessingData data = new ProcessingData();
        special.start("START", data);

        assertTrue(data.hadException());
        assertTrue(special.isConcluded());
        assertTrue(special.wasTerminated());
        assertFalse(special.hasReachedFinalState());

        // Step 1 had exception so should go to End without executing Split_end state.
        // This means the values of value1 and value2 could not be added.
        assertNull(data.get("value_sum"));

        System.out.println(data.toJson());
        special.getTrace().print();
    }

    @Test
    @Disabled
    // TODO: This one needs more work to work consistently
    public void missingAJoin(){
        SimpleFSM fsm = new SimpleFSM.Builder()
            .state("START", new NoopStep())
                .auto().goTo("STEP_SPLIT")
            .state( "STEP_SPLIT", new StepSplit() )
                .split().goTo("SPLIT1" )
                .split().goTo("SPLIT2" )
            .state("SPLIT1", new Split1() )
                .join( "SPLIT_END" )
            .state("SPLIT2", new Split2() )
            .state("SPLIT_END", new SplitEnd())
                .auto().goTo("END")
            .finalState("END", new NoopStep())
            .onExceptionGoTo("END")
            .withName("Test FSM")
                .splitHandler(new ThreadBasedSplitHandler())
            .withTrace()
            .build();

        ProcessingData data = new ProcessingData();

        AssertionError exception = assertThrows(AssertionError.class, () -> {
            fsm.start("START", data);
        });


    }

    @Test
    public void splitStateHasExtraState(){
        SimpleFSM fsm = new SimpleFSM.Builder()
            .state("START", new NoopStep())
                .auto().goTo("STEP_SPLIT")
            .state( "STEP_SPLIT", new StepSplit() )
                .split().goTo("SPLIT1" )
                .split().goTo("SPLIT2" )
            .state("SPLIT1", new Split1() )
                .join( "SPLIT_END" )
            .state("SPLIT2", new Split2() , true )
                .on("SOME_DELAY").goTo("AFTER2")
            .state("AFTER2", new NoopStep())
                .join("SPLIT_END")
            .state("SPLIT_END", new SplitEnd())
                .auto().goTo("END")
            .finalState("END", new NoopStep())
            .onExceptionGoTo("END")
            .withName("Test FSM")
                .splitHandler(new ThreadBasedSplitHandler())
            .withTrace()
            .build();
        ProcessingData data = new ProcessingData();
        AssertionError exception = assertThrows(AssertionError.class, () -> {
            fsm.start("START", data);
        });


    }


    @Test
    public void waitForEventAfterSplit(){
        SimpleFSM fsm= new SimpleFSM.Builder()
            .state("START", new NoopStep())
                .auto().goTo("STEP_SPLIT")
            .state( "STEP_SPLIT", new StepSplit() )
                .split().goTo("SPLIT1" )
                .split().goTo("SPLIT2" )
            .state("SPLIT1", new Split1() )
                .join( "SPLIT_END" )
            .state("SPLIT2", new Split2() )
                .join( "SPLIT_END" )
            .state("SPLIT_END", new SplitEnd(), true)
                .on("SPLIT_END_WAIT").goTo("END")
            .finalState("END", new NoopStep())
            .onExceptionGoTo("END")
            .withName("Test FSM")
                .splitHandler(new ThreadBasedSplitHandler())
            .withTrace()
            .build();
        ProcessingData data = new ProcessingData();
        fsm.start("START", data);

        assertTrue(fsm.isPaused());

        fsm.triggerEvent("SPLIT_END_WAIT", data);

        assertTrue(fsm.isConcluded());
        assertTrue(fsm.hasReachedFinalState());

    }

    @Test
    public void splitUsingPlaceholderHandler(){
        ProcessingData data = new ProcessingData();
        simpleFSM.addSplitHandler(new HandleSplitPlaceholder());
        simpleFSM.start("START", data);


        assertTrue(simpleFSM.isConcluded());
        assertTrue(simpleFSM.hasReachedFinalState());
        assertFalse(simpleFSM.wasTerminated());

        assertNotNull(simpleFSM.getFinalState());
        assertEquals("END", simpleFSM.getFinalState().getName());
        Integer result = (Integer) data.get("value_sum");
        assertEquals(5, result);
        simpleFSM.getTrace().print();
    }

}
