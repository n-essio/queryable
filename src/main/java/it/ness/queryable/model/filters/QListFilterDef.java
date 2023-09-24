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

public class QListFilterDef extends FilterDefBase {

    protected static String ANNOTATION_NAME = "QList";
    protected static String PREFIX = "obj";
    protected String nameInPlural;

    public QListFilterDef(final Log log) {
        super(log);
    }

    @Override
    public void addAnnotationToModelClass(JavaClassSource javaClass) {
        nameInPlural = null;
        if (name.contains("_")) {
            nameInPlural = stringUtil.getPlural(name.substring(name.indexOf("_") + 1));
            nameInPlural = name.substring(0, name.indexOf("_") + 1) + nameInPlural;
        } else {
            nameInPlural = stringUtil.getPlural(name);
        }
        filterName = entityName + "." + prefix + "." + nameInPlural;
        queryName = prefix + "." + nameInPlural;
        // remove existing annotation with same filtername
        removeFilterDef(javaClass, filterName);
        AnnotationSource<JavaClassSource> filterDefAnnotation = FilterUtils.addFilterDef(javaClass, filterName);
        FilterUtils.addParamDef(filterDefAnnotation, nameInPlural, "string");
        if (null == condition) {
            FilterUtils.addFilter(javaClass, filterName, String.format("%s IN (:%s)", name, nameInPlural));
        } else {
            FilterUtils.addFilter(javaClass, filterName, condition);
        }
    }

    @Override
    public QListFilterDef parseQFilterDef(String entityName, FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
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

        QListFilterDef fd = new QListFilterDef(log);
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

//    @Override
//    public String getSearchMethod() {
//        String formatBody = "if (nn(\"%s\")) {" +
//                "String[] %s = get(\"%s\").split(\",\");" +
//                "getEntityManager().unwrap(Session.class)" +
//                "         .enableFilter(\"%s\")" +
//                "         .setParameterList(\"%s\", %s);" +
//                "}";
//        return String.format(formatBody, filterName, nameInPlural, filterName, filterName, nameInPlural, nameInPlural);
//    }

    @Override
    public String getSearchMethod() {
        switch (fieldType) {
            case "string":
                return getStringSearchMethod();
            case "integer":
                return getIntegerSearchMethod();
            case "long":
                return getLongSearchMethod();
            case "big_integer":
                return getBigIntegerSearchMethod();
        }
        log.error("not handled getSearchMethod for fieldType: " + fieldType + " name: " + name);
        return "";
    }

    /*
     if (nn("obj.uuids")) {
            search.filter("obj.uuids", Parameters.with("uuids", asList("obj.uuids")));
        }
     */

    private String getStringSearchMethod() {
        if (null == condition) {
            String formatBody = "if (nn(\"%s\")) {" +
                    "search.filter(\"%s\", Parameters.with(\"%s\", asList(\"%s\")));" +
                    "}";
            return String.format(formatBody, queryName, filterName, nameInPlural, queryName);
        }
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\", Parameters.with(\"%s\", \"%s\"));" +
                "}";
        return String.format(formatBody, queryName, filterName, nameInPlural, condition);
    }

    private String getLongSearchMethod() {
        if (null == condition) {
            String formatBody = "if (nn(\"%s\")) {" +
                    "search.filter(\"%s\", Parameters.with(\"%s\", asLongList(\"%s\")));" +
                    "}";
            return String.format(formatBody, queryName, filterName, nameInPlural, queryName);
        }
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\", Parameters.with(\"%s\", \"%s\"));" +
                "}";
        return String.format(formatBody, queryName, filterName, nameInPlural, condition);
    }

    private String getIntegerSearchMethod() {
        if (null == condition) {
            String formatBody = "if (nn(\"%s\")) {" +
                    "search.filter(\"%s\", Parameters.with(\"%s\", asIntegerList(\"%s\")));" +
                    "}";
            return String.format(formatBody, queryName, filterName, nameInPlural, queryName);
        }
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\", Parameters.with(\"%s\", \"%s\"));" +
                "}";
        return String.format(formatBody, queryName, filterName, nameInPlural, condition);
    }

    private String getBigIntegerSearchMethod() {
        if (null == condition) {
            String formatBody = "if (nn(\"%s\")) {" +
                    "search.filter(\"%s\", Parameters.with(\"%s\", Arrays.stream(get(\"%s\").split(\",\")).map(number -> BigInteger.valueOf(Long.parseLong(number))).toList()));" +
                    "}";
            return String.format(formatBody, queryName, filterName, nameInPlural, queryName);
        }
        String formatBody = "if (nn(\"%s\")) {" +
                "search.filter(\"%s\", Parameters.with(\"%s\", \"%s\"));" +
                "}";
        return String.format(formatBody, queryName, filterName, nameInPlural, condition);
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
            case "Integer":
                return "int";
            case "Long":
                return "long";
            case "BigInteger":
                return "big_integer";
        }
        log.error("unknown getTypeFromFieldType from :" + fieldType);
        return null;
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
