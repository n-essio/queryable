package it.ness.queryable.model.predicates;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import it.ness.queryable.model.filters.FilterDefBase;
import it.ness.queryable.util.FilterUtils;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.HashSet;
import java.util.Set;

public class QLogicalDeleteFilter extends FilterBase {

    protected static String ANNOTATION_NAME = "QLogicalDelete";
    protected static String PREFIX = "obj";

    public QLogicalDeleteFilter(final Log log) {
        super(log);
    }


    @Override
    public QLogicalDeleteFilter parseQFilter(String entityName, FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
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

        QLogicalDeleteFilter fd = new QLogicalDeleteFilter(log);
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
                    Boolean valueof = _boolean("%s");
                    predicates.add(criteriaBuilder.equal(root.get("%s"), valueof));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("%s"), true));
                }
                """;
        return String.format(formatBody4, queryName, queryName, name, name);
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
        supported.add("boolean");
        return supported;
    }

}
