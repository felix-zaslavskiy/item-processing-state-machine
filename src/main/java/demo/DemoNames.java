package demo;

import nfsm.NamedEntity;

public enum DemoNames implements NamedEntity {
    START,
    STEP2,
    STEP3,
    END,
    ALT_PROCEED;


    @Override
    public String getName() {
        return this.name();
    }
}
