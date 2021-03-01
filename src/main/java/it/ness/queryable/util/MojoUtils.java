package it.ness.queryable.util;

import it.ness.queryable.builder.QueryableBuilder;
import it.ness.queryable.model.pojo.Data;
import it.ness.queryable.model.pojo.Parameters;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.Map;

public class MojoUtils {

    public static void addApi(Parameters parameters, Log log) {
        FileUtils.createPath(parameters.outputDir, parameters.apiPath, parameters.logging ? log : null);
        File filterPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/filter/", parameters.logging ? log : null);
        File producerPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/producer/", parameters.logging ? log : null);
        File utilPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/util/", parameters.logging ? log : null);
        File managementPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/management/", parameters.logging ? log : null);
        File servicePath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/service/", parameters.logging ? log : null);
        Map<String, Object> data = Data.with("groupId", parameters.groupId).map();
        FileUtils.createJavaClassFromTemplate(filterPath, "CorsFilter", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(utilPath, "DateUtils", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(producerPath, "CorsExceptionMapper", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(managementPath, "AppConstants", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(servicePath, "RsRepositoryServiceV3", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(servicePath, "RsResponseService", null, data, parameters.logging ? log : null);
    }

    public static void greeting(Parameters parameters, Log log) {
        FileUtils.createPath(parameters.outputDir, parameters.projectPath, parameters.logging ? log : null);
        File modelPath = FileUtils.createPath(parameters.outputDir, parameters.projectPath + "/model/", parameters.logging ? log : null);
        File enumPath = FileUtils.createPath(parameters.outputDir, parameters.projectPath + "/model/enums/", parameters.logging ? log : null);
        File managementPath = FileUtils.createPath(parameters.outputDir, parameters.projectPath + "/management/", parameters.logging ? log : null);
        Map<String, Object> data = Data.with("groupId", parameters.groupId).and("artifactId", parameters.artifactId).map();
        FileUtils.createJavaClassFromTemplate(managementPath, "AppConstantsApp", "AppConstants", data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(enumPath, "GreetingEnum", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(modelPath, "Greeting", null, data, parameters.logging ? log : null);
    }

    public static void source(Parameters parameters, Log log) {
        ModelFiles mf = new ModelFiles(parameters.logging ? log : null, parameters);
        if (!mf.isParsingSuccessful) return;
        try {
            QueryableBuilder.generateSources(mf, parameters.logging ? log : null, parameters);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
