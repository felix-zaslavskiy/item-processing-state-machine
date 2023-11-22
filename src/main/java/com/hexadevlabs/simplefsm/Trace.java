package com.hexadevlabs.simplefsm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trace {

    @JsonProperty("logs")
    private final Map<LocalDateTime, String> logs;

    public boolean isTraceMode() {
        return traceMode;
    }

    public void setTraceMode(boolean traceMode) {
        this.traceMode = traceMode;
    }

    @JsonProperty("traceMode")
    private boolean traceMode = false;

    public Trace() {
        logs = new LinkedHashMap<>();
    }

    public void add(String message) {
        logs.put(LocalDateTime.now(), message);
    }

    public String toString(){
        return String.join("\n", logs.values());
    }

    public void print() {
        logs.forEach((k, v) -> System.out.println(v));
    }

    public void addAll(Map<LocalDateTime, String> logs) {
        if(logs!=null)
            this.logs.putAll(logs);
    }
}