package it.ness.queryable.util;

import it.ness.queryable.model.predicates.FilterBase;
import org.apache.maven.plugin.logging.Log;

import java.util.Collection;

public class GetSearchMethodV4 {
    protected Log log;
    protected Collection<FilterBase> preQueryFilters;
    protected Collection<FilterBase> postQueryFilters;
    protected String className;

    public GetSearchMethodV4(Log log, Collection<FilterBase> preQueryFilters, Collection<FilterBase> postQueryFilters, String className) {
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
            for (FilterBase f : preQueryFilters) {
                sb.append(f.getSearchMethod());
            }
        }
        if (existsPostQueryFilters) {
            for (FilterBase f : postQueryFilters) {
                sb.append(f.getSearchMethod());
            }
        }
        sb.append("return predicates.toArray(new Predicate[]{});");
        log.info("query method body : " + sb.toString());
        return sb.toString();
    }

}
