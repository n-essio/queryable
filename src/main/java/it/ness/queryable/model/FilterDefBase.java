package it.ness.queryable.model;

import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.List;
import java.util.Objects;

public abstract class FilterDefBase implements Comparable<FilterDefBase> {

    protected Log log;
    protected StringUtil stringUtil;

    public String entityName;
    public String prefix;
    public String name;
    public String type;
    public String fieldType;
    public String condition;
    public String filterName;
    public String queryName;
    public QOption[] options;

    public abstract void addAnnotationToModelClass(JavaClassSource javaClass);
    public abstract FilterDefBase parseQFilterDef(String entityName, FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation);
    public abstract String getSearchMethod();
    public abstract FilterType getFilterType();
    public abstract boolean overrideOnSameFilterName();

    public FilterDefBase(final Log log) {
        this.log = log;
    }

    public boolean containsOption(QOption qOption) {
        if (options == null) {
            return false;
        }
        for (QOption o : options) {
            if (o.equals(qOption))
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterDefBase filterDef = (FilterDefBase) o;
        return filterName.equals(filterDef.filterName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterName);
    }

    public String toString() {
        return String.format("FilterDef={entityName=%s, prefix=%s, name=%s, type=%s, condition=%s}", entityName, prefix, name, type, condition);
    }

    public String getFilterName() {
        return filterName;
    }

    public String getType() { return type; }

    @Override
    public int compareTo(FilterDefBase filterDef) {
        if (this.getFilterType().equals(filterDef.getFilterType())) {
            return this.filterName.compareTo(filterDef.filterName);
        }
        return this.getFilterType().compareTo(filterDef.getFilterType());
    }

    protected void removeFilterDef(JavaClassSource javaClass, final String filterName) {
        List<AnnotationSource<JavaClassSource>> classAn = javaClass.getAnnotations();
        for (AnnotationSource<JavaClassSource> f : classAn) {
            if (f.getName().startsWith("FilterDef") || f.getName().startsWith("Filter")) {
                String name = f.getLiteralValue("name");
                if (name != null) {
                    name = StringUtil.removeQuotes(name);
                    if (name.equals(filterName)) {
                        javaClass.removeAnnotation(f);
                    }
                }
            }
        }
    }

    protected String getQAnnotationValue(final AnnotationSource<JavaClassSource> a, final String fieldName, final String defaultValue) {
        if (a == null || fieldName == null) {
            return defaultValue;
        }
        String value = a.getLiteralValue(fieldName);
        if (null == value) {
            value = defaultValue;
        }
        else {
            value = StringUtil.removeQuotes(value);
        }
        return value;
    }
}
