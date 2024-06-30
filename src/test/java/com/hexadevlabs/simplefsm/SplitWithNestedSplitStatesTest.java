package com.hexadevlabs.simplefsm;

import com.hexadevlabs.simplefsm.supporting.HandleSplitPlaceholder;
import com.hexadevlabs.simplefsm.testSteps.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SplitWithNestedSplitStatesTest {

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
            .state("SPLIT_END", new SplitEnd())
                .auto().goTo("END")
            .and()
            .state("SPLIT_NESTED_END", new NoopStep())
                .auto().goTo("SPLIT_END")
            .and()
            .finalState("END", new NoopStep())
            .state("SPLIT1", new Split1() )
                .split().goTo("SPLIT1_NESTED")
                .split().goTo("SPLIT2_NESTED")
            .and()
            .state("SPLIT2", new Split2() )
                .join( "SPLIT_END" )
            .and()
            .state("SPLIT1_NESTED", new Split1())
                .join("SPLIT_NESTED_END")
            .and()
            .state("SPLIT2_NESTED", new Split2())
                .join("SPLIT_NESTED_END")
            .and()
            .onExceptionGoTo("END")
            .withName("Test FSM")
                .splitHandler(new HandleSplitPlaceholder())
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
                	SPLIT_NESTED_END[label="SPLIT_NESTED_END\\n[NoopStep]"];
                	SPLIT_NESTED_END -> SPLIT_END[label="AUTO"];
                	START[label="START\\n[NoopStep]"];
                	START -> STEP_SPLIT[label="AUTO"];
                	END[label="END\\n[NoopStep]\\n<final>"];
                	SPLIT1[label="SPLIT1\\n[Split1]"];
                	SPLIT1 -> SPLIT2_NESTED[label="SPLIT_SPLIT2_NESTED"];
                	SPLIT1 -> SPLIT1_NESTED[label="SPLIT_SPLIT1_NESTED"];
                	STEP_SPLIT[label="STEP_SPLIT\\n[StepSplit]"];
                	STEP_SPLIT -> SPLIT2[label="SPLIT_SPLIT2"];
                	STEP_SPLIT -> SPLIT1[label="SPLIT_SPLIT1"];
                	SPLIT1_NESTED[label="SPLIT1_NESTED\\n[Split1]"];
                	SPLIT1_NESTED -> SPLIT_NESTED_END[label="SPLIT1_NESTED_TO_SPLIT_NESTED_END"];
                	SPLIT2_NESTED[label="SPLIT2_NESTED\\n[Split2]"];
                	SPLIT2_NESTED -> SPLIT_NESTED_END[label="SPLIT2_NESTED_TO_SPLIT_NESTED_END"];
                	Exception [label="Exception" shape="box"];
                	Exception -> END[label="ON_EXCEPTION"];
                }""";
        // a bit brittle but ok for now.
        assertEquals( expected , graphviz);

    }

    @Test
    public void runSimpleSplittingStateMachine(){
        // TODO: this does not function correctly.
        ProcessingData data = new ProcessingData();
        simpleFSM.start("START", data);
        assertTrue(simpleFSM.isConcluded());
        assertFalse(simpleFSM.wasTerminated());
        assertNotNull(simpleFSM.getFinalState());
        assertEquals("END", simpleFSM.getFinalState().getName());
        //Integer result = (Integer) data.get("value_sum");
        //assertEquals(5, result);
        simpleFSM.getTrace().print();
    }
}
