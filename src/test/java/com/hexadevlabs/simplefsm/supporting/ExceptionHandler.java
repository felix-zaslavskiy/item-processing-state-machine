package com.hexadevlabs.simplefsm.supporting;

import com.hexadevlabs.simplefsm.ProcessingData;
import com.hexadevlabs.simplefsm.ProcessingStep;

public class ExceptionHandler extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        data.set("error" , 1);
    }
}
