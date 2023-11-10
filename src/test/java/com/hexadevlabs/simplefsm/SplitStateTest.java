package com.hexadevlabs.simplefsm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
            .and()
            .state( "STEP_SPLIT", new StepSplit() )
                .split().goTo("SPLIT1" )
                .split().goTo("SPLIT2" )
            .and()
            .finalState("END", new Step4())
            .state("SPLIT1", new Split1() )
                .join( "END" )
            .and()
            .state("SPLIT2", new Split2() )
                .join( "END" )
            .and()
            .onExceptionGoTo("END")
            .withName("Test FSM")
                .splitHander(new HandleSplitPlaceholder())
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
                	SPLIT2[label="SPLIT2\\n[Split2]"];
                	SPLIT2 -> END[label="SPLIT2_TO_END"];
                	START[label="START\\n[NoopStep]"];
                	START -> STEP_SPLIT[label="AUTO"];
                	END[label="END\\n[Step4]\\n<final>"];
                	SPLIT1[label="SPLIT1\\n[Split1]"];
                	SPLIT1 -> END[label="SPLIT1_TO_END"];
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
        assertTrue(simpleFSM.isFinished());
        assertFalse(simpleFSM.wasTerminated());
        assertNotNull(simpleFSM.getFinalState());
        assertEquals("END", simpleFSM.getFinalState().getName());
        simpleFSM.getTrace().print();
    }

}
