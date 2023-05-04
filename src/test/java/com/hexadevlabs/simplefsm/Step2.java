package com.hexadevlabs.simplefsm;

class Step2 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Processing Step 2");
        Integer value = (Integer) data.get("value");
        data.set("value", value * 2);
    }
}