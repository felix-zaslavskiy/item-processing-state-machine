package com.hexadevlabs.simplefsm.testSteps;

import com.hexadevlabs.simplefsm.ProcessingData;
import com.hexadevlabs.simplefsm.ProcessingStep;

public class SplitEnd extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Split End");
        Integer value1 = (Integer) data.get("value1");
        Integer value2 = (Integer) data.get("value2");
        data.set("value_sum", value1 + value2);
    }
}
