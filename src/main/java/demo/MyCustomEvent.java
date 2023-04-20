package demo;

import simplefsm.NamedEntity;

public class MyCustomEvent implements NamedEntity {
    private final String name;

    public MyCustomEvent(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}