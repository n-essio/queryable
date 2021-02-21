package it.ness.queryable.util;

import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

public class FilterUtils {

    public static AnnotationSource<JavaClassSource> addFilterDef(JavaClassSource javaClass, String filterName) {
        AnnotationSource<JavaClassSource> filterDefAnnotation = javaClass.addAnnotation();
        filterDefAnnotation.setName("FilterDef");
        filterDefAnnotation.setStringValue("name", filterName);
        return filterDefAnnotation;
    }

    public static AnnotationSource<JavaClassSource> addFilter(JavaClassSource javaClass, String filterName, String condition) {
        AnnotationSource<JavaClassSource> filterAnnotation = javaClass.addAnnotation();
        filterAnnotation.setName("Filter");
        filterAnnotation.setStringValue("name", filterName);
        filterAnnotation.setStringValue("condition", condition);
        return filterAnnotation;
    }

    public static void addParamDef(AnnotationSource<JavaClassSource> filterDefAnnotation, String name, String type) {
        AnnotationSource<JavaClassSource> paramAnnotation = filterDefAnnotation.addAnnotationValue("parameters");
        paramAnnotation.setName("ParamDef");
        paramAnnotation.setStringValue("name", name);
        paramAnnotation.setStringValue("type", type);
    }
}
