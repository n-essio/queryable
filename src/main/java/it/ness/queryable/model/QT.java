package it.ness.queryable.model;

import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.List;

public class QT {
    public String defaultValue;
    public String updatedValue;
    public FieldSource<JavaClassSource> field;
    public List<AnnotationSource<JavaClassSource>> annotations;
    public boolean isId = false;

    @Override
    public String toString() {
        return "QT{" +
                "defaultValue='" + defaultValue + '\'' +
                ", updatedValue='" + updatedValue + '\'' +
                ", field=" + field +
                ", annotations=" + annotations +
                ", isId=" + isId +
                '}';
    }
}
