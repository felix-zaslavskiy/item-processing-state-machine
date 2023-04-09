import java.util.HashMap;
import java.util.Map;

public class ProcessingData {
    private Map<String, Object> dataMap;

    public ProcessingData() {
        this.dataMap = new HashMap<>();
    }

    public void set(String key, Object value) {
        dataMap.put(key, value);
    }

    public Object get(String key) {
        return dataMap.get(key);
    }
}