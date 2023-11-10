package com.hexadevlabs.simplefsm;

public class SplitEnd extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Split End");
        Integer value1 = (Integer) data.get("value1");
        Integer value2 = (Integer) data.get("value2");
        data.set("value_sum", value1 + value2);
    }
}
