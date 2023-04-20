package simplefsm;

import java.util.HashMap;
import java.util.Map;

public class ProcessingData {
    private final Map<String, Object> dataMap;
    private String nextState;

    // If state machine has exception info.
    private ExceptionInfo exceptionInfo;

    public ProcessingData() {
        this.dataMap = new HashMap<>();
        this.nextState = null;
    }

    public void set(String key, Object value) {
        dataMap.put(key, value);
    }

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

    public String getNextState() {
        return nextState;
    }

    public void setNextState(String nextState) {
        this.nextState = nextState;
    }

    public void setExceptionInfo(ExceptionInfo exceptionInfo){
        this.exceptionInfo = exceptionInfo;
    }
}