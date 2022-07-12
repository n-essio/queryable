package it.ness.queryable.model.pojo;

import it.ness.queryable.model.QApi;
import it.ness.queryable.model.QMethodApi;
import it.ness.queryable.model.QT;

import java.util.List;
import java.util.Map;

public class ApiDataPojo {
    public List<QApi> apiFieldList;
    public List<QMethodApi> apiMethodList;
    public Map<String, String> filterDefs;
    public String rsPath = null;
    public String qualifiedClassName;
    public String className;

    @Override
    public String toString() {
        return "ApiDataPojo{" +
                "apiFieldList=" + apiFieldList +
                ", apiMethodList=" + apiMethodList +
                ", filterDefs=" + filterDefs +
                ", rsPath='" + rsPath + '\'' +
                ", qualifiedClassName='" + qualifiedClassName + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
