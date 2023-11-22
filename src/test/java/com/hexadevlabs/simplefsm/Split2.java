package com.hexadevlabs.simplefsm;

public class Split2 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("split2");
        data.set("value2", 3);
    }
}
