package it.ness.queryable.model;

import org.jboss.forge.roaster.model.source.*;

import java.util.List;

public class QApi {
    public FieldSource<JavaClassSource> field;
    public EnumConstantSource enumField;
    public List<AnnotationSource<JavaClassSource>> annotations;
    public boolean isId = false;

    @Override
    public String toString() {
        return "QApi{" +
                "field=" + field +
                ", enumField=" + enumField +
                ", annotations=" + annotations +
                ", isId=" + isId +
                '}';
    }
}
