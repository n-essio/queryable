package it.ness.queryable.util;

import it.ness.queryable.builder.*;
import it.ness.queryable.model.api.Data;
import it.ness.queryable.model.api.Parameters;
import it.ness.queryable.model.quex.ModelQuex;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.util.Map;

import static it.ness.queryable.builder.Constants.*;

public class MojoUtils {

    public static void addApi(Parameters parameters, Log log) {
        FileUtils.createPath(parameters.outputDir, parameters.apiPath, parameters.logging ? log : null);
        File filterPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/filter/", parameters.logging ? log : null);
        File producerPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/producer/", parameters.logging ? log : null);
        File utilPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/util/", parameters.logging ? log : null);
        File managementPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/management/", parameters.logging ? log : null);
        File servicePath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/service/", parameters.logging ? log : null);
        Map<String, Object> data = Data.with("groupId", parameters.groupId).map();
        FileUtils.createJavaClassFromTemplate(filterPath, "api", "CorsFilter", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(utilPath, "api", "DateUtils", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(producerPath, "api", "CorsExceptionMapper", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(managementPath, "api", "AppConstants", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(servicePath, "v3", "RsRepositoryServiceV3", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(servicePath, "v4", "RsRepositoryServiceV4", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(servicePath, "api", "ValidationGroups", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(servicePath, "api", "RsResponseService", null, data, parameters.logging ? log : null);
    }

    public static void greeting(Parameters parameters, Log log) {
        FileUtils.createPath(parameters.outputDir, parameters.projectPath, parameters.logging ? log : null);
        File modelPath = FileUtils.createPath(parameters.outputDir, parameters.projectPath + "/model/", parameters.logging ? log : null);
        File enumPath = FileUtils.createPath(parameters.outputDir, parameters.projectPath + "/model/enums/", parameters.logging ? log : null);
        File managementPath = FileUtils.createPath(parameters.outputDir, parameters.projectPath + "/management/", parameters.logging ? log : null);
        Map<String, Object> data = Data.with("groupId", parameters.groupId).and("artifactId", parameters.artifactId).map();
        FileUtils.createJavaClassFromTemplate(managementPath, "greeting", "AppConstantsApp", "AppConstants", data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(enumPath, "greeting", "GreetingEnum", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(modelPath, "greeting", "Greeting", null, data, parameters.logging ? log : null);
    }

    public static void sourceV3(Parameters parameters, Log log) {
        ModelFilesV3 mf = new ModelFilesV3(parameters.logging ? log : null, parameters);
        if (!mf.isParsingSuccessful) return;
        try {
            QueryableV3Builder.generateSources(mf, parameters.logging ? log : null, parameters);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void sourceV4(Parameters parameters, Log log) {
        ModelFilesV4 mf = new ModelFilesV4(parameters.logging ? log : null, parameters);
        if (!mf.isParsingSuccessful) return;
        try {
            QueryableV4Builder.generateSources(mf, parameters.logging ? log : null, parameters);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void testsource(Parameters parameters, Log log) {
        ModelFilesV3 mf = new ModelFilesV3(parameters.logging ? log : null, parameters);

        if (!mf.isParsingSuccessful) {
            return;
        }
        try {
            String packageName = parameters.groupId + "." + parameters.artifactId;
            TestBuilder.generateSources(mf, log, parameters, packageName);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void openapisource(Parameters parameters, Log log) {
        ModelFilesV3 mf = new ModelFilesV3(parameters.logging ? log : null, parameters);
        if (!mf.isParsingSuccessful) {
            return;
        }
        try {
            String packageName = parameters.groupId + "." + parameters.artifactId;
            OpenApiBuilder.generateSources(mf, log, parameters, packageName);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }


    public static void addqeexapi(Parameters parameters, Log log) {
        FileUtils.createPath(parameters.outputDir, parameters.apiPath, parameters.logging ? log : null);
        File annotationsPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/qeex/annotations/", parameters.logging ? log : null);
        File exceptionsPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/qeex/exceptions/", parameters.logging ? log : null);
        File interceptorsPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/qeex/interceptors/", parameters.logging ? log : null);
        File rsPath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/qeex/rs/", parameters.logging ? log : null);
        Map<String, Object> data = Data.with("groupId", parameters.groupId).map();
        // annotationsPath: QeexConfig - QeexExceptionBundle - QeexMessage
        FileUtils.createJavaClassFromTemplate(annotationsPath, "qeex", "QeexConfig", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(annotationsPath, "qeex", "QeexExceptionBundle", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(annotationsPath, "qeex", "QeexMessage", null, data, parameters.logging ? log : null);
        // exceptionsPath: QeexWebException
        FileUtils.createJavaClassFromTemplate(exceptionsPath, "qeex", "QeexWebException", null, data, parameters.logging ? log : null);
        // interceptorsPath: LanguageInterceptor
        FileUtils.createJavaClassFromTemplate(interceptorsPath, "qeex", "LanguageInterceptor", null, data, parameters.logging ? log : null);
        // rsPath: QuarkusWebDefaultExceptionProvider - QuarkusWebExceptionProvider
        FileUtils.createJavaClassFromTemplate(rsPath, "qeex", "QuarkusWebDefaultExceptionProvider", null, data, parameters.logging ? log : null);
        FileUtils.createJavaClassFromTemplate(rsPath, "qeex", "QuarkusWebExceptionProvider", null, data, parameters.logging ? log : null);
    }

    public static void addqeexbundle(Parameters parameters, Log log) {
        FileUtils.createPath(parameters.outputDir, parameters.apiPath, parameters.logging ? log : null);
        File exceptionPath = FileUtils.createPath(parameters.outputDir, parameters.projectPath + "service/exception/", parameters.logging ? log : null);
        File servicePath = FileUtils.createPath(parameters.outputDir, parameters.apiPath + "/service/", parameters.logging ? log : null);
        // exceptionPath: ExceptionBundle
        Map<String, Object> data = Data.with("groupId", parameters.groupId).and("artifactId", parameters.artifactId).map();
        FileUtils.createJavaClassFromTemplate(exceptionPath, "qeex-bundle", "ExceptionBundle", null, data, parameters.logging ? log : null);
        FileUtils.deleteJavaClassFromTemplate(servicePath, "qeex-bundle", "RsResponseService");
        FileUtils.createJavaClassFromTemplate(servicePath, "qeex-bundle", "RsResponseService", null, data, parameters.logging ? log : null);
    }

    public static void qeexsource(Parameters parameters, Log log) {
        try {
            String packageName = parameters.groupId + "." + parameters.artifactId;
            ModelQuex modelQuex = new ModelQuex(log, parameters, packageName);
            QeexBuilder.readQexFromApplicationProperties(modelQuex);
            // find interfaces like ExceptionBundle.ftl
            //  interfaces annotated with: @QeexExceptionBundle - ie: @QeexExceptionBundle(project = "FLW", language = "it")
            QeexBuilder.generateClassesImplementsExceptionBundle(parameters, log, modelQuex);
            // generate
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static Model parsePomXmlFileToMavenPomModel(String path) throws Exception {
        Model model = null;
        FileReader reader = null;
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();
        reader = new FileReader(path);
        model = mavenreader.read(reader);
        return model;
    }

    public static void parseMavenPomModelToXmlString(String path, Model model) throws Exception {
        MavenXpp3Writer mavenWriter = new MavenXpp3Writer();
        Writer writer = new FileWriter(path);
        mavenWriter.write(writer, model);
    }

    public static void addQueryableDependency(Model model) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(Q_GROUPID);
        dependency.setArtifactId(Q_ARTIFACTID);
        dependency.setVersion(Q_VERSION);
        model.addDependency(dependency);
    }

    public static void addQueryablePlugin(Model model) {
        Plugin plugin = new Plugin();
        plugin.setGroupId(Q_GROUPID);
        plugin.setArtifactId(Q_ARTIFACTID);
        plugin.setVersion(Q_VERSION);
        model.getBuild().addPlugin(plugin);
    }
}
