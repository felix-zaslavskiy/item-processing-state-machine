package com.hexadevlabs.simplefsm;

public class Split1 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("split1");

        data.set("value1", 2);
    }
}