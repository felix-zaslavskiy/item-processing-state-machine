package nfsm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trace {

    @JsonProperty("log")
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