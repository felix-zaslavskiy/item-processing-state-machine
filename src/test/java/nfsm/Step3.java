package nfsm;

class Step3 extends ProcessingStep {
    @Override
    protected void process(ProcessingData data) {
        log("Processing Step 3");
        Integer value = (Integer) data.get("value");
        data.set("value", value - 3);
    }
}
