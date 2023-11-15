package com.hexadevlabs.simplefsm;

public class Split1 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("split1");

        // Add some random wait between 1 and 200 ms
        try {
            Thread.sleep((int)(Math.random() * 200));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        data.set("value1", 2);
    }
}