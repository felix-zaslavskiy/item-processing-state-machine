package com.hexadevlabs.simplefsm.testSteps;

import com.hexadevlabs.simplefsm.ProcessingData;
import com.hexadevlabs.simplefsm.ProcessingStep;

public class Step1 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Processing Step 1");
        Integer value = (Integer) data.get("value");

        // Select the next state based on the value
        if (value % 2 == 0) {
            nextState(data, "STEP2");
        } else {
            nextState(data, "STEP3");
        }
    }
}