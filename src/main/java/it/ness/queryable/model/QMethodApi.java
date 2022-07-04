package it.ness.queryable.model;

import org.jboss.forge.roaster.model.source.*;

import java.util.List;

public class QMethodApi {

    public List<ParameterSource<JavaClassSource>> methodParameters;
    public List<AnnotationSource<JavaClassSource>> methodAnnotations;
    public String rsPath;
    public String methodPath;

    @Override
    public String toString() {
        return "QMethodApi{" +
                "methodParameters=" + methodParameters +
                ", methodAnnotations=" + methodAnnotations +
                ", rsPath='" + rsPath + '\'' +
                ", methodPath='" + methodPath + '\'' +
                '}';
    }
}
