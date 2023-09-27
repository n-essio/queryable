package it.ness.queryable.model.v4;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.HashSet;
import java.util.Set;

import static it.ness.queryable.builder.Constants.OBJ_PREFIX;
import static it.ness.queryable.builder.Constants.QLIST_ANNOTATION_NAME;

public class QListFilter extends FilterBase {

    public QListFilter(final Log log) {
        super(log);
    }

    @Override
    public QListFilter parseQFilter(String entityName, FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
        AnnotationSource<JavaClassSource> a = f.getAnnotation(QLIST_ANNOTATION_NAME);
        if (null == a) {
            return null;
        }
        String prefix = getQAnnotationValue(a, "prefix", OBJ_PREFIX);
        String name = getQAnnotationValue(a, "name", f.getName());
        String condition = getQAnnotationValue(a, "condition", null);
        String options = getQAnnotationValue(a, "options", null);

        String fieldType = f.getType().getName();
        if (f.getAnnotation("Enumerated") != null) {
            enumerated = true;
        }
        Set<String> supportedTypes = getSupportedTypes();
        if (!enumerated && !supportedTypes.contains(fieldType)) {
            log.error(String.format("%s is not applicable for fieldType: %s fieldName: %s", QLIST_ANNOTATION_NAME, fieldType, name));
            return null;
        }

        QListFilter fd = new QListFilter(log);
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
        switch (fieldType) {
            case "String":
                return getStringSearchMethod();
            case "Integer":
                return getIntegerSearchMethod();
            case "Long":
                return getLongSearchMethod();
            case "BigInteger":
                return getBigIntegerSearchMethod();
        }
        log.error("not handled getSearchMethod for fieldType: " + fieldType + " name: " + name);
        return "";
    }

    private String getStringSearchMethod() {
        if (null == condition) {
            String formatBody = """
                    if (nn("%s")) {
                        CriteriaBuilder.In<String> inClause = criteriaBuilder.in(root.get("%s"));
                        for (String uuid : asList("%s")) {
                            inClause.value(uuid);
                        }
                        predicates.add(inClause);
                    }
                    """;
            return String.format(formatBody, queryName, name, queryName);
        }
        String formatBody = """
                if (nn("%s")) {
                    CriteriaBuilder.In<String> inClause = criteriaBuilder.in(root.get("%s"));
                    inClause.value(%s);
                    predicates.add(inClause);
                }
                """;
        return String.format(formatBody, queryName, name, condition);
    }

    private String getLongSearchMethod() {
        if (null == condition) {
            String formatBody = """
                    if (nn("%s")) {
                        CriteriaBuilder.In<String> inClause = criteriaBuilder.in(root.get("%s"));
                        for (Long uuid : asLongList("%s")) {
                            inClause.value(uuid);
                        }
                        predicates.add(inClause);
                    }
                    """;
            return String.format(formatBody, queryName, name, queryName);
        }
        String formatBody = """
                if (nn("%s")) {
                    CriteriaBuilder.In<String> inClause = criteriaBuilder.in(root.get("%s"));
                    inClause.value(%s);
                    predicates.add(inClause);
                }
                """;
        return String.format(formatBody, queryName, name, condition);
    }

    private String getIntegerSearchMethod() {
        if (null == condition) {
            String formatBody = """
                    if (nn("%s")) {
                        CriteriaBuilder.In<String> inClause = criteriaBuilder.in(root.get("%s"));
                        for (Integer uuid : asIntegerList("%s")) {
                            inClause.value(uuid);
                        }
                        predicates.add(inClause);
                    }
                    """;
            return String.format(formatBody, queryName, name, queryName);
        }
        String formatBody = """
                if (nn("%s")) {
                    CriteriaBuilder.In<String> inClause = criteriaBuilder.in(root.get("%s"));
                    inClause.value(%s);
                    predicates.add(inClause);
                }
                """;
        return String.format(formatBody, queryName, name, condition);
    }

    private String getBigIntegerSearchMethod() {
        if (null == condition) {
            String formatBody = """
                    if (nn("%s")) {
                        CriteriaBuilder.In<String> inClause = criteriaBuilder.in(root.get("%s"));
                        for (BigInteger uuid : asBigIntegerList("%s")) {
                            inClause.value(uuid);
                        }
                        predicates.add(inClause);
                    }
                    """;
            return String.format(formatBody, queryName, name, queryName);
        }
        String formatBody = """
                if (nn("%s")) {
                    CriteriaBuilder.In<String> inClause = criteriaBuilder.in(root.get("%s"));
                    inClause.value(%s);
                    predicates.add(inClause);
                }
                """;
        return String.format(formatBody, queryName, name, condition);
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
        supported.add("BigInteger");
        return supported;
    }
}