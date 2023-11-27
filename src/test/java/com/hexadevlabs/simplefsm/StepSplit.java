package com.hexadevlabs.simplefsm;

public class StepSplit extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("step split");

        data.set("Start", true);
    }
}
