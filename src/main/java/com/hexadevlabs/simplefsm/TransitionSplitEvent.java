package com.hexadevlabs.simplefsm;

public class TransitionSplitEvent  implements NamedEntity {

    @Override
    public String name() {
        return TransitionAutoEvent.NAME;
    }

    public final static String NAME = "SPLIT";
}
