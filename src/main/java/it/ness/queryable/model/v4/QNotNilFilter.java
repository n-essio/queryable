package it.ness.queryable.model.v4;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.HashSet;
import java.util.Set;

import static it.ness.queryable.builder.Constants.NOTNILL_PREFIX;
import static it.ness.queryable.builder.Constants.QNOTNILL_ANNOTATION_NAME;

public class QNotNilFilter extends FilterBase {

    public QNotNilFilter(final Log log) {
        super(log);
    }

    @Override
    public QNotNilFilter parseQFilter(String entityName, FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
        AnnotationSource<JavaClassSource> a = f.getAnnotation(QNOTNILL_ANNOTATION_NAME);
        if (null == a) {
            return null;
        }
        String prefix = getQAnnotationValue(a, "prefix", NOTNILL_PREFIX);
        String name = getQAnnotationValue(a, "name", f.getName());
        String condition = getQAnnotationValue(a, "condition", null);
        String options = getQAnnotationValue(a, "options", null);

        String fieldType = f.getType().getName();
        Set<String> supportedTypes = getSupportedTypes();
        // return null if type is not supported
        if (!supportedTypes.contains(fieldType)) {
            log.error(String.format("%s is not applicable for fieldType: %s fieldName: %s", QNOTNILL_ANNOTATION_NAME, fieldType, name));
            return null;
        }

        QNotNilFilter fd = new QNotNilFilter(log);
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
        String formatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.isNull(root.get("%s")));
                }
                """;
        return String.format(formatBody4, queryName, name);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.POSTQUERY;
    }

    @Override
    public boolean overrideOnSameFilterName() {
        return true;
    }

    private Set<String> getSupportedTypes() {
        Set<String> supported = new HashSet<>();
        supported.add("String");
        supported.add("Integer");
        supported.add("Long");
        supported.add("Boolean");
        supported.add("boolean");
        supported.add("BigDecimal");
        supported.add("BigInteger");
        supported.add("LocalDateTime");
        supported.add("LocalDate");
        supported.add("Date");
        return supported;
    }

}
