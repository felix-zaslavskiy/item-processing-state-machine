package com.hexadevlabs.simplefsm;


import com.hexadevlabs.simplefsm.testSteps.Step2;
import com.hexadevlabs.simplefsm.testSteps.Step4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Hooks1 implements ExecutionHooks {

    @Override
    public void before(State state, ProcessingData data)  {
        System.out.println("Before hook " + state.getName() + " " + state.getProcessStepClassName());
    }

    @Override
    public void after(State state, ProcessingData data) {
        System.out.println("After hook " + state.getName() + " " + state.getProcessStepClassName());
    }

}

class Hooks2 implements ExecutionHooks {

    @Override
    public void before(State state, ProcessingData data) {
         throw new RuntimeException();
    }

    @Override
    public void after(State state, ProcessingData data) {
        System.out.println("After hook " + state.getName() + " " + state.getProcessStepClassName());
    }

}

public class ExecutionHookTest {

    @Test
    public void testHooks1(){
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                    .state("STEP2", new Step2())
                    .auto().goTo("END")
                    .and()
                    .finalState("END", new Step4())
                    .withExecutionHook(new Hooks1())
                    .build();
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("STEP2", data);

        assertTrue(simpleFSM.isConcluded());
        assertEquals("END", simpleFSM.getFinalState().getName());

    }

    @Test
    public void testHook2(){
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("STEP2", new Step2())
                .auto().goTo("END")
                .and()
                .finalState("END", new Step4())
                .withExecutionHook(new Hooks2())
                .onExecutionHookExceptionTerminate()
                .withTrace()
                .build();
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("STEP2", data);
        assertTrue(simpleFSM.isStarted());
        assertFalse(simpleFSM.isPaused());
        assertTrue(simpleFSM.isConcluded());
        assertTrue(simpleFSM.wasTerminated());
        assertFalse(simpleFSM.hasReachedFinalState());
    }

    @Test
    public void testHook2a(){
        // unlike testHook2 this has onExceptionGoTo()
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("STEP2", new Step2())
                .auto().goTo("END")
                .and()
                .finalState("END", new Step4())
                .withExecutionHook(new Hooks2())
                .onExecutionHookExceptionTerminate()
                .onExceptionGoTo("END")
                .withTrace()
                .build();
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("STEP2", data);
        assertTrue(simpleFSM.isStarted());
        assertFalse(simpleFSM.isPaused());
        assertTrue(simpleFSM.isConcluded());
        assertTrue(simpleFSM.wasTerminated());
    }
    @Test
    public void testHook3(){
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("STEP2", new Step2())
                .auto().goTo("END")
                .and()
                .finalState("END", new Step4())
                .withExecutionHook(new ExecutionHooks() {
                    @Override
                    public void before(State state, ProcessingData data) {
                        // Nothing here.
                    }

                    @Override
                    public void after(State state, ProcessingData data)  {
                        throw new RuntimeException("Something went wrong");
                    }
                })
                .onExecutionHookExceptionTerminate()
                .withTrace()
                .build();
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("STEP2", data);
        assertTrue(simpleFSM.isStarted());
        assertTrue(simpleFSM.isConcluded());
        assertTrue(simpleFSM.wasTerminated());
    }


    @Test
    public void testHook4(){
        // Unlike testHook3 there is no .onExecutionHookExceptionTerminate()
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("STEP2", new Step2())
                .auto().goTo("END")
                .and()
                .finalState("END", new Step4())
                .withExecutionHook(new ExecutionHooks() {
                    @Override
                    public void before(State state, ProcessingData data) {
                        // Nothing here.
                    }

                    @Override
                    public void after(State state, ProcessingData data)  {
                        throw new RuntimeException();
                    }
                })
                .withTrace()
                .build();
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("STEP2", data);
        assertTrue(simpleFSM.isStarted());
        assertTrue(simpleFSM.isConcluded());
        assertTrue(simpleFSM.wasTerminated());
    }

    @Test
    public void testHook5(){
        // On exception in Hook got to END
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("STEP2", new Step2())
                .auto().goTo("END")
                .and()
                .finalState("END", new Step4())
                .withExecutionHook(new ExecutionHooks() {
                    @Override
                    public void before(State state, ProcessingData data) {
                        // Nothing here.
                    }

                    @Override
                    public void after(State state, ProcessingData data)  {
                        if( state.getName().equals("STEP2") )
                            throw new RuntimeException();
                    }
                })
                .onExceptionGoTo("END")
                .withTrace()
                .build();
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("STEP2", data);
        assertTrue(simpleFSM.isStarted());
        assertTrue(simpleFSM.isConcluded());
        assertFalse(simpleFSM.wasTerminated());
        assertEquals("END", simpleFSM.getFinalState().getName());
    }
    @Test
    public void onExceptionHandler(){
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("STEP2", new ProcessingStep() {
                    @Override
                    protected void process(ProcessingData data) {
                        throw new RuntimeException();
                    }
                })
                .auto().goTo("END")
                .and()
                .finalState("END", new Step4())
                .onExceptionGoTo("END")
                .withTrace()
                .build();
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("STEP2", data);
        assertTrue(simpleFSM.isStarted());
        assertTrue(simpleFSM.isConcluded());
        assertEquals("END", simpleFSM.getFinalState().getName());
    }

    @Test
    public void onExceptionHandler2(){
        // Cause Exception to go to END state but
        // END state also throws exception
        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state("STEP2", new ProcessingStep() {
                    @Override
                    protected void process(ProcessingData data) {
                        throw new RuntimeException();
                    }
                })
                .auto().goTo("END")
                .and()
                .finalState("END", new ProcessingStep() {
                    @Override
                    protected void process(ProcessingData data) {
                        throw new RuntimeException();
                    }
                })
                .onExceptionGoTo("END")
                .withTrace()
                .build();
        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start("STEP2", data);
        assertTrue(simpleFSM.isStarted());
        assertTrue(simpleFSM.isConcluded());
        assertTrue(simpleFSM.wasTerminated());

    }
}
