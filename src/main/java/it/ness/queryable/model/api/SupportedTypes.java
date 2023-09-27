package it.ness.queryable.model.api;

import java.util.HashSet;
import java.util.Set;

public class SupportedTypes {

    private static Set<String> objTypes;

    public static Set<String> getObjTypes() {
        if (null == objTypes) {
            objTypes = new HashSet<>();
            objTypes.add("String");
            objTypes.add("Integer");
            objTypes.add("Boolean");
            objTypes.add("BigDecimal");
            objTypes.add("LocalDateTime");
        }
        return objTypes;
    }
}
