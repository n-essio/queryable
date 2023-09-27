package io.quarkus.qeex.api.exceptions;

public class QeexWebException extends Exception {
    public String projectName;
    public int id;
    public int code;
    public String message;
    public String language;

    protected QeexWebException() {
    }

    public static QeexWebException builder(String projectName) {
        var quex = new QeexWebException();
        quex.projectName = projectName;
        return quex;
    }

    public QeexWebException id(int id) {
        this.id = id;
        return this;
    }

    public QeexWebException code(int code) {
        this.code = code;
        return this;
    }

    public QeexWebException message(String message) {
        this.message = message;
        return this;
    }

    public QeexWebException language(String language) {
        this.language = language;
        return this;
    }


    public String toJson() {
        return "{\"projectName\":\"" + this.projectName + "\"," +
                "\"id\":" + this.id + ", " +
                "\"code\":" + this.code + ", " +
                "\"message\":\"" + this.message + "\", " +
                "\"language\":\"" + this.language + "\"" +
                "}";
    }

    @Override
    public String toString() {
        return "QuarkusWebException{" +
                "projectName='" + projectName + '\'' +
                ", id=" + id +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
