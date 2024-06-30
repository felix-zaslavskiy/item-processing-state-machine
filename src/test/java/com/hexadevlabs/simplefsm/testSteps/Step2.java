package com.hexadevlabs.simplefsm.testSteps;

import com.hexadevlabs.simplefsm.ProcessingData;
import com.hexadevlabs.simplefsm.ProcessingStep;

public class Step2 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Processing Step 2");
        Integer value = (Integer) data.get("value");
        data.set("value", value * 2);
    }
}