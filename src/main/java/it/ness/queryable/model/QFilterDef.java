package it.ness.queryable.model;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import it.ness.queryable.model.pojo.SupportedTypes;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.Set;

public class QFilterDef extends FilterDefBase {

    final static String ANNOTATION_NAME = "Q";

    public QFilterDef(final Log log) {
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
        filterAnnotation.setStringValue("condition", String.format("%s = :%s", name, name));
    }

    @Override
    public QFilterDef parseQFilterDef(FieldSource<JavaClassSource> f) {
        AnnotationSource<JavaClassSource> a = f.getAnnotation(ANNOTATION_NAME);
        if (null == a) {
            return null;
        }
        String fieldType = f.getType().getName();
        Set<String> supportedTypes = SupportedTypes.getObjTypes();
        // return null if type is not supported
        if (!supportedType(type, supportedTypes)) {
            log.error("Q is not applicable for field: " + name);
            return null;
        }

        String prefix = getQAnnotationValue(a, "prefix", "obj");
        String name = getQAnnotationValue(a, "name", f.getName());
        String condition = getQAnnotationValue(a, "condition", null);
        String options = getQAnnotationValue(a, "options", null);

        QFilterDef fd = new QFilterDef(log);
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
            return getStringEqualsAlways(prefix);
        }
        if (containsOption(QOption.WITHOUT_PARAMETERS)) {
            return getStringEqualsWithoutParameters(prefix);
        }
        if (null == condition) {
            String formatBody = "if (nn(\"%s\")) {" +
                    "search.filter(\"%s\", Parameters.with(\"%s\", get(\"%s\")));" +
                    "}";
            return String.format(formatBody, filterName, filterName, name, filterName);
        }
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\", Parameters.with(\"%s\", %s));" +
                "}";
        return String.format(formatBody, filterName, filterName, name, condition);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.POSTQUERY;
    }

    @Override
    public boolean overrideOnSameFilterName() {
        return true;
    }

    private String getStringEqualsAlways(String prefix) {
        if (null == condition) {
            String formatBody = "search.filter(\"%s\", Parameters.with(\"%s\", get(\"%s\")));";
            return String.format(formatBody, filterName, name, filterName);
        }
        String formatBody = "search.filter(\"%s\", Parameters.with(\"%s\", %s));";
        return String.format(formatBody, filterName, name, condition);
    }

    private String getStringEqualsWithoutParameters(String prefix) {
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\");" +
                "}";
        return String.format(formatBody, filterName, filterName);
    }

    private String getTypeFromFieldType(final String fieldType) {
        switch (fieldType) {
            case "String":
                return "string";
            case "LocalDateTime":
                return "LocalDateTime";
            case "LocalDate":
                return "LocalDate";
            case "Boolean":
                return "boolean";
            case "BigDecimal":
                return "big_decimal";
            case "Integer":
                return "int";
        }
        log.error("unknown getTypeFromFieldType from :" + fieldType);
        return null;
    }

}
