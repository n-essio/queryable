package it.ness.queryable.model.api;

import org.apache.maven.plugin.logging.Log;

import java.io.File;

import static it.ness.queryable.builder.Constants.JAVA_FOLDER;
import static it.ness.queryable.builder.Constants.TEST_FOLDER;

public class Parameters {

    public boolean removeAnnotations;
    public String sourceVersion = "v3";
    public String sourceModelDirectory;
    public String sourceRestDirectory;
    public String outputDirectory;
    public boolean logging;
    public boolean overrideAnnotations;
    public boolean overrideSearchMethod;

    public String groupId;
    public String artifactId;

    // path to groupId/api
    public String apiPath;
    // path to groupId/artifactId
    public String projectPath;
    // path to groupId/artifactId/model
    public String modelPath;
    public String exceptionPath;
    // path to groupId/artifactId/service/exception
    public String serviceRsPath;
    // path to groupId/api
    public String testPath;
    // ie. src/main/java
    public File outputDir;

    public Parameters(Log log, String groupId, String artifactId, boolean removeAnnotations, String sourceModelDirectory,
                      String sourceRestDirectory, String outputDirectory,
                      boolean logging, boolean overrideAnnotations, boolean overrideSearchMethod) {
        this(log, groupId, artifactId, removeAnnotations, sourceModelDirectory,
                sourceRestDirectory, outputDirectory, null,
                logging, overrideAnnotations, overrideSearchMethod);
    }

    public Parameters(Log log, String groupId, String artifactId, boolean removeAnnotations, String sourceModelDirectory,
                      String sourceRestDirectory, String outputDirectory, String sourceExceptionDirectory,
                      boolean logging, boolean overrideAnnotations, boolean overrideSearchMethod) {
        this.removeAnnotations = removeAnnotations;
        this.sourceModelDirectory = sourceModelDirectory;
        this.sourceRestDirectory = sourceRestDirectory;
        this.outputDirectory = outputDirectory;
        this.logging = logging;
        this.overrideAnnotations = overrideAnnotations;
        this.overrideSearchMethod = overrideSearchMethod;
        this.groupId = groupId;
        this.artifactId = artifactId;
        log.info("groupId:" + groupId);
        log.info("artifactId:" + artifactId);
        this.apiPath = groupId.replaceAll("\\.", "/") + "/api" + "/";
        this.projectPath = groupId.replaceAll("\\.", "/") + "/" + artifactId + "/";
        log.info("apiPath:" + apiPath);
        log.info("projectPath:" + projectPath);
        this.outputDir = new File(outputDirectory);
        this.modelPath = JAVA_FOLDER + this.projectPath + sourceModelDirectory;
        this.exceptionPath = JAVA_FOLDER + this.projectPath + sourceExceptionDirectory;
        this.serviceRsPath = JAVA_FOLDER + this.projectPath + sourceRestDirectory;
        log.info("modelPath:" + modelPath);
        log.info("exceptionPath:" + exceptionPath);
        log.info("serviceRsPath:" + serviceRsPath);
        this.testPath = TEST_FOLDER + this.projectPath + sourceRestDirectory;
        log.info("testPath:" + testPath);
    }
}
