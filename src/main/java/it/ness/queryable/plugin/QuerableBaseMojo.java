package it.ness.queryable.plugin;

import it.ness.queryable.model.api.Parameters;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

import static it.ness.queryable.builder.Constants.JAVA_FOLDER;

public abstract class QuerableBaseMojo extends AbstractMojo {


    @Parameter(property = "artifactId", defaultValue = "${project.artifactId}", readonly = true, required = false)
    protected String artifactId;

    @Parameter(property = "groupId", defaultValue = "${project.groupId}", readonly = true, required = false)
    protected String groupId;


    @Parameter(property = "removeAnnotations", defaultValue = "false")
    boolean removeAnnotations;

    @Parameter(property = "sourceModelDirectory", defaultValue = "model")
    String sourceModelDirectory;

    @Parameter(property = "sourceRestDirectory", defaultValue = "service/rs")
    String sourceRestDirectory;

    @Parameter(property = "sourceExceptionDirectory", defaultValue = "service/exception")
    String sourceExceptionDirectory;

    @Parameter(property = "google_translate_apikey", defaultValue = "AIzaSyDPKAdKDvTaSb_dmYSpYPy4MjxwCnXfuL4")
    String google_translate_apikey;

    @Parameter(property = "outputDirectory", defaultValue = JAVA_FOLDER)
    String outputDirectory;

    @Parameter(property = "logging", defaultValue = "true")
    protected boolean logging;

    @Parameter(property = "overrideAnnotations", defaultValue = "true")
    boolean overrideAnnotations;

    @Parameter(property = "overrideSearchMethod", defaultValue = "true")
    boolean overrideSearchMethod;

    protected Parameters parameters;
    protected Log log;

    public void init(Log log) {
        this.log = log;
        this.parameters = new Parameters(log, groupId, artifactId, removeAnnotations, sourceModelDirectory, sourceRestDirectory,
                outputDirectory, sourceExceptionDirectory, logging,
                overrideAnnotations, overrideSearchMethod, google_translate_apikey);
    }
}
