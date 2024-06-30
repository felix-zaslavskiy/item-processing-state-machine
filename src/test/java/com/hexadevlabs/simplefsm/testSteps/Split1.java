package com.hexadevlabs.simplefsm.testSteps;

import com.hexadevlabs.simplefsm.ProcessingData;
import com.hexadevlabs.simplefsm.ProcessingStep;

public class Split1 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("split1");

        data.set("value1", 2);
    }
}