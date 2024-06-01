package com.hexadevlabs.simplefsm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trace {

    @JsonProperty("logs")
    private final List<LogEntry> logs;


    @JsonProperty("traceMode")
    private boolean traceMode = false;

    public Trace() {
        logs = new ArrayList<>();
    }

    public void add(String message) {
        logs.add(new LogEntry(LocalDateTime.now(), message));
    }

    public String toString() {
        return logs.stream()
                .map(LogEntry::toString)
                .collect(Collectors.joining("\n"));
    }

    public void print() {
        logs.forEach(entry -> System.out.println(entry.toString()));
    }

    public void addAll(List<LogEntry> newLogs) {
        if (newLogs != null) {
            logs.addAll(newLogs);
        }
    }

    public void merge(Trace trace) {
        if (trace != null) {
            logs.addAll(trace.logs);
        }
    }

    public boolean isTraceMode() {
        return traceMode;
    }

    public void setTraceMode(boolean traceMode) {
        this.traceMode = traceMode;
    }
}