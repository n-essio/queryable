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
        filterName = prefix + "." + nameInPlural;
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
    public QListFilterDef parseQFilterDef(FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
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
        String formatBody = "if (nn(\"%s\")) {" +
                "String[] %s = get(\"%s\").split(\",\");" +
                "getEntityManager().unwrap(Session.class)" +
                "         .enableFilter(\"%s\")" +
                "         .setParameterList(\"%s\", %s);" +
                "}";
        return String.format(formatBody, filterName, nameInPlural, filterName, filterName, nameInPlural, nameInPlural);
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
