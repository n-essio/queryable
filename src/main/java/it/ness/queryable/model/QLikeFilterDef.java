package it.ness.queryable.model;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.HashSet;
import java.util.Set;

public class QLikeFilterDef extends FilterDefBase {

    protected static String ANNOTATION_NAME = "QLike";
    protected static String PREFIX = "like";

    public QLikeFilterDef(final Log log) {
        super(log);
    }

    @Override
    public void addAnnotationToModelClass(JavaClassSource javaClass) {
        // remove existing annotation with same filtername
        removeFilterDef(javaClass, filterName);
        AnnotationSource<JavaClassSource> filterDefAnnotation = javaClass.addAnnotation();
        filterDefAnnotation.setName("FilterDef");
        filterDefAnnotation.setStringValue("name", filterName);
        AnnotationSource<JavaClassSource> paramAnnotation = filterDefAnnotation.addAnnotationValue("parameters");
        paramAnnotation.setName("ParamDef");
        paramAnnotation.setStringValue("name", name);
        paramAnnotation.setStringValue("type", type);

        AnnotationSource<JavaClassSource> filterAnnotation = javaClass.addAnnotation();
        filterAnnotation.setName("Filter");
        filterAnnotation.setStringValue("name", filterName);
        filterAnnotation.setStringValue("condition", String.format("lower(%s) LIKE :%s", name, name));
    }

    @Override
    public QLikeFilterDef parseQFilterDef(FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
        AnnotationSource<JavaClassSource> a = f.getAnnotation(ANNOTATION_NAME);
        if (null == a) {
            return null;
        }
        String prefix = getQAnnotationValue(a, "prefix", PREFIX);
        String name = getQAnnotationValue(a, "name", f.getName());
        String condition = getQAnnotationValue(a, "condition", null);
        String options = getQAnnotationValue(a, "options", null);

        String fieldType = f.getType().getName();
        Set<String> supportedTypes = getSupportedTypes();
        // return null if type is not supported
        if (!supportedTypes.contains(fieldType)) {
            log.error(String.format("%s is not applicable for fieldType: %s fieldName: %s", ANNOTATION_NAME, fieldType, name));
            return null;
        }

        QLikeFilterDef fd = new QLikeFilterDef(log);
        fd.prefix = prefix;
        fd.name = name;
        fd.fieldType = getTypeFromFieldType(fieldType);
        fd.type = getTypeFromFieldType(fieldType);
        fd.filterName = prefix + "." + name;
        fd.condition = condition;
        if (null != options) {
            fd.options = QOption.from(options);
        }
        return fd;
    }

    @Override
    public String getSearchMethod() {
        if (containsOption(QOption.EXECUTE_ALWAYS)) {
            return getStringEqualsAlways();
        }
        if (containsOption(QOption.WITHOUT_PARAMETERS)) {
            return getStringEqualsWithoutParameters();
        }
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\", Parameters.with(\"%s\", likeParamToLowerCase(\"%s\")));" +
                "}";
        return String.format(formatBody, filterName, filterName, name, filterName);
    }

    private String getStringEqualsAlways() {
        String formatBody = "search.filter(\"%s\", Parameters.with(\"%s\", likeParamToLowerCase(\"%s\")));";
        return String.format(formatBody, filterName, name, filterName);
    }

    private String getStringEqualsWithoutParameters() {
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\");" +
                "}";
        return String.format(formatBody, filterName, filterName);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.POSTQUERY;
    }

    @Override
    public boolean overrideOnSameFilterName() {
        return true;
    }

    private String getTypeFromFieldType(final String fieldType) {
        switch (fieldType) {
            case "String":
                return "string";
        }
        log.error("unknown getTypeFromFieldType from :" + fieldType);
        return null;
    }

    private Set<String> getSupportedTypes() {
        Set<String> supported = new HashSet<>();
        supported.add("String");
        return supported;
    }

}
