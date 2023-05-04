package com.hexadevlabs.simplefsm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trace {

    @JsonProperty("logs")
    private final List<String> logs;

    public boolean isTraceMode() {
        return traceMode;
    }

    public void setTraceMode(boolean traceMode) {
        this.traceMode = traceMode;
    }

    @JsonProperty("traceMode")
    private boolean traceMode = false;

    public Trace() {
        logs = new ArrayList<>();
    }

    public void add(String message) {
        logs.add(message);
    }

    public String toString(){
        return String.join("\n", logs);
    }

    public void print() {
        logs.forEach(System.out::println);
    }

    public void addAll(List<String> logs) {
        if(logs!=null)
            this.logs.addAll(logs);
    }
}