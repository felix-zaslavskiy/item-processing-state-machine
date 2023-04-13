package demo;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import nfsm.NFSM;
import nfsm.ProcessingData;
import nfsm.ProcessingStep;

import java.io.File;
import java.io.IOException;

class Step1 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        System.out.println("Processing Step 1");
        Integer value = (Integer) data.get("value");
        data.set("value", value + 1);

        // Select the next state based on the value
        if (value % 2 == 0) {
            nextState(data, "step2");
        } else {
            nextState(data, "step3");
        }
    }
}

class Step2 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        System.out.println("Processing Step 2");
        Integer value = (Integer) data.get("value");
        data.set("value", value * 2);
    }
}

class Step3 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        System.out.println("Processing Step 3");
        Integer value = (Integer) data.get("value");
        data.set("value", value - 3);
    }
}

class Step4 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        System.out.println("Processing Step 4");
        Integer value = (Integer) data.get("value");
        data.set("value", value / 2);
    }
}

public class NFSMDemo {
    public static void main(String[] args) {
        builderWay();
    }

    private static void builderWay() {
        NFSM nfsm = new NFSM.Builder()
                .state("start", new Step1())
                    .transition("step2") // Generates event name: start_to_step2
                    .transition("step3") // Generates event name: start_to_step3
                .and()
                .state("step2", new Step2(), true)
                    .transition("proceed", "end")
                .and()
                .state("step3", new Step3())
                    .autoTransition("end")
                .and()
                .state("end", new Step4())
                .build();


        String graphvizDot = nfsm.toGraphviz();
        renderGraph(graphvizDot, "state_machine.png");

        ProcessingData data = new ProcessingData();
        data.set("value", 5);
        nfsm.start("start", data); // Optional event parameter
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
