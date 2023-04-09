package nfsm;

import java.util.HashMap;
import java.util.Map;

public class ProcessingData {
    private Map<String, Object> dataMap;
    private String nextState;

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

    public String getNextState() {
        return nextState;
    }

    public void setNextState(String nextState) {
        this.nextState = nextState;
    }
}