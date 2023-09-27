package it.ness.queryable.model.quex;

import java.util.Map;

public class QeexWebExceptionMethod {

    public Integer id;
    public Integer code;
    public String message;
    public String methodName;
    public Map<String, String> arguments;
    public Map<String, String> msgMap;

    public QeexWebExceptionMethod() {
    }

    public QeexWebExceptionMethod(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "QeexWebExceptionMethod{" +
                "id=" + id +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", arguments=" + arguments +
                ", msgMap=" + msgMap +
                '}';
    }
}
