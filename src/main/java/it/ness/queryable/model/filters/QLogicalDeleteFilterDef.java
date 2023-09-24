package it.ness.queryable.model.filters;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import it.ness.queryable.util.FilterUtils;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.HashSet;
import java.util.Set;

public class QLogicalDeleteFilterDef extends FilterDefBase {

    protected static String ANNOTATION_NAME = "QLogicalDelete";
    protected static String PREFIX = "obj";

    public QLogicalDeleteFilterDef(final Log log) {
        super(log);
    }

    @Override
    public void addAnnotationToModelClass(JavaClassSource javaClass) {
        // remove existing annotation with same filtername
        removeFilterDef(javaClass, filterName);

        AnnotationSource<JavaClassSource> filterDefAnnotation = FilterUtils.addFilterDef(javaClass, filterName);
        FilterUtils.addParamDef(filterDefAnnotation, name, type);
        FilterUtils.addFilter(javaClass, filterName, String.format("%s = :%s", name, name));
    }

    @Override
    public QLogicalDeleteFilterDef parseQFilterDef(String entityName, FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
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

        QLogicalDeleteFilterDef fd = new QLogicalDeleteFilterDef(log);
        fd.entityName = entityName;
        fd.prefix = prefix;
        fd.name = name;
        fd.fieldType = getTypeFromFieldType(fieldType);
        fd.type = getTypeFromFieldType(fieldType);
        fd.filterName = entityName + "." + prefix + "." + name;
        fd.queryName = prefix + "." + name;
        fd.condition = condition;
        if (null != options) {
            fd.options = QOption.from(options);
        }
        return fd;
    }

    @Override
    public String getSearchMethod() {
        String formatBody = "if (nn(\"%s\")) {" +
                 "Boolean valueof = _boolean(\"%s\");" +
                 "search.filter(\"%s\", Parameters.with(\"%s\", valueof));" +
                 "} else {" +
                 "search.filter(\"%s\", Parameters.with(\"%s\", true));" +
                 "}";

        return String.format(formatBody, queryName, queryName, filterName, name, filterName, name);
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
            case "boolean":
                return "boolean";
        }
        log.error("unknown getTypeFromFieldType from :" + fieldType);
        return null;
    }

    private Set<String> getSupportedTypes() {
        Set<String> supported = new HashSet<>();
        supported.add("boolean");
        return supported;
    }

}
