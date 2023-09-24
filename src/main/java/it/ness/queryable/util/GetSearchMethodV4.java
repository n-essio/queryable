package it.ness.queryable.util;

import it.ness.queryable.model.filters.FilterDefBase;
import org.apache.maven.plugin.logging.Log;

import java.util.Collection;

public class GetSearchMethodV4 {
    protected Log log;
    protected Collection<FilterDefBase> preQueryFilters;
    protected Collection<FilterDefBase> postQueryFilters;
    protected String className;

    public GetSearchMethodV4(Log log, Collection<FilterDefBase> preQueryFilters, Collection<FilterDefBase> postQueryFilters, String className) {
        this.log = log;
        this.preQueryFilters = preQueryFilters;
        this.postQueryFilters = postQueryFilters;
        this.className = className;
    }

    private String getPreQuery() {
        return "var predicates = new ArrayList<Predicate>();";
    }

    public String create() {
        StringBuilder sb = new StringBuilder();
        boolean existsPreQueryFilters = preQueryFilters != null && preQueryFilters.size() > 0;
        boolean existsPostQueryFilters = postQueryFilters != null && postQueryFilters.size() > 0;
        sb.append(getPreQuery());
        if (existsPreQueryFilters) {
            for (FilterDefBase f : preQueryFilters) {
                sb.append(f.getSearchMethodV4());
            }
        }
        if (existsPostQueryFilters) {
            for (FilterDefBase f : postQueryFilters) {
                sb.append(f.getSearchMethodV4());
            }
        }
        sb.append("return predicates.toArray(new Predicate[]{});");
        return sb.toString();
    }

}
