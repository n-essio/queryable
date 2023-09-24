package it.ness.queryable.model.predicates;
import it.ness.queryable.annotations.QOption;
import it.ness.queryable.model.enums.FilterType;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.Objects;

public abstract class FilterBase implements Comparable<FilterBase> {

    protected Log log;
    public String entityName;
    public String prefix;
    public String name;
    public String fieldType;
    public String condition;
    public boolean enumerated;
    public String queryName;
    public QOption[] options;

    public abstract FilterBase parseQFilter(String entityName, FieldSource<JavaClassSource> f, boolean qClassLevelAnnotation);

    public abstract String getSearchMethod();

    public abstract FilterType getFilterType();

    public abstract boolean overrideOnSameFilterName();

    public FilterBase(final Log log) {
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
        FilterBase filterDef = (FilterBase) o;
        return name.equals(filterDef.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String toString() {
        return String.format("FilterBase={entityName=%s, prefix=%s, name=%s, type=%s, condition=%s, enumerated=%s }", entityName, prefix, name, fieldType, condition, enumerated);
    }

    @Override
    public int compareTo(FilterBase filterBase) {
        if (this.getFilterType().equals(filterBase.getFilterType())) {
            return this.name.compareTo(filterBase.name);
        }
        return this.getFilterType().compareTo(filterBase.getFilterType());
    }

    protected String getQAnnotationValue(final AnnotationSource<JavaClassSource> a, final String fieldName, final String defaultValue) {
        if (a == null || fieldName == null) {
            return defaultValue;
        }
        String value = a.getLiteralValue(fieldName);
        if (null == value) {
            value = defaultValue;
        } else {
            value = StringUtil.removeQuotes(value);
        }
        return value;
    }
}
