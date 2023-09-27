package it.ness.queryable.model.quex;

import java.util.Arrays;

public class QeexWebExceptionMethod {

    public int id;
    public int code;
    public String methodName;
    public String message;
    public String[] argumemnts;

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
                ", methodName='" + methodName + '\'' +
                ", message='" + message + '\'' +
                ", argumemnts=" + Arrays.toString(argumemnts) +
                '}';
    }
}
