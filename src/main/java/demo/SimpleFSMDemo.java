package demo;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import simplefsm.*;

import java.io.File;
import java.io.IOException;

import static demo.DemoNames.*;
class Step1 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Processing Step 1");
        Integer value = (Integer) data.get("value");
        data.set("value", value + 1);

        // Select the next state based on the value
        if (value % 2 == 0) {
            nextState(data, STEP2);
        } else {
            nextState(data, STEP3);
        }
    }
}

class Step2 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Processing Step 2");
        Integer value = (Integer) data.get("value");
        data.set("value", value * 2);
    }
}

class Step3 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Processing Step 3");
        Integer value = (Integer) data.get("value");
        data.set("value", value - 3);
    }
}

class Step4 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Processing Step 4");
        Integer value = (Integer) data.get("value");
        data.set("value", value / 2);
    }
}

class Hooks implements ExecutionHooks {

    @Override
    public void before(State state, ProcessingData data) throws Exception {
        throw new RuntimeException();
        // System.out.println("Before hook " + state.getName() + " " + state.getProcessStepClassName());
    }

    @Override
    public void after(State state, ProcessingData data) throws Exception {
        System.out.println("After hook " + state.getName() + " " + state.getProcessStepClassName());
    }

}

public class SimpleFSMDemo {
    public static void main(String[] args) {
        fluentBuilder();
    }

    private static void fluentBuilder() {
        NamedEntity myCustomEvent = new MyCustomEvent("PROCEED");

        SimpleFSM simpleFSM = new SimpleFSM.Builder()
                .state(START, new Step1())
                    .conditional().goTo(STEP2) // Generates event name: START_TO_STEP2
                    .conditional().goTo(STEP3) // Generates event name: START_TO_STEP3
                .and()
                .state(STEP2, new Step2(), true)
                    .on(myCustomEvent).goTo(END)
                    .on(ALT_PROCEED).goTo(STEP3)
                .and()
                .state(STEP3, new Step3())
                    .auto().goTo(END)
                .and()
                .finalState(END, new Step4())
                .onExceptionGoTo(END)
                .withTrace()
                .withExecutionHook(new Hooks())
                .build();

        String graphvizDot = simpleFSM.toGraphviz();
        renderGraph(graphvizDot, "state_machine.png");

        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        simpleFSM.start(START, data); // Optional event parameter

        String export = simpleFSM.exportState();
        simpleFSM.importState(export);

        if(simpleFSM.isPaused())
            simpleFSM.triggerEvent(myCustomEvent, data);

        if(simpleFSM.getFinalState()!=null)
            System.out.println("\nEnded with state: " + simpleFSM.getFinalState().getName());

        System.out.println("\nTrace: \n" + simpleFSM.getTrace());
    }

    public static void renderGraph(String dot, String outputPath) {
        try {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(800).render(Format.PNG).toFile(new File(outputPath));
            System.out.println("Graph saved to " + outputPath);
        } catch (IOException e) {
            System.err.println("Error rendering Graphviz graph: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
