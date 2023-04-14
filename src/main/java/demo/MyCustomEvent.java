package demo;

import nfsm.Event;

public class MyCustomEvent implements Event {
    private final String name;

    public MyCustomEvent(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}