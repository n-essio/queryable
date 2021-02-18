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

import static it.ness.queryable.builder.Constants.JAVA_FOLDER;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "source",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class QueryableMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "removeAnnotations", defaultValue = "false")
    boolean removeAnnotations;

    @Parameter(property = "sourceModelDirectory", defaultValue = "model")
    String sourceModelDirectory;

    @Parameter(property = "sourceRestDirectory", defaultValue = "service/rs")
    String sourceRestDirectory;

    @Parameter(property = "outputDirectory", defaultValue = JAVA_FOLDER)
    String outputDirectory;

    @Parameter(property = "logging", defaultValue = "true")
    boolean logging;

    @Parameter(property = "overrideAnnotations", defaultValue = "true")
    boolean overrideAnnotations;

    @Parameter(property = "overrideSearchMethod", defaultValue = "true")
    boolean overrideSearchMethod;

    public void execute() throws MojoExecutionException {
        final String groupId = project.getGroupId();
        final String artefactId = project.getArtifactId();

        Log log = getLog();
        if (logging)
            log.info(String.format("Begin generating sources for groupId {%s}, artefactId {%s}", groupId, artefactId));

        ModelFiles mf = new ModelFiles(log, logging, groupId, artefactId, sourceModelDirectory);
        if (!mf.isParsingSuccessful) return;

        QueryableBuilder queryableBuilder = new QueryableBuilder(log, logging, sourceModelDirectory);
        try {
            queryableBuilder.generateSources(mf, groupId, artefactId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (logging) log.info("Done generating sources");
    }
}