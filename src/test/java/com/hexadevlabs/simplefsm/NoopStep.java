package com.hexadevlabs.simplefsm;

public class NoopStep extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Noop step");
    }
}
