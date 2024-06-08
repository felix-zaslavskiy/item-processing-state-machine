package com.hexadevlabs.simplefsm;

public class TransitionAutoEvent implements NamedEntity {

    @Override
    public String name() {
        return TransitionAutoEvent.NAME;
    }

    public final static String NAME = "AUTO";
}