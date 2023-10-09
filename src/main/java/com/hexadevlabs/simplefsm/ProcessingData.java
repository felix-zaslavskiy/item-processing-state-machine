package com.hexadevlabs.simplefsm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The ProcessingData class represents a container for storing and managing data during the
 * execution of a finite state machine. It maintains a map of key-value pairs and allows
 * for setting and retrieving data by keys. It also holds the next state and exception
 * information, if any.
 */
public class ProcessingData implements Serializable {
    private final Map<String, Object> dataMap;
    private String nextState;

    // If state machine has exception info.
    private ExceptionInfo exceptionInfo;

    public ProcessingData() {
        this.dataMap = new HashMap<>();
        this.nextState = null;
    }

    /**
     * Sets a key-value pair in the data map.
     *
     * @param key   The key to store the value under.
     * @param value The value to store.
     */
    public void set(String key, Object value) {
        dataMap.put(key, value);
    }


    /**
     * Retrieves the value stored under the specified key.
     *
     * @param key The key to look up the value.
     * @return The value associated with the specified key, or null if the key is not present.
     */
    public Object get(String key) {
        return dataMap.get(key);
    }

    public boolean containsKey(String key) {
        return dataMap.containsKey(key);
    }

    public boolean hadException(){
        return exceptionInfo != null;
    }
    public Exception getException(){
        return exceptionInfo.exception;
    }

    String getNextState() {
        return nextState;
    }

    void setNextState(String nextState) {
        this.nextState = nextState;
    }

    void setExceptionInfo(ExceptionInfo exceptionInfo){
        this.exceptionInfo = exceptionInfo;
    }
}