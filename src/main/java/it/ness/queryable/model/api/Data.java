package it.ness.queryable.model.api;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Data {
    private final Map<String, Object> values = new HashMap();

    public Data() {
    }

    public Data and(String name, Object value) {
        this.values.put(name, value);
        return this;
    }

    public Map<String, Object> map() {
        return Collections.unmodifiableMap(this.values);
    }

    public static Data with(String name, Object value) {
        return (new Data()).and(name, value);
    }
}
