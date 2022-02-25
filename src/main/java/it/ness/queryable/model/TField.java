package it.ness.queryable.model;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

public class TField {
    public String defaultValue;
    public String updatedValue;
    public FieldSource<JavaClassSource> field;
    public boolean isId = false;

    @Override
    public String toString() {
        return "TField{" +
                "defaultValue='" + defaultValue + '\'' +
                ", updatedValue='" + updatedValue + '\'' +
                ", field=" + field +
                ", isId=" + isId +
                '}';
    }
}
