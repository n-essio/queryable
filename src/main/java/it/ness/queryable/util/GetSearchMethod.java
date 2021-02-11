package it.ness.queryable.util;

import it.ness.queryable.model.FilterDefBase;
import it.ness.queryable.model.enums.FilterType;
import org.apache.maven.plugin.logging.Log;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class GetSearchMethod {
    protected Log log;
    protected Collection<FilterDefBase> fd;
    protected String modelName;

    public GetSearchMethod(Log log, Collection<FilterDefBase> fd, String modelName) {
        this.log = log;
        this.fd = fd;
        this.modelName = modelName;
    }


    private String getQuery(String modelName, boolean existsPreQueryFilters) {
        if (!existsPreQueryFilters) {
            // using null,  Panache will generate query in Hibernate way (ie : " from com.flower.User ")
            String formatBody = "PanacheQuery<%s> search; Sort sort = sort(orderBy);" +
                    "if (sort != null) {" +
                    "search = %s.find(null, sort);" +
                    "} else {" +
                    "search = %s.find(null);" +
                    "}";

            return String.format(formatBody, modelName, modelName, modelName);
        }
        String formatBody = "PanacheQuery<%s> search; Sort sort = sort(orderBy);" +
                "if (sort != null) {" +
                "search = %s.find(query, sort, params);" +
                "} else {" +
                "search = %s.find(query, params);" +
                "}";

        return String.format(formatBody, modelName, modelName, modelName);
    }

    private String getPreQuery(boolean existsPreQueryFilters) {
        if (existsPreQueryFilters) {
            return "String query = null;" +
                    "Map<String, Object> params = null;";
        }
        return "";
    }

    private Set<FilterDefBase> getPreQueryFilters() {
        Set<FilterDefBase> preQueryFilters = new LinkedHashSet<>();
        for (FilterDefBase f : fd) {
            if (f.getFilterType() == FilterType.PREQUERY) {
                preQueryFilters.add(f);
            }
        }
        return preQueryFilters;
    }

    private Set<FilterDefBase> getPostQueryFilters() {
        Set<FilterDefBase> postQueryFilters = new LinkedHashSet<>();
        for (FilterDefBase f : fd) {
            if (f.getFilterType() == FilterType.POSTQUERY) {
                postQueryFilters.add(f);
            }
        }
        return postQueryFilters;
    }

    public String create() {
        StringBuilder sb = new StringBuilder();
        Set<FilterDefBase> preQueryFilters = getPreQueryFilters();
        boolean existsPreQueryFilters = preQueryFilters.size() > 0;
        Set<FilterDefBase> postQueryFilters = getPostQueryFilters();
        sb.append(getPreQuery(existsPreQueryFilters));
        for (FilterDefBase f : preQueryFilters) {
            sb.append(f.getSearchMethod());
        }
        sb.append(getQuery(modelName, existsPreQueryFilters));
        for (FilterDefBase f : postQueryFilters) {
            sb.append(f.getSearchMethod());
        }
        sb.append("return search;");
        return sb.toString();
    }

}
