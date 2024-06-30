package com.hexadevlabs.simplefsm;

import com.hexadevlabs.simplefsm.testSteps.Step1;
import com.hexadevlabs.simplefsm.testSteps.Step2;
import com.hexadevlabs.simplefsm.testSteps.Step3;
import com.hexadevlabs.simplefsm.testSteps.Step4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DiagramsTest {
    private SimpleFSM simpleFSM;

    @BeforeEach
    public void setUp() {
        simpleFSM = buildNew();
    }

    private SimpleFSM buildNew(){
        return new SimpleFSM.Builder()
                .state("START", new Step1())
                    .conditional().goTo("STEP2")
                    .conditional().goTo("STEP3")
                .newState()
                .state("STEP2", new Step2(), true)
                    .on("proceed").goTo("END")
                    .on("alt_proceed").goTo("EXCEPTION")
                .newState()
                    .state("STEP3", new Step3())
                .   auto().goTo("END")
                .newState()
                .state("EXCEPTION", new ExceptionState())
                .newState()
                    .finalState("END", new Step4())
                    .onExceptionGoTo("END")
                .withName("Test FSM")
                .build();
    }

    @Test
    public void graphTest(){
        String graphviz = simpleFSM.toGraphviz();
        assertNotNull(graphviz);
        String expected = """
                digraph G {
                labelloc="t";
                label=<<B>Test FSM</B>>;
                \tEXCEPTION[label="EXCEPTION\\n[ExceptionState]"];
                \tSTART[label="START\\n[Step1]"];
                \tSTART -> STEP3[label="START_TO_STEP3"];
                \tSTART -> STEP2[label="START_TO_STEP2"];
                \tEND[label="END\\n[Step4]\\n<final>"];
                \tSTEP2[label="STEP2\\n[Step2]\\n<wait>"];
                \tSTEP2 -> END[label="proceed"];
                \tSTEP2 -> EXCEPTION[label="alt_proceed"];
                \tSTEP3[label="STEP3\\n[Step3]"];
                \tSTEP3 -> END[label="AUTO"];
                \tException [label="Exception" shape="box"];
                \tException -> END[label="ON_EXCEPTION"];
                }""";

        // a bit brittle but ok for now.
        assertEquals( expected , graphviz);
    }

}
