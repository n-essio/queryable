package it.ness.queryable.util;

import it.ness.queryable.model.v3.FilterDefBase;
import org.apache.maven.plugin.logging.Log;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GetSearchMethodV3 {
    protected Log log;
    protected Collection<FilterDefBase> preQueryFilters;
    protected Collection<FilterDefBase> postQueryFilters;
    protected String className;

    public GetSearchMethodV3(Log log, Collection<FilterDefBase> preQueryFilters, Collection<FilterDefBase> postQueryFilters, String className) {
        this.log = log;
        this.preQueryFilters = preQueryFilters;
        this.postQueryFilters = postQueryFilters;
        this.className = className;
    }


    private String getQuery(String className, boolean existsPreQueryFilters) {
        if (!existsPreQueryFilters) {
            // using null,  Panache will generate query in Hibernate way (ie : " from com.flower.User ")
            String formatBody = "PanacheQuery<%s> search; Sort sort = sort(orderBy);" +
                    "if (sort != null) {" +
                    "search = %s.find(null, sort);" +
                    "} else {" +
                    "search = %s.find(null);" +
                    "}";

            return String.format(formatBody, className, className, className);
        }
        String formatBody = "PanacheQuery<%s> search; Sort sort = sort(orderBy);" +
                "if (sort != null) {" +
                "search = %s.find(query, sort, params);" +
                "} else {" +
                "search = %s.find(query, params);" +
                "}";

        return String.format(formatBody, className, className, className);
    }

    private String getPreQuery(boolean existsPreQueryFilters) {
        if (existsPreQueryFilters) {
            return "String query = null;" +
                    "Map<String, Object> params = null;";
        }
        return "";
    }

    public String create() {
        return create(Set.of(), List.of());
    }

    public String create(Set<String> preservedFilters, List<String> hooks) {
        StringBuilder sb = new StringBuilder();
        boolean existsPreQueryFilters = preQueryFilters != null && preQueryFilters.size() > 0;
        boolean existsPostQueryFilters = postQueryFilters != null && postQueryFilters.size() > 0;
        sb.append(getPreQuery(existsPreQueryFilters));
        if (existsPreQueryFilters) {
            for (FilterDefBase f : preQueryFilters) {
                sb.append(f.getSearchMethod(preservedFilters));
            }
        }
        sb.append(getQuery(className, existsPreQueryFilters));
        if (existsPostQueryFilters) {
            for (FilterDefBase f : postQueryFilters) {
                sb.append(f.getSearchMethod(preservedFilters));
            }
        }
        for (String hook : hooks) {
            sb.append(hook).append("(search);");
        }
        sb.append("return search;");
        return sb.toString();
    }

}
