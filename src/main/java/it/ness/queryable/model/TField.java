package it.ness.queryable.model;

public class TField {
    public String defaultValue;
    public String updatedValue;
    public String fieldName;

    @Override
    public String toString() {
        return "TField{" +
                "defaultValue='" + defaultValue + '\'' +
                ", updatedValue='" + updatedValue + '\'' +
                ", fieldName='" + fieldName + '\'' +
                '}';
    }
}
