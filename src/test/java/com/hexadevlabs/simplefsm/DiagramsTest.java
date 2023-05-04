package com.hexadevlabs.simplefsm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DiagramsTest {
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
                .on(proceedEvent).goTo("end")
                .on("alt_proceed").goTo("EXCEPTION")
                .and()
                .state("STEP3", new Step3())
                .auto().goTo("END")
                .and()
                .state("EXCEPTION", new ExceptionState())
                .and()
                .finalState("END", new Step4())
                .onExceptionGoTo("END")
                .withName("Test FSM")
                .build();
    }

    @Test
    public void graphTest(){
        String graphviz = simpleFSM.toGraphviz();
        assertNotNull(graphviz);
        // a bit brittle but ok for now.
        assertEquals( "digraph G {\n" +
                "labelloc=\"t\";\n" +
                "label=<<B>Test FSM</B>>;\n" +
                "\tEXCEPTION[label=\"EXCEPTION\\n[ExceptionState]\"];\n" +
                "\tSTART[label=\"START\\n[Step1]\"];\n" +
                "\tSTART -> STEP3[label=\"START_TO_STEP3\"];\n" +
                "\tSTART -> STEP2[label=\"START_TO_STEP2\"];\n" +
                "\tEND[label=\"END\\n[Step4]\\n<final>\"];\n" +
                "\tSTEP2[label=\"STEP2\\n[Step2]\\n<wait>\"];\n" +
                "\tSTEP2 -> end[label=\"proceed\"];\n" +
                "\tSTEP2 -> EXCEPTION[label=\"alt_proceed\"];\n" +
                "\tSTEP3[label=\"STEP3\\n[Step3]\"];\n" +
                "\tSTEP3 -> END[label=\"AUTO\"];\n" +
                "\tException [label=\"Exception\" shape=\"box\"];\n" +
                "\tException -> END[label=\"ON_EXCEPTION\"];\n" +
                "}" , graphviz);
    }

}
