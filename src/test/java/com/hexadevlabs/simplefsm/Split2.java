package com.hexadevlabs.simplefsm;

public class Split2 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("split2");
        // Add some random wait between 1 and 200 ms
        try {
            Thread.sleep((int)(Math.random() * 200));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        data.set("value2", 3);
    }
}
