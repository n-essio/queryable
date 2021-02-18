package it.ness.queryable.model;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.HashSet;
import java.util.Set;

public class QLikeListFilterDef extends FilterDefBase {

    protected static String ANNOTATION_NAME = "QLikeList";
    protected static String PREFIX = "like";
    protected String nameInPlural;

    public QLikeListFilterDef(final Log log) {
        super(log);
    }

    @Override
    public void addAnnotationToModelClass(JavaClassSource javaClass) {
        nameInPlural = null;
        if (name.contains("_")) {
            nameInPlural = stringUtil.getPlural(name.substring(name.indexOf("_") + 1));
            nameInPlural = name.substring(0, name.indexOf("_")+1) + nameInPlural;
        } else {
            nameInPlural = stringUtil.getPlural(name);
        }
        filterName = prefix + "." + nameInPlural;
        // remove existing annotation with same filtername
        removeFilterDef(javaClass, filterName);

        AnnotationSource<JavaClassSource> filterDefAnnotation = javaClass.addAnnotation();
        filterDefAnnotation.setName("FilterDef");
        filterDefAnnotation.setStringValue("name", filterName);
        AnnotationSource<JavaClassSource> paramAnnotation = filterDefAnnotation.addAnnotationValue("parameters");
        paramAnnotation.setName("ParamDef");
        paramAnnotation.setStringValue("name", nameInPlural);
        paramAnnotation.setStringValue("type", "string");

        AnnotationSource<JavaClassSource> filterAnnotation = javaClass.addAnnotation();
        filterAnnotation.setName("Filter");
        filterAnnotation.setStringValue("name", filterName);
        if (null == condition) {
            filterAnnotation.setStringValue("condition", String.format("lower(%s) LIKE :%s", nameInPlural, nameInPlural));
        } else {
            filterAnnotation.setStringValue("condition", condition);
        }
    }

    @Override
    public QLikeListFilterDef parseQFilterDef(FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
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

        QLikeListFilterDef fd = new QLikeListFilterDef(log);
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
        String formatBody =
                "if (nn(\"%s\")) {" +
                        "   String[] %s = get(\"%s\").split(\",\");" +
                        "   StringBuilder sb = new StringBuilder();" +
                        "   if (null == params) {" +
                        "      params = new HashMap<>();" +
                        "   }" +
                        "   for (int i = 0; i < %s.length; i++) {" +
                        "      final String paramName = String.format(\"%s%%d\", i);" +
                        "      sb.append(String.format(\"%s LIKE :%%s\", paramName));" +
                        "      params.put(paramName, %s[i]);" +
                        "      if (i < %s.length - 1) {" +
                        "         sb.append(\" OR \");" +
                        "      }" +
                        "   }" +
                        "   if (null == query) {" +
                        "      query = sb.toString();" +
                        "   }" +
                        "   else {" +
                        "      query = query + \" OR \" + sb.toString();\n" +
                        "   }" +
                        "}";
        return String.format(formatBody, filterName, nameInPlural, filterName, nameInPlural, name, name, nameInPlural, nameInPlural);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.PREQUERY;
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
