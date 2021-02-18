package it.ness.queryable.plugin;

import it.ness.queryable.builder.QueryableBuilder;
import it.ness.queryable.util.ModelFiles;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Queryable is maven plugin for filter defs.
 *
 */
@Mojo(name = "source",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class QueryableMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "pluralsJsonFile", defaultValue = "null")
    File pluralsJsonFile;

    @Parameter(property = "removeAnnotations", defaultValue = "false")
    boolean removeAnnotations;

    @Parameter(property = "sourceModelDirectory", defaultValue = "model")
    String sourceModelDirectory;

    @Parameter(property = "sourceRestDirectory", defaultValue = "service/rs")
    String sourceRestDirectory;

    @Parameter(property = "outputDirectory", defaultValue = "src/main/java")
    String outputDirectory;

    @Parameter(property = "logging", defaultValue = "true")
    boolean logging;

    @Parameter(property = "overideAnnotations", defaultValue = "true")
    boolean overideAnnotations;

    @Parameter(property = "overideSearchMethod", defaultValue = "true")
    boolean overideSearchMethod;

    public void execute() throws MojoExecutionException {
        final String groupId = project.getGroupId();

        Log log = getLog();
        if (logging) log.info(String.format("Begin generating sources for groupId {%s}", groupId));

        ModelFiles mf = new ModelFiles(log, logging, groupId, sourceModelDirectory);
        if (!mf.isParsingSuccessful) return;

        QueryableBuilder queryableBuilder = new QueryableBuilder(log, logging, sourceModelDirectory);
        try {
            queryableBuilder.generateSources(mf, groupId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (logging) log.info("Done generating sources");
    }
}