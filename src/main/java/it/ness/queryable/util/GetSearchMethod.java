package it.ness.queryable.util;

import it.ness.queryable.model.FilterDefBase;
import org.apache.maven.plugin.logging.Log;

import java.util.Collection;

public class GetSearchMethod {
    protected Log log;
    protected Collection<FilterDefBase> preQueryFilters;
    protected Collection<FilterDefBase> postQueryFilters;
    protected String className;

    public GetSearchMethod(Log log, Collection<FilterDefBase> preQueryFilters, Collection<FilterDefBase> postQueryFilters, String className) {
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
        StringBuilder sb = new StringBuilder();
        boolean existsPreQueryFilters = preQueryFilters != null && preQueryFilters.size() > 0;
        boolean existsPostQueryFilters = postQueryFilters != null && postQueryFilters.size() > 0;
        sb.append(getPreQuery(existsPreQueryFilters));
        if (existsPreQueryFilters) {
            for (FilterDefBase f : preQueryFilters) {
                sb.append(f.getSearchMethod());
            }
        }
        sb.append(getQuery(className, existsPreQueryFilters));
        if (existsPostQueryFilters) {
            for (FilterDefBase f : postQueryFilters) {
                sb.append(f.getSearchMethod());
            }
        }
        sb.append("return search;");
        return sb.toString();
    }

}
