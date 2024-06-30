package com.hexadevlabs.simplefsm.testSteps;

import com.hexadevlabs.simplefsm.ProcessingData;
import com.hexadevlabs.simplefsm.ProcessingStep;

public class Step1WithLongPause extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("split1");

        try {
            // Simulate a 10 second processing time.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        data.set("value1", 2);
    }
}
