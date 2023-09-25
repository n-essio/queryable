package it.ness.queryable.model.predicates;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.HashSet;
import java.util.Set;

import static it.ness.queryable.builder.Constants.Q_ANNOTATION_NAME;
import static it.ness.queryable.builder.Constants.OBJ_PREFIX;

public class QFilter extends FilterBase {


    public QFilter(final Log log) {
        super(log);
    }

    @Override
    public QFilter parseQFilter(String entityName, FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
        AnnotationSource<JavaClassSource> a = f.getAnnotation(Q_ANNOTATION_NAME);
        if (null == a) {
            if (!qClassLevelAnnotation) {
                return null;
            }
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
        // return null if type is not supported
        if (!enumerated && !supportedTypes.contains(fieldType)) {
            log.error(String.format("%s is not applicable for fieldType: %s fieldName: %s", Q_ANNOTATION_NAME, fieldType, name));
            return null;
        }

        QFilter fd = new QFilter(log);
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

    private String getStringSearchMethod() {
        if (null == condition) {
            String formatBody4 = """
                    if (nn("%s")) {
                        predicates.add(criteriaBuilder.equal(root.get("%s"), get("%s")));
                    }
                    """;
            return String.format(formatBody4, queryName, name, queryName);
        }
        String formatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.equal(root.get("%s"), "%s"));
                }
                """;
        return String.format(formatBody4, queryName, name, condition);
    }

    private String getIntegerSearchMethod() {
        String formatBody = "if (nn(\"%s\")) {" +
                "Integer numberof = _integer(\"%s\");" +
                "search.filter(\"%s\", Parameters.with(\"%s\", numberof));" +
                "}";
        String formatBody4 = """
                if (nn("%s")) {
                    Integer numberof = _integer("%s");
                    predicates.add(criteriaBuilder.equal(root.get("%s"), numberof));
                }
                """;
        return String.format(formatBody4, queryName, queryName, name);
    }

    private String getLongSearchMethod() {
        String formatBody4 = """
                if (nn("%s")) {
                    Long numberof = _long("%s");
                    predicates.add(criteriaBuilder.equal(root.get("%s"), numberof));
                }
                """;
        return String.format(formatBody4, queryName, queryName, name);
    }

    private String getBooleanSearchMethod() {
        if (null == condition) {
            String formatBody4 = """
                    if (nn("%s")) {
                        Boolean valueof = _boolean("%s");
                        predicates.add(criteriaBuilder.equal(root.get("%s"), valueof));
                    }
                    """;
            return String.format(formatBody4, queryName, queryName, name);
        }
        String formatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.equal(root.get("%s"), %s));
                }
                """;
        return String.format(formatBody4, queryName, queryName, condition);
    }

    private String getElementCollectionSearchMethod() {
        String formatBody4 = """
                if (nn("%s")) {
                    Expression<List<String>> tags = root.get("%s");
                    predicates.add(criteriaBuilder.isMember(get("%s"), tags));
                }
                """;
        return String.format(formatBody4, queryName, name, queryName);
    }

    private String getBigDecimalSearchMethod() {
        String formatBody4 = """
                if (nn("%s")) {
                    BigDecimal numberof = _bigdecimal("%s");
                    predicates.add(criteriaBuilder.equal(root.get("%s"), numberof));
                }
                """;
        return String.format(formatBody4, queryName, queryName, name);
    }

    private String getBigIntegerSearchMethod() {
        String formatBody4 = """
                if (nn("%s")) {
                    BigInteger numberof = _biginteger("%s");
                    predicates.add(criteriaBuilder.equal(root.get("%s"), numberof));
                }
                """;
        return String.format(formatBody4, queryName, queryName, name);
    }

    private String getLocalDateSearchMethod() {
        StringBuilder sb = new StringBuilder();
        String fromFormatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("%s"), _localDate("%s")));
                }
                    """;
        String toFormatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("%s"), _localDate("%s")));
                }
                        """;
        String eqFormatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.equal(root.get("%s"), _localDate("%s")));
                }    
                """;
        String queryName = "from." + name;
        sb.append(String.format(fromFormatBody4, queryName, name, queryName));
        queryName = "to." + name;
        sb.append(String.format(toFormatBody4, queryName, name, queryName));
        queryName = "obj." + name;
        sb.append(String.format(eqFormatBody4, queryName, name, queryName));
        return sb.toString();
    }

    private String getLocalDateTimeSearchMethod() {
        StringBuilder sb = new StringBuilder();
        String fromFormatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("%s"), _localDateTime("%s")));
                }
                    """;
        String toFormatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("%s"), _localDateTime("%s")));
                }
                        """;
        String queryName = "from." + name;
        sb.append(String.format(fromFormatBody4, queryName, name, queryName));
        queryName = "to." + name;
        sb.append(String.format(toFormatBody4, queryName, name, queryName));
        return sb.toString();
    }

    private String getZonedDateTimeSearchMethod() {
        StringBuilder sb = new StringBuilder();
        String fromFormatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("%s"), _zonedDateTime("%s")));
                }
                    """;
        String toFormatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("%s"), _zonedDateTime("%s")));
                }
                        """;
        String queryName = "from." + name;
        sb.append(String.format(fromFormatBody4, queryName, name, queryName));
        queryName = "to." + name;
        sb.append(String.format(toFormatBody4, queryName, name, queryName));
        return sb.toString();
    }

    private String getDateSearchMethod() {
        StringBuilder sb = new StringBuilder();
        String fromFormatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("%s"), _date("%s")));
                }
                    """;
        String toFormatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("%s"), _date("%s")));
                }
                        """;
        String eqFormatBody4 = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.equal(root.get("%s"), _date("%s")));
                }
                """;
        String queryName = "from." + name;
        sb.append(String.format(fromFormatBody4, name, queryName));
        queryName = "to." + name;
        sb.append(String.format(toFormatBody4, name, queryName));
        queryName = "obj." + name;
        sb.append(String.format(eqFormatBody4, name, queryName));
        return sb.toString();
    }

    @Override
    public String getSearchMethod() {
        if (containsOption(QOption.EXECUTE_ALWAYS)) {
            switch (fieldType) {
                case "boolean":
                    return getBooleanEqualsAlways();
            }
            return getStringEqualsAlways();
        }
        if (containsOption(QOption.WITHOUT_PARAMETERS)) {
            return getBooleanEqualsWithoutParameters();
        }
        switch (fieldType) {
            case "List":
            case "Set":
                return getElementCollectionSearchMethod();
            case "String":
                return getStringSearchMethod();
            case "LocalDateTime":
                return getLocalDateTimeSearchMethod();
            case "ZonedDateTime":
                return getZonedDateTimeSearchMethod();
            case "LocalDate":
                return getLocalDateSearchMethod();
            case "Date":
                return getDateSearchMethod();
            case "boolean":
                return getBooleanSearchMethod();
            case "Boolean":
                return getBooleanSearchMethod();
            case "BigDecimal":
                return getBigDecimalSearchMethod();
            case "BigInteger":
                return getBigIntegerSearchMethod();
            case "int":
                return getIntegerSearchMethod();
            case "long":
                return getLongSearchMethod();
            case "Number":
                return getLongSearchMethod();
        }
        log.error("not handled getSearchMethod for fieldType: " + fieldType + " name: " + name);
        return "";
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.POSTQUERY;
    }

    @Override
    public boolean overrideOnSameFilterName() {
        return true;
    }

    private String getStringEqualsAlways() {
        if (null == condition) {
            String formatBody4 = """
                    predicates.add(criteriaBuilder.equal(root.get("%s"), get("%s")));
                    """;
            return String.format(formatBody4, name, queryName);
        }
        String formatBody4 = """
                   predicates.add(criteriaBuilder.equal(root.get("%s"), %s));
                """;
        return String.format(formatBody4, name, condition);
    }

    private String getBooleanEqualsAlways() {
        if (null == condition) {
            String formatBody4 = """
                        Boolean valueof = _boolean("%s");
                        predicates.add(criteriaBuilder.equal(root.get("%s"), valueof));
                    """;
            return String.format(formatBody4, queryName, queryName, name);
        }
        String formatBody4 = """
                    predicates.add(criteriaBuilder.equal(root.get("%s"), %s));
                """;
        return String.format(formatBody4, queryName, queryName, condition);
    }

    private String getBooleanEqualsWithoutParameters() {
        String formatBody = """
                if (nn("%s")) {
                    predicates.add(criteriaBuilder.equal(root.get("%s"), true));
                }""";
        return String.format(formatBody, queryName, name);
    }

    private Set<String> getSupportedTypes() {
        Set<String> supported = new HashSet<>();
        supported.add("String");
        supported.add("Integer");
        supported.add("int");
        supported.add("Long");
        supported.add("long");
        supported.add("Boolean");
        supported.add("boolean");
        supported.add("BigDecimal");
        supported.add("BigInteger");
        supported.add("LocalDateTime");
        supported.add("ZonedDateTime");
        supported.add("LocalDate");
        supported.add("Date");
        supported.add("Set");
        supported.add("List");
        return supported;
    }

}
