package com.hexadevlabs.simplefsm.testSteps;

import com.hexadevlabs.simplefsm.ProcessingData;
import com.hexadevlabs.simplefsm.ProcessingStep;

public class NoopStep extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Noop step");
    }
}
