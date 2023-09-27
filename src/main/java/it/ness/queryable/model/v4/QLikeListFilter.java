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
import static it.ness.queryable.builder.Constants.QLIKELIST_ANNOTATION_NAME;

public class QLikeListFilter extends FilterBase {


    protected String nameInPlural;

    public QLikeListFilter(final Log log) {
        super(log);
    }

    @Override
    public QLikeListFilter parseQFilter(String entityName, FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation) {
        AnnotationSource<JavaClassSource> a = f.getAnnotation(QLIKELIST_ANNOTATION_NAME);
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
        if (!enumerated && !supportedTypes.contains(fieldType)) {
            log.error(String.format("%s is not applicable for fieldType: %s fieldName: %s", QLIKELIST_ANNOTATION_NAME, fieldType, name));
            return null;
        }

        QLikeListFilter fd = new QLikeListFilter(log);
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
        String formatBody =
                " // NOT WELL SUPPORTED!!!!" +
                        "if (nn(\"%s\")) {" +
                        "   String[] %s = get(\"%s\").split(\",\");" +
                        "   StringBuilder sb = new StringBuilder();" +
                        "   if (null == params) {" +
                        "      params = new HashMap<>();" +
                        "   }" +
                        "   for (int i = 0; i < %s.length; i++) {" +
                        "      final String paramName = String.format(\"%s%%d\", i);" +
                        "      sb.append(String.format(\"%s LIKE :%%s\", paramName));" +
                        "      params.put(paramName, %s + %s[i] + %s);" +
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
        return String.format(formatBody, queryName, nameInPlural, queryName, nameInPlural, name, name, "\"%\"", nameInPlural, "\"%\"", nameInPlural);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.PREQUERY;
    }

    @Override
    public boolean overrideOnSameFilterName() {
        return true;
    }

    private Set<String> getSupportedTypes() {
        Set<String> supported = new HashSet<>();
        supported.add("String");
        return supported;
    }

}
