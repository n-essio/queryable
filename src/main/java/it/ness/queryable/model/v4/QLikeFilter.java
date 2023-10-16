package it.ness.queryable.model.v4;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.HashSet;
import java.util.Set;

import static it.ness.queryable.builder.Constants.LIKE_PREFIX;
import static it.ness.queryable.builder.Constants.QLIKE_ANNOTATION_NAME;

public class QLikeFilter extends FilterBase {


    public QLikeFilter(final Log log) {
        super(log);
    }

    @Override
    public QLikeFilter parseQFilter(String entityName, FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
        AnnotationSource<JavaClassSource> a = f.getAnnotation(QLIKE_ANNOTATION_NAME);
        if (null == a) {
            return null;
        }
        String prefix = getQAnnotationValue(a, "prefix", LIKE_PREFIX);
        String name = getQAnnotationValue(a, "name", f.getName());
        String condition = getQAnnotationValue(a, "condition", null);
        String options = getQAnnotationValue(a, "options", null);

        String fieldType = f.getType().getName();
        if (f.getAnnotation("Enumerated") != null) {
            enumerated = true;
        }
        Set<String> supportedTypes = getSupportedTypes();
        // return null if type is not supported
        if (!enumerated && !supportedTypes.contains(fieldType)) {
            log.error(String.format("%s is not applicable for fieldType: %s fieldName: %s", QLIKE_ANNOTATION_NAME, fieldType, name));
            return null;
        }

        QLikeFilter fd = new QLikeFilter(log);
        fd.entityName = entityName;
        fd.prefix = prefix;
        fd.name = name;
        fd.fieldType = fieldType;
        fd.queryName = prefix + "." + name;
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
        String formatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("%s")), likeParamToLowerCase("%s")));
                }
                """;
        return String.format(formatBody4, queryName, name, queryName);
    }

    private String getStringEqualsAlways() {
        String formatBody4 = """
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("%s")), likeParamToLowerCase("%s")));
                """;
        return String.format(formatBody4, name, queryName);
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
        if ("String".equals(fieldType)) {
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
