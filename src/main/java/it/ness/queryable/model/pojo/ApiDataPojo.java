package it.ness.queryable.model.pojo;

import it.ness.queryable.model.QApi;
import it.ness.queryable.model.QMethodApi;
import it.ness.queryable.model.QT;

import java.util.List;

public class ApiDataPojo {
    public List<QApi> apiFieldList;
    public List<QMethodApi> apiMethodList;
    public String rsPath;
    public String qualifiedClassName;

    @Override
    public String toString() {
        return "ApiDataPojo{" +
                "apiFieldList=" + apiFieldList +
                ", apiMethodList=" + apiMethodList +
                ", rsPath='" + rsPath + '\'' +
                ", qualifiedClassName='" + qualifiedClassName + '\'' +
                '}';
    }
}
