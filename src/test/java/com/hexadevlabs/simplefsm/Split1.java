package com.hexadevlabs.simplefsm;

public class Split1 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Step1");

        data.set("value1", 2);
    }
}