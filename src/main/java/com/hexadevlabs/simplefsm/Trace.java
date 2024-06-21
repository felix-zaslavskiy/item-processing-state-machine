package com.hexadevlabs.simplefsm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trace {

    @JsonProperty("logs")
    final List<LogEntry> logs;


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

    public int size(){
        return logs.size();
    }

    /**
     * Efficiently adds all log entries from the newLogs list to the current list without duplicates.
     * Maintains the insertion order using LinkedHashSet.
     *
     * @param newLogs List of LogEntry to be added.
     */
    public void addAll(List<LogEntry> newLogs) {
        if (newLogs != null) {
            Set<LogEntry> set = new LinkedHashSet<>(logs); // Start with existing logs
            set.addAll(newLogs); // Add all, duplicates are ignored
            logs.clear();
            logs.addAll(set); // Replace old list with new set contents
        }
    }

    /**
     * Merge another trace object with this.
     * LogEntries that are identical will not be duplicated.
     * The order relative to each log is maintained.
     *
     * @param trace The Trace object to merge with this one.
     */
    public void merge(Trace trace) {
        if (trace != null) {
            addAll(trace.logs);
        } else {
            throw new NullPointerException("Trace merged on should not be null");
        }
    }

    public boolean isTraceMode() {
        return traceMode;
    }

    public void setTraceMode(boolean traceMode) {
        this.traceMode = traceMode;
    }
}