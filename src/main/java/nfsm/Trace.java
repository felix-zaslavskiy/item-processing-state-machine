package nfsm;

import java.util.ArrayList;
import java.util.List;

public class Trace {
    private List<String> log;

    public Trace() {
        log = new ArrayList<>();
    }

    public void add(String message) {
        log.add(message);
    }

    public void print() {
        log.forEach(System.out::println);
    }
}