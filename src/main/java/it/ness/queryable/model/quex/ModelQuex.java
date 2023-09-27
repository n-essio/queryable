package it.ness.queryable.model.quex;

import it.ness.queryable.model.api.Parameters;
import it.ness.queryable.model.quex.QeexWebExceptionMethod;
import org.apache.maven.plugin.logging.Log;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModelQuex {
    public boolean isParsingSuccessful;

    public String project = null;
    public int defaultId;
    public int defaultCode;
    // from application.properties
    public Map<Integer, Map<String, String>> messages = new LinkedHashMap<>();
    public Map<Integer, QeexWebExceptionMethod> qeexWebExceptionMethods = new LinkedHashMap<>();
    public Map<String, QeexWebExceptionMethod[]> interfaces = new LinkedHashMap<>();
    public Log log;
    public Parameters parameters;
    public String packageName;

    public ModelQuex(Log log, Parameters parameters, String packageName) {
        this.log = log;
        this.parameters = parameters;
        this.packageName = packageName;
        isParsingSuccessful = false;
    }
}
