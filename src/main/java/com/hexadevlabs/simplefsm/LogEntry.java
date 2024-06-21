package com.hexadevlabs.simplefsm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntry {

    @JsonProperty("timestamp")
    final LocalDateTime timestamp;

    @JsonProperty("message")
    final String message;

    @JsonCreator
    public LogEntry(@JsonProperty("timestamp") LocalDateTime timestamp,
                    @JsonProperty("message") String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    // Getters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return timestamp + ": " + message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntry logEntry = (LogEntry) o;

        if (!timestamp.equals(logEntry.timestamp)) return false;
        return message.equals(logEntry.message);
    }

    @Override
    public int hashCode() {
        int result = timestamp.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }
}
