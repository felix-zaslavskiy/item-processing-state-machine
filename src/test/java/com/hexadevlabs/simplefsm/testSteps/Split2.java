package com.hexadevlabs.simplefsm.testSteps;

import com.hexadevlabs.simplefsm.ProcessingData;
import com.hexadevlabs.simplefsm.ProcessingStep;

public class Split2 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("split2");
        data.set("value2", 3);
    }
}
