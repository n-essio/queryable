package it.ness.queryable.model;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import it.ness.queryable.util.FilterUtils;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.HashSet;
import java.util.Set;

public class QFilterDef extends FilterDefBase {

    protected static String ANNOTATION_NAME = "Q";
    protected static String PREFIX = "obj";

    public QFilterDef(final Log log) {
        super(log);
    }

    @Override
    public void addAnnotationToModelClass(JavaClassSource javaClass) {
        if ("LocalDateTime".equals(fieldType) || "LocalDate".equals(fieldType) || "Date".equals(fieldType)) {
            addAnnotationToModelClass_date(javaClass);
            return;
        }
        addAnnotationToModelClass_obj(javaClass);
    }

    public void addAnnotationToModelClass_obj(JavaClassSource javaClass) {
        // remove existing annotation with same filtername
        removeFilterDef(javaClass, filterName);

        AnnotationSource<JavaClassSource> filterDefAnnotation = FilterUtils.addFilterDef(javaClass, filterName);
        FilterUtils.addParamDef(filterDefAnnotation, name, type);
        FilterUtils.addFilter(javaClass, filterName, String.format("%s = :%s", name, name));
    }

    public void addAnnotationToModelClass_date(JavaClassSource javaClass) {
        String filterNameFrom = "from." + name;
        String filterNameTo = "to." + name;
        String filterNameObj = "obj." + name;
        removeFilterDef(javaClass, filterNameFrom);
        removeFilterDef(javaClass, filterNameTo);
        removeFilterDef(javaClass, filterNameObj);

        AnnotationSource<JavaClassSource> filterDefAnnotationObj = FilterUtils.addFilterDef(javaClass, filterNameObj);
        FilterUtils.addParamDef(filterDefAnnotationObj, name, type);
        FilterUtils.addFilter(javaClass, filterNameObj, String.format("%s = :%s", name, name));

        AnnotationSource<JavaClassSource> filterDefAnnotationFrom = FilterUtils.addFilterDef(javaClass, filterNameFrom);
        FilterUtils.addParamDef(filterDefAnnotationFrom, name, type);
        FilterUtils.addFilter(javaClass, filterNameFrom, String.format("%s >= :%s", name, name));

        AnnotationSource<JavaClassSource> filterDefAnnotationTo = FilterUtils.addFilterDef(javaClass, filterNameTo);
        FilterUtils.addParamDef(filterDefAnnotationTo, name, type);
        FilterUtils.addFilter(javaClass, filterNameTo, String.format("%s <= :%s", name, name));
    }

    @Override
    public QFilterDef parseQFilterDef(FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
        AnnotationSource<JavaClassSource> a = f.getAnnotation(ANNOTATION_NAME);
        if (null == a) {
            if (!qClassLevelAnnotation) {
                return null;
            }
        }
        String prefix = getQAnnotationValue(a, "prefix", PREFIX);
        String name = getQAnnotationValue(a, "name", f.getName());
        String condition = getQAnnotationValue(a, "condition", null);
        String options = getQAnnotationValue(a, "options", null);

        String fieldType = f.getType().getName();
        // treat enums as strings
        if (f.getAnnotation("Enumerated") != null) {
            fieldType = "String";
        }
        Set<String> supportedTypes = getSupportedTypes();
        // return null if type is not supported
        if (!supportedTypes.contains(fieldType)) {
            log.error(String.format("%s is not applicable for fieldType: %s fieldName: %s", ANNOTATION_NAME, fieldType, name));
            return null;
        }

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

    private String getStringSearchMethod() {
        if (null == condition) {
            String formatBody = "if (nn(\"%s\")) {" +
                    "search.filter(\"%s\", Parameters.with(\"%s\", get(\"%s\")));" +
                    "}";
            return String.format(formatBody, filterName, filterName, name, filterName);
        }
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\", Parameters.with(\"%s\", \"%s\"));" +
                "}";
        return String.format(formatBody, filterName, filterName, name, condition);
    }

    private String getIntegerSearchMethod() {
        String formatBody = "if (nn(\"%s\")) {" +
                "Integer numberof = _integer(\"%s\");" +
                "search.filter(\"%s\", Parameters.with(\"%s\", numberof));" +
                "}";
        return String.format(formatBody, filterName, filterName, filterName, name);
    }

    private String getLongSearchMethod() {
        String formatBody = "if (nn(\"%s\")) {" +
                "Long numberof = _long(\"%s\");" +
                "search.filter(\"%s\", Parameters.with(\"%s\", numberof));" +
                "}";
        return String.format(formatBody, filterName, filterName, filterName, name);
    }

    private String getBooleanSearchMethod() {
        if (null == condition) {
            String formatBody = "if (nn(\"%s\")) {" +
                    "Boolean valueof = _boolean(\"%s\");" +
                    "search.filter(\"%s\", Parameters.with(\"%s\", valueof));" +
                    "}";
            return String.format(formatBody, filterName, filterName, filterName, name);
        }
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\", Parameters.with(\"%s\", %s));" +
                "}";
        return String.format(formatBody, filterName, filterName, filterName, condition);
    }

    private String getBigDecimalSearchMethod() {
        String formatBody = "if (nn(\"%s\")) {" +
                "BigDecimal numberof = new BigDecimal(get(\"%s\"));" +
                "search.filter(\"%s\", Parameters.with(\"%s\", numberof));" +
                "}";
        return String.format(formatBody, filterName, filterName, filterName, name);
    }

    private String getLocalDateSearchMethod() {
        StringBuilder sb = new StringBuilder();
        String formatBody = "if (nn(\"%s\")) {" +
                "LocalDate date = LocalDate.parse(get(\"%s\"));" +
                "search.filter(\"%s\", Parameters.with(\"%s\", date));" +
                "}";
        String filterName = "from." + name;
        sb.append(String.format(formatBody, filterName, filterName, filterName, name));
        filterName = "to." + name;
        sb.append(String.format(formatBody, filterName, filterName, filterName, name));
        return sb.toString();
    }

    private String getLocalDateTimeSearchMethod() {
        StringBuilder sb = new StringBuilder();
        String formatBody = "if (nn(\"%s\")) {" +
                "LocalDateTime date = LocalDateTime.parse(get(\"%s\"));" +
                "search.filter(\"%s\", Parameters.with(\"%s\", date));" +
                "}";
        String filterName = "from." + name;
        sb.append(String.format(formatBody, filterName, filterName, filterName, name));
        filterName = "to." + name;
        sb.append(String.format(formatBody, filterName, filterName, filterName, name));
        return sb.toString();
    }

    private String getDateSearchMethod() {
        StringBuilder sb = new StringBuilder();
        String formatBody = "if (nn(\"%s\")) {" +
                "Date date = DateUtils.parse(get(\"%s\"));" +
                "search.filter(\"%s\", Parameters.with(\"%s\", date));" +
                "}";
        String filterName = "from." + name;
        sb.append(String.format(formatBody, filterName, filterName, filterName, name));
        filterName = "to." + name;
        sb.append(String.format(formatBody, filterName, filterName, filterName, name));
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
            switch (fieldType) {
                case "boolean":
                    return getBooleanEqualsWithoutParameters();
            }
            return getStringEqualsWithoutParameters();
        }
        switch (fieldType) {
            case "string":
                return getStringSearchMethod();
            case "LocalDateTime":
                return getLocalDateTimeSearchMethod();
            case "LocalDate":
                return getLocalDateSearchMethod();
            case "Date":
                return getDateSearchMethod();
            case "boolean":
                return getBooleanSearchMethod();
            case "big_decimal":
                return getBigDecimalSearchMethod();
            case "int":
                return getIntegerSearchMethod();
            case "long":
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
            String formatBody = "search.filter(\"%s\", Parameters.with(\"%s\", get(\"%s\")));";
            return String.format(formatBody, filterName, name, filterName);
        }
        String formatBody = "search.filter(\"%s\", Parameters.with(\"%s\", \"%s\"));";
        return String.format(formatBody, filterName, name, condition);
    }

    private String getStringEqualsWithoutParameters() {
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\");" +
                "}";
        return String.format(formatBody, filterName, filterName);
    }

    private String getBooleanEqualsAlways() {
        if (null == condition) {
            String formatBody = "search.filter(\"%s\", Parameters.with(\"%s\", get(\"%s\")));";
            return String.format(formatBody, filterName, name, filterName);
        }
        String formatBody = "search.filter(\"%s\", Parameters.with(\"%s\", %s));";
        return String.format(formatBody, filterName, name, condition);
    }

    private String getBooleanEqualsWithoutParameters() {
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
            case "Date":
                return "Date";
            case "Boolean":
            case "boolean":
                return "boolean";
            case "BigDecimal":
                return "big_decimal";
            case "Integer":
                return "int";
            case "Long":
                return "long";
        }
        log.error("unknown getTypeFromFieldType from :" + fieldType);
        return null;
    }

    private Set<String> getSupportedTypes() {
        Set<String> supported = new HashSet<>();
        supported.add("String");
        supported.add("Integer");
        supported.add("Long");
        supported.add("Boolean");
        supported.add("boolean");
        supported.add("BigDecimal");
        supported.add("LocalDateTime");
        supported.add("LocalDate");
        supported.add("Date");
        return supported;
    }

}
