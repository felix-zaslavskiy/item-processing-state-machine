package com.hexadevlabs.simplefsm.testSteps;

import com.hexadevlabs.simplefsm.ProcessingData;
import com.hexadevlabs.simplefsm.ProcessingStep;

public class Step4 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Processing Step 4");
    }
}
