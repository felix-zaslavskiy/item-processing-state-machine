package com.hexadevlabs.simplefsm;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ProcessingData class represents a container for storing and managing data during the
 * execution of a finite state machine. It maintains a map of key-value pairs and allows
 * for setting and retrieving data by keys. It also holds the next state and exception
 * information, if any.
 */
public class ProcessingData implements Serializable {

    static ObjectMapper mapper = JsonMapper.builder()
            .addModules(new JavaTimeModule())
            .build()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);


    private final Map<String, Object> dataMap;
    private String nextState;

    // Other properties remain unchanged.
    private List<ExceptionInfo> exceptions = new ArrayList<>();

    public void addException(ExceptionInfo exceptionInfo) {
        exceptions.add(exceptionInfo);
    }

    public List<ExceptionInfo> getExceptions() {
        return exceptions;
    }

    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }


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

    // These methods assume no Split state are used so
    // only a single exception is possible.
    public boolean hadException(){
        return !exceptions.isEmpty();
    }
    public Exception getException(){
        return exceptions.get(0).exception;
    }

    String getNextState() {
        return nextState;
    }

    void setNextState(String nextState) {
        this.nextState = nextState;
    }



    /**
     * Simple merge operation. Keys from data will be merged
     * to this object. If data has same key the values will
     * override what is in current data object.
     *
     * @param data
     */
    public void mergeTo(ProcessingData data) {
        this.dataMap.putAll(data.dataMap);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            sb.append(entry.getKey())
                    .append("->")
                    .append(entry.getValue())
                    .append(", ");
        }
        return sb.toString();
    }

    public String toJson(){
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            System.out.println("Got json exception" + e.getMessage());
            return "";
        }
    }

    public static ProcessingData fromJson(String json){
        try {
            return mapper.readValue(json, ProcessingData.class);
        } catch (JsonProcessingException e) {
            System.out.println("Got json exception" + e.getMessage());
            return new ProcessingData();
        }
    }

}